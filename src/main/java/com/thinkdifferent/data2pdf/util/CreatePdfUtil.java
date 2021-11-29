package com.thinkdifferent.data2pdf.util;

import com.thinkdifferent.data2pdf.config.Data2PDFConfig;
import com.thinkdifferent.data2pdf.service.Data2PdfService;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class CreatePdfUtil {

    public JSONObject data2PDF(Data2PdfService data2PdfService, JSONObject jsonInput) {
        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("flag", "error" );
        jsonReturn.put("message", "Create Pdf file Error" );

        try{
            // 报表文件路径和文件名（相对路径、文件名，不包含扩展名）
            String strReportFile = jsonInput.getString("reportFile");
            // 将输入路径中的\全部换为/
            strReportFile = strReportFile.replaceAll("\\\\", "/");
            // 获取报表路径（相对路径名）
            String strReportPath = strReportFile.substring(0, strReportFile.lastIndexOf("/")+1);
            if(strReportPath.startsWith("/")){
                strReportPath = strReportPath.substring(1, strReportPath.length());
            }

            String strWriteBackType = "path";
            // 本地服务报表输出路径
            String strOutPutPath = Data2PDFConfig.outPutPath;
            if(jsonReturn.get("writeBackType")!=null){
                strWriteBackType = String.valueOf(jsonReturn.get("writeBackType"));

                // 回写接口或回写路径
                JSONObject jsonWriteBack = JSONObject.fromObject(jsonReturn.get("writeBack"));
                // 如果回写类型是path，则将传入路径的值赋值给“输出路径”变量
                if("path".equalsIgnoreCase(strWriteBackType)){
                    strOutPutPath = jsonWriteBack.getString("path");
                }
            }

            // 报表文件名对应的JSON中的key
            String strFileNameKey = jsonInput.getString("fileNameKey");
            JSONArray jsonData = jsonInput.getJSONArray("data");

            // 获取报表模板文件流。
            String strReportPathFileName = System.getProperty("user.dir") + "/reportfile/" + strReportFile + ".jasper";
            InputStream jasperStream = new FileInputStream(strReportPathFileName);
            // 加载报表模板
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

            //报表文件临时存储设置，切记！！此临时文件夹一定要真实存在！！！
            JRFileVirtualizer virtualizer = new JRFileVirtualizer(2, "cacheDir");
            virtualizer.setReadOnly(true);

            String strOutputFileNames = "";
            // 读取JSON的data域中的内容。此部分是JSONArray，可以存放多组报表的数据。通过循环一次生成多张报表。
            for(int i=0;i<jsonData.size();i++){
                // 从指定的key中取值，设定新生成的PDF报表的文件名
                String strFileName = jsonData.getJSONObject(i).getString(strFileNameKey)+".pdf";
                // 设定pdf输出的路径和文件名
                String strOutputPathFileName = strOutPutPath+strFileName;

                //创建报表参数Map对象，需要传入报表的参数，均需要通过这个map对象传递
                Map<String, Object> mapParam = new HashMap<String, Object>();
                // 设定报表的缓冲区
                mapParam.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
                // 将输入的JSON参数转码为UTF-8，并转换为输入流（避免文字产生乱码）
                InputStream inputStream = new ByteArrayInputStream(jsonData.getJSONObject(i).toString().getBytes("UTF-8"));
                // 以数据流的形式，填充报表数据源
                mapParam.put("JSON_INPUT_STREAM", inputStream);
                // 设定参数，报表模板的路径（如果报表中有子报表、图片，会用到这个路径参数）
                mapParam.put("reportPath", System.getProperty("user.dir") + "/reportfile/" + strReportPath);

                // 生成pdf格式的报表文件
                data2PdfService.CreatePdf(mapParam, jasperReport, strOutputPathFileName);
                strOutputFileNames = strOutputFileNames + strOutputPathFileName + ";";
            }

            if(strOutputFileNames.endsWith(";")){
                strOutputFileNames = strOutputFileNames.substring(0, strOutputFileNames.length()-1);
            }

            jsonReturn.put("flag", "success" );
            jsonReturn.put("message", "Create Pdf Report file Success" );
            jsonReturn.put("file", strOutputFileNames);

            // manually cleaning up
            virtualizer.cleanup();

        }catch (Exception e){
            e.printStackTrace();
        }

        return jsonReturn;
    }


}
