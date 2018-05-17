package cn.wuxia.common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.commons.lang3.math.NumberUtils;

public class NumberUtil extends NumberUtils {

    /**
     * @param num1
     * @param num2
     * @return num1*num2
     */
    public static Number multiply(Number num1, Number num2) {
        BigDecimal val1 = new BigDecimal(num1.doubleValue());
        BigDecimal val2 = new BigDecimal(num2.doubleValue());
        return val1.multiply(val2);
    }

    /**
     * @param num1
     * @param num2 value by which this BigDecimal is to be divided.
     * @param scale scale scale of the BigDecimal quotient to be returned.
     * @return num1/ num2
     */
    public static Number divide(Number num1, Number num2, int scale) {
        return divide(num1, num2, BigDecimal.ROUND_HALF_UP, scale);
    }

    /**
     * @param num1
     * @param num2 value by which this BigDecimal is to be divided.
     * @param roundType rounding mode to apply.
     * @param scale scale of the BigDecimal quotient to be returned.
     * @return num1/ num2
     */
    public static Number divide(Number num1, Number num2, int roundingMode, int scale) {
        if (num1.intValue() == 0 || num2.intValue() == 0) {
            return new BigDecimal(0);
        } else if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }

        BigDecimal val1 = new BigDecimal(String.valueOf(num1));
        BigDecimal val2 = new BigDecimal(String.valueOf(num2));
        BigDecimal val3 = null;

        if (roundingMode != 0) {
            val3 = val1.divide(val2, scale, roundingMode);
        }

        return val3;
    }

    /**
     * @description : For equality
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Number a, Number b) {
        if (a == null && b == null)
            return true;

        if (a != null && b != null) {
            int result = Double.compare(a.doubleValue(), b.doubleValue());
            if (result == 0)
                return true;
        }
        return false;
    }

    /**
     * -1 为 a < b, 0 为a == b, 1 为 a > b
     * 
     * @param a
     * @param b
     * @return
     */
    public static int compare(Number a, Number b) {
        double c = a == null ? 0 : a.doubleValue();
        double d = b == null ? 0 : b.doubleValue();
        return Double.compare(c, d);
    }

    /**
     * @description : 数字格式的对象转换为Integer
     * @author songlin.li
     * @param obj
     * @return
     */
    public static Integer toInteger(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).intValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return NumberUtils.toInt(obj.toString());
        }
    }

    /**
     * @description : 数字格式的对象转换为Integer
     * @author songlin.li
     * @param obj
     * @return
     */
    public static Integer toInteger(Object obj, Integer defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).intValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return NumberUtils.toInt(obj.toString());
        }
    }

    /**
     * @description : 数字格式的对象转换为Long
     * @author songlin.li
     * @param obj
     * @return
     */
    public static Long toLong(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).longValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else {
            return toLong(obj.toString(), 0);
        }
    }

    public static Long toLong(Object obj, Long defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else {
            return toLong(obj);
        }
    }

    /**
     * @description : 数字格式的对象转换为Double
     * @author songlin.li
     * @param obj
     * @return
     */
    public static Double toDouble(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).doubleValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return NumberUtils.toDouble(obj.toString());
        }
    }

    public static Double toDouble(Object obj, Double defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).doubleValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return NumberUtils.toDouble(obj.toString());
        }
    }

    /**
     * @description : 数字格式的对象转换为Float
     * @author songlin.li
     * @param obj
     * @return
     */
    public static Float toFloat(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).floatValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        } else {
            return NumberUtils.toFloat(obj.toString());
        }
    }

    public static Float toFloat(Object obj, Float defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj).floatValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        } else {
            return NumberUtils.toFloat(obj.toString());
        }
    }

    public static String formatFinancing(Number n) {
        DecimalFormat df = new DecimalFormat("###,###.##");
        String financingNum = df.format(n);
        if (StringUtil.indexOf(financingNum, ".") < 0) {
            return financingNum + ".00";
        }
        return financingNum;
    }

    public static void main(String[] args) {
        System.out.println(divide(11111, 0.9, 5, 2));
        int a = (int) (11111 / 0.9);
        System.out.println(formatFinancing(10110.123123));
        System.out.println(formatFinancing(new BigDecimal(0.10)));
        System.out.println(formatFinancing(8.010));


        System.out.println(801/200);

        int x = 100;
        int y = 200;
        if(x%y != 0)
            System.out.println(x/y+1);
        else
            System.out.println(x/y);
    }
}
