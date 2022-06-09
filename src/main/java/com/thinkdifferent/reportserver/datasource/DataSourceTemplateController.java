/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Scripts
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.datasource;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thinkdifferent.reportserver.common.TemplateNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/dataSourceTemplateManager")
public class DataSourceTemplateController {
    private static final String ID="id";
    private static final String GROUP_ID="groupId";
    private static final String NAME="name";
    private static final String IS_GROUP="isGroup";
    private static final String CONTENT="content";

    @Autowired
    DataSourceTemplateManagerService managerService;

    @GetMapping("/getTemplates")
    @ResponseBody
    public Object getTemplates(@RequestParam(GROUP_ID) String groupId) throws JsonProcessingException {
        List<Map<String, Object>> templateInfos = managerService.getTemplates(groupId);
        return templateInfos;
    }
    
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
        
        String content=(String)templateInfo.get(CONTENT);
        returnTemplateInfo.put(CONTENT, content);
        
        return returnTemplateInfo;
    }
    
    @PostMapping("/createTemplate")
    @ResponseBody
    public Object createTemplate(@RequestParam Map<String,String> templateData) throws Exception {
    	String groupId=(String) templateData.get(GROUP_ID);
        String name=(String) templateData.get(NAME);
        String isGroup = (String) templateData.get(IS_GROUP);
        String code=(String) templateData.get(CONTENT);
        
        // do create
        Map<String, Object> templateInfo = new LinkedHashMap<String, Object>();
        templateInfo.put(GROUP_ID, groupId);
        templateInfo.put(NAME, name);
        templateInfo.put(IS_GROUP, Boolean.valueOf(isGroup));
        templateInfo.put(CONTENT, code);
        
        String templateId=managerService.createTemplate(templateInfo);
        Map<String, Object> addedTemplateInfo=managerService.getTemplate(templateId);
        return addedTemplateInfo;
    }
    
    @PostMapping("/updateTemplate")
    @ResponseBody
    public void updateTemplate(@RequestParam Map<String, String> templateData) throws UnsupportedEncodingException {
        String id=templateData.get(ID);
        String code=templateData.get(CONTENT);
        
        if(id==null || code==null) {
            throw new IllegalArgumentException(String.format("Illegal argument: id='%s', code='%s'!", id, code));
        }
        
        managerService.updateTemplateCode(id, code);
    }
    
    @PostMapping("/deleteTemplate")
    @ResponseBody
    public void deleteTemplate(@RequestParam(ID) String id) {
        managerService.deleteTemplate(id);
    }
    
    @PostMapping("/renameTemplate")
    @ResponseBody
    public void renameTemplate(@RequestParam(ID) String id, @RequestParam(NAME) String newName) {
        managerService.renameTemplate(id, newName);
    }
    
    @PostMapping("/moveTemplate")
    @ResponseBody
    public void moveTemplate(@RequestParam(ID) String id, @RequestParam(GROUP_ID) String groupId) {
        managerService.moveTemplate(id, groupId);
    }
    
    
    @PostMapping("/parseSqlDataSource")
    @ResponseBody
    public Object parseSqlDataSource(@RequestBody Map<String, Object> model) throws Exception {
        String driverClass=(String) model.get("driver");
        String jdbcUrl=(String) model.get("jdbcUrl");
        String user=(String) model.get("user");
        String password=(String) model.get("password");
        String sql=(String) model.get("sql");
        
        Class.forName(driverClass);
        Connection connection=null;
        Statement statement=null;
        ResultSet resultSet=null;
        try {
            connection=DriverManager.getConnection(jdbcUrl, user, password);
            statement=connection.createStatement();
            resultSet=statement.executeQuery(sql);
            
            Set<String> tableNames=new LinkedHashSet<String>();
            
            ResultSetMetaData metaData=resultSet.getMetaData();
            int colCount=metaData.getColumnCount();
            List<ColumnInfo> columnInfos=new ArrayList<ColumnInfo>();
            for(int i=1;i<=colCount;i++) {
                ColumnInfo columnInfo=new ColumnInfo();
                columnInfo.setColLabel(metaData.getColumnLabel(i));
                columnInfo.setColName(metaData.getColumnName(i));
                columnInfo.setTableName(metaData.getTableName(i));
                columnInfos.add(columnInfo);
                
                tableNames.add(columnInfo.getTableName());
            }
            resultSet.close();
            
            Map<String, String> columnCommentMap=new HashMap<String, String>();
            for(String tableName : tableNames) {
                String showColumnsSql="SHOW FULL COLUMNS FROM " + tableName;
                resultSet=statement.executeQuery(showColumnsSql);
                while(resultSet.next()) {
                    String key=tableName + "-" + resultSet.getObject("Field");
                    String comment=resultSet.getString("Comment");
                    columnCommentMap.put(key, comment);
                }
                resultSet.close();
            }
            
            for(ColumnInfo columnInfo: columnInfos) {
                String key=columnInfo.getTableName() + "-" + columnInfo.getColName();
                String comment=columnCommentMap.get(key);
                columnInfo.setColComment(comment);
            }
            
            return columnInfos;
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }
    
    private void close(Object object) {
        try {
            if(object instanceof Connection) {
                ((Connection)object).close();
            }
            if(object instanceof Statement) {
                ((Statement)object).close();
            }
            if(object instanceof ResultSet) {
                ((ResultSet)object).close();
            }
            if(object instanceof Closeable) {
                ((Closeable)object).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class ColumnInfo {
        private String colLabel;
        
        private String colName;
        private String colComment;
        
        private String tableName;
        
        public String getColLabel() {
            return colLabel;
        }
        public void setColLabel(String colLabel) {
            this.colLabel = colLabel;
        }
        public String getColName() {
            return colName;
        }
        public void setColName(String colName) {
            this.colName = colName;
        }
        public String getColComment() {
            return colComment;
        }
        public void setColComment(String colComment) {
            this.colComment = colComment;
        }
        public String getTableName() {
            return tableName;
        }
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        
    }
}
