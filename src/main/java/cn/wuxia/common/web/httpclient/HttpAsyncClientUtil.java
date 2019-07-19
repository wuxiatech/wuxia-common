/*
 * Created on :Nov 7, 2014
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.web.httpclient;

import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.web.MessageDigestUtil;
import com.google.common.collect.Lists;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.content.*;
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

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

/**
 * @author songlin
 */
public class HttpAsyncClientUtil {

    public static Logger logger = LoggerFactory.getLogger("httpclient");

    /**
     * 连接超时时间，由bean factory设置，缺省为无限制
     */
    private static int defaultConnectionTimeout = -1;

    /**
     * 回应超时时间, 由bean factory设置，缺省为无限制
     */
    private static int defaultSocketTimeout = -1;


    /**
     * HttpAsyncClient连接SSL
     */
    private static HttpAsyncClientBuilder ssl() throws HttpClientException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                @Override
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
     *
     * @param param
     * @return
     * @author songlin
     */
    private static CloseableHttpAsyncClient getHttpClient(HttpClientRequest param) throws HttpClientException {
        boolean needssl = StringUtil.indexOf(param.getUrl().toLowerCase(), "https:") == 0;
        HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();
        if (needssl) {
            builder = ssl();
        }
        // 设定自己需要的重定向策略
        switch (param.getMethod()) {
            case POST:
            case PUT:
                builder.setRedirectStrategy(new LaxRedirectStrategy());
                break;
        }

        //配置io线程  
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).build();
        //设置连接池大小  
        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            throw new HttpClientException(e);
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
     * 异步调用单个请求
     *
     * @param param
     * @return
     * @author songlin
     */
    public static HttpClientResponse call(HttpClientRequest param) throws HttpClientException {
        HttpClientRequest[] clientRequests = new HttpClientRequest[1];
        clientRequests[0] = param;
        return call(clientRequests).get(0);
    }

