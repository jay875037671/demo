package com.example.eurekaconsumer.controller;

import com.example.eurekacommon.annotation.DataProcess;
import com.example.eurekacommon.annotation.MultiArgumentResolver;
import com.example.eurekacommon.enums.DataHandle;
import com.example.eurekaconsumer.api.DemoApi;
import com.example.eurekaconsumer.dto.DemoDto;
import com.example.eurekaconsumer.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Slf4j
public class DemoController implements DemoApi {

    @Autowired
    DemoService demoService;

    @PostMapping("/hello")
    public String hello(@RequestParam("name") String name,
                        @RequestParam("age") Integer age) {
        return "name：" + name + "\nage：" + age;
    }

    @RequestMapping(value = "/rest")
    @DataProcess(sign = DataHandle.ENCRYPT)
    public String rest(@MultiArgumentResolver DemoDto dto, HttpServletRequest request) {
        return demoService.doPost(dto, request);
    }

    @RequestMapping(value = "/test")
    public String rest(@RequestParam(value = "file", required = false) String formStr,
                       @RequestBody(required = false) String jsonStr,
                       HttpServletRequest request) {
        return demoService.doPost(formStr, jsonStr, request);
    }

    @RequestMapping(value = "/dispatch-alt", consumes = "application/json")
    public String dispatchJson(@RequestBody String str, HttpServletRequest request) {
        return demoService.doPost(str, request);
    }

    @RequestMapping(value = "/dispatch", consumes = "application/json")
    public String dispatchJson(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        return demoService.doPost(params, request);
    }

    @RequestMapping(value = "/dispatch", consumes = "application/x-www-form-urlencoded")
    public String dispatchForm(@RequestParam(required = false) Map<String, Object> params, HttpServletRequest request) {
        return demoService.doPost(params, request);
    }

    @RequestMapping(value = "/dispatch", consumes = "multipart/form-data")
    public String dispatchFormData(@RequestParam(required = false) Map<String, Object> params, HttpServletRequest request) {
        return demoService.doPost(params, request);
    }
}