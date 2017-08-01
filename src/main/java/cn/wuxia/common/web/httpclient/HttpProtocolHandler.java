package cn.wuxia.common.web.httpclient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.util.StringUtil;

/* *
 * 类名：HttpProtocolHandler功能：HttpClient方式访问详细：获取远程HTTP数据版本：3.3日期：2012-08-17说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class HttpProtocolHandler {
    protected Logger logger = LoggerFactory.getLogger("httpclient");

    private static String DEFAULT_CHARSET = "UTF-8";

    /** 连接超时时间，由bean factory设置，缺省为8秒钟 */
    private int defaultConnectionTimeout = 8000;

    /** 回应超时时间, 由bean factory设置，缺省为30秒钟 */
    private int defaultSoTimeout = 30000;

    /** 闲置连接超时时间, 由bean factory设置，缺省为60秒钟 */
    private int defaultIdleConnTimeout = 60000;

    private int defaultMaxConnPerHost = 30;

    private int defaultMaxTotalConn = 80;

    /** 默认等待HttpConnectionManager返回连接超时（只有在达到最大连接数时起作用）：1秒*/
    private static final long defaultHttpConnectionManagerTimeout = 3 * 1000;

    /**
     * HTTP连接管理器，该连接管理器必须是线程安全的.
     */
    private HttpClientContext context = HttpClientContext.create();

    private PoolingHttpClientConnectionManager connectionManager;

    private static HttpProtocolHandler httpProtocolHandler = new HttpProtocolHandler();

    /**
     * 工厂方法
     * 
     * @return
     */
    public static HttpProtocolHandler getInstance() {
        return httpProtocolHandler;
    }

    /**
     * 私有的构造方法
     */
    private HttpProtocolHandler() {
        // 创建一个线程安全的HTTP连接池
        //        connectionManager = new PoolingHttpClientConnectionManager();
        //        connectionManager.setMaxTotal(defaultMaxTotalConn);
        //        connectionManager.setDefaultMaxPerRoute(defaultMaxConnPerHost);
        //
        //        IdleConnectionMonitorThread ict = new IdleConnectionMonitorThread(connectionManager);
        //        ict.setConnectionTimeout(defaultHttpConnectionManagerTimeout);
        //        ict.start();
    }

    /**
     * 执行Http请求
     * 
     * @param request 请求数据
     * @param strParaFileName 文件类型的参数名
     * @param strFilePath 文件路径
     * @return 
     * @throws HttpException, IOException 
     */
    public HttpClientResponse execute(HttpClientRequest request, String strParaFileName, String strFilePath) throws HttpException, IOException {

        // 设置连接超时
        int connectionTimeout = defaultConnectionTimeout;
        if (request.getConnectionTimeout() > 0) {
            connectionTimeout = request.getConnectionTimeout();
        }

        // 设置回应超时
        int soTimeout = defaultSoTimeout;
        if (request.getSocketTimeout() > 0) {
            soTimeout = request.getSocketTimeout();
        }

        String charset = request.getCharset();
        charset = charset == null ? DEFAULT_CHARSET : charset;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(connectionTimeout).build();

        CloseableHttpResponse response = null;
        HttpPost httpPost;
        // get模式且不带上传文件
        if (request.getMethod().equals(HttpClientRequest.METHOD_GET)) {
            HttpGet httpGet = new HttpGet(request.getUrl() + (StringUtil.indexOf(request.getUrl(), "?") > 0 ? "" : "?") + request.getQueryString());
            httpGet.setConfig(requestConfig);
            logger.debug("request====" + httpGet.getRequestLine());
            response = httpclient.execute(httpGet, context);
        } else if (strParaFileName.equals("") && strFilePath.equals("")) {
            // post模式且不带上传文件
            httpPost = new HttpPost(request.getUrl());
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=" + charset);
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(request.getParams(), charset);
            httpPost.setEntity(uefEntity);
            httpPost.setConfig(requestConfig);
            response = httpclient.execute(httpPost, context);
        } else {
            // post模式且带上传文件
            httpPost = new HttpPost(request.getUrl());
            // 增加文件参数，strParaFileName是参数名，使用本地文件
            FileBody bin = new FileBody(new File(strFilePath));
            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("bin", bin).addPart("comment", comment).build();
            httpPost.setEntity(reqEntity);
            httpPost.setConfig(requestConfig);
            response = httpclient.execute(httpPost, context);
        }

        logger.debug("status====" + response.getStatusLine());
        HttpClientResponse returnResponse = new HttpClientResponse();
        try {
            HttpEntity entity = response.getEntity();
            if (request.getResultType().equals(HttpResultType.STRING)) {
                returnResponse.setStringResult(EntityUtils.toString(entity, charset));
            } else if (request.getResultType().equals(HttpResultType.BYTES)) {
                returnResponse.setByteResult(EntityUtils.toByteArray(entity));
            }
            returnResponse.setResponseHeaders(new Header[] { entity.getContentEncoding(), entity.getContentType() });
        } finally {
            response.close();
        }

        return returnResponse;
    }

    public class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;

        private volatile boolean shutdown;

        private long connectionTimeout;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                // terminate
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public HttpClientConnectionManager getConnMgr() {
            return connMgr;
        }

    }
}
