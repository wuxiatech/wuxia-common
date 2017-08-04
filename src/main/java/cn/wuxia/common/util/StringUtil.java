package cn.wuxia.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import cn.wuxia.common.util.DateUtil.DateFormatter;
import cn.wuxia.common.util.reflection.ReflectionUtil;

/**
 * <h3>String handling.</h3> <h4>Description</h4> <h4>Special Notes</h4>
 * 
 * @version 0.5
 * @author songlin.li 2009-11-3
 */
public class StringUtil extends StringUtils {

    private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * @return Random character
     * @author songlin.li
     * @see RandomStringGenerator
     */
    public static String random(int count) {
        //        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', 'z').build();
        //        return generator.generate(count);
        return RandomStringUtils.randomAlphanumeric(count);
    }

    /**
     * @description : replace string from s to e
     * @param s from char
     * @param e end char
     * @param content original content
     * @param value replace content
     * @return
     */
    public static String replaceInValue(String s, String e, String content, String value) {
        if (content == null)
            return null;
        int i = 0;
        int k = 0;
        if ((i = (content.indexOf(s, i))) >= 0 && (k = (content.indexOf(e, k))) >= 0) {
            StringBuffer sbf = new StringBuffer();
            int s_length = s.length();
            int e_length = e.length();
            sbf.append(content.substring(0, i)).append(value);
            int n = i + s_length;
            int m = k + e_length;
            for (; ((i = (content.indexOf(s, n))) >= 0 && (k = (content.indexOf(e, m))) >= 0);) {
                sbf.append(content.substring(m, i)).append(value);
                n = i + s_length;
                m = k + e_length;
            }
            sbf.append(content.substring(m, content.length()));
            return sbf.toString();
        }
        return content;
    }

    /**
     * @description : Interception string a top front the length of the
     *              character
     * @param length To capture length
     * @param content
     * @return
     */
    public static String cutString(int length, String content) {
        if (content.length() > length)
            return content.substring(0, length);
        return content;
    }

    /**
     * @description : Interception string a top front the length of the
     *              character, add "..." to the end
     *              com.loyoyo.common.utils.StringUtil.java
     * @param length
     * @param content
     * @return String
     * @author: bobo 2008-8-22 Afternoon 04:12:12
     */
    public static String cutSubString(int length, String content) {
        if (content.length() > length)
            return content.substring(0, length) + "...";
        return content;
    }

    /**
     * @description : decode
     * @param src
     * @return
     * @author songlin.li
     */
    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    /**
     * @description : Add alias for each arguments in the SQL string
     * @param sqlString
     * @return
     * @author Sagax.Luo
     * @throws Exception
     */
    public static String addAliasForSQLArgs(String sqlString, String[] aliases) throws Exception {
        String lowSqlString = sqlString.toLowerCase();

        if (lowSqlString.indexOf("select") == -1 || lowSqlString.indexOf("from") == -1)
            throw new Exception("Illegal SQL statement!");

        String subArgsString = lowSqlString.substring(6, lowSqlString.indexOf("from"));
        String[] args = subArgsString.split(",");

        if (args.length != aliases.length)
            throw new Exception("Arguments length the Aliases's length does not match!");

        StringBuffer argsWithAliases = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i] + " as " + aliases[i];

