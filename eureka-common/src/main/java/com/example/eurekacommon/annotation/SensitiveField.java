package com.example.eurekacommon.annotation;

import com.example.eurekacommon.enums.EncryptWayEnum;
import com.example.eurekacommon.enums.PrivacyTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description: EncryptField2 <br>
 * date: 2022/9/3 15:33 <br>
 * author: 10412 <br>
 * version: 1.0 <br>
 *
 * @author 10412
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveField {

    /**
     * 加密方式 默认AES
     */
    EncryptWayEnum enumType() default EncryptWayEnum.SM4;

    /**
     * 加密秘钥默认 1234567890ABCDEF 秘钥默认16个字符
     */
    String key() default "1234567890ABCDEF";

    /**
     * 是否需要脱敏处理
     */
    boolean hasSensitive() default false;

    /**
     * 脱敏数据类型（默认自定义）
     */
    PrivacyTypeEnum privacyEnum() default PrivacyTypeEnum.NAME;

    /**
     * 前置不需要打码的长度
     */
    int prefixNoMaskLen() default 1;

    /**
     * 后置不需要打码的长度
     */
    int suffixNoMaskLen() default 1;

    /**
     * 用什么打码
     */
    String symbol() default "*";
}
