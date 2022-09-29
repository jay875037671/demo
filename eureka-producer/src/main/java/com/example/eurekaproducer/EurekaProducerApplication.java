package com.example.eurekaproducer;

import com.example.eurekacommon.annotation.DataProcess;
import com.example.eurekacommon.enums.DataHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableEurekaClient
@RestController
@Slf4j
public class EurekaProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaProducerApplication.class, args);
    }

    @RequestMapping("demo")
    @DataProcess(sign = DataHandle.ENCRYPT)
    public String doReq(String param){
        log.info("解密后的dto:{}", param);
        return param;
    }

}
