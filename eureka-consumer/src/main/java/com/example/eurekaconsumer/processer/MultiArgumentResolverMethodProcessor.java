package com.example.eurekaconsumer.processer;

import com.example.eurekacommon.annotation.MultiArgumentResolver;
import com.example.eurekaconsumer.resolver.DemoDtoMessageConverter;
import com.example.eurekaconsumer.resolver.HttpGetUrlParamsResolver;
import com.example.eurekaconsumer.resolver.JsonResolverProcessor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义参数解析器用以支持同一个参数支持application/json和application/x-www-form-urlencoded解析
 *
 * @author likj
 * @version 1.0
 * @date 2022/09/28 19:00
 * @see MultiArgumentResolver
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class MultiArgumentResolverMethodProcessor implements HandlerMethodArgumentResolver, ApplicationContextAware, SmartInitializingSingleton {
    private ApplicationContext applicationContext;
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
    //    private RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor;
    private ServletModelAttributeMethodProcessor servletModelAttributeMethodProcessor;
    private JsonResolverProcessor jsonResolverProcessor;

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
        assert httpServletRequest != null;
        String contentType = httpServletRequest.getContentType();
        if (null == contentType) {
            try {
                return HttpGetUrlParamsResolver.resolveUrlParam(httpServletRequest, methodParameter.getParameter().getType());
            } catch (Exception e) {
                throw new IllegalArgumentException("不支持contentType");
            }
        }
        init();
        if (contentType.contains(CONTENT_TYPE_JSON)) {
            return jsonResolverProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        } else if (contentType.contains(CONTENT_TYPE_FORM_URLENCODED)) {
            return servletModelAttributeMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        } else if (contentType.contains(CONTENT_TYPE_FORM_DATA)) {
            return servletModelAttributeMethodProcessor.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        }
        throw new IllegalArgumentException("不支持contentType");
    }

    private void init() {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();
        assert argumentResolvers != null;
//        argumentResolvers.stream()
//                .filter(argumentResolver -> argumentResolver instanceof RequestResponseBodyMethodProcessor)
//                .forEach(argumentResolver -> requestResponseBodyMethodProcessor = (RequestResponseBodyMethodProcessor) argumentResolver);
        // TODO form表单处理待重写 基本字段放入demoDto，其他字段放到extendMap
        argumentResolvers.stream()
                .filter(argumentResolver -> argumentResolver instanceof ServletModelAttributeMethodProcessor)
                .forEach(argumentResolver -> servletModelAttributeMethodProcessor = (ServletModelAttributeMethodProcessor) argumentResolver);
        jsonResolverProcessor = new JsonResolverProcessor(Collections.singletonList(new DemoDtoMessageConverter()),
                new ContentNegotiationManager(),
                new ArrayList());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.requestMappingHandlerAdapter = applicationContext.getBean(RequestMappingHandlerAdapter.class);
    }
}

