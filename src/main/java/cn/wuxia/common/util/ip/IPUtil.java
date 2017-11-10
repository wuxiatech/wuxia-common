package cn.wuxia.common.util.ip;

import cn.wuxia.common.util.StringUtil;

public class IPUtil {
    /**
     * 验证IP是否属于某个IP段
     *
     * @param ip        所验证的IP号码
     * @param ipSection IP段（以'-'分隔）
     */
    public static boolean existsInRange(String ip, String ipSection) {
        ipSection = StringUtil.trim(ipSection);
        ip = StringUtil.trim(ip.trim());
        String beginIP = StringUtil.substringBefore(ipSection, "-");
        String endIP = StringUtil.substringAfter(ipSection, "-");
        if (StringUtil.indexOf(endIP, ".") < 0) {
            String _pre = StringUtil.substringBeforeLast(beginIP, ".");
            endIP = _pre + "." + endIP;
        }
        return getIp2long(beginIP) <= getIp2long(ip) && getIp2long(ip) <= getIp2long(endIP);
    }

    private static long getIp2long(String ip) {
        ip = ip.trim();
        String[] ips = ip.split("\\.");
        long ip2long = 0L;
        for (int i = 0; i < 4; ++i) {
            ip2long = ip2long << 8 | Integer.parseInt(ips[i]);
        }
        return ip2long;
    }

    private static long getIp2long2(String ip) {
        ip = ip.trim();
        String[] ips = ip.split("\\.");
        long ip1 = Integer.parseInt(ips[0]);
        long ip2 = Integer.parseInt(ips[1]);
        long ip3 = Integer.parseInt(ips[2]);
        long ip4 = Integer.parseInt(ips[3]);
        long ip2long = 1L * ip1 * 256 * 256 * 256 + ip2 * 256 * 256 + ip3 * 256 + ip4;
        return ip2long;

    }
}
