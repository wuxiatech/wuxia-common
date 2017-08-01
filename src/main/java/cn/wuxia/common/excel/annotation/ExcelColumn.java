/*
* Created on :2017年1月23日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 wuxia.gd.cn All right reserved.
*/
package cn.wuxia.common.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {
    /**
     * excel表头列索引，从0开始
     * @author songlin
     * @return
     */
    int no();

    /**
     * excel表头列名，如为空则以no为主
     * @author songlin
     * @return
     */
    String name() default "";

    /**
     * 如果有值，则此值为默认值将替换excel的列值
     * @author songlin
     * @return
     */
    String value() default "";
}
