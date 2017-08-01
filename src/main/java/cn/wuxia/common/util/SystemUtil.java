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
    
    public static String changePathByOS(String path){
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
		}if(os.indexOf("mac") != -1){
			path = path.replaceAll("/", "/");
			path = path.replaceAll("\\\\", "/");
		}
		return path;
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
                if (ip != null && ip instanceof Inet4Address && ip.getHostAddress().indexOf(".") != -1
                        && !ip.getHostAddress().equals(defaultIp)) {
                    resultIp.add(ip.getHostAddress());
                }
            }
        }
        if (ListUtil.isNotEmpty(resultIp))
            return StringUtil.join(resultIp, " ");
        return defaultIp;
    }
}