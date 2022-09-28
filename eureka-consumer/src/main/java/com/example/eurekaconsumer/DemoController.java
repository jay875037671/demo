package com.example.eurekaconsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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

    public String doPost(String str) {
        String req = "param: " + str;
        String url = "http://EUREKA-PRODUCER/demo?param={req}";
        log.info(String.format("url:%s req: %s", url, req));
        return restTemplate.getForObject(url, String.class, req);
    }

    @RequestMapping("rest")
    public String rest(@MultiArgumentResolver String str) {
        return doPost(str);
    }

}