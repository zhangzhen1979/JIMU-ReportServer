package com.thinkdifferent.data2pdf.service.impl;

import com.thinkdifferent.data2pdf.service.Data2PdfService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class Data2PdfServiceImpl implements Data2PdfService {

    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和PDF文件名
     */
    public void createPdf(Map<String, Object> parameters, JasperReport jasperReport,
                   String strOutputPathFileName) throws Exception {
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
        JasperExportManager.exportReportToPdfFile(jasperPrint, strOutputPathFileName);
    }

    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param response Http响应对象
     */
    public void getPdf(Map<String, Object> parameters, JasperReport jasperReport,
                          HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
        OutputStream os = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, os);

        if(os != null){
            os.close();
        }

    }

    /**
     * 将传入的JSON对象，转换为PDF文件，转换为Base64字符串。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     */
    public String getBase64(Map<String, Object> parameters, JasperReport jasperReport) throws Exception {

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
        byte[] bytes = JasperExportManager.exportReportToPdf(jasperPrint);

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将传入的JSON对象，转换为HTML文件流，返回到Response中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param response Http响应对象
     * @param outPutFile html临时文件（路径+文件名）
     */
    public void getHtml(Map<String, Object> parameters, JasperReport jasperReport,
                       HttpServletResponse response, String outPutFile) throws Exception {
        response.setContentType("text/html");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
        JasperExportManager.exportReportToHtmlFile(jasperPrint, outPutFile);

        File fileHtml = new File(outPutFile);
        if(fileHtml.exists()){
            OutputStream os = response.getOutputStream();

            InputStream is = new FileInputStream(fileHtml);
            byte[] bytes = new byte[2048];
            int intLength;
            while ((intLength = is.read(bytes)) != -1) {
                os.write(bytes, 0, intLength);
            }

            if(is != null){
                is.close();
            }
            if(os != null){
                os.close();
            }

            fileHtml.delete();
        }

    }

}
