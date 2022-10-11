package com.example.eurekaconsumer.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class FormResolverProcessor extends ServletModelAttributeMethodProcessor {
    public FormResolverProcessor(boolean annotationNotRequired) {
        super(annotationNotRequired);
    }

    @Nullable
    public final Object resolveDtoArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        Assert.state(mavContainer != null, "ModelAttributeMethodProcessor requires ModelAndViewContainer");
        Assert.state(binderFactory != null, "ModelAttributeMethodProcessor requires WebDataBinderFactory");
        String name = ModelFactory.getNameForParameter(parameter);
        ModelAttribute ann = (ModelAttribute) parameter.getParameterAnnotation(ModelAttribute.class);
        if (ann != null) {
            mavContainer.setBinding(name, ann.binding());
        }

        Object attribute = null;
        BindingResult bindingResult = null;
        if (mavContainer.containsAttribute(name)) {
            attribute = mavContainer.getModel().get(name);
        } else {
            try {
                attribute = this.createAttribute(name, parameter, binderFactory, webRequest);
            } catch (BindException var10) {
                if (this.isBindExceptionRequired(parameter)) {
                    throw var10;
                }

                if (parameter.getParameterType() == Optional.class) {
                    attribute = Optional.empty();
                } else {
                    attribute = var10.getTarget();
                }

                bindingResult = var10.getBindingResult();
            }
        }

        if (bindingResult == null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, attribute, name);
            if (binder.getTarget() != null) {
                if (!mavContainer.isBindingDisabled(name)) {
                    this.bindRequestParameters(binder, webRequest);
                }

                this.validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors() && this.isBindExceptionRequired(binder, parameter)) {
                    throw new BindException(binder.getBindingResult());
                }
            }

            if (!parameter.getParameterType().isInstance(attribute)) {
                attribute = binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
            }

            bindingResult = binder.getBindingResult();

            /**
             * 基本字段放入demoDto，其他字段放到extendMap
             */
            log.info("转换form请求基本字段开始!");
            Map parameterMap = webRequest.getParameterMap();
            Class<?> clazz = parameter.getParameterType();
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> parameterMap.remove(field.getName()));
            Method mapGetter = clazz.getMethod("getExtendMap");
            Method mapSetter = clazz.getMethod("setExtendMap", Map.class);

            Map to = (Map) mapGetter.invoke(attribute);
            if (null == to) {
                to = new HashMap<>();
            }
            for (Object o : parameterMap.keySet()) {
                String key = (String) o;
                to.put(key, parameterMap.get(key));
            }
            mapSetter.invoke(attribute, to);
            log.info("转换form请求基本字段完成!");
        }

        Map<String, Object> bindingResultModel = bindingResult.getModel();
        mavContainer.removeAttributes(bindingResultModel);
        mavContainer.addAllAttributes(bindingResultModel);
        return attribute;
    }

}
