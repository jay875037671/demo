package com.example.eurekaproducer.vo;

import com.example.eurekacommon.annotation.SensitiveField;
import com.example.eurekacommon.enums.EncryptWayEnum;
import com.example.eurekacommon.enums.PrivacyTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemoVo implements Serializable {
    private static final long serialVersionUID = -8762524609645000937L;

    @SensitiveField(hasSensitive = true,privacyEnum = PrivacyTypeEnum.NAME,enumType = EncryptWayEnum.SM4,key = "1234567891234567")
    private String name;

    private String age;

    @SensitiveField(hasSensitive = true,privacyEnum = PrivacyTypeEnum.ID_CARD)
    private String id;

    @SensitiveField(hasSensitive = true,privacyEnum = PrivacyTypeEnum.BANK_CARD)
    private String cardNo;

    @SensitiveField(hasSensitive = true,privacyEnum = PrivacyTypeEnum.PHONE)
    private String phone;

    @SensitiveField(hasSensitive = false,privacyEnum = PrivacyTypeEnum.NAME)
    private String abc;

    private Map<String,Object> extendMap;
}
