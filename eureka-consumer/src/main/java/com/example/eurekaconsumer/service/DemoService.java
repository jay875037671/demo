package com.example.eurekaconsumer.service;

import com.example.eurekacommon.annotation.DataProcess;
import com.example.eurekacommon.enums.DataHandle;
import com.example.eurekaconsumer.dto.DemoDto;
import com.example.eurekaconsumer.vo.DemoVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
@Slf4j
public class DemoService {
    @Autowired
    RestTemplate restTemplate;

    public String doPost(String str, HttpServletRequest request) {
        String contentType = request.getHeader("content-type");
        log.info("入参值:{}", str);
        String req = String.format("Content-Type: %s\nparam: %s", contentType, str);
        String url = "http://EUREKA-PRODUCER/demo?param={req}";
        log.info(String.format("url:%s\n req: %s", url, req));
        return restTemplate.getForObject(url, String.class, req);
    }

    public String doPost(DemoDto dto, HttpServletRequest request) {
        return post(dto, request);
    }

    public String doPost(Map<String, Object> params, HttpServletRequest request) {
        return post(params, request);
    }

    public String doPost(String formStr, String jsonStr, HttpServletRequest request) {
        String str = "";
        if (StringUtils.isNotBlank(jsonStr)) {
            str = jsonStr;
        } else if (StringUtils.isNotBlank(formStr)) {
            str = formStr;
        }
        return doPost(str, request);
    }

    private String post(Object o, HttpServletRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        String str;
        try {
            str = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return doPost(str, request);
    }
}
