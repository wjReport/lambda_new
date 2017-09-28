package com.pccw.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.pccw.common.AppCache;
import com.pccw.common.BaseService;
import com.pccw.job.CacheJobNew;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * 缓存管理
 * @author kevin
 *
 */
@Service
public class CacheManage extends BaseService{
	
	@Resource
	CacheDefinitionManage cdm ;
	private static Cache cache = AppCache.getCache();
	
	/**
	 * 加载所有缓存
	 */
	public void loadAllCache(){
		Map<String, Object> p = new HashMap<>();
		p.put("isActive", 1);
		List<Map<String, Object>> list = cdm.selectAll(p);
		list.stream().forEach(m->this.loadCache(m));
	}
	
	/**
	 * 加载单个缓存
	 * @param dbUser
	 * @param selectId
	 * @param parameter
	 */
	public void loadCache(Map<String, Object> cacheDef){
		long before = System.currentTimeMillis();
		String selectId = (String)cacheDef.get("select_id");
		String dbUser = (String)cacheDef.get("db_user");
		Object para = cacheDef.get("parameter");
		List<Map<String, Object>> result = this.doInCallBack(dbUser, session->{
			Map<String, Object> parameter = null;
			if(para != null && !"".equals(para)){
				parameter = JSON.parseObject((String)para, Map.class);
			}
			return session.selectList(selectId, parameter);
		});
		Element element = new Element(selectId, result);
		cache.put(element);
		log.info("加载缓存:"+selectId+" 耗时："+(System.currentTimeMillis()-before)+"毫秒");
	}
	
	/**
	 * 将缓存从cache中移除，同时将数据库中缓存定义为不启用
	 * @param selectId
	 */
	public void disableCache(String selectId){
		cache.remove(selectId);
		cdm.disable(selectId);
	}
	
	/**
	 * 启用一个cache
	 * @param dbUser
	 * @param selectId
	 * @param parameter
	 */
	public void enableCache(String selectId){
		cdm.endable(selectId);
		Map<String, Object> cacheDef = cdm.findBySelectId(selectId);
		loadCache(cacheDef);
	}
	
	/**
	 * 获取缓存
	 * @param key=select_id
	 * @return
	 */
	public Object getCacheValueByKey(String key){
		Element element = cache.get(key);
		if(element != null && element.getObjectValue() != null){
			return element.getObjectValue();
		}else{
			Map<String, Object> def = cdm.findBySelectId(key);
			this.loadCache(def);
			return cache.get(key).getObjectValue();
		}
	}
	
	/**
	 * 获取list类型缓存
	 * @param key
	 * @return
	 */
	public List<Map<String, Object>> getListMapCacheValueByKey(String key){
		Object obj = this.getCacheValueByKey(key);
		if(obj instanceof List){
			return (List<Map<String, Object>>)obj;
		}
		return null;
	}
	
	/**
	 * 将缓存添加到缓存调度中
	 * @throws SchedulerException 
	 */
	public void loadAndScheduleAllCache() throws SchedulerException{
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("isActive", 1);
		List<Map<String, Object>> list = cdm.selectAll(parameter);
		for(Map<String, Object> m : list){
			String selectId = (String)m.get("select_id");
			String pa = (String)m.get("parameter");
			String dbUser = (String)m.get("db_user");
			String cronExp = (String)m.get("load_strategy");
			//定义job
			JobDetail job = newJob(CacheJobNew.class).withIdentity("job_"+selectId, "group1")
							 .usingJobData("select_id", selectId)
							 .usingJobData("parameter", pa)
							 .usingJobData("db_user", dbUser).build();
			Trigger trigger = newTrigger().withIdentity("trigger_"+selectId, "group1").startNow()
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExp)).build();
			scheduler.scheduleJob(job, trigger);
		}
	}
}
