package com.example.eurekaconsumer.Resolver;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpGetUrlParamsResolver {
    public static Object resolveUrlParam(HttpServletRequest httpServletRequest, Class<?> clazz) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        for (String param : httpServletRequest.getQueryString().split("&")) {
            params.put(param.split("=")[0], param.split("=")[1]);
        }
        Object o = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        params.keySet().stream()
                .filter(key -> Arrays.asList(fields).stream().anyMatch(field -> field.getName().equals(key)))
                .forEach(key -> {
                    try {
                        Field field = clazz.getDeclaredField(key);
                        StringBuffer sb = new StringBuffer();
                        sb.append("set").append(key.substring(0, 1).toUpperCase()).append(key.substring(1));
                        Method setter = clazz.getMethod(sb.toString(), field.getType());
                        String value = URLDecoder.decode(params.get(key),"UTF-8");
                        setter.invoke(o, value);
                    } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException |
                             IllegalAccessException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });
        return o;
    }
}
