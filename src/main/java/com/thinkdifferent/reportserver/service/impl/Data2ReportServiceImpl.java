package com.thinkdifferent.reportserver.service.impl;

import com.thinkdifferent.reportserver.service.Data2ReportService;
import com.thinkdifferent.reportserver.util.FileUtil;
import com.thinkdifferent.reportserver.util.IOUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Base64;
import java.util.Map;

@Service
public class Data2ReportServiceImpl implements Data2ReportService {

    /**
     * 根据传入的JSON对象/数据表参数，生成报表文件，输出到指定的目录；或返回到Response中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和P文件名
     * @param strDocType 输出文件类型
     * @param response Http响应对象
     * @param conn 数据库连接对象
     * @return 返回报表文件对象。如果Response不为空，则此处返回空值。
     * @throws Exception
     */
    public File getReportFile(Map<String, Object> parameters, JasperReport jasperReport,
                               String strOutputPathFileName, String strDocType,
                               HttpServletResponse response,  Connection conn) throws Exception {
        JasperPrint jasperPrint;
        if(conn != null){
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
        }else{
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
        }

        JRAbstractExporter exporter = null;
        String strContentType = "application/pdf";
        if("html".equalsIgnoreCase(strDocType)){
            strContentType="text/html";
            JasperExportManager.exportReportToHtmlFile(jasperPrint, strOutputPathFileName);

        }else if("pdf".equalsIgnoreCase(strDocType)){
            strContentType="application/pdf";
            JasperExportManager.exportReportToPdfFile(jasperPrint, strOutputPathFileName);

        }else if("word".equalsIgnoreCase(strDocType)){
            strContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            exporter = new JRDocxExporter();

        }else if("excel".equalsIgnoreCase(strDocType)){
            strContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            exporter = new JRXlsxExporter();

        }else if("csv".equalsIgnoreCase(strDocType)){
            strContentType="text/csv";
            exporter=new JRCsvExporter();

        }else if("xml".equalsIgnoreCase(strDocType)){
            strContentType="application/xml";
            exporter = new JRXmlExporter();

        }else if("odt".equalsIgnoreCase(strDocType)){
            strContentType="application/vnd.oasis.opendocument.text";
            exporter = new JROdtExporter();

        }


        File fileOutput = new File(strOutputPathFileName);
        if(!"html".equalsIgnoreCase(strDocType) &&
                !"pdf".equalsIgnoreCase(strDocType)){
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileOutput));

            if("Excel".equalsIgnoreCase(strDocType)){
                SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
                configuration.setOnePagePerSheet(true);
                exporter.setConfiguration(configuration);
            }
            exporter.exportReport();
        }

        if(response != null){
            response.setContentType(strContentType);
            response.setHeader("Content-Disposition", "inline;filename=" + fileOutput.getName());

            InputStream fileIs=null;
            try {
                fileIs=new FileInputStream(fileOutput);
                OutputStream os=response.getOutputStream();
                IOUtils.copyStream(fileIs, os);
            } finally {
                IOUtils.close(fileIs);
                if(fileOutput!=null) {
                    fileOutput.delete();

                    if("html".equalsIgnoreCase(strDocType)){
                        fileOutput = new File(fileOutput.getCanonicalPath()+"_files");
                        if(fileOutput.exists() && fileOutput.isDirectory()){
                            FileUtil.deleteFolderAndFiles(fileOutput.getCanonicalPath());
                        }
                    }

                    fileOutput = null;
                }
            }
        }

        return fileOutput;
    }

    /**
     * 根据传入的JSON对象/数据表参数，生成报表文件，转换为Base64字符串。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和P文件名（不含扩展名）
     * @param strDocType 输出文件类型
     * @param response Http响应对象
     * @param conn 数据库连接对象
     * @return 返回报表文件对象。如果Response不为空，则此处返回空值。
     * @throws Exception
     */
    public String getReportBase64(Map<String, Object> parameters, JasperReport jasperReport,
                                                   String strOutputPathFileName, String strDocType,
                                                   HttpServletResponse response,  Connection conn) throws Exception {
        File file = getReportFile(parameters, jasperReport,
                strOutputPathFileName, strDocType,
                response,  conn);
        byte[] bytes = fileToByte(file);

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将文件转为byte数组
     * @param file 输入的文件
     * @return 返回文件的byte数组
     */
    private byte[] fileToByte(File file) {
        byte[] fileBytes = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileBytes;
    }


}
