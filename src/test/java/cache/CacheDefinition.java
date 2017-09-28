package cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.SchedulerException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pccw.common.AppCache;
import com.pccw.common.PageAssistant;
import com.pccw.service.CacheDefinitionManage;
import com.pccw.service.CacheManage;
import com.pccw.util.ErpUtil;

import common.BaseTest;
import net.sf.ehcache.Element;
public class CacheDefinition extends BaseTest{
	@Resource
	CacheDefinitionManage cdm ;
	
	@Resource
	CacheManage cacheManage;
	@Test
	public void getAll(){
		Map<String, Object> p = new HashMap<>();
		p.put("isActive", 1);
		List<Map<String, Object>> list = cdm.selectAll(p);
		String json = JSONObject.toJSON(list).toString();
		System.out.println(json);
	}
	
	@Test
	public void disable(){
		cacheManage.disableCache("gl.getPeriod3");
	}
	@Test
	public void getByPage(){
		String queryJson = "{currentPage:2,pageSize:3,queryObj:{selectId:'gl.getPeriod1'}}";
		PageAssistant assistant = new PageAssistant(queryJson);
		cdm.selectByPage(assistant, "cacheDefinition.select");
		System.out.println(JSON.toJSON(assistant));
	}
	
	@Test
	public void findBySelectId(){
		Assert.assertNotNull(cdm.findBySelectId("gl.getSetofbooks"));
	}
	
	@Test
	public void loadAllCache(){
		cacheManage.loadAllCache();
		Element el = AppCache.getCache().get("gl.getPeriod");
		System.out.println(el);
	}
	
	@Test
	public void decode(){
		try {
			String s = new ErpUtil().decode("j42N1qJTUoLGQPRVyCNkVA==");
			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void bigDecimal(){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<10;i++){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("PERIOD_YEAR", new BigDecimal("200"+i));
			list.add(map);
		}
		list.stream().filter(x->((BigDecimal) x.get("PERIOD_YEAR")).compareTo(new BigDecimal("2003"))==1).count();
	}
	
	@Test
	public void loadAndScheduleAllCache(){
		try {
			cacheManage.loadAndScheduleAllCache();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void lambdaEx(){
		List<Map<String, Object>> list = cacheManage.getListMapCacheValueByKey("gl.getBal");
		System.out.println(list.size());
		long count = list.stream().filter(m->"2510".equals(m.get("SEGMENT1"))).count();
		System.out.println(count);
	}
}
