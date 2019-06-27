package com.lc.activiti;

import com.lc.activiti.modeler.JsonpCallbackFilter;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * spring boot启动类.
 *
 * @author zyz.
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class })
//@EnableAsync
@MapperScan(basePackages = "com.lc.activiti.mapper")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public JsonpCallbackFilter filter(){
        return new JsonpCallbackFilter();
    }
}
