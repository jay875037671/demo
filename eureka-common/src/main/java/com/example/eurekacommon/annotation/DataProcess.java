package com.example.eurekacommon.annotation;

import com.example.eurekacommon.enums.DataHandle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description: 方法层面声明加解密参数,数据加密解密注解 <br>
 * date: 2022/9/3 14:31 <br>
 * author: 10412 <br>
 * version: 1.0 <br>
 *
 * @author 10412
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataProcess {

    /**
     * 处理数据方式 加密,解密,通过,默认
     */
    DataHandle sign() default DataHandle.PASS_ADN_SENSITIVE;

}