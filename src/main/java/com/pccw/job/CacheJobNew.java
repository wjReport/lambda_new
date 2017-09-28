package com.pccw.job;

import java.util.HashMap;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.pccw.common.BaseService;
import com.pccw.service.CacheManage;
import com.pccw.util.SpringUtil;


/**
 * 加载所有数据缓存
 * @author kevin
 *
 */
public class CacheJobNew extends BaseService implements org.quartz.Job{
	
	CacheManage cacheManage = (CacheManage)SpringUtil.getBean("cacheManage");
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		Map<String, Object> cacheDef = new HashMap<String, Object>(3);
		for(String key : dataMap.getKeys()){
			cacheDef.put(key, dataMap.getString(key));
		}
		cacheManage.loadCache(cacheDef);
	}

}
