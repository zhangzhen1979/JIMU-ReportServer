package com.thinkdifferent.reportserver.service;

import net.sf.jasperreports.engine.JasperReport;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Connection;
import java.util.Map;

public interface Data2ReportService {
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
    File getReportFile(Map<String, Object> parameters, JasperReport jasperReport,
                              String strOutputPathFileName, String strDocType,
                              HttpServletResponse response,  Connection conn) throws Exception;

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
    String getReportBase64(Map<String, Object> parameters, JasperReport jasperReport,
                                  String strOutputPathFileName, String strDocType,
                                  HttpServletResponse response,  Connection conn) throws Exception;


}
