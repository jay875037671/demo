package com.example.eurekaproducer.Controller;

import com.example.eurekaproducer.service.ProducerService;
import com.example.eurekaproducer.vo.DemoVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProducerController {
    @Autowired
    ProducerService producerService;

    @RequestMapping("/rest/deal")
    public String doRest(String param) throws JsonProcessingException {
        DemoVo vo = producerService.convertVo(param);
        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(vo);
        log.info("解密后的dto:{}", str);
        return str;
    }

    @RequestMapping("/dispatch/deal")
    public String doDispatch(String param) {
        return param;
    }

    @RequestMapping("")
    public String doTest(String param) {
        return param;
    }
}
