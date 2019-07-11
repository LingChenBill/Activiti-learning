package com.lc.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * 自定义配置器测试。
 *
 * @author zyz.
 */
public class MyConfiguratorTest {

    @Test
    public void testMyConfigurator() {

        InputStream inputStream = MyConfiguratorTest.class.getClassLoader().getResourceAsStream("activiti.cfg.xml");
        ProcessEngineConfiguration pcf = ProcessEngineConfiguration.createProcessEngineConfigurationFromInputStream(inputStream);

        ProcessEngine processEngine = pcf.buildProcessEngine();
        Assert.assertNotNull("not null", processEngine);

    }

}
