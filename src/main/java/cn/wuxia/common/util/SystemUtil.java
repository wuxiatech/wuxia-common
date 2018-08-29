package cn.wuxia.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;

import com.google.common.collect.Lists;

/**
 * <h3>Class name</h3> <h4>Description</h4> <h4>Special Notes</h4>
 * 
 * @ver 0.1
 * @author songlin.li 2012-09-18
 */
public class SystemUtil extends SystemUtils {

    /**
     * @description : get SystemInfo
     * @return
     */
    public static List<Entry<Object, Object>> getOSInfo() {
        Properties props = System.getProperties();
        List<Entry<Object, Object>> l = new ArrayList<Entry<Object, Object>>();
        for (Map.Entry<Object, Object> s : props.entrySet()) {
            l.add(s);
        }
        return l;
    }

    public static String changePathByOS(String path) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") != -1) {
            path = path.replaceAll("\\\\", "\\\\");
            path = path.replaceAll("/", "\\\\");
        }
        if (os.indexOf("aix") != -1) {
            path = path.replaceAll("/", "/");
            path = path.replaceAll("\\\\", "/");
        }
        if (os.indexOf("linux") != -1) {
            path = path.replaceAll("/", "/");
            path = path.replaceAll("\\\\", "/");
        }
        if (os.indexOf("mac") != -1) {
            path = path.replaceAll("/", "/");
            path = path.replaceAll("\\\\", "/");
        }
        return path;
    }

    /**
     * 获得内网IP
     * @return 内网IP
     */
    public static String getIntranetIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get OS ip Address
     * @author songlin.li
     * @return
     */
    public static String getOSIpAddr() {
        String defaultIp = "127.0.0.1";
        Enumeration<NetworkInterface> allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress ip = null;
        List<String> resultIp = Lists.newArrayList();
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address && ip.getHostAddress().indexOf(".") != -1 && !ip.getHostAddress().equals(defaultIp)
                        && !ip.getHostAddress().equals(getIntranetIp())) {
                    resultIp.add(ip.getHostAddress());
                }
            }
        }
        if (ListUtil.isNotEmpty(resultIp))
            return StringUtil.join(resultIp, " ");
        return getIntranetIp();
    }

    public static void main(String args[]) {
        System.out.println(getOSIpAddr());
        System.out.println("java_vendor:" + System.getProperty("java.vendor"));
        System.out.println("java_vendor_url:" + System.getProperty("java.vendor.url"));
        System.out.println("java_home:" + System.getProperty("java.home"));
        System.out.println("java_class_version:" + System.getProperty("java.class.version"));
        System.out.println("java_class_path:" + System.getProperty("java.class.path"));
        System.out.println("os_name:" + System.getProperty("os.name"));
        System.out.println("os_arch:" + System.getProperty("os.arch"));
        System.out.println("os_version:" + System.getProperty("os.version"));
        System.out.println("user_name:" + System.getProperty("user.name"));
        System.out.println("user_home:" + System.getProperty("user.home"));
        System.out.println("user_dir:" + System.getProperty("user.dir"));
        System.out.println("java_vm_specification_version:" + System.getProperty("java.vm.specification.version"));
        System.out.println("java_vm_specification_vendor:" + System.getProperty("java.vm.specification.vendor"));
        System.out.println("java_vm_specification_name:" + System.getProperty("java.vm.specification.name"));
        System.out.println("java_vm_version:" + System.getProperty("java.vm.version"));
        System.out.println("java_vm_vendor:" + System.getProperty("java.vm.vendor"));
        System.out.println("java_vm_name:" + System.getProperty("java.vm.name"));
        System.out.println("java_ext_dirs:" + System.getProperty("java.ext.dirs"));
        System.out.println("file_separator:" + System.getProperty("file.separator"));
        System.out.println("path_separator:" + System.getProperty("path.separator"));
        System.out.println("line_separator:" + System.getProperty("line.separator"));
    }
}
