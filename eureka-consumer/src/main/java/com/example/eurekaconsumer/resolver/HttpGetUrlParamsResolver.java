package com.example.eurekaconsumer.resolver;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpGetUrlParamsResolver {
    public static Object resolveUrlParam(HttpServletRequest httpServletRequest, Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Map<String, Object> params = new HashMap<>();
        for (String param : httpServletRequest.getQueryString().split("&")) {
            params.put(param.split("=")[0], param.split("=")[1]);
        }
        Object o = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        params.keySet().forEach(key -> {
            try {
                if (Arrays.stream(fields).anyMatch(field -> field.getName().equals(key))) {
                    Field field = clazz.getDeclaredField(key);
                    Method setter = clazz.getMethod("set" + key.substring(0, 1).toUpperCase() + key.substring(1),
                            field.getType());
                    String value = URLDecoder.decode((String) params.get(key), "UTF-8");
                    setter.invoke(o, value);
                } else {
                    makeExtendMap(clazz, o, key, params);
                }
            } catch (UnsupportedEncodingException | ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
        return o;
    }

    /**
     * 其他字段放到extendMap
     *
     * @param clazz
     * @param o
     * @param key
     * @param params
     * @throws ReflectiveOperationException
     */
    public static void makeExtendMap(Class<?> clazz, Object o, String key, Map<String, Object> params) throws ReflectiveOperationException {
        Method mapGetter = clazz.getMethod("getExtendMap");
        Method mapSetter = clazz.getMethod("setExtendMap", Map.class);
        Map<String, Object> to = (Map<String, Object>) mapGetter.invoke(o);
        if (null == to) {
            to = new HashMap<>();
        }
        to.put(key, params.get(key));
        mapSetter.invoke(o, to);
    }
}
