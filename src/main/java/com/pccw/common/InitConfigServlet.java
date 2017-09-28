package com.pccw.common;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import com.pccw.service.CacheManage;
import com.pccw.util.SpringUtil;


public class InitConfigServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	Logger log = Logger.getLogger(this.getClass());
	
	public void init(ServletConfig config) throws ServletException{
		CacheManage cacheManage = (CacheManage)SpringUtil.getBean("cacheManage");
		//首先加载所有缓存
		cacheManage.loadAllCache();
		try {
			log.info("缓存加载任务调度开始");
			cacheManage.loadAndScheduleAllCache();
			log.info("缓存加载任务调度结束");
		} catch (SchedulerException e) {
			log.error("初始化缓存调度任务失败", e);
		}
	}
	
}
