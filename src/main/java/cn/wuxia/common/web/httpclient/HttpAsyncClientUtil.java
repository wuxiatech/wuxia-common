/*
* Created on :Nov 7, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.web.httpclient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.client.util.HttpAsyncClientUtils;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import cn.wuxia.common.util.StringUtil;

public class HttpAsyncClientUtil {

    public static Logger logger = LoggerFactory.getLogger("httpclient");

    /** 连接超时时间，由bean factory设置，缺省为无限制 */
    private static int defaultConnectionTimeout = -1;

    /** 回应超时时间, 由bean factory设置，缺省为无限制 */
    private static int defaultSocketTimeout = -1;

    public static void main(String[] args) throws Exception {
        HttpClientRequest request = new HttpClientRequest("http://fdd.link/url/create");
        request.addParam("url", "http://fsdalfkjsadflkjsadlfkjasdlf");
        long start = System.currentTimeMillis();
        HttpClientRequest[] par = new HttpClientRequest[10];
        for (int i = 0; i < 10; i++) {
            //System.out.println("***********第" + i + "个***********");
            //call(request);
            //HttpClientUtil.get(request);
            par[i] = request;
        }
        posts(par);
        System.out.println("*************" + (System.currentTimeMillis() - start));
    }

    /**
     * HttpAsyncClient连接SSL
     */
    private static HttpAsyncClientBuilder ssl() throws HttpClientException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            HttpAsyncClientBuilder builder = HttpAsyncClients.custom();
            builder.setSSLContext(sslContext);
            return builder;
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.warn(e.getMessage());
            throw new HttpClientException(e.getMessage());
        }
    }

    /**
     * 根据不同的请求自动适配HttpClient
     * @author songlin
     * @param param
     * @return
     */
    private static CloseableHttpAsyncClient getHttpClient(HttpClientRequest param, String method) throws HttpClientException {
        boolean needssl = StringUtil.indexOf(param.getUrl().toLowerCase(), "https:") == 0;
        HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();
        if (needssl) {
            builder = ssl();
        }
        // 设定自己需要的重定向策略
        if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
            builder.setRedirectStrategy(new LaxRedirectStrategy());
        }

        //配置io线程  
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).build();
        //设置连接池大小  
        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        //每个路由最大连接数
        cm.setDefaultMaxPerRoute(200);
        //连接池最大连接数
        cm.setMaxTotal(400);

        MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).setMaxLineLength(2000).build();

        /**
         * http连接设置
         * 
         * //忽略不合法的输入
         * //忽略不匹配的输入
         */
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE).setMessageConstraints(messageConstraints).setCharset(Consts.UTF_8).build();

        /**
         * ConnectTimeout： 链接建立的超时时间；
         * SocketTimeout：响应超时时间，超过此时间不再读取响应；
         * ConnectionRequestTimeout： http clilent中从connetcion pool中获得一个connection的超时时间；
         * 
         */
        int connectionTimeout = param.getConnectionTimeout() > 0 ? param.getConnectionTimeout() : defaultConnectionTimeout;
        int socketTimeout = param.getSocketTimeout() > 0 ? param.getSocketTimeout() : defaultSocketTimeout;
        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(5000).build();

        cm.setDefaultConnectionConfig(connectionConfig);
        builder.setConnectionManager(cm);
        builder.setDefaultRequestConfig(defaultRequestConfig);
        return builder.build();

    }

    /**
     * 简单异步请求，没有回复
     * @author songlin
     * @param param
     */
    public static void call(HttpClientRequest... param) throws HttpClientException {
        CloseableHttpAsyncClient httpclient = getHttpClient(param[0], HttpGet.METHOD_NAME);
        try {
            // Start the client
            httpclient.start();
            // Execute request
            List<Future<org.apache.http.HttpResponse>> lists = Lists.newArrayList();
            for (HttpClientRequest req : param) {
                final HttpGet httpget = new HttpGet(req.getUrl() + (StringUtil.indexOf(req.getUrl(), "?") > 0 ? "" : "?") + req.getQueryString());
                logger.info("async executing request " + httpget.getRequestLine().toString());
                Future<org.apache.http.HttpResponse> future = httpclient.execute(httpget, null);
                lists.add(future);
            }
            for (Future<org.apache.http.HttpResponse> future : lists) {
                // and wait until a response is received
                org.apache.http.HttpResponse response1 = null;
                try {
                    response1 = future.get();
                    logger.info("" + response1.getStatusLine());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("", e);
                }
            }
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            System.out.println("完成");
            HttpAsyncClientUtils.closeQuietly(httpclient);
        }
    }

    /**
     * 异步调用多个请求
     * @author songlin
     * @param param
     * @return
     */
    public static List<HttpClientResponse> gets(HttpClientRequest... param) throws HttpClientException {
        CloseableHttpAsyncClient httpclient = getHttpClient(param[0], HttpGet.METHOD_NAME);
        List<HttpClientResponse> resps = Lists.newArrayList();
        try {
            // Start the client
            httpclient.start();

            // One most likely would want to use a callback for operation result
            final CountDownLatch latch = new CountDownLatch(param.length);
            List<Future<org.apache.http.HttpResponse>> lists = Lists.newArrayList();
            for (HttpClientRequest req : param) {
                final HttpGet request = new HttpGet(req.getUrl() + (StringUtil.indexOf(req.getUrl(), "?") > 0 ? "" : "?") + req.getQueryString());
                logger.info("async executing request " + request.getRequestLine().toString());
                Future<org.apache.http.HttpResponse> future = httpclient.execute(request, new FutureCallback<org.apache.http.HttpResponse>() {
                    public void completed(final org.apache.http.HttpResponse response) {
                        latch.countDown();
                        logger.info(request.getRequestLine() + "->" + response.getStatusLine());
                    }

                    public void failed(final Exception ex) {
                        latch.countDown();
                        logger.warn(request.getRequestLine().toString(), ex);
                    }

                    public void cancelled() {
                        latch.countDown();
                        logger.warn(request.getRequestLine().toString() + " cancelled");
                    }

                });
                lists.add(future);
            }
            latch.await();
            for (Future<org.apache.http.HttpResponse> future : lists) {
                resps.add(getResponse(future));
            }
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            logger.info("完成异步请求，关闭连接");
            HttpAsyncClientUtils.closeQuietly(httpclient);
        }
        return resps;
    }

    /**
     * 异步调用多个请求,单个建议使用 {@link HttpClientUtil}
     * @author songlin
     * @param param
     * @return
     */
    public static List<HttpClientResponse> posts(HttpClientRequest... param) throws HttpClientException {
        CloseableHttpAsyncClient httpclient = getHttpClient(param[0], HttpPost.METHOD_NAME);
        List<HttpClientResponse> resps = Lists.newArrayList();
        try {
            // Start the client
            httpclient.start();

            // One most likely would want to use a callback for operation result
            final CountDownLatch latch = new CountDownLatch(param.length);
            List<Future<org.apache.http.HttpResponse>> lists = Lists.newArrayList();
            for (final HttpClientRequest req : param) {
                try {
                    // 创建httppost 
                    final HttpPost httppost = new HttpPost(req.getUrl());
                    if (req.isMultipart()) {
                        // 设置请求体
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        for (Map.Entry<String, ContentBody> parm : req.getContent().entrySet()) {
                            builder.addPart(parm.getKey(), parm.getValue());
                        }
                        HttpEntity reqEntity = builder.build();
                        httppost.setEntity(reqEntity);
                        // 设置Http Header中的User-Agent属性
                        BasicHeader agentHeader = new BasicHeader("User-Agent", "Mozilla/4.0");
                        httppost.addHeader(agentHeader);
                        httppost.addHeader(HttpClientUtil.HEADER_ACCEPT_ENCODING, HttpClientUtil.ENCODING_GZIP);
                    } else {
                        // 创建参数队列  
                        UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(req.getParams(), req.getCharset());
                        httppost.setEntity(uefEntity);
                    }
                    Future<org.apache.http.HttpResponse> future = httpclient.execute(httppost, new FutureCallback<org.apache.http.HttpResponse>() {
                        public void completed(final org.apache.http.HttpResponse response) {
                            latch.countDown();
                            logger.info(httppost.getRequestLine() + "->" + response.getStatusLine());
                        }

                        public void failed(final Exception ex) {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString(), ex);
                        }

                        public void cancelled() {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString() + " cancelled");
                        }

                    });

                    logger.info("async executing request " + httppost.getRequestLine().toString());
                    lists.add(future);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            try {
                latch.await();
                for (Future<org.apache.http.HttpResponse> future : lists) {
                    resps.add(getResponse(future));
                }
            } catch (InterruptedException e) {
            }
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            logger.info("完成异步请求，关闭连接");
            HttpAsyncClientUtils.closeQuietly(httpclient);
        }
        return resps;
    }

    /**
     * 异步post多个text, 单个建议 {@link HttpClientUtil.post(param, text)}
     * @author songlin
     * @param param
     * @param text
     * @return
     */
    public static List<HttpClientResponse> postTexts(String url, String... text) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(url);
        CloseableHttpAsyncClient httpclient = getHttpClient(param, HttpPost.METHOD_NAME);
        List<HttpClientResponse> resps = Lists.newArrayList();
        try {
            // Start the client
            httpclient.start();

            // One most likely would want to use a callback for operation result
            final CountDownLatch latch = new CountDownLatch(text.length);
            List<Future<org.apache.http.HttpResponse>> lists = Lists.newArrayList();
            for (String t : text) {
                try {
                    // 创建httppost 
                    final HttpPost httppost = new HttpPost(param.getUrl());
                    httppost.addHeader(HttpClientUtil.HEADER_ACCEPT_ENCODING, HttpClientUtil.ENCODING_GZIP);
                    // 创建参数队列  
                    StringEntity uefEntity = new StringEntity(t, param.getCharset());
                    httppost.setEntity(uefEntity);
                    Future<org.apache.http.HttpResponse> future = httpclient.execute(httppost, new FutureCallback<org.apache.http.HttpResponse>() {
                        public void completed(final org.apache.http.HttpResponse response) {
                            latch.countDown();
                            logger.info(httppost.getRequestLine() + "->" + response.getStatusLine());
                        }

                        public void failed(final Exception ex) {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString(), ex);
                        }

                        public void cancelled() {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString() + " cancelled");
                        }

                    });
                    logger.info("async executing request " + httppost.getRequestLine().toString());
                    lists.add(future);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            latch.await();
            for (Future<org.apache.http.HttpResponse> future : lists) {
                resps.add(getResponse(future));
            }
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            logger.info("完成异步请求，关闭连接");
            HttpAsyncClientUtils.closeQuietly(httpclient);
        }
        return resps;
    }

    /**
     * 从Future中获取回复
     * @author songlin
     * @param future
     * @return
     */
    public static HttpClientResponse getResponse(Future<org.apache.http.HttpResponse> future) throws HttpClientException {
        HttpClientResponse resp = new HttpClientResponse();
        try {
            org.apache.http.HttpResponse response = future.get();
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("async executing response " + response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (entity != null && statusCode == HttpStatus.SC_OK) {
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase("gzip")) {
                            entity = new GzipDecompressingEntity(entity);
                            break;
                        }
                    }
                }
                try {
                    resp.setByteResult(EntityUtils.toByteArray(entity));
                } catch (IOException e) {
                }
                Charset respcharset = ContentType.getOrDefault(entity).getCharset();
                //如果返回编码为空则使用默认的http 编码 ISO-8859-1
                if (null != respcharset) {
                    resp.setCharset(respcharset.name());
                } else {
                    resp.setCharset(HTTP.DEF_CONTENT_CHARSET.name());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("--------------------------------------");
                    logger.debug("Response : ContentType {}, ContentEncoding {}, /t/n Content {}", ContentType.getOrDefault(entity),
                            entity.getContentEncoding(), resp.getStringResult());
                    logger.debug("--------------------------------------");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            throw new HttpClientException(e);
        }
        return resp;
    }
    /**
    public static void get() {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            // Start the client
            httpclient.start();
    
            // In real world one most likely would also want to stream
            // request and response body content
            final CountDownLatch latch2 = new CountDownLatch(1);
            final HttpGet request3 = new HttpGet("http://www.apache.org/");
            HttpAsyncRequestProducer producer3 = HttpAsyncMethods.create(request3);
            AsyncCharConsumer<org.apache.http.HttpResponse> consumer3 = new AsyncCharConsumer<org.apache.http.HttpResponse>() {
    
                org.apache.http.HttpResponse response;
    
                @Override
                protected void onResponseReceived(final org.apache.http.HttpResponse response) {
                    this.response = response;
                }
    
                @Override
                protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl) throws IOException {
                    // Do something useful
                }
    
                @Override
                protected void releaseResources() {
                }
    
                @Override
                protected org.apache.http.HttpResponse buildResult(final HttpContext context) {
                    return this.response;
                }
    
            };
            httpclient.execute(producer3, consumer3, new FutureCallback<org.apache.http.HttpResponse>() {
    
                public void completed(final org.apache.http.HttpResponse response3) {
                    latch2.countDown();
                    System.out.println(request3.getRequestLine() + "->" + response3.getStatusLine());
                }
    
                public void failed(final Exception ex) {
                    latch2.countDown();
                    System.out.println(request3.getRequestLine() + "->" + ex);
                }
    
                public void cancelled() {
                    latch2.countDown();
                    System.out.println(request3.getRequestLine() + " cancelled");
                }
    
            });
            try {
                latch2.await();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }*/

}
