/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.datasource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;

import com.thinkdifferent.reportserver.util.IDUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Administrators
 */
@Service
public class DataSourceTemplateManagerService {
    
    private static final String DATASOURCE_PREFIX="datasource";
    
    private static final String ID="id";
    private static final String GROUP_ID="groupId";
    private static final String NAME="name";
    private static final String PATH="path";
    private static final String IS_GROUP="isGroup";
    private static final String CONTENT="content";

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;
    
    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager(SynchronizationType.SYNCHRONIZED);
    }
    
    @Transactional
    public List<Map<String, Object>> getTemplates(String groupId) {
        List<DataSourceTemplateEntity> shareTemplates;
        EntityManager entityManager=getEntityManager();
        if(groupId!=null && !groupId.isEmpty()) {
            String jpql="SELECT s FROM DataSourceTemplate s WHERE s.groupId=:groupId";
            Query query=entityManager.createQuery(jpql);
            query.setParameter("groupId", groupId);
            shareTemplates=query.getResultList();
        } else {
            String jpql="SELECT s FROM DataSourceTemplate s";
            Query query=entityManager.createQuery(jpql);
            shareTemplates=query.getResultList();
        }
        
        List<Map<String, Object>> templateInfos=new ArrayList<Map<String, Object>>();
        for(DataSourceTemplateEntity shareTemplate: shareTemplates) {
            Map<String, Object> templateInfo=toInfo(shareTemplate);
            templateInfos.add(templateInfo);
        }
        return templateInfos;
    }
    
    @Transactional
    public Map<String, Object> getTemplate(String id) {
        EntityManager entityManager=getEntityManager();
        DataSourceTemplateEntity template = entityManager.find(DataSourceTemplateEntity.class, id);
        if(template==null) {
            return null;
        }
        
        Map<String, Object> templateInfo = toInfo(template);
        return templateInfo;
    }
    
    @Transactional
    public Map<String, Object> getTemplateByPath(String path) {
        EntityManager entityManager=getEntityManager();
        
        String jpql="SELECT s FROM DataSourceTemplate s WHERE s.path=:path";
        Query query=entityManager.createQuery(jpql);
        query.setParameter("path", path);
        List<DataSourceTemplateEntity> templateEntities=query.getResultList();
        if(templateEntities.isEmpty()) {
            return null;
        }

        DataSourceTemplateEntity templateEntity=templateEntities.get(0);
        Map<String, Object> templateInfo=toInfo(templateEntity);
        return templateInfo;
    }
    
    @Transactional
    public String createTemplate(Map<String, Object> templateInfo) {
        EntityManager entityManager=getEntityManager();
        
        String groupId=(String)templateInfo.get(GROUP_ID);
        String name=(String)templateInfo.get(NAME);
        Boolean isGroup=(Boolean)templateInfo.get(IS_GROUP);
        String content=(String)templateInfo.get(CONTENT);
        
        Query query=entityManager.createQuery("SELECT s.id as id FROM DataSourceTemplate s where s.groupId=:groupId and s.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", name);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Template with name '%s' already exists!", name));
        }
        
        DataSourceTemplateEntity templateGroup = entityManager.find(DataSourceTemplateEntity.class, groupId);
        String groupPath="";
        if(templateGroup!=null) {
            groupPath=templateGroup.getPath();
        }

        DataSourceTemplateEntity template=new DataSourceTemplateEntity();
        template.setGroupId(groupId);
        template.setName(name);
        template.setPath(groupPath+ "/" + name);
        template.setGroup(isGroup);
        template.setContent(content);
        
        String id= IDUtils.createId(DATASOURCE_PREFIX);
        template.setId(id);
        
        entityManager.persist(template);
        return id;
    }
    
    @Transactional
    public void updateTemplateCode(String id, String content) {
        EntityManager entityManager=getEntityManager();
        DataSourceTemplateEntity template=entityManager.find(DataSourceTemplateEntity.class, id);
        template.setContent(content);
    }
    
    @Transactional
    public void deleteTemplate(String id) {
        EntityManager entityManager=getEntityManager();
        DataSourceTemplateEntity template=entityManager.find(DataSourceTemplateEntity.class, id);
        entityManager.remove(template);
    }
    
    @Transactional
    public void renameTemplate(String id, String newName) {
        EntityManager entityManager=getEntityManager();
        DataSourceTemplateEntity template=entityManager.find(DataSourceTemplateEntity.class, id);
        
        String groupId=template.getGroupId();
        Query query=entityManager.createQuery("SELECT s.id as id FROM DataSourceTemplate s where s.groupId=:groupId and s.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", newName);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Template with name '%s' already exists!", newName));
        }
        
        DataSourceTemplateEntity templateGroup = entityManager.find(DataSourceTemplateEntity.class, groupId);
        String groupPath="";
        if(templateGroup!=null) {
            groupPath=templateGroup.getPath();
        }
        
        template.setName(newName);
        template.setPath(groupPath + "/" + newName);
    }
    
    @Transactional
    public void moveTemplate(String id, String groupId) {
        EntityManager entityManager=getEntityManager();
        DataSourceTemplateEntity template=entityManager.find(DataSourceTemplateEntity.class, id);
        
        String name=template.getName();
        Query query=entityManager.createQuery("SELECT s.id as id FROM DataSourceTemplate s where s.groupId=:groupId and s.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", name);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Template with name '%s' already exists!", name));
        }
        
        DataSourceTemplateEntity templateGroup = entityManager.find(DataSourceTemplateEntity.class, groupId);
        String groupPath="";
        if(templateGroup!=null) {
            groupPath=templateGroup.getPath();
        }

        template.setPath(groupPath + "/" + name);
        template.setGroupId(groupId);
    }
    
    private static Map<String, Object> toInfo(DataSourceTemplateEntity entity) {
        Map<String, Object> templateInfo=new LinkedHashMap<String, Object>();
        templateInfo.put(ID, entity.getId());
        templateInfo.put(GROUP_ID, entity.getGroupId());
        templateInfo.put(NAME, entity.getName());
        templateInfo.put(PATH, entity.getPath());
        templateInfo.put(IS_GROUP, entity.isGroup());
        templateInfo.put(CONTENT, entity.getContent());
        
        return templateInfo;
    }
}
