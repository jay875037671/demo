package com.example.eurekaconsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

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

    public String doPost(String str, HttpServletRequest request) {
        String contentType = request.getHeader("content-type");
        log.info("入参值：{}", str);
        String req = String.format("Content-Type: %s\nparam: %s", contentType, str);
        String url = "http://EUREKA-PRODUCER/demo?param={req}";
        log.info(String.format("url:%s\n req: %s", url, req));
        return restTemplate.getForObject(url, String.class, req);
    }

    @RequestMapping("/rest")
    public String rest(@MultiArgumentResolver String str, HttpServletRequest request) {
        return doPost(str, request);
    }
}