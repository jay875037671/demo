package com.example.eurekaconsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

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

    public String doPost(Map<String, Object> params, HttpServletRequest request) {
        String contentType = request.getHeader("content-type");
        ObjectMapper objectMapper = new ObjectMapper();
        String str = null;
        try {
            str = objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("入参值：{}", str);
        String req = String.format("Content-Type: %s\nparam: %s", contentType, str);
        String url = "http://EUREKA-PRODUCER/demo?param={req}";
        log.info(String.format("url:%s\n req: %s", url, req));
        return restTemplate.getForObject(url, String.class, req);
    }

    @RequestMapping(value = "/rest",consumes = "application/json")
    public String restJson(@MultiArgumentResolver Map<String, Object> params, HttpServletRequest request) {
        return doPost(params, request);
    }

    @RequestMapping(value = "/rest",consumes = "application/x-www-form-urlencoded")
    public String restForm(Map<String, Object> params, HttpServletRequest request) {
        return doPost(params, request);
    }

    @RequestMapping(value = "/rest",consumes = "multipart/form-data")
    public String restFormData(Map<String, Object> params, HttpServletRequest request) {
        return doPost(params, request);
    }
}