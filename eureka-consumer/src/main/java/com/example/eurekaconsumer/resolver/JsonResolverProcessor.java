package com.example.eurekaconsumer.resolver;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

/**
 * 解析基础dto字段，其他字段放到extentdMap中
 */
public class JsonResolverProcessor extends RequestResponseBodyMethodProcessor {

    public JsonResolverProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    public JsonResolverProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager) {
        super(converters, manager);
    }

    public JsonResolverProcessor(List<HttpMessageConverter<?>> converters, List<Object> requestResponseBodyAdvice) {
        super(converters, requestResponseBodyAdvice);
    }

    public JsonResolverProcessor(List<HttpMessageConverter<?>> converters, ContentNegotiationManager manager, List<Object> requestResponseBodyAdvice) {
        super(converters, manager, requestResponseBodyAdvice);
    }
}
