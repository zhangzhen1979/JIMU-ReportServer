package com.thinkdifferent.reportserver.entity;

import com.thinkdifferent.reportserver.service.Data2PdfService;
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
    private Data2PdfService data2PdfService;
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

    /**
     * 报表数据库查询参数：table\where\orderBy
     */
    private JSONArray jaTableParams;


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

    public Data2PdfService getData2PdfService() {
        return data2PdfService;
    }

    public void setData2PdfService(Data2PdfService data2PdfService) {
        this.data2PdfService = data2PdfService;
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

    public JSONArray getJaTableParams() {
        return jaTableParams;
    }

    public void setJaTableParams(JSONArray jaTableParams) {
        this.jaTableParams = jaTableParams;
    }
}
