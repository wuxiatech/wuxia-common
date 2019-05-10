/*
 * Created on :Sep 10, 2012 Author :songlin.li
 */
package cn.wuxia.common.spring.support;

import cn.wuxia.common.util.StringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * statistics service method spend time
 * 
 * @author songlin.li @ Version : V<Ver.No> <Sep 10, 2012>
 */
public abstract class LogAspect {

    protected Logger logger = LoggerFactory.getLogger(LogAspect.class);

    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = "";//getIp();
        logger.debug("$$$" + ip + " Beginning invoke method: " + joinPoint.toLongString()+" -> ({})", StringUtil.join(joinPoint.getArgs(), ","));
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long totalTime = System.currentTimeMillis() - startTime;
            if (totalTime >= 1000 && totalTime <= 3 * 1000) {
                logger.info("$$$" + ip + " Method " + joinPoint.toShortString() + " invocation time(Level-1): " + totalTime + " ms.");
            } else if (totalTime > 3 * 1000 && totalTime <= 10 * 1000) {
                logger.info("$$$" + ip + " Method " + joinPoint.toShortString() + " invocation time(Level-2): " + (totalTime) + " ms.");
            } else if (totalTime > 10 * 1000) {
                logger.warn("$$$" + ip + " Method " + joinPoint.toShortString() + " invocation time(Level-3): " + (totalTime) + " ms.");
            }

        }
        return result;
    }

    public abstract String getIp();
    /*private String getIp() {
        try {
            Class<?> springSecurityUtils = ClassLoaderUtil
                    .loadClass("cn.wuxia.ueq.ucm.core.security.support.SpringSecurityUtils");
            Object obj = springSecurityUtils.newInstance();
            Object ipObj = ClassUtil.invokeMethod(obj, "getCurrentUserIp", null);
            return "[" + ipObj + "]";
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return "";
    }*/
}
