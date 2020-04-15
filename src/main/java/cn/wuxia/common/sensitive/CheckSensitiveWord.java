/*
 * Copyright 2011-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.sensitive;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 检查敏感词
 * [ticket id]
 * Description of the class 
 * @author 金
 * @ Version : V<Ver.No> <2014-12-25>
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckSensitiveWord {
   String massage();
}
