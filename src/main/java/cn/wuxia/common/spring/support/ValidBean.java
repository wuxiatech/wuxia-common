package cn.wuxia.common.spring.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于formBean的验证注解
 */
@Target({ ElementType.METHOD,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidBean {
}
