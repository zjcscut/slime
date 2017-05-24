package org.throwable.context;

import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version 2017/1/14 17:27
 * @function bean注册构件
 */
public class BeanDefinitionComponent {

	private String beanName;
	private String className;
	private Class<?> beanClass;
	private Map<String, Object> beanPropertyValues;
	private Map<String, String> beanPropertyReferences;
	private List<String> dependsOn;
	private List<Object> constructorArgValues;
	private List<String> constructorArgReferences;
	private String initMethod;
	private String destoryMethod;

	public BeanDefinitionComponent() {
	}

	public BeanDefinitionComponent(String className) {
		this.className = className;
	}

	public BeanDefinitionComponent(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public BeanDefinitionComponent(String beanName, String className) {
		this.beanName = beanName;
		this.className = className;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Map<String, Object> getBeanPropertyValues() {
		return beanPropertyValues;
	}

	public void setBeanPropertyValues(Map<String, Object> beanPropertyValues) {
		this.beanPropertyValues = beanPropertyValues;
	}

	public Map<String, String> getBeanPropertyReferences() {
		return beanPropertyReferences;
	}

	public void setBeanPropertyReferences(Map<String, String> beanPropertyReferences) {
		this.beanPropertyReferences = beanPropertyReferences;
	}

	public List<Object> getConstructorArgValues() {
		return constructorArgValues;
	}

	public void setConstructorArgValues(List<Object> constructorArgValues) {
		this.constructorArgValues = constructorArgValues;
	}

	public List<String> getConstructorArgReferences() {
		return constructorArgReferences;
	}

	public void setConstructorArgReferences(List<String> constructorArgReferences) {
		this.constructorArgReferences = constructorArgReferences;
	}

	public String getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	public String getDestoryMethod() {
		return destoryMethod;
	}

	public void setDestoryMethod(String destoryMethod) {
		this.destoryMethod = destoryMethod;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}

	public List<String> getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(List<String> dependsOn) {
		this.dependsOn = dependsOn;
	}
}
