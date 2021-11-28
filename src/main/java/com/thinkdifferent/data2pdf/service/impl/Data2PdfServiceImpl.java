package com.thinkdifferent.data2pdf.service.impl;

import com.thinkdifferent.data2pdf.service.Data2PdfService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class Data2PdfServiceImpl implements Data2PdfService {

    /**
     * 将传入的JSON对象，转换为PDF文件，输出到指定的目录中。
     * @param parameters 输入的参数，包括JSON数据对象
     * @param jasperReport 报表文件对象
     * @param strOutputPathFileName 输出的PDF所在路径（绝对路径）和PDF文件名
     */
    public void CreatePdf(Map<String, Object> parameters, JasperReport jasperReport,
                   String strOutputPathFileName) {
        try{
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters);
            JasperExportManager.exportReportToPdfFile(jasperPrint, strOutputPathFileName);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
