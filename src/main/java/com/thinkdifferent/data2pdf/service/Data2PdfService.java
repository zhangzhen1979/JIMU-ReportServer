package com.thinkdifferent.data2pdf.service;

import net.sf.jasperreports.engine.JasperReport;

import java.util.Map;

public interface Data2PdfService {
    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和PDF文件名
     */
    void CreatePdf(Map<String, Object> parameters, JasperReport jasperReport,
                   String strOutputPathFileName);
}
