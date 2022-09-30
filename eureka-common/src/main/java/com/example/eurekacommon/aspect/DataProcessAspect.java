package com.example.eurekacommon.aspect;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.SM4;
import com.example.eurekacommon.annotation.DataProcess;
import com.example.eurekacommon.annotation.SensitiveField;
import com.example.eurekacommon.enums.DataHandle;
import com.example.eurekacommon.enums.EncryptWayEnum;
import com.example.eurekacommon.enums.PrivacyTypeEnum;
import com.example.eurekacommon.util.PrivacyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 数据加密解密,脱敏操作
 *
 * @author xiaYZ 2022/9/3
 * @version: 1.0
 */
@Slf4j
@Aspect
@Component
public class DataProcessAspect {

    //声明注解所处的位置
    @Pointcut("@annotation(com.example.eurekacommon.annotation.DataProcess)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 通过反射获得原始方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取方法注解DateProcess中参数
        DataProcess dateProcess = signature.getMethod().getAnnotation(DataProcess.class);
        log.info("操作类型为:{}", dateProcess.sign());
        if (dateProcess.sign() == DataHandle.ENCRYPT) {
            //加密方法参数
            encrypt(joinPoint);
        } else if (dateProcess.sign() == DataHandle.DECRYPT) {
            //判断是否为解密操作,进行解密,方法返回解密之后的数据
            return decrypt(joinPoint);
        } else if (dateProcess.sign() == DataHandle.PASS_ADN_SENSITIVE) {
            return desensitization(joinPoint);
        }
        //其他情况直接返回原数据
        return joinPoint.proceed();
    }

