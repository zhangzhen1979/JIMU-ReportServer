/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thinkdifferent.reportserver.template;

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
public class TemplateManagerService {
    
    private static final String TEMPLATE_PREFIX="template";
    
    private static final String ID="id";
    private static final String GROUP_ID="groupId";
    private static final String NAME="name";
    private static final String IS_GROUP="isGroup";
    private static final String DATA="data";

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;
    
    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager(SynchronizationType.SYNCHRONIZED);
    }
    
    @Transactional
    public List<Map<String, Object>> getTemplates(String groupId) {
        EntityManager entityManager=getEntityManager();
        Query query=entityManager.createQuery("SELECT t.id AS id, t.groupId AS groupId, t.name AS name, t.group as isGroup FROM Template t WHERE t.groupId=:groupId");
        query.setParameter("groupId", groupId);
        List<Object[]> resultList=query.getResultList();
        
        List<Map<String, Object>> templateInfos=new ArrayList<Map<String, Object>>();
        for(Object[] rowFields: resultList) {
            Map<String, Object> templateInfo=new LinkedHashMap<String, Object>();
            templateInfo.put(ID, rowFields[0]);
            templateInfo.put(GROUP_ID, rowFields[1]);
            templateInfo.put(NAME, rowFields[2]);
            templateInfo.put(IS_GROUP, rowFields[3]);
            templateInfos.add(templateInfo);
        }
        return templateInfos;
    }
    
    @Transactional
    public Map<String, Object> getTemplate(String id) {
        EntityManager entityManager=getEntityManager();
        TemplateEntity template = entityManager.find(TemplateEntity.class, id);
        if(template==null) {
            return null;
        }
        
        Map<String, Object> templateInfo = new LinkedHashMap<String, Object>();
        templateInfo.put(ID, template.getId());
        templateInfo.put(GROUP_ID, template.getId());
        templateInfo.put(NAME, template.getName());
        templateInfo.put(DATA, template.getData());

        return templateInfo;
    }
    
    @Transactional
    public String createTemplate(Map<String, Object> templateInfo) {
        EntityManager entityManager=getEntityManager();
        
        String groupId=(String)templateInfo.get(GROUP_ID);
        String name=(String)templateInfo.get(NAME);
        Boolean isGroup=(Boolean)templateInfo.get(IS_GROUP);
        byte[] data=(byte[])templateInfo.get(DATA);
        
        Query query=entityManager.createQuery("SELECT t.id as id FROM Template t where t.groupId=:groupId and t.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", name);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Template with name '%s' already exists!", name));
        }
        
        TemplateEntity template=new TemplateEntity();
        template.setGroupId(groupId);
        template.setName(name);
        template.setGroup(isGroup);
        template.setData(data);
        
        String id= IDUtils.createId(TEMPLATE_PREFIX);
        template.setId(id);
        
        entityManager.persist(template);
        return id;
    }
    
    @Transactional
    public void updateTemplateData(String id, byte[] data) {
        EntityManager entityManager=getEntityManager();
        TemplateEntity template=entityManager.find(TemplateEntity.class, id);
        template.setData(data);
    }
    
    @Transactional
    public void deleteTemplate(String id) {
        EntityManager entityManager=getEntityManager();
        TemplateEntity template=entityManager.find(TemplateEntity.class, id);
        entityManager.remove(template);
    }
    
    @Transactional
    public void renameTemplate(String id, String newName) {
        EntityManager entityManager=getEntityManager();
        TemplateEntity template=entityManager.find(TemplateEntity.class, id);
        
        String groupId=template.getGroupId();
        Query query=entityManager.createQuery("SELECT t.id as id FROM Template t where t.groupId=:groupId and t.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", newName);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Template with name '%s' already exists!", newName));
        }
        template.setName(newName);
    }
    
    @Transactional
    public void moveTemplate(String id, String groupId) {
        EntityManager entityManager=getEntityManager();
        TemplateEntity template=entityManager.find(TemplateEntity.class, id);
        
        String name=template.getName();
        Query query=entityManager.createQuery("SELECT t.id as id FROM Template t where t.groupId=:groupId and t.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", name);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Template with name '%s' already exists!", name));
        }
        template.setGroupId(groupId);
    }
}
