package cn.wuxia.common.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * <h3>Class name</h3> Used to load the class resource file, under the ClassPath
 * properties file. <h4>Description</h4> getExtendResource(StringrelativePath)
 * method, you can use the the ../ Symbol loading ClassPath external resources.
 * <h4>Special Notes</h4>
 * 
 * @ver 0.1
 * @author songlin.li 2008-11-24
 */
public class ClassLoaderUtil {

    private static Logger logger = LoggerFactory.getLogger(ClassLoaderUtil.class);

    /**
     * Thread.currentThread().getContextClassLoader().getResource("")
     */

    /**
     * @description :Java class is loaded. Use the fully qualified name of the
     *              class
     * @param className
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Class loadClass(String className) {
        try {
            return getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(" class not found ' " + className + " ' ", e);
        }
    }

    /**
     * @description : get Class loader
     * @return
     */
    public static ClassLoader getClassLoader() {
        return ClassLoaderUtil.class.getClassLoader();
    }

    /**
     * @description : To provide relative to the path of of ClassPath the
     *              resources, return file input stream
     * @param relativePath -Must pass the relative path of the resource.
     *            ClassPath relative to the path. If you need to find the
     *            ClassPath external resources, the need to use ../ to find
     * @return File input stream
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream getStream(String relativePath) throws MalformedURLException, IOException {
        if (!relativePath.contains("../")) {
            return getClassLoader().getResourceAsStream(relativePath);

        } else {
            return ClassLoaderUtil.getStreamByExtendResource(relativePath);
        }

    }

    /**
     * @description : get Input Stream
     * @param url
     * @return
     * @throws IOException
     */
    public static InputStream getStream(URL url) throws IOException {
        if (url != null) {

            return url.openStream();

        } else {
            return null;
        }
    }

    /**
     * @description : get Input Stream By ExtendResource method.
     * @param relativePath -Relative path must transfer resources. ClassPath
     *            relative to the path. If you need to find the ClassPath
     *            external resources, the need to use ../ to find.
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream getStreamByExtendResource(String relativePath) throws MalformedURLException, IOException {
        return ClassLoaderUtil.getStream(ClassLoaderUtil.getExtendResource(relativePath));

    }

    /**
     * @description :The the ClassLoader of Classpat of the Class where the
     *              absolute path. URL in the form of
     * @return
     */
    public static String getAbsolutePathOfClassLoaderClassPath() {
        logger.info(ClassLoaderUtil.getClassLoader().getResource("").toString());
        return ClassLoaderUtil.getClassLoader().getResource("").toString();
    }

    /**
     * @description : Get absolute path of the incoming resources
     * @param relativePath - You must pass the relative path of the resource.
     *            ClassPath relative to the path. If you need to find the
     *            ClassPath external resources, the need to use ../ to find
     * @return The absolute URL of resources
     * @throws MalformedURLException
     */
    public static URL getExtendResource(String relativePath) throws MalformedURLException {

        logger.info("The relative path of the incoming ： " + relativePath);
        // ClassLoaderUtil.log.info(Integer.valueOf(relativePath.indexOf("../")))
        // ;
        if (!relativePath.contains("../")) {
            return ClassLoaderUtil.getResource(relativePath);
        }

        String classPathAbsolutePath = ClassLoaderUtil.getAbsolutePathOfClassLoaderClassPath();
        if (relativePath.substring(0, 1).equals("/")) {
            relativePath = relativePath.substring(1);
        }
        logger.info("" + relativePath.lastIndexOf("../"));

        String wildcardString = relativePath.substring(0, relativePath.lastIndexOf("../") + 3);
        relativePath = relativePath.substring(relativePath.lastIndexOf("../") + 3);
        int containSum = ClassLoaderUtil.containSum(wildcardString, "../");
        classPathAbsolutePath = ClassLoaderUtil.cutLastString(classPathAbsolutePath, "/", containSum);
        String resourceAbsolutePath = classPathAbsolutePath + relativePath;
        logger.info("Absolute Path ： " + resourceAbsolutePath);
        URL resourceAbsoluteURL = new URL(resourceAbsolutePath);
        return resourceAbsoluteURL;
    }

