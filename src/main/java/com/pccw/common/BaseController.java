package com.pccw.common;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class BaseController {

	private static final String MESSAGE = "message";
	private static final String SUCCESS = "success";
	private static final String RESULT = "result";
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected HttpSession session;

	@ModelAttribute
	public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.session = request.getSession();
	}

	protected void printJson(InnerFunction<?> inner) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		JSONObject retJson = new JSONObject();
		PrintWriter out = null;
		try {
			out = response.getWriter();
			Object result = inner.execute();
			retJson.put(SUCCESS, Boolean.TRUE);
			retJson.put(RESULT, result);
		} catch (Exception e) {
			e.printStackTrace();
			retJson.put(SUCCESS, Boolean.FALSE);
			retJson.put(MESSAGE, e.getMessage());
		} finally {
			if (null != out) {
				out.print(JSON.toJSONString(retJson, SerializerFeature.WriteDateUseDateFormat));
				out.flush();
				out.close();
			}
		}
	}
}
