package com.example.eurekaproducer.vo;

import com.example.eurekacommon.annotation.SensitiveField;
import com.example.eurekacommon.enums.EncryptWayEnum;
import com.example.eurekacommon.enums.PrivacyTypeEnum;
import lombok.Data;

@Data
public class DemoVo {
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
}
