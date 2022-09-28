package com.example.eurekaconsumer;

import java.lang.annotation.*;

/**
 * <p>标识参数可以被多个参数解析器尝试进行参数解析</p >
 *
 * 同一个参数支持application/json和application/x-www-form-urlencoded
 *
 * @see com.example.eurekaconsumer.MultiArgumentResolverMethodProcessor
 * @author Snowball
 * @version 1.0
 * @date 2020/08/31 18:57
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiArgumentResolver {
}
