package com.pccw.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 80845530
 *
 */
public class SpringUtil implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SpringUtil.applicationContext = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext(){
		return applicationContext;
	}
	/**
	 * 获取spring核心容器中的bean,如果没有，会尝试采用参数name去注册bean
	 * @param name
	 * @return
	 */
	public static Object getBean(String name){
		Object obj = null;
		try {
			obj = applicationContext.getBean(name);
		} catch (BeansException e) {
			obj = registerMyBean(name);
		}
		return obj;
	}
	
	private synchronized static Object registerMyBean(String name){
		ConfigurableApplicationContext cac = (ConfigurableApplicationContext) applicationContext;
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) cac.getBeanFactory();
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(name);
		beanFactory.registerBeanDefinition(name, beanDefinitionBuilder.getRawBeanDefinition());
		return beanFactory.getBean(name);
	}
	
}
