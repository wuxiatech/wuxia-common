package cn.wuxia.common.util;

import java.util.Date;

import org.apache.commons.text.RandomStringGenerator;

public abstract class NoGenerateUtil {

    /**
     * 默认返回15位的数字
     * @author songlin
     * @param length
     * @return
     */
    public static String generateNo() {
        return generateNo("", 15);
    }

    /**
     *  默认返回15位的数字
     * @author songlin
     * @param length 长度的数字
     * @return
     */
    public static String generateNo(int length) {
        return generateNo("", length);
    }

    /**
     * 
     * @author songlin
     * @param prefix 前缀
     * @param length 长度的数字
     * @return
     */
    public static String generateNo(String prefix, int length) {
        StringBuilder orderNo = new StringBuilder();
        orderNo.append(prefix);
        if (length <= 6) {
            orderNo.append(generate(length));
        } else if (6 < length && length <= 10) {
            orderNo.append(DateUtil.dateToString(new Date(), "yyMMdd"));
            orderNo.append(generate(length - 6));
        } else if (10 < length && length <= 15) {
            orderNo.append(DateUtil.dateToString(new Date(), "yyMMddHHmm"));
            orderNo.append(generate(length - 10));
        } else if (15 < length) {
            orderNo.append(DateUtil.dateToString(new Date(), "yyMMddHHmmssSSS"));
            orderNo.append(generate(length - 15));
        }
        return orderNo.toString();
    }

    private static String generate(int length) {
        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', '9').build();
        return generator.generate(length);
    }

    public static void main(String[] args) {
        System.out.println(generateNo());
        System.out.println(generateNo(6));
        System.out.println(generateNo(10));
        System.out.println(generateNo(11));
        System.out.println(generateNo(15));
        System.out.println(generateNo(16));
        System.out.println(generateNo("UCM", 19));
        System.out.println(generateNo("CS", 20));
    }

}
