package com.example.eurekaconsumer.processer;

import com.example.eurekaconsumer.annotation.MultiArgumentResolver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义参数解析器用以支持同一个参数支持application/json和application/x-www-form-urlencoded解析
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/08/31 19:00
 * @see MultiArgumentResolver
 */
@Slf4j
@AllArgsConstructor
public class MultiArgumentResolverMethodProcessor implements HandlerMethodArgumentResolver {

    private RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor;

    private ServletModelAttributeMethodProcessor servletModelAttributeMethodProcessor;

    public MultiArgumentResolverMethodProcessor() {
        requestResponseBodyMethodProcessor = new RequestResponseBodyMethodProcessor(initHttpMessageConverters());
        servletModelAttributeMethodProcessor = new ServletModelAttributeMethodProcessor(true);
    }

    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String CONTENT_TYPE_FORM_DATA = "multipart/form-data";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameter().isAnnotationPresent(MultiArgumentResolver.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest httpServletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        String contentType = httpServletRequest.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("不支持contentType");
        }

        if (contentType.contains(CONTENT_TYPE_JSON)) {
            return requestResponseBodyMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        } else if (contentType.contains(CONTENT_TYPE_FORM_URLENCODED)) {
            return servletModelAttributeMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        } else if (contentType.contains(CONTENT_TYPE_FORM_DATA)) {
            return servletModelAttributeMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        }
        throw new IllegalArgumentException("不支持contentType");
    }

    public List<HttpMessageConverter<?>> initHttpMessageConverters() {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new ResourceRegionHttpMessageConverter());
        messageConverters.add(new SourceHttpMessageConverter<>());
        messageConverters.add(new AllEncompassingFormHttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
        return messageConverters;
    }
}

