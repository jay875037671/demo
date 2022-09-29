package com.example.eurekaproducer.service;

import com.example.eurekacommon.annotation.DataProcess;
import com.example.eurekacommon.enums.DataHandle;
import com.example.eurekaproducer.vo.DemoVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProducerService {
    @DataProcess(sign = DataHandle.DECRYPT)
    public DemoVo convertVo(String str) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(str,DemoVo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
