package org.throwable.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/18 16:04
 */
public final class YamlParseUtils {

	private static final YAMLFactory yamlFactory = new YAMLFactory();
	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public static <T> T parse(String path, Class<T> clazz) {
		try {
			InputStream inputStream = new ClassPathResource(path).getInputStream();
			YAMLParser yamlParser = yamlFactory.createParser(inputStream);
			final JsonNode node = mapper.readTree(yamlParser);
			TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
			return mapper.readValues(treeTraversingParser, clazz).next();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
