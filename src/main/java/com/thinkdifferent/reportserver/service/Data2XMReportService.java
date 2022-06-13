package com.thinkdifferent.reportserver.service;

import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface Data2XMReportService {
    /**
     * 根据传入的参数，创建报表文件
     * @param templateId 报表模板ID
     * @param templateData 报表模板数据（模板ID有值时，此参数可空）
     * @param previewDataJson 预览用的JSON数据
     * @param previewOptions 预览参数
     * @param docType 文件类型
     * @param httpServletResponse HTTP响应对象
     * @throws Exception
     */
    File createReport(String templateId, String templateData,
                              String previewDataJson, Map<String, Object> previewOptions, String docType,
                              HttpServletResponse httpServletResponse)
            throws Exception;

    /**
     * 根据传入的参数（模板ID），生成报表文件，根据需要，返回HTTP响应
     * @param jsonInput 输入的JSON对象
     * @param httpServletResponse HTTP响应
     * @return 报表文件对象
     * @throws Exception
     */
    File getReportFileByID(JSONObject jsonInput, HttpServletResponse httpServletResponse)
            throws Exception;

    Object readJson(String json) throws IOException;


}
