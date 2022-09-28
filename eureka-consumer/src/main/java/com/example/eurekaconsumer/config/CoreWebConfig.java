package com.example.eurekaconsumer.config;

import com.example.eurekaconsumer.processer.MultiArgumentResolverMethodProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author likj
 * @version 1.0
 * @date 2022/09/28 18:57
 */
@Configuration
@Order
public class CoreWebConfig implements WebMvcConfigurer {

    /**
     * 注册自定义参数解析器
     * @return
     */
    @Bean
    public MultiArgumentResolverMethodProcessor multiArgumentResolverMethodProcessor() {
        return new MultiArgumentResolverMethodProcessor();
    }

    /**
     * 添加自定义参数解析器
     * @param resolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, multiArgumentResolverMethodProcessor());
    }
}