    public void encrypt(ProceedingJoinPoint joinPoint) {
        Object[] params = null;
        try {
            //获取方法的参数列表,方法参数有多个数据arg1,arg2等
            params = joinPoint.getArgs();
            Object target = joinPoint.getTarget();
            System.out.println(target);
            if (params.length != 0) {

                for (int i = 0; i < params.length; i++) {
                    //判断此方法参数是否为List数组类型
                    if (params[i] instanceof List) {
                        //加密对象为数组时,构建数组对象
                        List<Object> result = new ArrayList<>((List<?>) params[i]);
                        for (Object object : result) {
                            //数组对象循环加密
                            encryptObject(object);
                        }
                    } else {
                        //对象类加密
                        encryptObject(params[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加密对象
     *
     * @param obj
     * @throws IllegalAccessException
     */
    private void encryptObject(Object obj) throws Exception {

        if (Objects.isNull(obj)) {
            log.info("当前需要加密的object为null");
            return;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            //类的每个字段判断是否需要加密
            SensitiveField encryptField = field.getAnnotation(SensitiveField.class);
            if (Objects.nonNull(encryptField)) {
                //获取访问权
                field.setAccessible(true);
                String encryptCode = null == field.get(obj) ? null : String.valueOf(field.get(obj));
                if (StringUtils.isBlank(encryptCode)) {
                    return;
                }
                //加密秘钥
                String key = encryptField.key();
                if (encryptField.enumType() == EncryptWayEnum.SM4) {
                    SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, key.getBytes(StandardCharsets.UTF_8));
                    // SM4加密
                    encryptCode = sm4.encryptHex(encryptCode);
                } else if (encryptField.enumType() == EncryptWayEnum.AES) {
                    AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
                    // AES加密
                    encryptCode = aes.encryptHex(encryptCode);
                } else if (encryptField.enumType() == EncryptWayEnum.DES) {
                    DES des = SecureUtil.des(key.getBytes(StandardCharsets.UTF_8));
                    // DES加密
                    encryptCode = des.encryptHex(encryptCode);
                }
                field.set(obj, String.format("ENCRYPT-%s-%s", encryptField.enumType(), encryptCode));
            }
        }
    }


    /***
     * description:解密操作
     * @author xiaYZ
     * create time:
     * @param joinPoint 解密的节点
     */
    public Object decrypt(ProceedingJoinPoint joinPoint) {
        Object returnObject = null;
        try {
            //获取方法返回的对象
            returnObject = joinPoint.proceed();
            if (returnObject != null) {
                //解密对象
                if (returnObject instanceof ArrayList) {
                    //构建对象数组,并解密,针对list<实体来> 进行反射、解密
                    List<Object> resultList = new ArrayList<>((List<?>) returnObject);
                    for (Object object : resultList) {
                        decryptObj(object);
                    }
                } else {
                    decryptObj(returnObject);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return returnObject;
    }


    /**
     * 针对单个实体类进行 解密
     *
     * @param obj
     * @throws IllegalAccessException
     */
    private void decryptObj(Object obj) throws IOException, IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            //类的每个字段判断是否需要加密
            SensitiveField encryptField = field.getAnnotation(SensitiveField.class);
            if (Objects.nonNull(encryptField)) {
                //获取访问权
                field.setAccessible(true);
                String decryptCode = String.valueOf(field.get(obj)).split("-")[2];
                //加密秘钥
                String key = encryptField.key();
                if (encryptField.enumType() == EncryptWayEnum.SM4) {
                    SM4 sm4 = new SM4(Mode.ECB, Padding.PKCS5Padding, key.getBytes(StandardCharsets.UTF_8));
                    // SM4解密
                    decryptCode = sm4.decryptStr(decryptCode);
                } else if (encryptField.enumType() == EncryptWayEnum.AES) {
                    // 构建
                    AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
                    // AES解密
                    decryptCode = aes.decryptStr(decryptCode);
                } else if (encryptField.enumType() == EncryptWayEnum.DES) {
                    DES des = SecureUtil.des(key.getBytes(StandardCharsets.UTF_8));
                    // DES解密
                    decryptCode = des.decryptStr(decryptCode);
                }
                //判断解密是否需要脱敏处理
                if (encryptField.hasSensitive()) {
                    decryptCode = desensitizationWay(decryptCode, encryptField);
                }
                field.set(obj, decryptCode);
            }
        }
    }


    /***
     * description 方法返回数据直接脱敏
     * version 1.0
     * @date 2022/9/10 14:42
     * @author xiaYZ
     * @param joinPoint
     * @return
     */
    public Object desensitization(ProceedingJoinPoint joinPoint) {
        Object returnObject = null;
        try {
            //获取方法返回的对象
            returnObject = joinPoint.proceed();
            if (returnObject != null) {
                //判断脱敏的数据是否为List数组
                if (returnObject instanceof ArrayList) {
                    //构建对象数组,数组数据脱敏
                    List<Object> resultList = new ArrayList<>((List<?>) returnObject);
                    for (Object object : resultList) {
                        desensitizationObject(object);
                    }
                } else {
                    //对象类数据脱敏
                    desensitizationObject(returnObject);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return returnObject;
    }

    /***
     * description 数据对象脱敏
     * version 1.0
     * @date 2022/9/10 14:43
     * @author xiaYZ
     * @param obj
     * @return
     */
    public void desensitizationObject(Object obj) throws IOException, IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            //类的每个字段判断是否需要加密
            SensitiveField encryptField = field.getAnnotation(SensitiveField.class);
            if (Objects.nonNull(encryptField)) {
                //获取访问权
                field.setAccessible(true);
                String encode = String.valueOf(field.get(obj));
                String desensitizeCode = desensitizationWay(encode, encryptField);
                field.set(obj, desensitizeCode);
            }
        }
    }


    /***
     * description:脱敏操作
     * @author xiaYZ
     * create time:
     * @param code 脱敏前字符串
     * @param encryptField 脱敏规则
     * @return java.lang.String
     */
    public String desensitizationWay(String code, SensitiveField encryptField) throws IOException {
        if (encryptField.privacyEnum() == PrivacyTypeEnum.CUSTOMER) {
            return PrivacyUtil.desValue(code, encryptField.prefixNoMaskLen(), encryptField.suffixNoMaskLen(), encryptField.symbol());
        } else if (encryptField.privacyEnum() == PrivacyTypeEnum.NAME) {
            return PrivacyUtil.hideChineseName(code);
        } else if (encryptField.privacyEnum() == PrivacyTypeEnum.ID_CARD) {
            return PrivacyUtil.hideIdCard(code);
        } else if (encryptField.privacyEnum() == PrivacyTypeEnum.BANK_CARD) {
            return PrivacyUtil.hideBankCard(code);
        } else if (encryptField.privacyEnum() == PrivacyTypeEnum.PHONE) {
            return PrivacyUtil.hidePhone(code);
        } else {
            return code;
        }
    }


}