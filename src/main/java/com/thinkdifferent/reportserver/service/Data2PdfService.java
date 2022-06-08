package com.thinkdifferent.reportserver.service;

import net.sf.jasperreports.engine.JasperReport;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.util.Map;

public interface Data2PdfService {
    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和PDF文件名
     */
    void getJson2Pdf(Map<String, Object> parameters, JasperReport jasperReport,
                     String strOutputPathFileName) throws Exception;

    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param response Http响应对象
     */
    void getJson2PdfStream(Map<String, Object> parameters, JasperReport jasperReport,
                           HttpServletResponse response) throws Exception;

    /**
     * 将传入的JSON对象，转换为PDF文件，转换为Base64字符串。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     */
    String getJson2PdfBase64(Map<String, Object> parameters, JasperReport jasperReport) throws Exception;

    /**
     * 将传入的JSON对象，转换为HTML文件流，返回到Response中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param response Http响应对象
     * @param outPutFile html临时文件（路径+文件名）
     */
    void getJson2HtmlStream(Map<String, Object> parameters, JasperReport jasperReport,
                            HttpServletResponse response, String outPutFile) throws Exception;



    /**
     * 以下，均为连接数据库生成报表代码
     */

    /**
     * 将传入的数据库连接、查询语句，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和PDF文件名
     * @param conn 数据库连接对象
     */
    void getDb2Pdf(Map<String, Object> parameters, JasperReport jasperReport,
                   String strOutputPathFileName,
                   Connection conn) throws Exception;

    /**
     * 将传入的数据库连接、查询语句，转换为HTML文件流，返回到Response中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param response Http响应对象
     * @param conn 数据库连接对象
     */
    void getDb2PdfStream(Map<String, Object> parameters, JasperReport jasperReport,
                         HttpServletResponse response,
                         Connection conn) throws Exception;

    /**
     * 将传入的数据库连接、查询语句，转换为PDF文件，转换为Base64字符串。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param conn 数据库连接对象
     */
    String getDb2PdfBase64(Map<String, Object> parameters, JasperReport jasperReport,
                           Connection conn) throws Exception;

    /**
     * 将传入的数据库连接、查询语句，转换为HTML文件流，返回到Response中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param response Http响应对象
     * @param outPutFile html临时文件（路径+文件名）
     * @param conn 数据库连接对象
     */
    void getDb2HtmlStream(Map<String, Object> parameters, JasperReport jasperReport,
                          HttpServletResponse response, String outPutFile,
                          Connection conn) throws Exception;




}
