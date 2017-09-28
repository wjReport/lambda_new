package lambda;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.junit.Test;

import com.pccw.service.LambdaExpressionService;
import com.pccw.util.ObjectUtil;
import com.pccw.util.complier.JavaStringCompiler;

import common.BaseTest;

public class LambdaExpressionTest extends BaseTest{

	@Resource
	LambdaExpressionService lambdaService;

	@Test
	public void insert() {
		String javaStr = 
				"import java.util.Comparator;\n" +
				"import java.util.stream.Stream;\n" + 
				"public class Test {\n" + 
				"  public void test(){\n" + 
				"    int min = Stream.of(4,5,3,1).filter(x->x>2).min(Comparator.comparing(x -> x)).get();\n" + 
				"    System.out.println(min);\n" + 
				"  }\n" + 
				"}";

		Map<String, Object> parameter = new HashMap<String, Object>();
		JavaStringCompiler compiler = new JavaStringCompiler();
		Map<String, byte[]> results = null;
		try {
			results = compiler.compile("Test.java", javaStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		parameter.put("name", "查询账单");
		parameter.put("lambda_expression", javaStr);
		parameter.put("code_bytes", results);
		parameter.put("select_id", "gl.getBal");
		parameter.put("create_date", new Date());
		parameter.put("create_by", "zkw");
		parameter.put("is_active", "1");
		lambdaService.insert(parameter);
	}

	@Test
	public void findById() throws SQLException, IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		Map<String, Object> retMap = lambdaService.selectById("2");
		byte[] bytes = (byte[])retMap.get("code_bytes");
		Map<String, byte[]> results = (Map<String, byte[]>)ObjectUtil.ByteToObject(bytes);
		JavaStringCompiler compiler = new JavaStringCompiler();
		Class<?> clazz = compiler.loadClass("Test", results);
		Method  call = clazz.getMethod("test");
		call.invoke(clazz.newInstance());
	}
	
	@Test
	public void update(){
		Map<String, Object> retMap = lambdaService.selectById("1");
		retMap.put("lambdaId", "lambdaId");
		retMap.put("lambdaExpression", "lambdaExpression");
		retMap.put("codeBytes", "codeBytes");
		retMap.put("isActive", "0");
		lambdaService.update(retMap);
	}

	@Test
	public void selectAll(){
		Assert.assertEquals(lambdaService.select(null, null).size(),3);
	}

}
