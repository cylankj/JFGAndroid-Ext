package com.cylan.ext.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 *  @项目名：  APT 
 *  @包名：    com.annotation
 *  @文件名:   DPProperty
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/13 22:16
 *  @描述：    TODO
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DPProperty {
    int msgId() default 0;
}
