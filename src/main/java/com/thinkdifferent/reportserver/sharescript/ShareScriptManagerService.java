/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this script file, choose Tools | Scripts
 * and open the script in the editor.
 */
package com.thinkdifferent.reportserver.sharescript;

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
public class ShareScriptManagerService {
    
    private static final String SCRIPT_PREFIX="script";
    
    private static final String ID="id";
    private static final String GROUP_ID="groupId";
    private static final String NAME="name";
    private static final String PATH="path";
    private static final String IS_GROUP="isGroup";
    private static final String CODE="code";

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;
    
    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager(SynchronizationType.SYNCHRONIZED);
    }
    
    @Transactional
    public List<Map<String, Object>> getScripts(String groupId) {
        List<ShareScriptEntity> shareScripts;
        EntityManager entityManager=getEntityManager();
        if(groupId!=null && !groupId.isEmpty()) {
            String jpql="SELECT s FROM ShareScript s WHERE s.groupId=:groupId";
            Query query=entityManager.createQuery(jpql);
            query.setParameter("groupId", groupId);
            shareScripts=query.getResultList();
        } else {
            String jpql="SELECT s FROM ShareScript s";
            Query query=entityManager.createQuery(jpql);
            shareScripts=query.getResultList();
        }
        
        List<Map<String, Object>> scriptInfos=new ArrayList<Map<String, Object>>();
        for(ShareScriptEntity shareScript: shareScripts) {
            Map<String, Object> scriptInfo=toInfo(shareScript);
            scriptInfos.add(scriptInfo);
        }
        return scriptInfos;
    }
    
    @Transactional
    public Map<String, Object> getScript(String id) {
        EntityManager entityManager=getEntityManager();
        ShareScriptEntity script = entityManager.find(ShareScriptEntity.class, id);
        if(script==null) {
            return null;
        }
        
        Map<String, Object> scriptInfo = toInfo(script);
        return scriptInfo;
    }
    
    @Transactional
    public Map<String, Object> getScriptByPath(String path) {
        EntityManager entityManager=getEntityManager();
        
        String jpql="SELECT s FROM ShareScript s WHERE s.path=:path";
        Query query=entityManager.createQuery(jpql);
        query.setParameter("path", path);
        List<ShareScriptEntity> scriptEntities=query.getResultList();
        if(scriptEntities.isEmpty()) {
            return null;
        }

        ShareScriptEntity scriptEntity=scriptEntities.get(0);
        Map<String, Object> scriptInfo=toInfo(scriptEntity);
        return scriptInfo;
    }
    
    @Transactional
    public String createScript(Map<String, Object> scriptInfo) {
        EntityManager entityManager=getEntityManager();
        
        String groupId=(String)scriptInfo.get(GROUP_ID);
        String name=(String)scriptInfo.get(NAME);
        Boolean isGroup=(Boolean)scriptInfo.get(IS_GROUP);
        String code=(String)scriptInfo.get(CODE);
        
        Query query=entityManager.createQuery("SELECT s.id as id FROM ShareScript s where s.groupId=:groupId and s.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", name);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Script with name '%s' already exists!", name));
        }
        
        ShareScriptEntity scriptGroup = entityManager.find(ShareScriptEntity.class, groupId);
        String groupPath="";
        if(scriptGroup!=null) {
            groupPath=scriptGroup.getPath();
        }

        ShareScriptEntity script=new ShareScriptEntity();
        script.setGroupId(groupId);
        script.setName(name);
        script.setPath(groupPath+ "/" + name);
        script.setGroup(isGroup);
        script.setCode(code);
        
        String id= IDUtils.createId(SCRIPT_PREFIX);
        script.setId(id);
        
        entityManager.persist(script);
        return id;
    }
    
    @Transactional
    public void updateScriptCode(String id, String code) {
        EntityManager entityManager=getEntityManager();
        ShareScriptEntity script=entityManager.find(ShareScriptEntity.class, id);
        script.setCode(code);
    }
    
    @Transactional
    public void deleteScript(String id) {
        EntityManager entityManager=getEntityManager();
        ShareScriptEntity script=entityManager.find(ShareScriptEntity.class, id);
        entityManager.remove(script);
    }
    
    @Transactional
    public void renameScript(String id, String newName) {
        EntityManager entityManager=getEntityManager();
        ShareScriptEntity script=entityManager.find(ShareScriptEntity.class, id);
        
        String groupId=script.getGroupId();
        Query query=entityManager.createQuery("SELECT s.id as id FROM ShareScript s where s.groupId=:groupId and s.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", newName);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Script with name '%s' already exists!", newName));
        }
        
        ShareScriptEntity scriptGroup = entityManager.find(ShareScriptEntity.class, groupId);
        String groupPath="";
        if(scriptGroup!=null) {
            groupPath=scriptGroup.getPath();
        }
        
        script.setName(newName);
        script.setPath(groupPath + "/" + newName);
    }
    
    @Transactional
    public void moveScript(String id, String groupId) {
        EntityManager entityManager=getEntityManager();
        ShareScriptEntity script=entityManager.find(ShareScriptEntity.class, id);
        
        String name=script.getName();
        Query query=entityManager.createQuery("SELECT s.id as id FROM ShareScript s where s.groupId=:groupId and s.name=:name");
        query.setParameter("groupId", groupId);
        query.setParameter("name", name);
        List<Object[]> resultList=query.getResultList();
        if(!resultList.isEmpty()) {
        	throw new RuntimeException(String.format("Script with name '%s' already exists!", name));
        }
        
        ShareScriptEntity scriptGroup = entityManager.find(ShareScriptEntity.class, groupId);
        String groupPath="";
        if(scriptGroup!=null) {
            groupPath=scriptGroup.getPath();
        }

        script.setPath(groupPath + "/" + name);
        script.setGroupId(groupId);
    }
    
    private static Map<String, Object> toInfo(ShareScriptEntity entity) {
        Map<String, Object> scriptInfo=new LinkedHashMap<String, Object>();
        scriptInfo.put(ID, entity.getId());
        scriptInfo.put(GROUP_ID, entity.getGroupId());
        scriptInfo.put(NAME, entity.getName());
        scriptInfo.put(PATH, entity.getPath());
        scriptInfo.put(IS_GROUP, entity.isGroup());
        scriptInfo.put(CODE, entity.getCode());
        
        return scriptInfo;
    }
}
