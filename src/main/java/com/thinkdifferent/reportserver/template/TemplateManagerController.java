/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.template;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.thinkdifferent.reportserver.common.TemplateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/templateManager")
public class TemplateManagerController {
    private static final String ID="id";
    private static final String GROUP_ID="groupId";
    private static final String NAME="name";
    private static final String IS_GROUP="isGroup";
    private static final String DATA="data";

    @Autowired
    TemplateManagerService managerService;
    
    // 获取目录下面的模板列表
    @GetMapping("/getTemplates")
    @ResponseBody
    public Object getTemplates(@RequestParam(GROUP_ID) String groupId) throws Exception {
        List<Map<String, Object>> templateInfos;
        if(groupId != null && !groupId.isEmpty()){
            templateInfos = managerService.getTemplates(groupId);
        }else{
            templateInfos = managerService.getTemplates();
        }

        return templateInfos;
    }
    
    // 获取一个模板的数据
    @GetMapping("/getTemplate")
    @ResponseBody
    public Object getTemplate(@RequestParam(ID) String templateId) throws Exception {
        Map<String, Object> templateInfo = managerService.getTemplate(templateId);
        if(templateInfo==null) {
            throw new TemplateNotFoundException("Template with id '" + templateId + "' was not found.");
        }
        
        Map<String, String> returnTemplateInfo=new LinkedHashMap<String, String>();
        String id=(String) templateInfo.get(ID);
        returnTemplateInfo.put(ID, id);
        
        byte[] dataBytes=(byte[])templateInfo.get(DATA);
        String data=new String(dataBytes, "UTF-8");
        returnTemplateInfo.put(DATA, data);
        
        return returnTemplateInfo;
    }
    
    // 创建一个模板
    @PostMapping("/createTemplate")
    @ResponseBody
    public Object createTemplate(@RequestParam Map<String,String> templateData) throws Exception {
    	String groupId=(String) templateData.get(GROUP_ID);
        String name=(String) templateData.get(NAME);
        String isGroup = (String) templateData.get(IS_GROUP);
        String data=(String) templateData.get(DATA);
        
        byte[] dataBytes=data==null?null: data.getBytes("UTF-8");

        // do create
        Map<String, Object> templateInfo = new LinkedHashMap<String, Object>();
        templateInfo.put(GROUP_ID, groupId);
        templateInfo.put(NAME, name);
        templateInfo.put(IS_GROUP, Boolean.valueOf(isGroup));
        templateInfo.put(DATA, dataBytes);
        
        String templateId=managerService.createTemplate(templateInfo);
        Map<String, Object> addedTemplateInfo=managerService.getTemplate(templateId);
        return addedTemplateInfo;
    }
    
    // 更新一个模板的数据
    @PostMapping("/updateTemplate")
    @ResponseBody
    public void updateTemplate(@RequestParam Map<String, String> templateData) throws UnsupportedEncodingException {
        String id=templateData.get(ID);
        String data=templateData.get(DATA);
        
        if(id==null || data==null) {
            throw new IllegalArgumentException(String.format("Illegal argument: id='%s', data='%s'!", id, data));
        }
        
        byte[] dataBytes=data.getBytes("UTF-8");
        managerService.updateTemplateData(id, dataBytes);
    }
    
    // 删除模板
    @PostMapping("/deleteTemplate")
    @ResponseBody
    public void deleteTemplate(@RequestParam(ID) String id) {
        managerService.deleteTemplate(id);
    }
    
    // 重命名模板
    @PostMapping("/renameTemplate")
    @ResponseBody
    public void renameTemplate(@RequestParam(ID) String id, @RequestParam(NAME) String newName) {
        managerService.renameTemplate(id, newName);
    }
    
    // 移动模板
    @PostMapping("/moveTemplate")
    @ResponseBody
    public void moveTemplate(@RequestParam(ID) String id, @RequestParam(GROUP_ID) String groupId) {
        managerService.moveTemplate(id, groupId);
    }
}
