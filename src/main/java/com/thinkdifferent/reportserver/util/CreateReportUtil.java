package com.thinkdifferent.reportserver.util;

import com.thinkdifferent.reportserver.config.ReportServerConfig;
import com.thinkdifferent.reportserver.entity.ReportParamEntity;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class CreateReportUtil {

    /**
     * 从输入参数中，获取dataSource的值
     * @param jsonInput 输入参数
     * @return dataSource值
     */
    public String getDataSource(JSONObject jsonInput){
        String strDataSource = "json";
        if(jsonInput.has("dataSource") &&
                !jsonInput.getString("dataSource").isEmpty()){
            strDataSource = jsonInput.getString("dataSource");
        }
        return strDataSource;
    }

    /**
     * JSON、DB数据生成报表
     * @param reportParamEntity 生成报表传入参数
     * @return
     * @throws Exception
     */
    public JSONObject createReportFromData(ReportParamEntity reportParamEntity)
            throws Exception{
        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error" );
        jsonReturn.put("message", "Create Report file Error" );

        // 报表文件路径和文件名（相对路径、文件名，不包含扩展名）
        String strReportFile = reportParamEntity.getJoInput().getString("reportFile");
        // 将输入路径中的\全部换为/
        strReportFile = strReportFile.replaceAll("\\\\", "/");
        // 获取报表路径（相对路径名）
        String strReportPath = strReportFile.substring(0, strReportFile.lastIndexOf("/")+1);
        if(strReportPath.startsWith("/")){
            strReportPath = strReportPath.substring(1);
        }
        reportParamEntity.setReportPath(strReportPath);

        String strDocType = "pdf";
        if(reportParamEntity.getJoInput().has("docType") &&
                !reportParamEntity.getJoInput().getString("docType").isEmpty() ){
            strDocType = reportParamEntity.getJoInput().getString("docType");
        }
        reportParamEntity.setDocType(strDocType);

        String strFileExt = strDocType;
        if("word".equalsIgnoreCase(strDocType)){
            strFileExt = "docx";
        }else if("excel".equalsIgnoreCase(strDocType)) {
            strFileExt = "xlsx";
        }

        String strWriteBackType = "path";
        // 本地服务报表输出路径
        String strOutPutPath = ReportServerConfig.outPutPath;
        if(jsonReturn.get("writeBackType")!=null){
            strWriteBackType = String.valueOf(jsonReturn.get("writeBackType"));

            // 回写接口或回写路径
            JSONObject jsonWriteBack = JSONObject.fromObject(jsonReturn.get("writeBack"));
            // 如果回写类型是path，则将传入路径的值赋值给“输出路径”变量
            if("path".equalsIgnoreCase(strWriteBackType)){
                strOutPutPath = jsonWriteBack.getString("path");
            }
        }

        // 按照数据，生成多个PDF文件：报表文件名对应的JSON中的key
        String strFileNameKey = null;
        if(reportParamEntity.getJoInput().has("fileNameKey")){
            strFileNameKey = reportParamEntity.getJoInput().getString("fileNameKey");
        }
        // 或者，生成一个PDF文件：获取PDF文件名
        String strOutFileName = null;
        if(reportParamEntity.getJoInput().has("fileName")){
            strOutFileName = reportParamEntity.getJoInput().getString("fileName");
        }

        JSONArray jaData = null;
        if(reportParamEntity.getJoInput().has("data")) {
            jaData = reportParamEntity.getJoInput().getJSONArray("data");
            if(jaData !=null && !jaData.isEmpty()){
                reportParamEntity.setJaData(jaData);
            }
        }

        // 获取报表模板文件流。
        String strReportPathFileName = System.getProperty("user.dir") + "/reportfile/" + strReportFile + ".jasper";
        InputStream jasperStream = new FileInputStream(strReportPathFileName);
        // 加载报表模板
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
        reportParamEntity.setJasperReport(jasperReport);

        if(jasperStream != null){
            jasperStream.close();
        }

        //报表文件临时存储设置，切记！！此临时文件夹一定要真实存在！！！
        JRFileVirtualizer jrFileVirtualizer = new JRFileVirtualizer(2, "cacheDir");
        jrFileVirtualizer.setReadOnly(true);
        reportParamEntity.setJrFileVirtualizer(jrFileVirtualizer);

        String strOutputFileNames = "";
        String strBase64 = "";
        JSONArray jaBase64 = new JSONArray();

        if("stream".equalsIgnoreCase(reportParamEntity.getOutputType()) ||
                "singleWriteBack".equalsIgnoreCase(reportParamEntity.getOutputType()) ||
                "singleBase64".equalsIgnoreCase(reportParamEntity.getOutputType())){
            // 生成一个PDF报表；否则生成多个PDF报表文件
            strOutputFileNames = strOutFileName + "." + strFileExt;
            String strOutputPathFileName = strOutPutPath + strOutputFileNames;

            reportParamEntity.setOutputPathFileName(strOutputPathFileName);

            strBase64 = createReport(reportParamEntity, 0);
            if(strBase64 !=null && !"".equals(strBase64)){
                JSONObject joBase64 = new JSONObject();
                joBase64.put("value", strBase64);

                jaBase64.add(joBase64);

                File file = new File(strOutputPathFileName);
                if(file.exists()){
                    file.delete();
                }
            }
        }else{
            // 读取JSON的data域中的内容。此部分是JSONArray，可以存放多组报表的数据。通过循环一次生成多张报表。
            for(int i=0;i<jaData.size();i++){
                reportParamEntity.setJoData(jaData.getJSONObject(i));

                // 从指定的key中取值，设定新生成的PDF报表的文件名
                String strFileName = jaData.getJSONObject(i).getString(strFileNameKey) + "." + strFileExt;
                // 设定pdf输出的路径和文件名
                String strOutputPathFileName = strOutPutPath+strFileName;

                reportParamEntity.setOutputPathFileName(strOutputPathFileName);
                if(jaData !=null && !jaData.isEmpty()){
                    reportParamEntity.setJoData(jaData.getJSONObject(i));
                }

                strBase64 = createReport(reportParamEntity, i);
                if(strBase64 !=null && !"".equals(strBase64)) {
                    JSONObject joBase64 = new JSONObject();
                    joBase64.put("value", strBase64);

                    jaBase64.add(joBase64);

                    File file = new File(strOutputPathFileName);
                    if(file.exists()){
                        file.delete();
                    }
                }

                strOutputFileNames = strOutputFileNames + strOutputPathFileName + ";";
            }

            if(strOutputFileNames.endsWith(";")){
                strOutputFileNames = strOutputFileNames.substring(0, strOutputFileNames.length()-1);
            }
        }

        jsonReturn.put("flag", "success" );
        jsonReturn.put("message", "Create Pdf Report file Success." );
        jsonReturn.put("file", strOutputFileNames);
        if(jaBase64 !=null && !jaBase64.isEmpty()) {
            jsonReturn.put("base64", jaBase64);
        }

        // manually cleaning up
        reportParamEntity.getJrFileVirtualizer().cleanup();

        return jsonReturn;
    }

    /**
     * 将JSON数据生成PDF，返回Response的文件流、或保存到指定的位置、或返回Base64字符串
     * @param reportParamEntity 报表生成参数
     * @param intIndex 报表的下标值
     * @return 返回的Base64字符串
     * @throws Exception
     */
    private String createReport(ReportParamEntity reportParamEntity, int intIndex) throws Exception{
        String strBase64 = null;

        //创建报表参数Map对象，需要传入报表的参数，均需要通过这个map对象传递
        Map<String, Object> mapParam = new HashMap<String, Object>();
        // 设定报表的缓冲区
        mapParam.put(JRParameter.REPORT_VIRTUALIZER, reportParamEntity.getJrFileVirtualizer());

        // 将输入的JSON参数转码为UTF-8，并转换为输入流（避免文字产生乱码）
        InputStream inputStream = null;
        if("json".equalsIgnoreCase(reportParamEntity.getDataSource())){
            if("stream".equalsIgnoreCase(reportParamEntity.getOutputType()) ||
                    "singleWriteBack".equalsIgnoreCase(reportParamEntity.getOutputType()) ||
                    "singleBase64".equalsIgnoreCase(reportParamEntity.getOutputType())){
                inputStream = new ByteArrayInputStream(reportParamEntity.getJaData()
                        .toString().getBytes("UTF-8"));
            }else{
                inputStream = new ByteArrayInputStream(reportParamEntity.getJoInput()
                        .toString().getBytes("UTF-8"));
            }
            // 以数据流的形式，填充报表数据源
            mapParam.put("JSON_INPUT_STREAM", inputStream);
        }else{
            // 数据库连接方式，获取数据
            mapParam.putAll(reportParamEntity.getJaData()
                    .getJSONObject(intIndex));
        }

        // 设定参数，报表模板的路径（如果报表中有子报表、图片，会用到这个路径参数）
        String strReportPath = reportParamEntity.getReportPath();
        strReportPath = strReportPath.replaceAll("\\\\","/");
        if(!strReportPath.endsWith("/")){
            strReportPath = strReportPath + "/";
        }
        mapParam.put("reportPath", System.getProperty("user.dir") + "/reportfile/" + strReportPath);

        if("singleBase64".equalsIgnoreCase(reportParamEntity.getOutputType()) ||
                "multiBase64".equalsIgnoreCase(reportParamEntity.getOutputType())){
            strBase64 = reportParamEntity.getData2ReportService().
                    getReportBase64(mapParam, reportParamEntity.getJasperReport(),
                            reportParamEntity.getOutputPathFileName(), reportParamEntity.getDocType(),
                            reportParamEntity.getResponse(), reportParamEntity.getConn());
        }else{
            reportParamEntity.getData2ReportService().
                    getReportFile(mapParam, reportParamEntity.getJasperReport(),
                            reportParamEntity.getOutputPathFileName(), reportParamEntity.getDocType(),
                            reportParamEntity.getResponse(), reportParamEntity.getConn());
        }

        if(inputStream != null){
            inputStream.close();
        }

        return strBase64;
    }

}
