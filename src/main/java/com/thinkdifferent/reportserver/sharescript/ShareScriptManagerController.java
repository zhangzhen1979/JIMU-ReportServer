/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this script file, choose Tools | Scripts
 * and open the script in the editor.
 */
package com.thinkdifferent.reportserver.sharescript;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/shareScriptManager")
public class ShareScriptManagerController {
    private static final String ID="id";
    private static final String GROUP_ID="groupId";
    private static final String NAME="name";
    private static final String IS_GROUP="isGroup";
    private static final String CODE="code";

    @Autowired
    ShareScriptManagerService managerService;
    
    @GetMapping("/getScripts")
    @ResponseBody
    public Object getScripts(@RequestParam(GROUP_ID) String groupId) throws JsonProcessingException {
        List<Map<String, Object>> scriptInfos = managerService.getScripts(groupId);
        return scriptInfos;
    }
    
    @GetMapping("/getScript")
    @ResponseBody
    public Object getScript(@RequestParam(ID) String scriptId) throws Exception {
        Map<String, Object> scriptInfo = managerService.getScript(scriptId);
        if(scriptInfo==null) {
            throw new ShareScriptNotFoundException("Script with id '" + scriptId + "' was not found.");
        }
        
        Map<String, String> returnScriptInfo=new LinkedHashMap<String, String>();
        String id=(String) scriptInfo.get(ID);
        returnScriptInfo.put(ID, id);
        
        String code=(String)scriptInfo.get(CODE);
        returnScriptInfo.put(CODE, code);
        
        return returnScriptInfo;
    }
    
    @PostMapping("/createScript")
    @ResponseBody
    public Object createScript(@RequestParam Map<String,String> scriptData) throws Exception {
    	String groupId=(String) scriptData.get(GROUP_ID);
        String name=(String) scriptData.get(NAME);
        String isGroup = (String) scriptData.get(IS_GROUP);
        String code=(String) scriptData.get(CODE);
        
        // do create
        Map<String, Object> scriptInfo = new LinkedHashMap<String, Object>();
        scriptInfo.put(GROUP_ID, groupId);
        scriptInfo.put(NAME, name);
        scriptInfo.put(IS_GROUP, Boolean.valueOf(isGroup));
        scriptInfo.put(CODE, code);
        
        String scriptId=managerService.createScript(scriptInfo);
        Map<String, Object> addedScriptInfo=managerService.getScript(scriptId);
        return addedScriptInfo;
    }
    
    @PostMapping("/updateScript")
    @ResponseBody
    public void updateScript(@RequestParam Map<String, String> scriptData) throws UnsupportedEncodingException {
        String id=scriptData.get(ID);
        String code=scriptData.get(CODE);
        
        if(id==null || code==null) {
            throw new IllegalArgumentException(String.format("Illegal argument: id='%s', code='%s'!", id, code));
        }
        
        managerService.updateScriptCode(id, code);
    }
    
    @PostMapping("/deleteScript")
    @ResponseBody
    public void deleteScript(@RequestParam(ID) String id) {
        managerService.deleteScript(id);
    }
    
    @PostMapping("/renameScript")
    @ResponseBody
    public void renameScript(@RequestParam(ID) String id, @RequestParam(NAME) String newName) {
        managerService.renameScript(id, newName);
    }
    
    @PostMapping("/moveScript")
    @ResponseBody
    public void moveScript(@RequestParam(ID) String id, @RequestParam(GROUP_ID) String groupId) {
        managerService.moveScript(id, groupId);
    }
}
