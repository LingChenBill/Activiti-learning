package com.lc.activiti;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * spring boot启动类.
 *
 * @author zyz.
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class })
//@EnableAsync
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

//    @Bean
//    public JsonpCallbackFilter filter(){
//        return new JsonpCallbackFilter();
//    }
}
