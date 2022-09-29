package com.example.eurekacommon.enums;

import lombok.Getter;

@Getter
public enum PrivacyTypeEnum {

    /**
     * 自定义（此项需设置脱敏的范围）
     */
    CUSTOMER,

    /**
     * 姓名
     */
    NAME,

    /**
     * 身份证号
     */
    ID_CARD,

    /**
     * 银行卡号
     */
    BANK_CARD,

    /**
     * 手机号
     */
    PHONE,
}