    /**
     * @param source
     * @param dest
     * @return
     */
    private static int containSum(String source, String dest) {
        int containSum = 0;
        int destLength = dest.length();
        while (source.contains(dest)) {
            containSum = containSum + 1;
            source = source.substring(destLength);

        }

        return containSum;
    }

    /**
     * @param source
     * @param dest
     * @param num
     * @return
     */
    private static String cutLastString(String source, String dest, int num) {
        // String cutSource=null;
        for (int i = 0; i < num; i++) {
            source = source.substring(0, source.lastIndexOf(dest, source.length() - 2) + 1);

        }
        return source;
    }

    /**
     * @description : get Resource URL
     * @param resource -The incoming of the path relative to the ClassPath.
     * @return
     */
    public static URL getResource(String resource) {
        logger.info(" The incoming of the path relative to the ClassPath ： " + resource);
        return ClassLoaderUtil.getClassLoader().getResource(resource);
    }

    /**
     * @description : Spring class path resource
     * @param fileName
     * @return
     */
    public static String getClassesPath(String fileName) {
        // URL url = getResource(fileName);
        // String classPathName = url.getPath();
        // int endingIndex = classPathName.length() - fileName.length();
        // classPathName = classPathName.substring(0, endingIndex);
        ClassPathResource resource = new ClassPathResource(fileName);
        String path = "";
        try {
            path = resource.getURL().getPath().replace("%20", " ");
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        return path;
    }

    /**
     * Through the constructor obtain examples
     * 
     * @param classObj Kind of fully qualified name
     * @param argsClass The constructor argument types
     * @param args The constructor parameter value
     * @return Object
     */
    public static Object newInstanceByConstructor(Class<?> classObj, Class<?>[] argsClass, Object[] args) {
        Object returnObj = null;
        try {
            // Find the specified structure method
            Constructor<?> constructor = classObj.getDeclaredConstructor(argsClass);
            // Set up safety inspection, access to private constructor must
            constructor.setAccessible(true);
            returnObj = constructor.newInstance(args);
        } catch (Exception e) {
            logger.error("", e);
            e.printStackTrace();
        }

        return returnObj;
    }

    /** 
     * 从包package中获取所有的Class 
     * @param packageName
     * @return 
     */
    public static List<Class<?>> getAllClassesByPackage(String packageName) {

        //第一个class类的集合  
        List<Class<?>> classes = new ArrayList<Class<?>>();
        //是否循环迭代  
        boolean recursive = true;
        //获取包的名字 并进行替换  
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things  
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去  
            while (dirs.hasMoreElements()) {
                //获取下一个元素  
                URL url = dirs.nextElement();
                //得到协议的名称  
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上  
                if ("file".equals(protocol)) {
                    //获取包的物理路径  
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中  
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    //如果是jar包文件   
                    //定义一个JarFile  
                    JarFile jar;
                    try {
                        //获取jar  
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        //从此jar包 得到一个枚举类  
                        Enumeration<JarEntry> entries = jar.entries();
                        //同样的进行循环迭代  
                        while (entries.hasMoreElements()) {
                            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件  
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            //如果是以/开头的  
                            if (name.charAt(0) == '/') {
                                //获取后面的字符串  
                                name = name.substring(1);
                            }
                            //如果前半部分和定义的包名相同  
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                //如果以"/"结尾 是一个包  
                                if (idx != -1) {
                                    //获取包名 把"/"替换成"."  
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //如果可以迭代下去 并且是一个包  
                                if ((idx != -1) || recursive) {
                                    //如果是一个.class文件 而且不是目录  
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        //去掉后面的".class" 获取真正的类名  
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            //添加到classes  
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /** 
     * 以文件的形式来获取包下的所有Class 
     * @param packageName 
     * @param packagePath 
     * @param recursive 
     * @param classes 
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        //获取此包的目录 建立一个File  
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回  
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录  
        File[] dirfiles = dir.listFiles(new FileFilter() {
            //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)  
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        //循环所有文件  
        for (File file : dirfiles) {
            //如果是目录 则继续扫描  
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                //如果是java类文件 去掉后面的.class 只留下类名  
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    //添加到集合中去  
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(getAbsolutePathOfClassLoaderClassPath());
        System.out.println("class cn.zuji.fdd.core.security.service.impl.SecurityUriServiceImpl.getNoNeedLoginURI".replaceAll("\\s*", ""));
    }
}
