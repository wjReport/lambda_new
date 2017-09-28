package com.pccw.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.pccw.common.BaseController;
import com.pccw.common.PageAssistant;
import com.pccw.service.CacheManage;
import com.pccw.service.LambdaExpressionService;
import com.pccw.util.ObjectUtil;
import com.pccw.util.complier.JavaStringCompiler;

@RequestMapping("/calculate")
@RestController
public class LambdaInvoke extends BaseController {
	
	@Resource
	CacheManage cacheManage;
	@Resource
	LambdaExpressionService lambdaService;
	
	final String className = "DynamicInvoke";
	
	final String javaStr = 
			"import java.util.*;\n" + 	
			"import java.util.stream.*;\n" + 
			"import java.math.BigDecimal;\n" + 
			"import java.util.function.*;\n" +		
			"public class "+className+" {\n" + 
			"\n" + 
			"  public Object call(Stream<Map<String, Object>> stream){\n" + 
			"    Object result = stream.parallel().#lambdaExpression#;\n" + 
			"    return result;\n" + 
			"  }\n" + 
			"}";
	
	/**
	 * 执行lambda查询
	 * @param name
	 */
	@RequestMapping(value = "/exec/{name}", produces = "text/plain;charset=UTF-8")
	public void execute(@PathVariable("name") String name) {
		Map<String, Object> map = lambdaService.selectByName(name);
		this.printJson(() -> this.executeLambada(map));
	}
	
	private Object executeLambada(Map<String, Object> map) throws Exception {
		byte[] bytes = (byte[])map.get("code_bytes");
		Map<String, byte[]> codeBytes = (Map<String, byte[]>)ObjectUtil.ByteToObject(bytes);
		JavaStringCompiler compiler = new JavaStringCompiler();
		Class<?> clazz = compiler.loadClass(className, codeBytes);
		Method  m = clazz.getMethod("call", Stream.class);
		List<Map<String, Object>> listMap = cacheManage.getListMapCacheValueByKey((String)map.get("select_id"));
		return m.invoke(clazz.newInstance(), listMap.stream());
	}
	
	/**
	 * 添加缓存定义
	 * @param pJson
	 */
	@RequestMapping(value = "/insert", produces = "text/plain;charset=UTF-8")
	public void insert(@RequestBody String pJson){
		this.printJson(()->this.insertLambdaDef(pJson));
	}
	
	private Object insertLambdaDef(String pJson) throws IOException {
		Map<String, Object> lambdaDef = JSONObject.parseObject(pJson, Map.class);
		JavaStringCompiler compiler = new JavaStringCompiler();
		Map<String, byte[]> codeBytes = generateCodeBytes(lambdaDef, compiler);
		lambdaDef.put("code_bytes", codeBytes);
		lambdaDef.put("is_active", "1");
		lambdaDef.put("create_date", new Date());
		lambdaService.insert(lambdaDef);
		return null;
	}

	//生成代码字节码
	private Map<String, byte[]> generateCodeBytes(Map<String, Object> lambdaDef, JavaStringCompiler compiler) throws IOException {
		String str = new String(javaStr);
		String lambdaExpression = (String)lambdaDef.get("lambda_expression");
		str = str.replaceAll("#lambdaExpression#", lambdaExpression);
		Map<String, byte[]> codeBytes = compiler.compile(className+".java", str);
		return codeBytes;
	}
	
	/**
	 * 更新lambda表达式定义
	 * @param pJson
	 */
	@RequestMapping(value = "/update", produces = "text/plain;charset=UTF-8")
	public void updateLambdaDef(@RequestBody String pJson){
		this.printJson(()->{
			Map<String, Object> vo = JSONObject.parseObject(pJson, Map.class);
			String id = (String)vo.get("id");
			Map<String, Object> po = lambdaService.selectById(id);
			BeanUtils.copyProperties(vo, po);
			Map<String, byte[]> codeBytes = generateCodeBytes(vo, new JavaStringCompiler());
			po.put("code_bytes", codeBytes);
			lambdaService.update(po);
			return null;
		});
	}
	
	/**
	 * 校验lambda表达式是否正确
	 * @param pJson
	 */
	@RequestMapping(value = "/validate", produces = "text/plain;charset=UTF-8")
	public void validateLambdaDef(@RequestBody String pJson){
		this.printJson(()->{
			Map<String, Object> lambdaDef = JSONObject.parseObject(pJson, Map.class);
			//TODO校验表达式名称是否有重复
			JavaStringCompiler compiler = new JavaStringCompiler();
			Map<String, byte[]> codeBytes = generateCodeBytes(lambdaDef, compiler);
			Class<?> clazz = compiler.loadClass(className, codeBytes);
			Method  m = clazz.getMethod("call", Stream.class);
			List<Map<String, Object>> listMap = cacheManage.getListMapCacheValueByKey((String)lambdaDef.get("select_id"));
			return m.invoke(clazz.newInstance(), listMap.stream());
		});
	}
	
	/**
	 * 获取lambda表达式定义分页列表
	 * @param pJson
	 */
	@RequestMapping(value = "/findByPage")
	public void findByPage(@RequestBody String pJson) {
		this.printJson(()->{
			PageAssistant assistant = new PageAssistant(pJson);
			lambdaService.selectByPage(assistant);
			return assistant;
		});
	}
}
