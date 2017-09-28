package com.pccw.service;

import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.pccw.common.BaseService;
import com.pccw.common.PageAssistant;


/**
 * 缓存定义管理
 * @author kevin
 *
 */
@Service("cacheDefinition")
public class CacheDefinitionManage extends BaseService{
	
	public void selectByPage(PageAssistant assistant){
		super.selectByPage(assistant, "cacheDefinition.select");
	}
	
	public List<Map<String, Object>> select(final Map<String, Object> parameter, final RowBounds rowBounds){
		return this.doInCallBackWithFormUser(session -> session.selectList("cacheDefinition.select", parameter, rowBounds));
	}
	
	/**
	 * 根据selectId查询缓存定义
	 * @param selectId
	 * @return
	 */
	public Map<String, Object> findBySelectId(String selectId){
		final Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("select_id", selectId);
		List<Map<String, Object>> list = this.select(parameter, new RowBounds());
		if(list != null) return list.get(0);
		return null;
	}
	
	/**
	 * 查询所定义的缓存
	 * @return
	 */
	public List<Map<String, Object>> selectAll(Map<String, Object> parameter){
		return this.select(parameter, new RowBounds());
	}

	
	public void activeToggle(String selectId, int status) {
		final Map<String, Object> parameter = new HashMap<String, Object>(2);
		parameter.put("select_id", selectId);
		parameter.put("is_active", status);
		this.doInCallBackWithFormUser(s->s.update("cacheDefinition.update", parameter));
	}
	
	/**
	 * 将一条记录标记为删除
	 * @param selectId
	 */
	public void disable(String selectId){
		this.activeToggle(selectId, 0);
	}
	
	/**
	 * 将一条记录标记为启用
	 * @param selectId
	 */
	public void endable(String selectId){
		this.activeToggle(selectId, 1);
	}
	
	public void add(Map<String, Object> cacheDef){
		this.doInCallBackWithFormUser(sqlSession->{
			return sqlSession.insert("cacheDefinition.insert", cacheDef);
		});
	}
	
	public void update(Map<String, Object> cacheDef){
		this.doInCallBackWithFormUser(sqlSession->{
			return sqlSession.update("cacheDefinition.update", cacheDef);
		});
	}
}
