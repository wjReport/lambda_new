package com.pccw.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;

import com.pccw.service.CacheManage;
import com.pccw.service.db.DbFactory;


public class BaseService {
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	protected <R> R doInCallBack(String dbUser, Function<SqlSession, R> function){
		SqlSession sqlSession = DbFactory.open(dbUser);
		R ret = null;
		try {
			ret = function.apply(sqlSession);
		} finally{
			sqlSession.close();
		}
		return ret;
	}
	
	protected <R> R doInCallBackWithFormUser(Function<SqlSession, R> function){
		return this.doInCallBack(DbFactory.FORM, function);
	}
	
	/*
	 * 分页查询
	 */
	public void selectByPage(PageAssistant assistant, String sqlId) {
		this.doInCallBackWithFormUser(session -> {
			//查询list
			RowBounds bounds = new RowBounds(assistant.getOffsetRow(), assistant.getPageSize());
			List<Map<?,?>> list = session.selectList(sqlId, assistant.getParamMap(), bounds);
			assistant.setList(list);
			//查询总条数
			MappedStatement statement = session.getConfiguration().getMappedStatement(sqlId);
			BoundSql boundSql = statement.getBoundSql(assistant.getParamMap());
			DefaultParameterHandler parameterHandler = new DefaultParameterHandler(statement, assistant.getParamMap(), boundSql);
			Connection connection = null;
			PreparedStatement prepareStatement = null;
			ResultSet resultSet = null;
			try {
				SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
				SqlSessionFactory factory = builder.build(session.getConfiguration());
				connection = factory.openSession().getConnection();
				prepareStatement = connection.prepareStatement("select count(1) as row_count from (" + boundSql.getSql() + ") as d");
				parameterHandler.setParameters(prepareStatement);
				resultSet = prepareStatement.executeQuery();
				if (resultSet.next())
					assistant.setTotalItems(Integer.valueOf(resultSet.getInt("row_count")));
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if (resultSet != null) {
						resultSet.close();
					}
					if (prepareStatement != null) {
						prepareStatement.close();
					}
					if (connection != null)
						connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return Boolean.TRUE;
		});
	}
}