    /**
     * 异步调用多个请求
     *
     * @param param
     * @return
     * @author songlin
     */
    public static List<HttpClientResponse> call(HttpClientRequest... param) throws HttpClientException {
        CloseableHttpAsyncClient httpclient = getHttpClient(param[0]);
        List<HttpClientResponse> resps = Lists.newArrayList();
        try {
            // Start the client
            httpclient.start();

            // One most likely would want to use a callback for operation result
            final CountDownLatch latch = new CountDownLatch(param.length);
            List<Future<org.apache.http.HttpResponse>> lists = Lists.newArrayList();
            for (final HttpClientRequest clientRequest : param) {
                String url = clientRequest.getUrl();
                try {
                    HttpRequestBase httpRequest = null;
                    switch (clientRequest.getMethod()) {
                        case GET:
                            if (clientRequest.getContent() != null) {
                                throw new HttpClientException("GET方法不支持多媒体内容, 请使用POST");
                            }
                            if (ListUtil.isNotEmpty(clientRequest.getParams())) {
                                url += (StringUtil.indexOf(url, "?") > 0 ? "&" : "?") + clientRequest.getQueryString();
                            }
                            // 创建httpget.
                            httpRequest = new HttpGet(url);
                            break;
                        case DELETE:
                            if (clientRequest.getContent() != null) {
                                throw new HttpClientException("DELETE方法不支持多媒体内容, 请使用POST");
                            }
                            if (ListUtil.isNotEmpty(clientRequest.getParams())) {
                                url += (StringUtil.indexOf(url, "?") > 0 ? "&" : "?") + clientRequest.getQueryString();
                            }
                            // 创建httpget.
                            httpRequest = new HttpDelete(url);
                            break;
                        case POST:
                            if (clientRequest.getContent() != null) {
                                if (ListUtil.isNotEmpty(clientRequest.getParams())) {
                                    url += (StringUtil.indexOf(url, "?") > 0 ? "&" : "?") + clientRequest.getQueryString();
                                }
                                // 创建httppost
                                HttpPost httppost = new HttpPost(url);
                                AbstractHttpEntity httpEntity = null;

                                if (clientRequest.getContent() instanceof FileBody) {
                                    FileBody fileBody = (FileBody) clientRequest.getContent();
                                    httpEntity = new FileEntity(fileBody.getFile(), fileBody.getContentType());
                                } else {
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    clientRequest.getContent().writeTo(outputStream);
                                    AbstractContentBody contentBody = (AbstractContentBody) clientRequest.getContent();

                                    if (clientRequest.getContent() instanceof StringBody) {
                                        httpEntity = new StringEntity(new String(outputStream.toByteArray()), contentBody.getContentType());
                                    } else if (clientRequest.getContent() instanceof InputStreamBody) {
                                        httpEntity = new InputStreamEntity(new ByteArrayInputStream(outputStream.toByteArray()), ContentType.DEFAULT_BINARY);
                                    } else if (clientRequest.getContent() instanceof ByteArrayBody) {
                                        httpEntity = new ByteArrayEntity(outputStream.toByteArray(), contentBody.getContentType());
                                    } else {

                                    }
                                }
                                httppost.setEntity(httpEntity);
                                httpRequest = httppost;
                            } else if (ListUtil.isNotEmpty(clientRequest.getParams())) {
                                // 创建参数队列
                                UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(clientRequest.getParams(), clientRequest.getCharset());
                                // 创建httppost
                                HttpPost httppost = new HttpPost(url);
                                httppost.setEntity(uefEntity);
                                httpRequest = httppost;
                            } else {
                                httpRequest = new HttpPost(url);
                            }
                            break;
                        case PUT:
                            if (clientRequest.getContent() != null) {
                                throw new HttpClientException("PUT方法不支持多媒体内容, 请使用POST");
                            }
                            // 创建httppost
                            HttpPut httpput = new HttpPut(clientRequest.getUrl());
                            // 创建参数队列
                            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(clientRequest.getParams(), clientRequest.getCharset());
                            httpput.setEntity(uefEntity);
                            httpRequest = httpput;
                            break;
                    }
                    // 设置Http Header中的User-Agent属性
                    httpRequest.addHeader(new BasicHeader("User-Agent", "Mozilla/4.0"));
                    httpRequest.addHeader(HttpClientUtil.HEADER_ACCEPT_ENCODING, HttpClientUtil.ENCODING_GZIP);
                    /**
                     * 使用param.addHeader防止重复
                     */
                    for (Map.Entry<String, String> head : clientRequest.getHeader().entrySet()) {
                        httpRequest.addHeader(head.getKey(), MessageDigestUtil.utf8ToIso88591(head.getValue()));
                    }
                    logger.info("executing request " + httpRequest.getRequestLine().toString());
                    final HttpUriRequest httpUriRequest = httpRequest;
                    Future<org.apache.http.HttpResponse> future = httpclient.execute(httpUriRequest,
                            new FutureCallback<org.apache.http.HttpResponse>() {
                                @Override
                                public void completed(final org.apache.http.HttpResponse response) {
                                    latch.countDown();
                                    logger.info(httpUriRequest.getRequestLine() + "->" + response.getStatusLine());
                                }

                                @Override
                                public void failed(final Exception ex) {
                                    latch.countDown();
                                    logger.warn(httpUriRequest.getRequestLine().toString(), ex);
                                }

                                @Override
                                public void cancelled() {
                                    latch.countDown();
                                    logger.warn(httpUriRequest.getRequestLine().toString() + " cancelled");
                                }

                            });

                    logger.info("async executing request " + httpRequest.getRequestLine().toString());
                    lists.add(future);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
            try {
                latch.await();
                for (Future<org.apache.http.HttpResponse> future : lists) {
                    try {
                        resps.add(getResponse(future));
                    } catch (HttpClientException e) {
                        logger.error("", e);
                    }
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
     * 异步post多个text
     *
     * @param url
     * @param text
     * @return
     * @author songlin
     */
    public static List<HttpClientResponse> postTexts(String url, String... text) throws HttpClientException {
        HttpClientRequest param = HttpClientRequest.post(url);
        CloseableHttpAsyncClient httpclient = getHttpClient(param);
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
                        @Override
                        public void completed(final org.apache.http.HttpResponse response) {
                            latch.countDown();
                            logger.info(httppost.getRequestLine() + "->" + response.getStatusLine());
                        }

                        @Override
                        public void failed(final Exception ex) {
                            latch.countDown();
                            logger.warn(httppost.getRequestLine().toString(), ex);
                        }

                        @Override
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
                try {
                    resps.add(getResponse(future));
                } catch (Exception e) {
                    logger.error("", e);
                }
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
     *
     * @param future
     * @return
     * @author songlin
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
                    throw new HttpClientException(e);
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
            } else {
                throw new HttpClientException("" + statusCode);
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

     @Override protected void onResponseReceived(final org.apache.http.HttpResponse response) {
     this.response = response;
     }

     @Override protected void onCharReceived(final CharBuffer buf, final IOControl ioctrl) throws IOException {
     // Do something useful
     }

     @Override protected void releaseResources() {
     }

     @Override protected org.apache.http.HttpResponse buildResult(final HttpContext context) {
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
