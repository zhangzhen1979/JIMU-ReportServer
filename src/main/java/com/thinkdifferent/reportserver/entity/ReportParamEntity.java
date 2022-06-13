package com.thinkdifferent.reportserver.entity;

import com.thinkdifferent.reportserver.service.Data2ReportService;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;

/**
 * 生成报表时传入参数的对象
 *
 * @author 张镇
 * @version 1.0
 * @date 2022-6-8
 */
public class ReportParamEntity {

    /**
     * 数据源：json、db
     */
    private String dataSource;
    /**
     * 报表输出方式：流(stream）、
     *      单文件回写（singleWriteBack）、多文件回写（multiWriteBack）、
     *      单文件Base64（singleBase64）、多文件Base64（multiBase64）
     */
    private String outputType;
    /**
     * 报表输出的格式：
     * 公共：HTML、PDF、Word（docx）、Excel（Xlsx）、
     * JasperReport：CSV、XML、ODT
     * XMReport：Image（png）
     */
    private String docType;

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
     * 传入的JSON对象（JSONObject）
     */
    private JSONObject joInput;
    /**
     * 传入的data中的JSON数组（JSONArray）
     */
    private JSONArray jaData;
    /**
     * 传入的data中的JSON对象（JSONObject）
     */
    private JSONObject joData;
    /**
     * Service对象
     */
    private Data2ReportService data2ReportService;
    /**
     * JasperReport报表模板对象
     */
    private JasperReport jasperReport;
    /**
     * Http响应对象
     */
    private HttpServletResponse response;

    /**
     * 数据库连接对象
     */
    private Connection conn;

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

    public JSONObject getJoInput() {
        return joInput;
    }

    public void setJoInput(JSONObject joInput) {
        this.joInput = joInput;
    }

    public JSONArray getJaData() {
        return jaData;
    }

    public void setJaData(JSONArray jaData) {
        this.jaData = jaData;
    }

    public Data2ReportService getData2ReportService() {
        return data2ReportService;
    }

    public void setData2ReportService(Data2ReportService data2ReportService) {
        this.data2ReportService = data2ReportService;
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

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public JSONObject getJoData() {
        return joData;
    }

    public void setJoData(JSONObject joData) {
        this.joData = joData;
    }
}
