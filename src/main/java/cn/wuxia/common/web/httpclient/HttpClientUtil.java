/*
 * Created on :Nov 7, 2014
 * Author     :songlin
 * Change History
 * Version       Date         Author           Reason
 * <Ver.No>     <date>        <who modify>       <reason>
 * Copyright 2014-2020 武侠科技 All right reserved.
 */
package cn.wuxia.common.web.httpclient;

import java.io.*;
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

import cn.wuxia.common.util.ListUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import cn.wuxia.common.util.FileUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.web.MediaTypes;
import cn.wuxia.common.web.MessageDigestUtil;

public class HttpClientUtil {
    public static Logger logger = LoggerFactory.getLogger("httpclient");

    /**
     * 连接超时时间，由bean factory设置，缺省为无限制
     */
    private static int defaultConnectionTimeout = -1;

    /**
     * 回应超时时间, 由bean factory设置，缺省为无限制
     */
    private static int defaultSocketTimeout = -1;

    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    public static final String ENCODING_GZIP = "gzip";

    //请求Header Content-Type
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * HttpClient连接SSL
     */
    public static HttpClientBuilder ssl() throws HttpClientException {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                @Override
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
     *
     * @param param
     * @return
     * @author songlin
     */
    public static CloseableHttpClient getHttpClient(HttpClientRequest param) throws HttpClientException {
        boolean needssl = StringUtil.indexOf(param.getUrl().toLowerCase(), "https:") == 0;

        HttpClientBuilder builder = HttpClientBuilder.create();
        if (needssl) {
            builder = ssl();
        }
        // 设定自己需要的重定向策略
        switch (param.getMethod()) {
            case POST:
            case PUT:
                builder.setRedirectStrategy(new LaxRedirectStrategy());
                break;
            case DELETE:
            case GET:
                break;
            default:
                break;
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
     * 如果返回有中文乱码，请调用 HttpClientUtil.get(HttpClientRequest param)方法，该方法默认使用UTF8转字符串
     *
     * @param url
     * @return
     * @author songlin
     * @see {@link #get(HttpClientRequest)}
     * @see {@link HttpClientResponse#getStringResult()}
     */
    public static String get(String url) throws HttpClientException {
        return HttpClientRequest.get(url).setResultType(HttpResultType.STRING).execute().getStringResult();
    }

    /**
     * 简单的post方法，传参使用默认的UTF-8编码, 返回使用ISO-8859-1编码转为字符串
     * 如果返回有中文乱码，请调用 HttpClientUtil.post(HttpRequest param)方法，该方法默认使用UTF8转字符串
     *
     * @param url
     * @return
     * @author songlin
     * @see {@link HttpClientUtil#post(HttpClientRequest param)}
     * @see {@link HttpClientResponse#getStringResult()}
     */
    public static String post(String url) throws HttpClientException {
        return HttpClientRequest.post(url).setResultType(HttpResultType.STRING).addHeader(HEADER_CONTENT_TYPE, MediaTypes.FORM_UTF_8).execute()
                .getStringResult();
    }

    /**
     * post方式提交表单
     */
    public static HttpClientResponse post(HttpClientRequest param) throws HttpClientException {
        if (param.getContent() != null) {
            // 设置Http Header中的User-Agent属性
            return execute(param.setMethod(HttpClientMethod.POST).addHeader("User-Agent", "Mozilla/5.0").addHeader(HEADER_CONTENT_TYPE,
                    ContentType.create(MediaTypes.MULTIPART_FORM_DATA, param.getCharset()).toString()));
        } else {
            if (param.getHeader().get(HEADER_CONTENT_TYPE) == null) {
                param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.FORM, param.getCharset()).toString());
            }
            return execute(param.setMethod(HttpClientMethod.POST));
        }
    }

    /**
     * pub方式提交表单
     */
    public static HttpClientResponse put(HttpClientRequest param) throws HttpClientException {
        if (param.getHeader().get(HEADER_CONTENT_TYPE) == null) {
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.FORM, param.getCharset()).toString());
        }
        return execute(param.setMethod(HttpClientMethod.PUT));
    }

    /**
     * 发送 get请求
     */
    public static HttpClientResponse get(HttpClientRequest param) throws HttpClientException {
        if (param.getHeader().get(HEADER_CONTENT_TYPE) == null) {
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.FORM, param.getCharset()).toString());
        }
        return execute(param.setMethod(HttpGet.METHOD_NAME));
    }

    /**
     * 发送 delete请求
     */
    public static HttpClientResponse delete(HttpClientRequest param) throws HttpClientException {
        if (param.getHeader().get(HEADER_CONTENT_TYPE) == null) {
            param.addHeader(HEADER_CONTENT_TYPE, ContentType.create(MediaTypes.FORM, param.getCharset()).toString());
        }
        return execute(param.setMethod(HttpDelete.METHOD_NAME));
    }

    /**
     * 上传文件
     * 如服务端为springmvc，需要使用MultipartHttpServletRequest获取数据
     */
    public static HttpClientResponse post(String url, byte[] bytes) throws HttpClientException {
        return execute(HttpClientRequest.post(url), new ByteArrayEntity(bytes, ContentType.DEFAULT_BINARY));
    }

    /**
     * post inputStream
     * 如服务端为springmvc，需要使用MultipartHttpServletRequest获取数据
     */
    public static HttpClientResponse post(String url, InputStream inputStream) throws HttpClientException {
        return execute(HttpClientRequest.post(url), new InputStreamEntity(inputStream, ContentType.DEFAULT_BINARY));
    }

    /**
     * 上传文件
     * 如服务端为springmvc，需要使用HttpServletRequest.getInputStream获取数据
     */
    public static HttpClientResponse post(String url, File file) throws HttpClientException {
        return execute(HttpClientRequest.post(url), new FileEntity(file, ContentType.DEFAULT_BINARY));
    }

    /**
     * 上传文件
     * 如服务端为springmvc，需要使用MultipartHttpServletRequest获取数据
     */
    public static HttpClientResponse upload(String url, File file, String parameterName) throws HttpClientException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().addBinaryBody(parameterName, file);
        return execute(HttpClientRequest.post(url), builder.build());
    }

    /**
     * "application/json;charset=UTF_8"
     *
     * @param url
     * @param json
     * @return
     * @author songlin
     */
    public static HttpClientResponse postJson(String url, String json) throws HttpClientException {
        return postJson(HttpClientRequest.post(url), json);
    }

    /**
     * "application/json;charset=UTF_8"
     *
     * @param param
     * @param json
     * @return
     * @author songlin
     */
    public static HttpClientResponse postJson(HttpClientRequest param, String json) throws HttpClientException {
        return execute(param.setMethod(HttpPost.METHOD_NAME), new StringEntity(json, ContentType.create(MediaTypes.JSON, param.getCharset())));
    }

    /**
     * "application/json;charset=UTF_8"
     *
     * @param url
     * @param text
     * @return
     * @author songlin
     */
    public static HttpClientResponse postText(String url, String text) throws HttpClientException {
        return postText(HttpClientRequest.post(url), text);
    }

    /**
     * "application/json;charset=UTF_8"
     *
     * @param param
     * @param text
     * @return
     * @author songlin
     */
    public static HttpClientResponse postText(HttpClientRequest param, String text) throws HttpClientException {
        return execute(param.setMethod(HttpPost.METHOD_NAME), new StringEntity(text, ContentType.create(MediaTypes.TEXT_PLAIN, param.getCharset())));
    }

    /**
     * 发送xml "application/xml"
     *
     * @param url
     * @param xml
     * @return
     * @author songlin
     */
    public static HttpClientResponse postXml(String url, String xml) throws HttpClientException {
        return postXml(HttpClientRequest.post(url), xml);
    }

    /**
     * 发送xml "application/xml"
     *
     * @param param
     * @param xml
     * @return
     * @author songlin
     */
    public static HttpClientResponse postXml(HttpClientRequest param, String xml) throws HttpClientException {
        return execute(param.setMethod(HttpPost.METHOD_NAME),
                new StringEntity(xml, ContentType.create(MediaTypes.APPLICATION_XML, param.getCharset())));
    }

    /**
     * 执行请求，不建议直接使用，参考使用明确的请求
     *
     * @param clientRequest
     * @return HttpClientResponse
     * @throws HttpClientException
     * @author songlin
     * @see {@link #post(HttpClientRequest)}
     * <br>{@link #get(HttpClientRequest)}
     * <br>{@link #put(HttpClientRequest)}
     * <br>{@link #delete(HttpClientRequest)}
     */
    public static HttpClientResponse execute(HttpClientRequest clientRequest) throws HttpClientException {
        Assert.notNull(clientRequest.getUrl(), "request url can not be null");
        String url = clientRequest.getUrl();

        HttpClientResponse result = new HttpClientResponse();
        CloseableHttpClient httpclient = getHttpClient(clientRequest);
        try {
            long start = System.currentTimeMillis();
            HttpUriRequest httpRequest = null;
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
            result = httpclient.execute(httpRequest, HttpClientUtil.responseHandler);
            long interval = System.currentTimeMillis() - start;
            logger.info("{} 请求耗时：{}ms ", clientRequest.getUrl(), interval);
        } catch (Exception e) {
            throw new HttpClientException(e);
        } finally {
            // 关闭连接,释放资源  
            HttpClientUtils.closeQuietly(httpclient);
        }
        return result;
    }

    public static HttpClientResponse execute(HttpClientRequest param, HttpEntity entity) throws HttpClientException {
        if (param.getMethod().compareTo(HttpClientMethod.POST) != 0) {
            logger.warn("不支持{},将使用POST方法调用{}", param.getMethod(), param.getUrl());
        }
        // 创建默认的httpClient实例.  
        CloseableHttpClient httpclient = getHttpClient(param);
        HttpClientResponse result = new HttpClientResponse();
        try {
            long start = System.currentTimeMillis();
            String url = param.getUrl();
            if (ListUtil.isNotEmpty(param.getParams())) {
                url += (StringUtil.indexOf(url, "?") > 0 ? "&" : "?") + param.getQueryString();
            }
            // 创建httppost
            HttpPost httppost = new HttpPost(url);

            // 创建参数队列  
            httppost.setEntity(entity);
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
     * 上传文件 <br>
     * 1.      request.getParameter("");//获取客户端通过addTextBody方法添加的String类型的数据。<br>
     * <p>
     * 2.      request.getPart("");//获取客户端通过addBinaryBody、addPart、addTextBody方法添加的指定数据，返回Part类型的对象。<br>
     * <p>
     * 3.      request.getParts();//获取客户端通过addBinaryBody、addPart、addTextBody方法添加的所有数据，返回Collection<Part>类型的对象。
     * <br>
     * 请使用
     *
     * @see {@link HttpClientUtil#post(String, byte[])}
     * <br> {@link HttpClientUtil#post(String, File)}
     * <br> {@link HttpClientUtil#post(String, InputStream)}
     * <br> {@link HttpClientUtil#post(HttpClientRequest)}
     */
    @Deprecated
    public static HttpClientResponse upload(HttpClientRequest param) throws HttpClientException {
        return post(param);
    }

    /**
     * 使用HttpClient下载文件
     *
     * @param url
     * @return
     * @throws IOException
     * @author songlin
     */
    public static InputStream download(String url) throws IOException, HttpClientException {
        return download(new HttpClientRequest(url));
    }

    /**
     * 使用HttpClient下载文件
     *
     * @param param
     * @return
     * @throws IOException
     */
    public static InputStream download(HttpClientRequest param) throws IOException, HttpClientException {
        return execute(param).getContent();
    }

    /**
     * 使用HttpClient下载文件
     *
     * @param param
     * @param filePath 文件保存路径：如/app/tmp/abc.jpg ...
     * @return
     * @throws IOException
     */
    public static File download(HttpClientRequest param, String filePath) throws IOException, HttpClientException {
        Assert.notNull(filePath, "filePath不能为空");
        HttpClientResponse response = execute(param);
        //        Header[] headers = response.getResponseHeaders();
        //        for (Header h : headers) {
        //            if (h.getName().equalsIgnoreCase("Content-Type")) {
        //                //根据contentType获取文件名
        //            }
        //        }
        File file = new File(filePath);
        FileUtil.forceMkdirParent(file);
        FileOutputStream output = FileUtils.openOutputStream(file);
        try {
            IOUtils.copy(response.getContent(), output);
        } finally {
            IOUtils.closeQuietly(response.getContent());
            IOUtils.closeQuietly(output);
            if (FileUtil.sizeOf(file) == 0) {
                FileUtil.deleteQuietly(file);
            }
        }
        return file;
    }

    /**
     * 从URL上下载文件
     *
     * @param url      下载地址
     * @param filePath 保存文件路径
     * @return
     * @throws Exception
     * @author songlin.li
     */
    public static File download(String url, String filePath) throws IOException {
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
        FileUtil.forceMkdirParent(file);
        FileOutputStream output = FileUtils.openOutputStream(file);
        try {
            // 通过输入流获取文件数据
            IOUtils.copy(conn.getInputStream(), output);
        } finally {
            IOUtils.closeQuietly(conn.getInputStream());
            IOUtils.closeQuietly(output);
            if (FileUtil.sizeOf(file) == 0) {
                FileUtil.deleteQuietly(file);
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
            HttpClientResponse resp = new HttpClientResponse();
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            logger.info("executing response: {}, {}", ContentType.get(entity), statusCode);
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
            } else {
                throw new IOException("" + statusCode);
            }
            HttpClientUtils.closeQuietly(response);
            return resp;
        }

    };

    private static void addHeader(HttpMessage httpRequest, HttpClientRequest request) {
        httpRequest.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
        /**
         * 使用param.addHeader防止重复
         */
        for (Map.Entry<String, String> head : request.getHeader().entrySet()) {
            httpRequest.addHeader(head.getKey(), MessageDigestUtil.utf8ToIso88591(head.getValue()));
        }
    }
}
