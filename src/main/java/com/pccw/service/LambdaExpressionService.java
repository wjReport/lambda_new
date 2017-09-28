package com.pccw.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.pccw.common.BaseService;
import com.pccw.common.PageAssistant;

@Service
public class LambdaExpressionService extends BaseService{
	
	public void insert(Map<String, Object> parameter){
		this.doInCallBackWithFormUser(s->s.insert("lambda.insert", parameter));
	}
	
	public void update(Map<String, Object> parameter){
		this.doInCallBackWithFormUser(s->s.update("lambda.update", parameter));
	}
	
	/**
	 * 查询lambda表达式定义
	 * @param parameter
	 * @param rowBounds
	 * @return
	 */
	public List<Map<String, Object>> select(final Map<String, Object> parameter, final RowBounds rowBounds){
		return this.doInCallBackWithFormUser(session -> 
			session.selectList("lambda.select", parameter, rowBounds==null?new RowBounds():rowBounds));
	}
	
	/**
	 * 分页查询
	 * @param assistant
	 */
	public void selectByPage(PageAssistant assistant){
		super.selectByPage(assistant, "lambda.select");
	}
	
	/**
	 * 通过表达式ID查询表达式定义
	 * @param id
	 * @return
	 */
	public Map<String, Object> selectById(String id){
		Map<String, Object> parameter = new HashMap<String, Object>(1);
		parameter.put("id", id);
		List<Map<String, Object>> retList = this.select(parameter, null);
		if(retList != null) return retList.get(0);
		return null;
	}
	
	/**
	 * 通过表单是name查询表达式定义
	 * @param name
	 * @return
	 */
	public Map<String, Object> selectByName(String name){
		Map<String, Object> parameter = new HashMap<String, Object>(1);
		parameter.put("name", name);
		List<Map<String, Object>> retList = this.select(parameter, null);
		if(retList != null) return retList.get(0);
		return null;
	}
}
