package org.throwable.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.throwable.Application;

import static org.junit.Assert.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/22 17:24
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class EnvironmentUtilsTest {

    @Autowired
    private Environment environment;

    @Test
    public void parseEnvironmentPropertiesToBean() throws Exception {
        AuthorEntity author = EnvironmentUtils.parseEnvironmentPropertiesToBean(environment,AuthorEntity.class,"slime.author-entity");
        assertNotNull(author);
    }

}