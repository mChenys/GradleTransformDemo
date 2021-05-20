package com.mchenys.gradletransform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

/**
 * @Author: mChenys
 * @Date: 2021/5/20
 * @Description: 用于标记哪些类需要被插桩
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface Inject {
}
