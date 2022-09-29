package com.example.eurekaconsumer.service;

import com.example.eurekaconsumer.dto.DemoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ConsumerService {
    @Autowired
    RestTemplate restTemplate;

    static Map<String, String> routeMap = new HashMap<>();

    static {
        routeMap.put("rest", "/rest");
        routeMap.put("dispatch", "/dispatch");
        routeMap.put("", "");
    }

    public String doPost(String str, HttpServletRequest request, String type) {
        String contentType = request.getHeader("content-type");
        log.info("入参值:{}", str);
        String url = "http://EUREKA-PRODUCER/" + routeMap.get(type) + "/deal?param={req}";
        log.info(String.format("content-type:%s", contentType));
        log.info(String.format("url:%s", url));
        log.info(String.format("req: %s", str));
        return restTemplate.getForObject(url, String.class, str);
    }

    public String doPost(DemoDto dto, HttpServletRequest request, String type) {
        return convertPost(dto, request, type);
    }

    public String doPost(Map<String, Object> params, HttpServletRequest request, String type) {
        return convertPost(params, request, type);
    }

    public String doPost(String formStr, String jsonStr, HttpServletRequest request, String type) {
        String str = "";
        if (StringUtils.isNotBlank(jsonStr)) {
            str = jsonStr;
        } else if (StringUtils.isNotBlank(formStr)) {
            str = formStr;
        }
        return doPost(str, request, type);
    }

    private String convertPost(Object o, HttpServletRequest request, String type) {
        ObjectMapper objectMapper = new ObjectMapper();
        String str;
        try {
            str = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return doPost(str, request, type);
    }
}
