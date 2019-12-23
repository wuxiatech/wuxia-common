package cn.wuxia.common.sensitive;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ClassUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @version 1.0
 * @Description: 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 * @Project：test
 * @Author : chenming
 * @Date ： 2014年4月20日 下午2:27:06
 */
public class SensitiveWordInit {
    public static final String path = ClassUtils.getDefaultClassLoader().getResource("").getPath() + "words/";

    private String ENCODING = "UTF-8"; //字符编码

    @SuppressWarnings("rawtypes")
    public ConcurrentMap sensitiveWordMap;

    public SensitiveWordInit() {
        super();
    }

    /**
     * @author chenming
     * @date 2014年4月20日 下午2:28:32
     * @version 1.0
     */
    @SuppressWarnings("rawtypes")
    public Map initKeyWord() {
        try {
            //读取敏感词库
            Set<String> keyWordSet = readSensitiveWordFile();
            //将敏感词库加入到HashMap中
            addSensitiveWordToHashMap(keyWordSet);
            //spring获取application，然后application.setAttribute("sensitiveWordMap",sensitiveWordMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensitiveWordMap;
    }

    /**
     * 初始化传入set
     *
     * @param keyWordSet
     * @return
     * @author 金
     */
    @SuppressWarnings("rawtypes")
    public Map initKeyWord(Set<String> keyWordSet) {
        try {
            //将敏感词库加入到HashMap中
            addSensitiveWordToHashMap(keyWordSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensitiveWordMap;
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
     * 中 = {
     * isEnd = 0
     * 国 = {<br>
     * isEnd = 1
     * 人 = {isEnd = 0
     * 民 = {isEnd = 1}
     * }
     * 男  = {
     * isEnd = 0
     * 人 = {
     * isEnd = 1
     * }
     * }
     * }
     * }
     * 五 = {
     * isEnd = 0
     * 星 = {
     * isEnd = 0
     * 红 = {
     * isEnd = 0
     * 旗 = {
     * isEnd = 1
     * }
     * }
     * }
     * }
     *
     * @param keyWordSet 敏感词库
     * @author chenming
     * @date 2014年4月20日 下午3:04:20
     * @version 1.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
        sensitiveWordMap = Maps.newConcurrentMap(); //初始化敏感词容器，减少扩容操作
        String key = null;
        Map nowMap = null;
        Map<String, String> newWorMap = null;
        //迭代keyWordSet
        Iterator<String> iterator = keyWordSet.iterator();
        while (iterator.hasNext()) {
            key = iterator.next(); //关键字
            nowMap = sensitiveWordMap;
            for (int i = 0; i < key.length(); i++) {
                char keyChar = key.charAt(i); //转换成char型
                Object wordMap = nowMap.get(keyChar); //获取

                if (wordMap != null) { //如果存在该key，直接赋值
                    nowMap = (Map) wordMap;
                } else { //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new HashMap<String, String>();
                    newWorMap.put("isEnd", "0"); //不是最后一个
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }

                if (i == key.length() - 1) {
                    nowMap.put("isEnd", "1"); //最后一个
                }
            }
        }
    }

    /**
     * 读取敏感词库中的内容，将内容添加到set集合中
     *
     * @return
     * @throws Exception
     * @author chenming
     * @date 2014年4月20日 下午2:31:18
     * @version 1.0
     */
    private Set<String> readSensitiveWordFile() throws Exception {
        Set<String> set = Sets.newHashSet();
        String wordPath = path;
        File file = new File(wordPath); //读取文件,或目录
        if (file.isDirectory()) {
            Collection<File> files = FileUtils.listFiles(file, null, false);
            for (File f : files) {
                set.addAll(readSensitiveWordFile(f));
            }
        } else {
            set.addAll(readSensitiveWordFile(file));
        }
        return set;
    }

    private Set<String> readSensitiveWordFile(File file) {
        if (file.isFile() && file.exists()) { //文件流是否存在

        } else { //不存在抛出异常信息
            throw new RuntimeException("敏感词库文件不存在");
        }
        Set<String> set = Sets.newHashSet();
        try (InputStreamReader read = new InputStreamReader(new FileInputStream(file), ENCODING);) {
            BufferedReader bufferedReader = new BufferedReader(read);
            String txt = null;
            while ((txt = bufferedReader.readLine()) != null) { //读取文件，将文件内容放入到set中
                set.add(txt);
            }
            bufferedReader.close();
        } catch (IOException e) {

        }
        return set;
    }
}