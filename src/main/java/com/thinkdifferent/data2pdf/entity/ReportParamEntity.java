package com.thinkdifferent.data2pdf.entity;

import com.thinkdifferent.data2pdf.service.Data2PdfService;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletResponse;

/**
 * 生成报表时传入参数的对象
 *
 * @author 张镇
 * @version 1.0
 * @date 2022-6-8
 */
public class ReportParamEntity {

    /**
     * 报表输出方式：流(stream）、
     *      单文件回写（singleWriteBack）、多文件回写（multiWriteBack）、
     *      单文件Base64（singleBase64）、多文件Base64（multiBase64）
     */
    private String outputType;

    /**
     * 报表模板的路径（不包含“reportfile”。如果报表中有子报表、图片，会用到这个路径参数）
      */
    private String reportPath;
    /**
     * 报表缓冲对象
     */
    private JRFileVirtualizer jrFileVirtualizer;
    /**
     * 输出的PDF报表文件的路径和文件名
     */
    private String outputPathFileName;
    /**
     * 传入的JSON数据对象（JSONObject）
     */
    private JSONObject joData;
    /**
     * 传入的JSON数组（JSONArray）
     */
    private JSONArray jaData;
    /**
     * Service对象
     */
    private Data2PdfService data2PdfServicep;
    /**
     * JasperReport报表模板对象
     */
    private JasperReport jasperReport;
    /**
     * Http响应对象
     */
    private HttpServletResponse response;


    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public JRFileVirtualizer getJrFileVirtualizer() {
        return jrFileVirtualizer;
    }

    public void setJrFileVirtualizer(JRFileVirtualizer jrFileVirtualizer) {
        this.jrFileVirtualizer = jrFileVirtualizer;
    }

    public JSONObject getJoData() {
        return joData;
    }

    public void setJoData(JSONObject joData) {
        this.joData = joData;
    }

    public JSONArray getJaData() {
        return jaData;
    }

    public void setJaData(JSONArray jaData) {
        this.jaData = jaData;
    }

    public Data2PdfService getData2PdfServicep() {
        return data2PdfServicep;
    }

    public void setData2PdfServicep(Data2PdfService data2PdfServicep) {
        this.data2PdfServicep = data2PdfServicep;
    }

    public JasperReport getJasperReport() {
        return jasperReport;
    }

    public void setJasperReport(JasperReport jasperReport) {
        this.jasperReport = jasperReport;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    public String getOutputPathFileName() {
        return outputPathFileName;
    }

    public void setOutputPathFileName(String outputPathFileName) {
        this.outputPathFileName = outputPathFileName;
    }
}
