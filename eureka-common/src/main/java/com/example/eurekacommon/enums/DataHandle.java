package com.example.eurekacommon.enums;

/**
 * 判断是否加密,解密或者不处理数据
 *
 * @author xiaYZ  2022/9/3
 * @version: 1.0
 */
public enum DataHandle {
    /**
     * 加密数据
     */
    ENCRYPT,

    /**
     * 解密数据
     */
    DECRYPT,

    /**
     * 数据不做加解密处理直接通过,然后进行脱敏处理
     */
    PASS_ADN_SENSITIVE,
}