package com.pccw.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.pccw.common.BaseController;
import com.pccw.common.PageAssistant;
import com.pccw.service.CacheDefinitionManage;
import com.pccw.service.CacheManage;


@RestController
@RequestMapping("/cacheManage")
public class CacheController extends BaseController {
	@Autowired
	CacheDefinitionManage cdm;
	
	@Autowired
	CacheManage cacheManage;
	
	/**
	 * 获取缓存定义分页列表
	 * @param pJson
	 */
	@RequestMapping(value = "/findByPage")
	public void findByPage(@RequestBody String pJson) {
		this.printJson(()->{
			PageAssistant assistant = new PageAssistant(pJson);
			cdm.selectByPage(assistant);
			return assistant;
		});
	}
	
	/**
	 * 单独加载一个缓存
	 * @param selectId
	 */
	@RequestMapping(value = "/loadCache/{selectId}")
	public void loadCache(@PathVariable("selectId") String selectId){
		this.printJson(()->{
			Map<String, Object> cacheDef = cdm.findBySelectId(selectId);
			cacheManage.loadCache(cacheDef);
			return Boolean.TRUE;
		});
	}
	
	/**
	 * 添加缓存定义
	 * @param pJson
	 */
	@RequestMapping(value = "/insert")
	public void insertCacheDef(@RequestBody String pJson){
		this.printJson(()->{
			Map<String, Object> map = JSON.parseObject(pJson, Map.class);
			cdm.add(map);
			return Boolean.TRUE;
		});
	}
	
	/**
	 * 更新缓存定义
	 * @param pJson
	 */
	@RequestMapping(value = "/update")
	public void updateCacheDef(@RequestBody String pJson){
		this.printJson(()->{
			Map<String, Object> map = JSON.parseObject(pJson, Map.class);
			cdm.update(map);
			return Boolean.TRUE;
		});
	}
	
	/**
	 * 禁用一个缓存缓存
	 * @param pJson
	 */
	@RequestMapping(value = "/disable")
	public void disable(@RequestBody String pJson){
		String selectId = JSON.parseObject(pJson).getString("selectId");
		this.printJson(()->{cacheManage.disableCache(selectId);return Boolean.TRUE;});
	}
	/**
	 * 启用一个缓存
	 * @param pJson
	 */
	@RequestMapping(value = "/enable")
	public void enable(@RequestBody String pJson){
		String selectId = JSON.parseObject(pJson).getString("selectId");
		this.printJson(()->{cacheManage.enableCache(selectId);return Boolean.TRUE;});
	}
	
	@RequestMapping(value = "/mht")
	public void printMht(){
		response.setContentType("message/rfc822");
		try {
			byte[] byts = FileUtils.readFileToByteArray(new File("C:\\Users\\kevin\\Desktop\\New Document.mht"));
			System.out.println(byts.length);
			response.getWriter().print(byts);
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
