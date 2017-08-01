package cn.wuxia.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.text.DecimalFormat;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h3>Class name</h3> 文件操作类 <h4>Description</h4> <h4>Special Notes</h4>
 * 
 * @ver 0.1
 * @author songlin.li 2008-11-21
 */
public class FileUtil extends FileUtils {
    protected static transient final Logger log = LoggerFactory.getLogger(FileUtil.class);

    private BufferedWriter out;

    private BufferedReader in;

    private InputStream ins;

    private OutputStream outs;

    private String path;

    private File file;

    private String fileName;

    private String charset = "UTF-8";

    public FileUtil(File file) {
        this.file = file;
        this.path = file.getAbsolutePath();
        this.fileName = file.getName();
    }

    public FileUtil(String path, String fileName) {
        path = path.endsWith(File.separator) ? path : path + File.separator;
        this.path = path;
        this.fileName = fileName;
        this.file = new File(path + fileName);
    }

    public void createFile() throws Exception {
        file = new File(path);

        if (!file.exists()) {
            file.mkdirs();
        }
        path = path.endsWith(File.separator) ? path : path + File.separator;
        file = new File(path + fileName);

        if (file.exists()) {
            file.delete();
        }

        file.createNewFile();
    }

    /**
     * 读取流内容
     * 
     * @param inputStream
     * @return
     * @throws Exception
     * @author songlin.li
     */
    public String read(InputStream inputStream) {
        try {
            in = new BufferedReader(new InputStreamReader(inputStream, charset));
            StringBuffer result = new StringBuffer("");
            String str;

            while ((str = in.readLine()) != null) {
                result.append(str + "\n");
            }

            return result.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "";
    }

    public void append(String content) throws Exception {
        String oldStr = read();
        close();

        if (out == null) {
            log.debug("init BufferedWriter in path：" + path + fileName);

            file = new File(path + fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + fileName), charset));
        }

        out.write(oldStr + content);
    }

    /**
     * 写入文件
     * 
     * @param content
     * @throws Exception
     * @author songlin.li
     */
    public void write(String content) throws Exception {
        if (content == null) {
            content = "";
        }

        if (out == null) {
            log.debug("init BufferedWriter in path：" + path + fileName);
            createFile();
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + fileName), charset));
        }

        out.write(content);
    }

    /**
     * 写文件完成后关闭
     * 
     * @author songlin.li
     */
    public void close() {
        log.debug("Close FileUtil");
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }

        if (ins != null) {
            try {
                ins.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }

        if (outs != null) {
            try {
                outs.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    /**
     * 将对象序列化到文件
     * 
     * @param obj
     * @throws Exception
     * @throws IOException
     * @author songlin.li
     */
    public void wirteObj(Object obj) throws Exception {
        if (out == null) {
            log.debug("init ObjectOutputStream in path：" + path + fileName);
            createFile();
            outs = new ObjectOutputStream(new FileOutputStream(path + fileName));
        }

        ((ObjectOutputStream) outs).writeObject(obj);
    }

    public String read() {
        if (ins == null) {
            log.debug("init InputStream in path：" + path + fileName);
            file = new File(path + fileName);

            if (file.exists()) {

                try {
                    ins = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                    return "";
                }
            } else {
                return "";
            }
        }

        return read(ins);
    }

    /**
     * 删除文件
     * 
     * @author songlin.li
     * @throws Exception
     */
    public void delete() throws Exception {
        String fileUrl = path + fileName;
        File dFile = new File(fileUrl);

        try {
            delFile(dFile);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 删除文件夹或者文件
     * 
     * @param file
     * @author songlin.li
     */
    public void delFile(File file) {
        if (file.isDirectory() && file.listFiles().length > 0) {
            log.debug("directory=" + file.getPath());
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                delFile(files[i]);
            }
        } else {
            log.debug("delete file=" + file.getPath());
            file.delete();
        }
    }

    /**
     * Description of the method
     * 
     * @author songlin
     * @return
     */
    public static String getCurrentPath() {
        String path = FileUtil.class.getClassLoader().getResource("").toString().substring(6);
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            log.error("", e);
            path = null;
        }

        return path;
    }

    /**
     * 转换文件大小
     * @see FileUtils.byteCountToDisplaySize(long)
     */
    @Deprecated
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static long getlist(File f) {// 递归求取目录文件个数
        long size = 0;
        File flist[] = f.listFiles();
        size = flist.length;
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getlist(flist[i]);
                size--;
            }
        }
        return size;

    }

    public static void main(String args[]) {
        long startTime = System.currentTimeMillis();
        try {
            long l = 0;
            String path = "/Users/songlin/Documents/ibmall/字段.docx";
            File ff = new File(path);
            if (ff.isDirectory()) { // 如果路径是文件夹的时候
                System.out.println("文件个数           " + getlist(ff));
                System.out.println("目录");
                l = sizeOf(ff);
                System.out.println(path + "目录的大小为：" + formatFileSize(l));
            } else {
                System.out.println("     文件个数           1");
                System.out.println("文件");
                l = sizeOf(ff);
                System.out.println(path + "文件的大小为：" + formatFileSize(l));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("总共花费时间为：" + (endTime - startTime) + "毫秒...");
    }

    public BufferedWriter getOut() {
        return out;
    }

    public void setOut(BufferedWriter out) {
        this.out = out;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        out = null;
        this.path = path;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}
