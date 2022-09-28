package com.example.eurekaconsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Slf4j
public class DemoController {

    @Autowired
    RestTemplate restTemplate;

    @PostMapping("/hello")
    public String hello(@RequestParam("name") String name,
                        @RequestParam("age") Integer age) {
        return "name：" + name + "\nage：" + age;
    }

    @RequestMapping(value = "/rest")
    public String rest(@MultiArgumentResolver DemoDto dto, HttpServletRequest request) {
        return doPost(dto, request);
    }

    @RequestMapping(value = "/test")
    public String rest(@RequestParam(value = "file", required = false) String formStr,
                       @RequestBody(required = false) String jsonStr,
                       HttpServletRequest request) {
        return doPost(formStr, jsonStr, request);
    }

    @RequestMapping(value = "/dispatch", consumes = "application/json")
    public String dispatchJson(@RequestBody String str, HttpServletRequest request) {
        return doPost(str, request);
    }

    @RequestMapping(value = "/dispatch", consumes = "application/x-www-form-urlencoded")
    public String dispatchForm(@RequestParam(required = false) Map<String, Object> params, HttpServletRequest request) {
        return doPost(params, request);
    }

    @RequestMapping(value = "/dispatch", consumes = "multipart/form-data")
    public String dispatchFormData(@RequestParam(required = false) Map<String, Object> params, HttpServletRequest request) {
        return doPost(params, request);
    }

    private String doPost(String str, HttpServletRequest request) {
        String contentType = request.getHeader("content-type");
        log.info("入参值：{}", str);
        String req = String.format("Content-Type: %s\nparam: %s", contentType, str);
        String url = "http://EUREKA-PRODUCER/demo?param={req}";
        log.info(String.format("url:%s\n req: %s", url, req));
        return restTemplate.getForObject(url, String.class, req);
    }

    public String doPost(DemoDto dto, HttpServletRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        String str = "";
        try {
            str = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return doPost(str, request);
    }

    private String doPost(String formStr, String jsonStr, HttpServletRequest request) {
        String str = "";
        if (StringUtils.isNotBlank(jsonStr)) {
            str = jsonStr;
        } else if (StringUtils.isNotBlank(formStr)) {
            str = formStr;
        }
        return doPost(str, request);
    }

    private String doPost(Map<String, Object> params, HttpServletRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        String str = "";
        try {
            str = objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return doPost(str, request);
    }
}