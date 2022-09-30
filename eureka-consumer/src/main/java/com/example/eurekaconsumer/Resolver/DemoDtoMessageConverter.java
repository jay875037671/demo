package com.example.eurekaconsumer.Resolver;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.*;

public class DemoDtoMessageConverter extends MappingJackson2HttpMessageConverter {
    private static final Map<String, JsonEncoding> ENCODINGS = CollectionUtils.newHashMap(JsonEncoding.values().length);
    @Nullable
    @Deprecated
    public static final Charset DEFAULT_CHARSET;

    @Nullable
    private Map<Class<?>, Map<MediaType, ObjectMapper>> objectMapperRegistrations;

    private Map<Class<?>, Map<MediaType, ObjectMapper>> getObjectMapperRegistrations() {
        return this.objectMapperRegistrations != null ? this.objectMapperRegistrations : Collections.emptyMap();
    }

    private Object readDemoDtoType(JavaType javaType, HttpInputMessage inputMessage) throws IOException {
        MediaType contentType = inputMessage.getHeaders().getContentType();
        Charset charset = this.getCharset(contentType);
        ObjectMapper objectMapper = this.selectObjectMapper(javaType.getRawClass(), contentType);
        Assert.state(objectMapper != null, "No ObjectMapper for " + javaType);
        boolean isUnicode = ENCODINGS.containsKey(charset.name()) || "UTF-16".equals(charset.name()) || "UTF-32".equals(charset.name());

        try {
            InputStream inputStream = StreamUtils.nonClosing(inputMessage.getBody());
            if (inputMessage instanceof MappingJacksonInputMessage) {
                Class<?> deserializationView = ((MappingJacksonInputMessage) inputMessage).getDeserializationView();
                if (deserializationView != null) {
                    ObjectReader objectReader = objectMapper.readerWithView(deserializationView).forType(javaType);
                    if (isUnicode) {
                        return objectReader.readValue(inputStream);
                    }

                    Reader reader = new InputStreamReader(inputStream, charset);
                    return objectReader.readValue(reader);
                }
            }

            if (isUnicode) {
                // 转换请求中的基本字段到demoDto
                Map<String, Object> map = objectMapper.readValue(inputStream, Map.class);
                Class clazz = javaType.getRawClass();
                Object o = clazz.newInstance();
                Field[] fields = clazz.getDeclaredFields();
                map.keySet().stream()
                        .forEach(key -> {
                            // 如果请求的字段在demoDto中
                            if (Arrays.asList(fields).stream().anyMatch(field -> field.getName().equals(key))) {
                                try {
                                    Field field = clazz.getDeclaredField(key);
                                    StringBuffer sb = new StringBuffer();
                                    sb.append("set").append(key.substring(0, 1).toUpperCase()).append(key.substring(1));
                                    Method setter = clazz.getMethod(sb.toString(), field.getType());
                                    setter.invoke(o, map.get(key));
                                } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException |
                                         IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                //其他字段放到extendMap中
                                try {
                                    Method mapGetter = clazz.getMethod("getExtendMap");
                                    Method mapSetter = clazz.getMethod("setExtendMap", Map.class);
                                    Map<String, Object> to = (Map<String, Object>) mapGetter.invoke(o);
                                    if(null == to){
                                        to = new HashMap<>();
                                    }
                                    to.put(key, map.get(key));
                                    mapSetter.invoke(o, to);
                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                return o;
            } else {
                Reader reader = new InputStreamReader(inputStream, charset);
                return objectMapper.readValue(reader, javaType);
            }
        } catch (InvalidDefinitionException var11) {
            throw new HttpMessageConversionException("Type definition error: " + var11.getType(), var11);
        } catch (JsonProcessingException var12) {
            throw new HttpMessageNotReadableException("JSON parse error: " + var12.getOriginalMessage(), var12, inputMessage);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private ObjectMapper selectObjectMapper(Class<?> targetType, @Nullable MediaType targetMediaType) {
        if (targetMediaType != null && !CollectionUtils.isEmpty(this.objectMapperRegistrations)) {
            Iterator var3 = this.getObjectMapperRegistrations().entrySet().iterator();

            Map.Entry typeEntry;
            do {
                if (!var3.hasNext()) {
                    return this.defaultObjectMapper;
                }

                typeEntry = (Map.Entry) var3.next();
            } while (!((Class) typeEntry.getKey()).isAssignableFrom(targetType));

            Iterator var5 = ((Map) typeEntry.getValue()).entrySet().iterator();

            Map.Entry objectMapperEntry;
            do {
                if (!var5.hasNext()) {
                    return null;
                }

                objectMapperEntry = (Map.Entry) var5.next();
            } while (!((MediaType) objectMapperEntry.getKey()).includes(targetMediaType));

            return (ObjectMapper) objectMapperEntry.getValue();
        } else {
            return this.defaultObjectMapper;
        }
    }

    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        JavaType javaType = super.getJavaType(type, contextClass);
        return this.readDemoDtoType(javaType, inputMessage);
    }

    static {
        JsonEncoding[] var0 = JsonEncoding.values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            JsonEncoding encoding = var0[var2];
            ENCODINGS.put(encoding.getJavaName(), encoding);
        }

        ENCODINGS.put("US-ASCII", JsonEncoding.UTF8);
        DEFAULT_CHARSET = null;
    }
}
