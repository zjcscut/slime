package org.throwable.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.throwable.exception.JsonParseException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author zjc
 * @version 2016/11/20 12:19
 * @description 提供jackson转换功能
 */
public final class JacksonUtils {

	private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);

	private static final String DEFAULT_CHARTSET = "UTF-8";

	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		//设置输出时包含属性的风格
		mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
		//设置日期输出格式
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		//禁用未知属性打断序列化
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		//禁止使用int代表Enum的order()來反序列化Enum
		mapper.disable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS);
		//不输出null或者空字符串
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	/**
	 * 如果对象为Null, 返回"null".
	 * 如果集合为空集合, 返回"[]".
	 */
	public static String toJson(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			logger.error("process object value to json string failed.");
			throw new JsonParseException(e);
		}
	}

	/**
	 * 如果JSON字符串为Null或"null"字符串, 返回Null.
	 * 如果JSON字符串为"[]", 返回空集合.
	 * <p>
	 * 如需读取集合如List/Map, 且不是List<String>这种简单类型时,先使用函數constructParametricType构造类型.
	 *
	 * @see com.fasterxml.jackson.databind.type.TypeFactory
	 */
	public static <T> T parse(String jsonString, Class<T> clazz) {
		if (StringUtil.isBlank(jsonString)) {
			return null;
		}
		try {
			return mapper.readValue(jsonString, clazz);
		} catch (IOException e) {
			logger.error(String.format("process json string to class <%s> fail", clazz.getTypeName()));
			throw new JsonParseException(e);
		}
	}

	/**
	 * 如果JSON字符串为Null或"null"字符串, 返回Null.
	 * 需读取集合如List/Map, 且不是List<String>这种简单类型时,先使用函數constructParametricType构造类型.
	 */
	public static <T> List<T> parseList(String jsonString, Class<T> clazz) {
		return parse(jsonString, List.class, clazz);
	}

	/**
	 * 構造泛型的Type如List<MyBean>, 则调用constructParametricType(ArrayList.class,MyBean.class)
	 * Map<String,MyBean>则调用(HashMap.class,String.class, MyBean.class)
	 */
	public static JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
		return mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parse(String jsonString, JavaType javaType) {
		if (StringUtil.isBlank(jsonString)) {
			return null;
		}
		try {
			return (T) mapper.readValue(jsonString, javaType);
		} catch (IOException e) {
			throw new JsonParseException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T parse(String jsonString, Class<?> parametrized, Class<?>... parameterClasses) {
		return (T) parse(jsonString, constructParametricType(parametrized, parameterClasses));
	}


	public static JsonNode parseNode(String json) {
		try {
			return mapper.readValue(json, JsonNode.class);
		} catch (IOException e) {
			throw new JsonParseException(e);
		}
	}

	/**
	 * 解析json文件到javaBean
	 *
	 * @param localtion 文件路径
	 * @param clazz     目标对象class
	 * @return T
	 * @throws JsonParseException e
	 */
	public static <T> T parseFromJsonFile(String localtion, Class<T> clazz) {
		try {
			Resource resource = ResourceUtils.getResourcesByLocation(localtion);
			String value = StreamUtils.copyToString(resource.getInputStream(), Charset.forName(DEFAULT_CHARTSET));
			return parse(value, clazz);
		} catch (Exception e) {
			throw new JsonParseException(e);
		}
	}

	/**
	 * 解析json文件到List javaBean
	 *
	 * @param localtion 文件路径
	 * @param clazz     目标对象class
	 * @return List
	 * @throws JsonParseException e
	 */
	public static <T> List<T> parseListFromJsonFile(String localtion, Class<T> clazz) {
		try {
			Resource resource = ResourceUtils.getResourcesByLocation(localtion);
			String value = StreamUtils.copyToString(resource.getInputStream(), Charset.forName(DEFAULT_CHARTSET));
			return parseList(value, clazz);
		} catch (Exception e) {
			throw new JsonParseException(e);
		}
	}

}
