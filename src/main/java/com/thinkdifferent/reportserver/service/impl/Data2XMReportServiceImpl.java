package com.thinkdifferent.reportserver.service.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkdifferent.reportserver.config.ReportServerConfig;
import com.thinkdifferent.reportserver.service.Data2XMReportService;
import com.thinkdifferent.reportserver.sharescript.ShareScriptManagerService;
import com.thinkdifferent.reportserver.template.TemplateManagerService;
import com.thinkdifferent.reportserver.util.IOUtils;
import net.sf.json.JSONObject;
import org.mosmith.tools.report.engine.execute.context.ExecuteContext;
import org.mosmith.tools.report.engine.execute.script.javascript.JSContextAware;
import org.mosmith.tools.report.engine.execute.script.javascript.JSEngineTopLevel;
import org.mosmith.tools.report.engine.output.ReportHelper;
import org.mosmith.tools.report.engine.util.StringUtils;
import org.mozilla.javascript.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Data2XMReportServiceImpl implements Data2XMReportService {

    private static final String DATA="data";
    private static final String CODE="code";

    @Autowired
    private TemplateManagerService templateManagerService;

    @Autowired
    private ShareScriptManagerService shareScriptManagerService;

    /**
     * 根据传入的参数，创建报表文件
     * @param templateId 报表模板ID
     * @param templateData 报表模板数据（模板ID有值时，此参数可空）
     * @param previewDataJson 预览用的JSON数据
     * @param previewOptions 预览参数
     * @param docType 文件类型
     * @param httpServletResponse HTTP响应对象
     * @throws Exception
     */
    public File createReport(String templateId, String templateData,
                              String previewDataJson, Map<String, Object> previewOptions, String docType,
                              HttpServletResponse httpServletResponse)
            throws Exception{

        if(templateId!=null && !templateId.trim().isEmpty()) {
            Map<String, Object> templateInfo=templateManagerService.getTemplate(templateId);
            templateData=new String((byte[]) templateInfo.get(DATA), "utf-8");
        }

        Reader templateDataReader=new StringReader(templateData);
        Object previewData=readJson(previewDataJson);

        File file;
        String fileName;
        String contentType;
        if(docType.equalsIgnoreCase("PDF")) {
            file=getReportHelper().toPdf(templateDataReader, previewData, previewOptions);
            fileName="preview.pdf";
            contentType="application/pdf";
        } else if(docType.equalsIgnoreCase("Word")) {
            file=getReportHelper().toWord(templateDataReader, previewData, previewOptions);
            fileName="preview.docx";
            contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if(docType.equalsIgnoreCase("Excel")) {
            file=getReportHelper().toExcel(templateDataReader, previewData, previewOptions);
            fileName="preview.xlsx";
            contentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if(docType.equalsIgnoreCase("HTML")) {
            file=getReportHelper().toHtml(templateDataReader, previewData, previewOptions);
            fileName="preview.html";
            contentType="text/html";
        } else if(docType.equalsIgnoreCase("Image")) {
            // previewOptions.put("imgDpi", 300);
            // previewOptions.put("imgFormat", "png");
            List<File> imageFiles=getReportHelper().toImages(templateDataReader, previewData, previewOptions);
            if(imageFiles.isEmpty()) {
                imageFiles.add(File.createTempFile("output-", ".png"));
            }
            for(int i=1;i<imageFiles.size();i++) {
                imageFiles.get(i).delete();
            }
            file=imageFiles.get(0);
            fileName="preview.png";
            contentType="image/png";
        } else {
            file=getReportHelper().toPdf(templateDataReader, previewData, previewOptions);
            fileName="preview.pdf";
            contentType="application/pdf";
        }

        if(httpServletResponse != null){
            httpServletResponse.setContentType(contentType);
            httpServletResponse.setHeader("Content-Disposition", "inline;filename=" + fileName);

            InputStream fileIs=null;
            try {
                fileIs=new FileInputStream(file);
                OutputStream os=httpServletResponse.getOutputStream();
                IOUtils.copyStream(fileIs, os);
            } finally {
                IOUtils.close(fileIs);
                if(file!=null) {
                    file.delete();
                }
            }

        }

        return file;

    }


    /**
     * 根据传入的参数（模板ID），生成报表文件，根据需要，返回HTTP响应
     * @param jsonInput 输入的JSON对象
     * @param httpServletResponse HTTP响应
     * @return 报表文件对象
     * @throws Exception
     */
    public File getReportFileByID(JSONObject jsonInput, HttpServletResponse httpServletResponse)
            throws Exception {
        String previewOptionsJson = "{\"docType\": \"PDF\",\"dividePage\": true}";
        // 获取reportFile中的值作为“报表模板ID”。即为template表中的F_id字段的值，形如：template-e7e2ad60-596b-41d4-80f8-65991da9049a
        String templateId = jsonInput.getString("reportFile");
        // 获取“预览数据”
        String previewDataJson = jsonInput.toString();
        // 获取options中的值，作为“预览参数”。形如：{"docType":"PDF","dividePage":true}
        if(jsonInput.has("options")){
            previewOptionsJson = jsonInput.getJSONObject("options").toString();
        }

        Map<String, Object> previewOptions=(Map<String, Object>) readJson(previewOptionsJson);
        String docType= StringUtils.nonull(previewOptions.get("docType"));

        File file = createReport(templateId, null,
                previewDataJson, previewOptions, docType,
                httpServletResponse);

        String strFileExt = docType;
        if(docType.equalsIgnoreCase("Word")) {
            strFileExt="docx";
        }else if(docType.equalsIgnoreCase("Excel")) {
            strFileExt="xlsx";
        }else if(docType.equalsIgnoreCase("Image")) {
            strFileExt="png";
        }

        String strFileName = "temp."+strFileExt;
        if (jsonInput.has("fileName") && jsonInput.get("fileName") != null) {
            strFileName = jsonInput.getString("fileName") + "." + strFileExt;
        }

        String strDest = ReportServerConfig.outPutPath + strFileName;
        if(jsonInput.has("writeBackType") &&
                jsonInput.has("writeBack")){
            if("path".equalsIgnoreCase(jsonInput.getString("writeBackType"))){
                String strDestPath = jsonInput.getJSONObject("writeBack").getString("path");
                strDestPath = strDestPath.replaceAll("\\\\", "/");
                if(!strDestPath.endsWith("/")){
                    strDestPath = strDestPath + "/";
                }
                strDest = strDestPath + strFileName;
            }

        }
        File fileDest = new File(strDest);
        Files.copy(file.toPath(), fileDest.toPath());
        file.delete();

        return fileDest;
    }






    private ReportHelper getReportHelper() {
        ReportHelper reportHelper=new ReportHelper();
        reportHelper.setBuiltIn("scriptLoader", new ScriptLoader());
        reportHelper.setBuiltIn("jdbcExecutor", new JDBCExecutor());
        return reportHelper;
    }

    public Object readJson(String json) throws IOException {
        if(json==null || json.isEmpty()) {
            return new HashMap();
        }

        Character firstC=null;
        for(int i=0, length=json.length();i<length;i++) {
            char c= json.charAt(i);
            if(Character.isWhitespace(c)) {
                continue;
            }

            firstC=c;
            break;
        }

        if(firstC=='[') {
            ObjectMapper objectMapper=new ObjectMapper();

            JavaType listType=objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class);
            List<Object> list=objectMapper.readValue(json, listType);
            return list;
        } else if(firstC=='{') {
            ObjectMapper objectMapper=new ObjectMapper();

            Map map=objectMapper.readValue(json, Map.class);
            return map;
        } else {
            throw new IllegalArgumentException("Invalid json data!");
        }
    }

    public class ScriptLoader implements JSContextAware {
        private Context context;
        private JSEngineTopLevel topLevel;

        @Override
        public void setContext(Context context) {
            this.context=context;
        }

        @Override
        public void setTopLevel(JSEngineTopLevel topLevel) {
            this.topLevel=topLevel;
        }

        public Object load(String scriptPath) {
            Map<String, Object> scriptInfo=shareScriptManagerService.getScriptByPath(scriptPath);
            if(scriptInfo==null) {
                return null;
            }

            String code=(String) scriptInfo.get(CODE);
            if(code==null) {
                return null;
            }

            Object jsObject=context.evaluateString(topLevel, code, scriptPath, 1, null);
            return jsObject;
        }
    }

    @Autowired
    private DataSource dataSource;

    public class JDBCExecutor {
        private final Pattern placeHolderPattern=Pattern.compile("\\$\\{(.*?)\\}");

        public Object executeJDBCQuery(String sql) throws Exception {
            sql=replacePlaceHolders(sql);

            Connection connection=null;
            Statement statement=null;
            ResultSet resultSet=null;
            try {
                connection = dataSource.getConnection();

                statement=connection.createStatement();
                resultSet=statement.executeQuery(sql);

                ResultSetMetaData metaData=resultSet.getMetaData();
                int colCount=metaData.getColumnCount();
                String[] colLabels=new String[colCount];
                for(int i=1;i<=colCount;i++) {
                    colLabels[i-1]=metaData.getColumnLabel(i);
                }

                List<Object> result=new ArrayList<Object>();
                while(resultSet.next()) {
                    Map<String, Object> rowData=new LinkedHashMap<String, Object>();
                    for(String colLabel: colLabels) {
                        rowData.put(colLabel, resultSet.getObject(colLabel));
                    }
                    result.add(rowData);
                }
                return result;
            } finally {
                close(resultSet);
                close(statement);
                close(connection);
            }
        }

        private void close(Object object) {
            try {
                if(object instanceof Connection) {
                    ((Connection)object).close();
                }
                if(object instanceof Statement) {
                    ((Statement)object).close();
                }
                if(object instanceof ResultSet) {
                    ((ResultSet)object).close();
                }
                if(object instanceof Closeable) {
                    ((Closeable)object).close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String replacePlaceHolders(String content) {

            StringBuilder sb=new StringBuilder();
            int start=0;
            int end=0;

            while(true) {
                Matcher matcher=placeHolderPattern.matcher(content);
                if(!matcher.find()) {
                    break;
                }
                while(true) {
                    start=matcher.start();
                    sb.append(content.substring(end, start));
                    end=matcher.end();

                    matcher.toMatchResult();
                    String expression=matcher.group(1);

                    Object value= ExecuteContext.get().lookup(expression);
                    if(value!=ExecuteContext.NOT_FOUND && value!=null) {
                        sb.append(value);
                    }

                    if(!matcher.find()) {
                        break;
                    }
                }

                sb.append(content.substring(end));

                content=sb.toString();
                sb=new StringBuilder();
            }

            return content;
        }

    }



}
