package com.pccw.service.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pccw.util.ErpUtil;
import com.pccw.util.FileUtil;
import com.pccw.util.XmlUtil;


public class DbManager
{
    private static final Logger log = Logger.getLogger(DbManager.class);
    private static ErpUtil erpUtil = null;
    
    public DbManager()
    {
        try
        {
            erpUtil = new ErpUtil(FileUtil.getValue("des3.desKey"),FileUtil.getValue("des3.desIV"));
        }
        catch(Exception e)
        {
            log.error("初始化加密工具异常!");
            e.printStackTrace();
        }
    }
    
    public void init() throws Exception
    {
        JSONArray dbs = JSON.parseArray(getAllDBConnections());
        JSONObject obj = null;
        for (int i = 0; i < dbs.size(); i++)
        {
            obj = dbs.getJSONObject(i);
            String dbtype = obj.getString("dbtype");
            if(!"Mysql".equals(dbtype)&&!"Oracle".equals(dbtype)&&!"db2".equals(dbtype))
            {
                continue;
            }
            DbFactory.init(obj.getString("name"));
        }
    }
    
    public String getAllDBConnections()
    {
        JSONArray array = new JSONArray();
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(DbManager.class.getResourceAsStream("/DBConfig.xml"));
        } 
        catch (Exception e)
        {
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
        } 
        List<Element> dbs = dom.selectNodes("/DBConnection/DB");
        JSONObject obj = null;
        for (Element element:dbs)
        {
           obj = new JSONObject(true);
           obj.put("name", element.selectSingleNode("name").getText());
           obj.put("driver", element.selectSingleNode("driver").getText());
           obj.put("dbtype", element.selectSingleNode("dbtype").getText());
           obj.put("url", element.selectSingleNode("url").getText());
           obj.put("username", element.selectSingleNode("username").getText());
           obj.put("password", element.selectSingleNode("password").getText());
           obj.put("maxPoolSize", element.selectSingleNode("maxPoolSize").getText());
           obj.put("minPoolSize", element.selectSingleNode("minPoolSize").getText());
           array.add(obj);
        }
        
        return array.toJSONString();
    }
  
    public String getDBConnectionByName(@RequestBody String name)
    {
        JSONObject obj = new JSONObject(true);
        Document dom = null;
        try
        {
            dom = XmlUtil.parseXmlToDom(DbManager.class.getResourceAsStream("/DBConfig.xml"));
        } 
        catch (Exception e)
        {
            log.error("解析DBConfig.xml异常!");
            e.printStackTrace();
        } 
        Node node = dom.selectSingleNode("/DBConnection/DB[name='"+name+"']");
        if(node!=null)
        {
            obj.put("name", node.selectSingleNode("name").getText());
            obj.put("driver", node.selectSingleNode("driver").getText());
            obj.put("dbtype", node.selectSingleNode("dbtype").getText());
            obj.put("url", node.selectSingleNode("url").getText());
            obj.put("username", node.selectSingleNode("username").getText());
            obj.put("password", node.selectSingleNode("password").getText());
            obj.put("maxPoolSize", node.selectSingleNode("maxPoolSize").getText());
            obj.put("minPoolSize", node.selectSingleNode("minPoolSize").getText());
        }
        
        return obj.toJSONString();
    }
}
