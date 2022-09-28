package com.example.eurekaconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义参数解析器用以支持同一个参数支持application/json和application/x-www-form-urlencoded解析
 *
 * @see MultiArgumentResolver
 * @author Snowball
 * @version 1.0
 * @date 2020/08/31 19:00
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
        ObjectMapper objectMapper = new ObjectMapper();

        if (contentType.contains(CONTENT_TYPE_JSON)) {
            Object o = requestResponseBodyMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
            Map<String,String> map = StringUtils.isNotBlank((String) o) ? objectMapper.readValue(o.toString(), Map.class) : new HashMap<>();
            map.put("type","json");
            return objectMapper.writeValueAsString(map);
        }

        if (contentType.contains(CONTENT_TYPE_FORM_URLENCODED)) {
            Object o =  servletModelAttributeMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
            Map<String,String> map = StringUtils.isNotBlank((String) o) ? objectMapper.readValue(o.toString(), Map.class) : new HashMap<>();
            map.put("type","form");
            return objectMapper.writeValueAsString(map);
        }

        if (contentType.contains(CONTENT_TYPE_FORM_DATA)) {
            Object o =  servletModelAttributeMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
            Map<String,String> map = StringUtils.isNotBlank((String) o) ? objectMapper.readValue(o.toString(), Map.class) : new HashMap<>();
            map.put("type","form-data");
            return objectMapper.writeValueAsString(map);
        }
        throw new IllegalArgumentException("不支持contentType");
    }

    public List<HttpMessageConverter<?>> initHttpMessageConverters(){
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

