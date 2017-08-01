/**
 * Copyright (c) 2005-2010 springside.org.cn Licensed under the Apache License,
 * Version 2.0 (the "License"); $Id: ThreadUtils.java 1211 2010-09-10 16:20:45Z
 * calvinxiu $
 */
package cn.wuxia.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The thread related Utils set of functions.
 * 
 * @author calvin
 */
public class ThreadUtil {

    /**
     * sleep to wait, in milliseconds, ignore InterruptedException.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    /**
     * @description :Graceful Shutdown. Prepared in accordance with the sample
     *              code ExecutorService JavaDoc first the shutdown attempt to
     *              perform all tasks. Called timeout the task of shutdownNow
     *              cancel Pending the, workQueue, and interrupt all blocking
     *              function. Another shutdown when the thread itself is called
     *              interrupt processing.
     * @param pool
     * @param shutdownTimeout
     * @param shutdownNowTimeout
     * @param timeUnit
     */
    public static void gracefulShutdown(ExecutorService pool, int shutdownTimeout, int shutdownNowTimeout,
            TimeUnit timeUnit) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(shutdownTimeout, timeUnit)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(shutdownNowTimeout, timeUnit)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @description : The direct call shutdownNow method.
     * @param pool
     * @param timeout
     * @param timeUnit
     */
    public static void normalShutdown(ExecutorService pool, int timeout, TimeUnit timeUnit) {
        try {
            pool.shutdownNow();
            if (!pool.awaitTermination(timeout, timeUnit)) {
                System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * For custom ThreadFactory to customize the name of the thread pool.
     */
    public static class CustomizableThreadFactory implements ThreadFactory {

        private final String namePrefix;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public CustomizableThreadFactory(String poolName) {
            namePrefix = poolName + "-";
        }

        public Thread newThread(Runnable runable) {
            return new Thread(runable, namePrefix + threadNumber.getAndIncrement());
        }
    }
}