            argsWithAliases.append(args.length - 1 > i ? args[i] + ", " : args[i] + " ");
        }

        return sqlString.replaceFirst(subArgsString, argsWithAliases.toString());
    }

    /**
     * @description : string support el tags <code>${contents}</code>
     * @param paraMap
     * @param destStr
     * @return
     * @author songlin.li
     */
    public static String replaceKeysSimple(Object bean, String destStr) {
        if (bean == null) {
            return destStr;
        }

        for (String key : getTemplateKey(destStr)) {
            Object value = ReflectionUtil.invokeGetterMethod(bean, key);
            if (StringUtil.isBlank(value))
                value = "";
            key = "${" + key + "}";
            destStr = destStr.replace(key, value.toString());
        }

        return destStr;
    }

    /**
     * @description : string support el tags <code>${contents}</code>
     * @param paraMap
     * @param destStr
     * @return
     * @author songlin.li
     */
    public static String replaceKeysSimple(Map<String, Object> paraMap, String destStr) {
        if (paraMap == null) {
            return destStr;
        }
        Pattern p = Pattern.compile("[${]+\\w+[}]");
        Matcher m = p.matcher(destStr);

        while (m.find()) {
            String k = m.group();
            k = k.substring(2, k.length() - 1);
            for (String key : paraMap.keySet()) {
                if (StringUtil.equals(k, key)) {
                    String value = paraMap.get(key) == null ? "" : paraMap.get(key).toString();
                    key = "${" + key + "}";
                    destStr = destStr.replace(key, value);
                }
            }
        }
        return destStr;
    }

    /**
     * @description : string support el tags <code>${contents}</code>
     * @param destStr
     * @param key
     * @param repalcement
     * @return
     * @author songlin.li
     */
    public static String replaceKeysSimple(String destStr, String key, String repalcement) {

        Pattern p = Pattern.compile("[${]+\\w+[}]");
        Matcher m = p.matcher(destStr);

        while (m.find()) {
            String k = m.group();
            k = k.substring(2, k.length() - 1);
            if (StringUtil.equals(k, key)) {
                String value = repalcement == null ? "" : repalcement;
                key = "${" + key + "}";
                destStr = destStr.replace(key, value);
            }
        }
        return destStr;
    }

    /**
     * @description : support Object and String is null
     * @param target
     * @return
     */
    public static boolean isNotBlankPlus(Object target) {
        if (target == null)
            return false;

        String targetStr = (String) target;

        if (isBlank(targetStr))
            return false;

        return !targetStr.equals("null");
    }

    /**
     * @description : Escape function
     * @param rules
     * @param target
     * @author Jay Wu
     * @return
     */
    public static String transferRegex(String rules, String target) {
        if (StringUtil.isBlank(rules) || StringUtil.isBlank(target))
            return target;

        String[] objs = rules.split(",");
        for (int i = 0; i < objs.length; i++) {
            String rule = objs[i].trim();
            target = target.replace(rule, "\\" + rule);
        }

        return target;
    }

    /**
     * @description : count target appear times at strSource
     * @param strSource
     * @param target
     * @author songlin.li
     * @return
     */
    public static int countString(String strSource, String target) {
        int result = 0;

        if (isBlank(strSource))
            return result;

        while (strSource.indexOf(target) != -1) {
            strSource = replaceOnce(strSource, target, "");
            result++;
        }

        return result;
    }

    /**
     * @description : get the time target at strSource
     * @param strSource
     * @param target
     * @param times
     * @author songlin.li
     * @return
     */
    public static int indexOf(String strSource, String target, int times) {
        int result = -1;

        if (isBlank(strSource))
            return result;

        result = 0;
        int temp = 0;
        int count = 1;
        while ((temp = strSource.indexOf(target, temp) + 1) != -1) {
            if (times < count) {
                break;
            }

            if (result <= temp) {
                result = temp;
            } else {
                break;
            }

            count++;
        }

        return result - 1;
    }

    /**
     * @description : Returns the number of times times the target appeared in
     *              strSource
     * @param strSource
     * @param target
     * @param times
     * @return
     * @author songlin.li
     */
    public static int lastIndexOf(String strSource, String target, int times) {
        int result = -1;

        if (isBlank(strSource))
            return result;

        int temp = strSource.length() - 1;
        int count = 1;
        result = strSource.length();

        while ((temp = strSource.lastIndexOf(target, temp)) != -1) {
            if (times < count) {
                break;
            }

            if (result >= temp) {
                result = temp;
            } else {
                break;
            }

            temp--;

            count++;
        }

        if (result == strSource.length())
            return -1;

        return result;
    }

    /**
     * @description : validate String length, One Chinese char is 3 bit
     * @param strParameter To validate the string
     * @param limitLength Verification length
     * @return Conform to the length then return true,out of range the return
     *         false
     */
    public static boolean validateStrByLength(String strParameter, int limitLength) {
        int temp_int = 0;
        byte[] b = strParameter.getBytes();

        for (int i = 0; i < b.length; i++) {
            if (b[i] >= 0) {
                temp_int = temp_int + 1;
            } else {
                temp_int = temp_int + 3;
                i++;
            }
        }

        if (temp_int > limitLength)
            return false;
        else
            return true;
    }

    /**
     * @description : Determine whether case letter
     * @param s
     * @return
     */
    public static boolean isLetter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!((s.charAt(i) > 'a' && s.charAt(i) < 'z') || (s.charAt(i) > 'A' && s.charAt(i) < 'Z')))
                return false;
        }

        return true;
    }

    /**
     * @description : string to object
     * @param str
     * @param classObj
     * @return
     */
    public static Object string2Object(String str, Class<?> classObj) {
        if (isEmpty(str))
            return null;

        if (classObj.equals(Date.class))
            return DateUtil.stringToDate(str, DateFormatter.FORMAT_DD_MMM_YYYY_HH_MM_SS);

        return ClassLoaderUtil.newInstanceByConstructor(classObj, new Class<?>[] { String.class }, new Object[] { str });
    }

    public static boolean isBlank(Object value) {
        if (value == null)
            return true;

        if (value instanceof String) {
            if (StringUtil.isBlank((String) value))
                return true;
        }

        return false;
    }

    public static boolean isNotBlank(Object value) {
        return !isBlank(value);
    }

    /**
     * @description :replace Regex
     * @param regex
     * @param orgi
     * @param pos
     * @return
     */
    public static String replaceRegex(String regex, String orgi, int pos) {
        if (isBlank(orgi) || isBlank(regex))
            return orgi;

        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(orgi);

        while (m.find()) {
            String outStr = m.group(pos);
            orgi = orgi.replace(m.group(0), outStr);
        }

        return orgi;
    }

    public static String get(String regex, String orgi, int pos) {
        if (isBlank(orgi) || isBlank(regex))
            return null;

        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(orgi);

        while (m.find()) {
            String outStr = m.group(pos);
            return outStr;
        }

        return null;
    }

    /**
     * @description : String to Map
     * @param mapStr key1=val1,key2=val2,key3=val3
     * @return
     */
    public static Map<String, Object> String2Map(String mapStr) {
        Map<String, Object> map = new HashMap<String, Object>();
        mapStr = mapStr.substring(1, mapStr.length() - 1);
        String[] mapElements = mapStr.split(",");

        for (int i = 0; i < mapElements.length; i++) {
            String[] obj = mapElements[i].split("=");
            if (obj.length == 1) {
                continue;
            }

            String key = obj[0];
            String value = obj[1];
            map.put(key, value);
        }

        return map;
    }

    public static boolean in(String[] targets, String value, boolean ignoreCase) {
        if (targets == null)
            return false;

        for (String target : targets) {
            if (ignoreCase) {
                if (value.equalsIgnoreCase(target))
                    return true;
            } else {
                if (value.equals(target))
                    return true;
            }
        }

        return false;
    }

    /**
     * @description :compress
     * @param value
     * @return
     */
    public static String compress(String value) {
        if (isBlank(value))
            return value;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(value.getBytes());
            gzip.close();
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * @description : unCompress
     * @param value
     * @return
     */
    public static String unCompress(String value) {
        if (isBlank(value))
            return value;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(value.getBytes("ISO-8859-1"));
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }

            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * @description : if input String is null then return "";
     * @param input
     * @return
     * @author Solex.Li
     */
    public static String nullToEmpty(Object input) {
        String output;
        if (input == null)
            return "";
        try {
            output = input.toString();
        } catch (Exception e) {
            output = (String) input;
        }
        return output;
    }

    /**
     * String support like EL Tag <br>
     * <code>
     * Map<String, Object> map = new HashMap<String, Object>();<br><br>
     * 
     * Map<String, Object> test = new HashMap<String, Object>();<br>
     * test.put("a", "I'm a！");<br>
     * map.put("test", test);<br><br>
     *  
     *  Map<String, Object> test2 = new HashMap<String, Object>();<br>  
     *  test2.put("b", "I'm b！");   <br>
     *  map.put("test2", test2);<br>
     *  
     *  String str = "${map.test[a]}";<br>
     *  System.out.println(replaceKeys(map, str));<br><br>
     * 
     * String str2 = "${map.test[a]}，and more，${map.test2[b]}";<br>
     * System.out.println(replaceKeys(map, str2));<br>
     * 
      </code>
     * 
     * @param map
     * @param key
     * @return
     * @author songlin.li
     */
    @SuppressWarnings("unchecked")
    public static String replaceKeys(Map<String, Object> paraMap, String destStr) {
        if (paraMap == null)
            return destStr;

        String variableTypeMap = "map";

        for (String key : paraMap.keySet()) {
            String regex = "\\$\\{" + variableTypeMap + "\\." + key + "\\[(.+?)\\]\\}";
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(destStr);

            while (m.find()) {
                try {
                    String outStr = m.group(1);
                    Map<String, Object> targetMap = (Map<String, Object>) paraMap.get(key);
                    Object targetValue = targetMap.get(outStr);

                    if (targetValue != null) {
                        destStr = destStr.replace("${" + variableTypeMap + "." + key + "[" + outStr + "]}", targetValue.toString());
                    } else {
                        destStr = "";
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return destStr;
    }

    /**
     * @description : get key from str,like ${abc}
     * @author songlin.li
     * @param str
     * @return
     */
    public static String[] getTemplateKey(String str) {
        Pattern p = Pattern.compile("[${]+\\w+[}]");
        Matcher m = p.matcher(str);

        List<String> value = new ArrayList<String>();
        while (m.find()) {
            String key = m.group();
            key = key.substring(2, key.length() - 1);
            value.add(key);
        }
        if (ListUtil.isEmpty(value)) {
            return new String[] {};
        }
        value = ListUtil.removeDuplicateBySet(value);
        return ListUtil.listToArray(value);
    }

    /**
     * @description : trim all string blank
     * @author songlin.li
     * @param source
     * @return
     */
    public static String trimBlank(String source) {
        if (isBlank(source)) {
            return "";
        }
        return source.replaceAll(" ", "");
    }

    /**
     * @description : for test
     * @author songlin.li
     */
    public static void sql2code() {
        Scanner objScanner = new Scanner(System.in);
        System.out.println("enter StringBuffer parameter：");
        String strBuffer = objScanner.next();
        System.out.println("enter SQL and end with enter key：");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String s;
        int index = 0;
        try {
            while ((s = br.readLine()).length() != 0) {
                if (index == 0) {
                    System.out.println("\n\n sql what you want：\n");
                }
                System.out.println(strBuffer + ".append(\" " + s + " \");");
                index++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    /**
     * 把"1,2,3"转换成"'1','2','3'"
     * 
     * @param string
     * @return
     */
    public static String toStringWithSingleQuotes(String string, String septationString) {
        if (StringUtils.isBlank(string) || StringUtils.isBlank(septationString))
            throw new IllegalArgumentException("string or septationString illegal!");
        String[] strings = string.split(septationString);
        StringBuilder resultString = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i < strings.length - 1)
                resultString.append("'" + strings[i] + "',");
            else
                resultString.append("'" + strings[i] + "'");
        }
        return resultString.toString();

    }

    public static void main(String[] args) throws Exception {
        //        System.out.println(RandomStringUtils.randomAscii(6));
        //        System.out.println(RandomStringUtils.randomNumeric(6));
        //        for (int i = 0; i < 6; i++)
        //            System.out.println(RandomStringUtils.randomAlphanumeric(6));
        //        System.out.println(random(6));
        String[] a = getTemplateKey("aaaaafasdfsadf${Abc}afsdfasd sadfasdfasdf");
        Map m = Maps.newHashMap();

        m.put("accountName", "  yangjin ");
        System.out.println(replaceKeysSimple(m, "aaaaafasdfsadf${accountName}afsdfasd sadfasdfasdf"));
        String desc = "<p><a href=\"http://武侠科技\">点我啊</a><br/></p>";
        System.out.println(desc.replaceAll("href='\"(.+?)\"'", "").replaceAll("style='\"(.+?)\"'", "").replaceAll("class='\"(.+?)\"'", "")
                .replaceAll("href=\"(.+?)\"", "").replaceAll("style=\"(.+?)\"", "").replaceAll("class=\"\"(.+?)\"\"", ""));
        //        System.out.println(EncodeUtils.htmlEscape("廣 <州 寰 球 國 際 物 流 有 限 公 司"));
        //        System.out.println("fs中a1df a".matches("[a-zA-z0-9\u4E00-\u9FA5]*$"));
        //        System.out.println(org.apache.commons.lang3.StringEscapeUtils.escapeHtml4("廣 <州 寰 球 國 際 物 流 有 限 公 司"));

        System.out.println(StringUtil.indexOf("http://abc.a.c/ag/cafsa/f", "/", 3));
        System.out.println(StringUtil.substring("http://abc.a.c/ag/cafsa/f", 0, 14));
        String sql = "select * from group by aaa";
        int s = StringUtils.lastIndexOfIgnoreCase(sql, "group by");
        String i = " and abc= ? ";
        System.out.println(s + "   " + sql.length() + "  " + i.length());
        sql = StringUtil.insert(sql, i, s);
        System.out.println(sql + "   " + sql.length());

        System.out.println(Hex.encodeHexString(sql.getBytes()));
    }

    public static String parseValue(Object valueObj) {
        String value = null;

        if (valueObj != null) {
            if (valueObj instanceof Object[]) {
                value = StringUtils.join((Object[]) valueObj, "*&*");
            } else if (valueObj instanceof Iterable) {
                value = StringUtils.join((Iterable<?>) valueObj, "*&*");
            } else {
                value = ObjectUtils.toString(valueObj, null);
            }
        }
        return value;
    }

    /**
     * @author songlin
     * @param sorceStr 为原字符串
     * @param insertStr 为要插入的字符串
     * @param location 为插入位置
     * @return
     */
    public static String insert(String sorceStr, String insertStr, int location) {
        return StringUtils.substring(sorceStr, 0, location) + insertStr + StringUtils.substring(sorceStr, location, sorceStr.length());
    }

    /**
     * 首字母转小写
     * 
     * {@link org.apache.commons.lang3.StringUtils#uncapitalize(String str)}
     * @author songlin
     * @param s
     * @return
     */
    public static String toLowerCaseFirstChar(String s) {
        return uncapitalize(s);
        //        if (Character.isLowerCase(s.charAt(0)))
        //            return s;
        //        else
        //            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    /**
     * 首字母转大写
     * 
     * {@link org.apache.commons.lang3.StringUtils#capitalize(String str)}
     * @author songlin
     * @param s
     * @return
     */
    public static String toUpperCaseFirstChar(String s) {
        return capitalize(s);
        //        if (Character.isUpperCase(s.charAt(0)))
        //            return s;
        //        else
        //            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }
}
