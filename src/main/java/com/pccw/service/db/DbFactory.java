package com.pccw.service.db;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.pccw.util.ErpUtil;
import com.pccw.util.FileUtil;
import com.pccw.util.PropertyUtil;


public class DbFactory{
	
	private static final Logger log = Logger.getLogger(DbManager.class);
    public static final String SYSTEM = "system";
    public static final String FORM = "form";
    private static Map<String,SqlSessionFactory> mapFactory = new HashMap<String,SqlSessionFactory>();
    private static ErpUtil erpUtil = null;
    private static DbManager manager = new DbManager();
    
    static{
        try{
            erpUtil = new ErpUtil(FileUtil.getValue("des3.desKey"),FileUtil.getValue("des3.desIV"));
        }catch(Exception e)
        {
            log.error("初始化加密工具异常!");
        }
    }
    // 初始化
    public static void init(String dbName){
    	long t1 = System.nanoTime();
    	try
    	{
	    	JSONObject dbJson = JSONObject.parseObject(manager.getDBConnectionByName(dbName));
	    	String dbtype = dbJson.getString("dbtype");
	    	if(!"Mysql".equals(dbtype)&&!"Oracle".equals(dbtype)&&!"db2".equals(dbtype))
	        {
	            return;
	        }
	    	SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
	    	DruidDataSource dataSource = new DruidDataSource();
	    	dataSource.setUsername(dbJson.getString("username"));
	        dataSource.setPassword(erpUtil.decode(dbJson.getString("password")));
	    	dataSource.setDriverClassName(dbJson.getString("driver"));
	    	if("Mysql".equals(dbtype)){
	    		dataSource.setUrl(dbJson.getString("url")+"?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&rewriteBatchedStatements=true");	
	    	}else{
	    		dataSource.setUrl(dbJson.getString("url"));
	    	}
	        dataSource.setMaxActive(Integer.valueOf(dbJson.getString("maxPoolSize")));
	        dataSource.setInitialSize(Integer.valueOf(dbJson.getString("minPoolSize")));
	        dataSource.setMinIdle(Integer.valueOf(dbJson.getString("minPoolSize")));
	        dataSource.setTimeBetweenEvictionRunsMillis(60000);//检测数据源空连接间隔时间
	        dataSource.setMinEvictableIdleTimeMillis(300000);//连接空闲时间
	        dataSource.setTestWhileIdle(true);
	        if("Oracle".equals(dbtype)){
	        	dataSource.setPoolPreparedStatements(true);
	        }
	        dataSource.setValidationQuery("select 'x' FROM DUAL");
	        dataSource.init();
	        //填充数据源
	        factoryBean.setDataSource(dataSource);
	        //填充SQL文件
	        factoryBean.setMapperLocations(getMapLocations(dbtype,dbName));
	        Configuration configuration = new Configuration();
	        configuration.setCallSettersOnNulls(true);
	        //启动SQL日志
	        //configuration.setLogImpl(Log4jImpl.class);
	        factoryBean.setConfiguration(configuration);
	        mapFactory.put(dbJson.getString("name"), factoryBean.getObject());
	        long t2 = System.nanoTime();
	        log.info("初始化数据库【"+dbName+"】耗时"+ String.format("%.4fs", (t2 - t1) * 1e-9));
    	}
    	catch(Exception e)
    	{
    		log.error("初始化数据库【"+dbName+"】失败!");
    		e.printStackTrace();
    	}
    }
    
    public static SqlSession open(String dbName) {
        return open(true, dbName);
    }

    // 获取一个session
    public static SqlSession open(boolean autoCommit, String dbName) 
    {
    	if(mapFactory.get(dbName) == null) init(dbName);
    	return mapFactory.get(dbName).openSession(autoCommit);
    }

   
    
    private static Resource[] getMapLocations(String dbType,String dbName)
    {
    	String[] locations = new String[3];
    	if("Oracle".equals(dbType)&&DbFactory.SYSTEM.equals(dbName))
    	{
    		locations = new String[4];
    		locations[3] = DbFactory.class.getClassLoader().getResource("oracleSql").getPath();
    	}
    	else if("Mysql".equals(dbType)&&DbFactory.FORM.equals(dbName))
    	{
    		locations = new String[4];
    		locations[3] = DbFactory.class.getClassLoader().getResource("mySqlSql").getPath();
    	}
    	locations[0] = PropertyUtil.getByKey("file.userSqlPath");
    	locations[1] = PropertyUtil.getByKey("file.userFunctionPath");
    	locations[2] = PropertyUtil.getByKey("file.userDictionaryPath");
    	List<FileSystemResource> resources = new ArrayList<FileSystemResource>();
    	for (String location:locations)
    	{
    		File[] fileList = new File(location).listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xml")) {
						return true;
					}
					return false;
				}});
    		if(fileList != null){
    			for(int i = 0;i<fileList.length;i++)
    			{
    				resources.add(new FileSystemResource(fileList[i])); 
    			}
    		}
		}
    	FileSystemResource[] a = new FileSystemResource[resources.size()];
    	return resources.toArray(a);
    }
}
