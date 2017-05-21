package org.throwable.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 14:57
 */
public class JacksonUtilsTest {

	@Test
	public void testParseFile() throws Exception {
		List<AuthorEntity> authorEntitys = JacksonUtils.parseListFromJsonFile("classpath:mq.json", AuthorEntity.class);
		assertEquals(2, authorEntitys.size());
		System.out.println(authorEntitys);
	}


}