/*
* Created on :Nov 7, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.web.httpclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.util.FileUtil;
import cn.wuxia.common.util.MapUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.web.MediaTypes;
import cn.wuxia.common.web.MessageDigestUtil;

public class HttpClientUtil {
    public static Logger logger = LoggerFactory.getLogger("httpclient");

    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    //请求Header Content-Type
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String ENCODING_GZIP = "gzip";

    /** 连接超时时间，由bean factory设置，缺省为无限制 */
    private static int defaultConnectionTimeout = -1;

    /** 回应超时时间, 由bean factory设置，缺省为无限制 */
    private static int defaultSocketTimeout = -1;

    /**
     * HttpClient连接SSL
     */
    private static HttpClientBuilder ssl() throws HttpClientException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            HttpClientBuilder builder = HttpClients.custom();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            builder.setSSLSocketFactory(sslsf);
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
    private static CloseableHttpClient getHttpClient(HttpClientRequest param, String method) throws HttpClientException {
        boolean needssl = StringUtil.indexOf(param.getUrl().toLowerCase(), "https:") == 0;
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (needssl) {
            builder = ssl();
        }
        // 设定自己需要的重定向策略
        if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
            builder.setRedirectStrategy(new LaxRedirectStrategy());
        }

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
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
     * 简单的get方法，传参使用默认的UTF-8编码, 返回使用ISO-8859-1编码转为字符串
     * 如果返回有中文乱码，请调用 HttpClientUtil.post(HttpRequest param)方法，该方法默认使用UTF8转字符串
     * @see HttpClientUtil.post(HttpRequest param)
     * @see HttpResponse.getStringResult()
     * @author songlin
     * @param url
     * @return
     */
    public static String get() throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(HttpResultType.STRING);
        return get(param).getStringResult();
    }

    /**
     * 简单的get方法，传参使用默认的UTF-8编码, 返回使用ISO-8859-1编码转为字符串
     * 如果返回有中文乱码，请调用 HttpClientUtil.post(HttpRequest param)方法，该方法默认使用UTF8转字符串
     * @see HttpClientUtil.post(HttpRequest param)
     * @see HttpResponse.getStringResult()
     * @author songlin
     * @param url
     * @return
     */
    public static String get(String url) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(HttpResultType.STRING);
        param.setUrl(url);
        return get(param).getStringResult();
    }

    /**
     * 简单的post方法，传参使用默认的UTF-8编码, 返回使用ISO-8859-1编码转为字符串
     * 如果返回有中文乱码，请调用 HttpClientUtil.post(HttpRequest param)方法，该方法默认使用UTF8转字符串
     * @see HttpClientUtil.post(HttpRequest param)
     * @see HttpResponse.getStringResult()
     * @author songlin
     * @param url
     * @return
     */
    public static String post() throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(HttpResultType.STRING);
        return post(param).getStringResult();
    }

    /**
     * 简单的post方法，传参使用默认的UTF-8编码, 返回使用ISO-8859-1编码转为字符串
     * 如果返回有中文乱码，请调用 HttpClientUtil.post(HttpRequest param)方法，该方法默认使用UTF8转字符串
     * @see HttpClientUtil.post(HttpRequest param)
     * @see HttpResponse.getStringResult()
     * @author songlin
     * @param url
     * @return
     */
    public static String post(String url) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(HttpResultType.STRING);
        param.setUrl(url);
        return post(param).getStringResult();
    }

    /**
     * 简单的post方法，传参使用默认的UTF-8编码, 返回使用返回的编码转译如没有返回编码默认使用ISO-8859-1编码转为字符串
     * 如果返回有中文乱码，请调用 HttpClientUtil.postText(HttpRequest param)方法，该方法默认使用UTF8转字符串
     * @see HttpClientUtil.postText(HttpRequest param)
     * @see HttpResponse.getStringResult()
     * @author songlin
     * @param url
     * @return
     */
    public static String post(String url, String text) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(HttpResultType.STRING);
        param.setUrl(url);
        return postText(param, text).getStringResult();
    }

    /**
     * post方式提交表单
     */
    public static HttpClientResponse post(HttpClientRequest param) throws HttpClientException {
        // 创建默认的httpClient实例.  
        CloseableHttpClient httpclient = getHttpClient(param, HttpPost.METHOD_NAME);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();

            // 创建httppost 
            HttpPost httppost = new HttpPost(param.getUrl());
            /**
             * 使用param.addHeader防止重复
             */
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.FORM, param.getCharset()).toString());
            /**
             * 自定义的头
             */
            addHeader(httppost, param);
            // 创建参数队列  
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(param.getParams(), param.getCharset());
            httppost.setEntity(uefEntity);
            logger.info("executing request " + httppost.getRequestLine().toString());
            logger.debug("post parameter:", param.getParams());
            result = httpclient.execute(httppost, responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.info("{} 请求耗时：{}ms ", param.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源  
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * 发送 get请求
     */
    public static HttpClientResponse get(HttpClientRequest param) throws HttpClientException {
        CloseableHttpClient httpclient = getHttpClient(param, HttpGet.METHOD_NAME);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();
            // 创建httpget.  
            HttpGet httpget = new HttpGet(param.getUrl() + (StringUtil.indexOf(param.getUrl(), "?") > 0 ? "" : "?") + param.getQueryString());
            /**
             * 使用param.addHeader防止重复
             */
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.FORM, param.getCharset()).toString());
            /**
             * 自定义的头
             */
            addHeader(httpget, param);
            // 执行get请求.  
            logger.info("executing request " + httpget.getRequestLine().toString());
            result = httpclient.execute(httpget, responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.info("{} 请求耗时：{}ms ", param.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源  
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * 上传文件
     * 如服务端为springmvc，需要使用MultipartHttpServletRequest获取数据，MultipartFile 的名字为media
     */
    public static HttpClientResponse post(String url, byte[] bytes) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(url);
        param.addParam("media", bytes);
        return upload(param);
    }

    /**
     * post inputStream
     * 如服务端为springmvc，需要使用MultipartHttpServletRequest获取数据，MultipartFile 的名字为media
     */
    public static HttpClientResponse post(String url, InputStream inputStream) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(url);
        param.addParam("media", inputStream);
        return upload(param);
    }

    /**
     * 上传文件
     * 如服务端为springmvc，需要使用MultipartHttpServletRequest获取数据，MultipartFile 的名字为media
     */
    public static HttpClientResponse post(String url, File file) throws HttpClientException {
        HttpClientRequest param = new HttpClientRequest(url);
        param.addParam("media", file);
        return upload(param);
    }

    /**
     * 上传文件 <br>
     * 1.      request.getParameter("");//获取客户端通过addTextBody方法添加的String类型的数据。<br>
     *
     * 2.      request.getPart("");//获取客户端通过addBinaryBody、addPart、addTextBody方法添加的指定数据，返回Part类型的对象。<br>
     *
     * 3.      request.getParts();//获取客户端通过addBinaryBody、addPart、addTextBody方法添加的所有数据，返回Collection<Part>类型的对象。
     */
    public static HttpClientResponse upload(HttpClientRequest param) throws HttpClientException {
        // 创建默认的httpClient实例.  
        CloseableHttpClient httpclient = getHttpClient(param, HttpPost.METHOD_NAME);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();
            // post模式且带上传文件
            HttpPost httppost = new HttpPost(param.getUrl());

            // 设置请求体
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (Map.Entry<String, ContentBody> parm : param.getContent().entrySet()) {
                builder.addPart(parm.getKey(), parm.getValue());
            }
            HttpEntity reqEntity = builder.build();
            httppost.setEntity(reqEntity);
            // 设置Http Header中的User-Agent属性
            /**
             * 使用param.addHeader防止重复
             */
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.MULTIPART_FORM_DATA, param.getCharset()).toString());
            // 设置Http Header中的User-Agent属性
            param.addHeader("User-Agent", "Mozilla/4.0");
            /**
             * 自定义的头
             */
            addHeader(httppost, param);
            logger.info("executing request " + httppost.getRequestLine().toString());
            result = httpclient.execute(httppost, responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.debug("{} 请求耗时：{}ms ", param.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源  
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * "application/json;charset=UTF_8"
     * @author songlin
     * @param param
     * @param jsonString
     * @return
     */
    public static HttpClientResponse postJSON(HttpClientRequest param, String jsonString) throws HttpClientException {
        // 创建默认的httpClient实例.  
        CloseableHttpClient httpclient = getHttpClient(param, HttpPost.METHOD_NAME);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();
            // 创建httppost 
            HttpPost httppost = new HttpPost(param.getUrl() + (StringUtil.indexOf(param.getUrl(), "?") > 0 ? "" : "?") + param.getQueryString());
            /**
             * 使用param.addHeader防止重复
             */
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.JSON, param.getCharset()).toString());
            addHeader(httppost, param);
            // 创建参数队列  
            StringEntity e = new StringEntity(jsonString, ContentType.create(MediaTypes.JSON, param.getCharset()));
            httppost.setEntity(e);
            logger.info("executing request " + httppost.getRequestLine().toString());
            result = httpclient.execute(httppost, responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.debug("{} 请求耗时：{}ms ", param.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源 
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * "text/plain"
     * @author songlin
     * @param param
     * @param text
     * @return
     */
    public static HttpClientResponse postText(HttpClientRequest param, String text) throws HttpClientException {
        // 创建默认的httpClient实例.  
        CloseableHttpClient httpclient = getHttpClient(param, HttpPost.METHOD_NAME);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();
            // 创建httppost 
            HttpPost httppost = new HttpPost(param.getUrl() + (StringUtil.indexOf(param.getUrl(), "?") > 0 ? "" : "?") + param.getQueryString());
            /**
             * 使用param.addHeader防止重复
             */
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.TEXT_PLAIN, param.getCharset()).toString());
            addHeader(httppost, param);
            // 创建参数队列  
            StringEntity e = new StringEntity(text, ContentType.create(MediaTypes.TEXT_PLAIN, param.getCharset()));
            httppost.setEntity(e);
            logger.info("executing request " + httppost.getRequestLine().toString());
            result = httpclient.execute(httppost, responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.debug("{} 请求耗时：{}ms ", param.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源 
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * 发送xml "application/xml"
     * @author songlin
     * @param param
     * @param xmlString
     * @return
     */
    public static HttpClientResponse postXML(HttpClientRequest param, String xmlString) throws HttpClientException {
        // 创建默认的httpClient实例.  
        CloseableHttpClient httpclient = getHttpClient(param, HttpPost.METHOD_NAME);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();
            // 创建httppost
            HttpPost httppost = new HttpPost(param.getUrl() + (StringUtil.indexOf(param.getUrl(), "?") > 0 ? "" : "?") + param.getQueryString());
            /**
             * 使用param.addHeader防止重复
             */
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.APPLICATION_XML, param.getCharset()).toString());
            addHeader(httppost, param);
            // 创建参数队列  
            StringEntity e = new StringEntity(xmlString, ContentType.create(MediaTypes.APPLICATION_XML, param.getCharset()));
            httppost.setEntity(e);
            logger.info("executing request " + httppost.getRequestLine().toString());
            result = httpclient.execute(httppost, responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.debug("{} 请求耗时：{}ms ", param.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源 
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    /**
     * 使用HttpClient下载文件
     * @author songlin
     * @param url
     * @return
     * @throws IOException
     */
    public static File download(String url) throws IOException, HttpClientException {
        return download(new HttpClientRequest(url));
    }

    /**
     * 使用HttpClient下载文件
     * @param param
     * @return
     * @throws IOException
     */
    public static File download(HttpClientRequest param) throws IOException, HttpClientException {
        return download(param, null);
    }

    /**
     * 使用HttpClient下载文件
     * @param param
     * @param filePath 文件保存路径：如/app/tmp/abc.jpg ...
     * @return
     * @throws IOException
     */
    public static File download(HttpClientRequest param, String filePath) throws IOException, HttpClientException {
        HttpClientResponse response = null;
        switch (param.getMethod()) {
            case HttpGet.METHOD_NAME: {
                response = get(param);
                break;
            }
            case HttpPost.METHOD_NAME: {
                response = post(param);
                break;
            }
        }
        //        Header[] headers = response.getResponseHeaders();
        //        for (Header h : headers) {
        //            if (h.getName().equalsIgnoreCase("Content-Type")) {
        //                //根据contentType获取文件名
        //            }
        //        }
        File tempFile = null;
        if (StringUtil.isNotBlank(filePath)) {
            tempFile = new File(filePath);
        } else {
            //FIXME TODO 文件名
            tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + StringUtil.random(5));
        }
        FileOutputStream output = FileUtils.openOutputStream(tempFile);
        try {
            IOUtils.copy(response.getContent(), output);
        } finally {
            IOUtils.closeQuietly(response.getContent());
            IOUtils.closeQuietly(output);
            if (FileUtil.sizeOf(tempFile) == 0) {
                FileUtil.deleteQuietly(tempFile);
                return null;
            }
        }
        return tempFile;
    }

    /**
     * 从URL上下载文件
     * @author songlin.li
     * @param url 下载地址
     * @param filePath 保存文件路径
     * @return
     * @throws Exception 
     */
    public static File download(String url, String filePath) throws Exception {
        // new一个URL对象
        URL u = new URL(url);
        // 打开链接
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        // 设置请求方式为"GET"
        conn.setRequestMethod("GET");
        // 超时响应时间为5秒
        conn.setConnectTimeout(5 * 1000);
        //可通过 contentType 得知资源的类型
        String contentType = conn.getContentType();
        logger.info("contentType: " + contentType);
        File file = new File(filePath);
        if (!file.exists())
            file.mkdirs();
        FileOutputStream output = FileUtils.openOutputStream(file);
        try {
            // 通过输入流获取文件数据
            IOUtils.copy(conn.getInputStream(), output);
        } finally {
            IOUtils.closeQuietly(conn.getInputStream());
            IOUtils.closeQuietly(output);
            if (FileUtil.sizeOf(file) == 0) {
                FileUtil.deleteQuietly(file);
                return null;
            }
        }
        return file;

    }

    /**
     * 手动处理回复数据
     */
    public static ResponseHandler<HttpClientResponse> responseHandler = new ResponseHandler<HttpClientResponse>() {

        @Override
        public HttpClientResponse handleResponse(final org.apache.http.HttpResponse response) throws ClientProtocolException, IOException {
            logger.info("executing response " + response.getStatusLine().toString());
            HttpClientResponse resp = new HttpClientResponse();
            int statusCode = response.getStatusLine().getStatusCode();
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

                resp.setByteResult(EntityUtils.toByteArray(entity));
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
                resp.setResponseHeaders(response.getAllHeaders());
            }
            HttpClientUtils.closeQuietly(response);
            return resp;
        }

    };

    /**
     * set header
     * @author songlin
     * @param message
     * @param request
     */
    private static void addHeader(HttpMessage message, HttpClientRequest request) {
        message.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
        for (Map.Entry<String, String> head : request.getHeader().entrySet()) {
            message.addHeader(head.getKey(), MessageDigestUtil.utf8ToIso88591(head.getValue()));
        }
    }
}
