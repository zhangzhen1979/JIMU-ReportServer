/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.controller;

import com.thinkdifferent.reportserver.service.Data2XMReportService;
import com.thinkdifferent.reportserver.service.RabbitMQService;
import com.thinkdifferent.reportserver.util.IOUtils;
import com.thinkdifferent.reportserver.util.WriteBackUtil;
import net.sf.json.JSONObject;
import org.mosmith.tools.report.engine.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/report")
public class XMReportController {
    
    @Autowired
    private Data2XMReportService data2XMReportService;

    @Autowired
    private RabbitMQService rabbitMQService;

    @PostMapping("/preview")
    public void preview(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String templateData=httpServletRequest.getParameter("templateData");
        String previewDataJson=httpServletRequest.getParameter("previewData");
        String previewOptionsJson=httpServletRequest.getParameter("previewOptions");

        String templateId=httpServletRequest.getParameter("templateId");

        Map<String, Object> previewOptions=(Map<String, Object>) data2XMReportService.readJson(previewOptionsJson);

        String docType=StringUtils.nonull(previewOptions.get("docType"));
        if (httpServletRequest.getParameter("docType")!=null) {
            docType=httpServletRequest.getParameter("docType");
        }

        File file = data2XMReportService.createReport(templateId, templateData,
                previewDataJson, previewOptions, docType,
                httpServletResponse);

        InputStream fileIs=null;
        try {
            fileIs=new FileInputStream(file);
            OutputStream os=httpServletResponse.getOutputStream();
            IOUtils.copyStream(fileIs, os);
        } finally {
            IOUtils.close(fileIs);
            if(file!=null) {
                file.delete();
            }
        }


    }



    /**
     * 根据传入的JSON数据，给HTTP请求返回报表文件
     * @param jsonInput 传入的JSON数据
     * @param httpServletResponse HTTP响应
     * @throws Exception
     */
    @PostMapping("/getStream")
    public void getStream(@RequestBody JSONObject jsonInput, HttpServletResponse httpServletResponse) throws Exception {
        data2XMReportService.getReportFileByID(jsonInput, httpServletResponse);
    }

    /**
     * 根据传入的JSON数据，生成报表文件，并回写到指定位置
     * @param jsonInput
     * @throws Exception
     */
    @PostMapping("/getFile")
    public JSONObject getFile(@RequestBody JSONObject jsonInput) throws Exception {
        // 生成报表文件
        File file = data2XMReportService.getReportFileByID(jsonInput, null);

        JSONObject jsonFile = new JSONObject();
        if(file.exists()){
            jsonFile.put("flag", "success");
            jsonFile.put("file", file.getCanonicalPath());
        }

        boolean blnSuccess = WriteBackUtil.writeBack(jsonInput, jsonFile);
        JSONObject jsonReturn = new JSONObject();
        if(blnSuccess){
            jsonReturn.put("flag", "success" );
            jsonReturn.put("message", "Report file write back success. API call back success." );
        }else{
            jsonReturn.put("flag", "error" );
            jsonReturn.put("message", "Report file write back error. OR API call back error." );
        }
        jsonReturn.put("file", file.getCanonicalPath());

        return jsonReturn;
    }

    /**
     * 根据传入的JSON数据，生成报表文件，并返回Base64字符串
     * @param jsonInput
     * @throws Exception
     */
    @PostMapping("/getBase64")
    public String getBase64(@RequestBody JSONObject jsonInput) throws Exception {
        // 生成报表文件
        File file = data2XMReportService.getReportFileByID(jsonInput, null);

        if(file.exists()){
            byte[] b = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
            // 转换为byte后，PDF文件即可删除
            file.delete();
            return Base64.getEncoder().encodeToString(b);
        }

        return null;
    }


    @RequestMapping(value = "/put2Mq", method = RequestMethod.POST)
    public Map<String, String> put2Mq(@RequestBody JSONObject jsonInput) {
        Map<String, String> mapReturn = new HashMap<>();
        mapReturn.put("flag", "success" );
        mapReturn.put("message", "Set JSON Data to MQ Success" );

        rabbitMQService.setXMData2MQ(jsonInput);

        return mapReturn;
    }


}

