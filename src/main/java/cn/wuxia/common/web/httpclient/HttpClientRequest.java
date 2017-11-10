package cn.wuxia.common.web.httpclient;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import cn.wuxia.common.util.MapUtil;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/* *
 * 类名：HttpRequest功能：Http请求对象的封装详细：封装Http请求版本：3.3日期：2011-08-17说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class HttpClientRequest {
    public Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 待请求的url
     */
    private String url;

    /**
     * 默认的请求方式
     */

    private HttpClientMethod method = HttpClientMethod.GET;

    /**
     * 回应超时时间, 由bean factory设置，缺省为30秒钟
     */
    private int socketTimeout = 30000;

    /**
     * 连接超时时间，由bean factory设置，缺省为10秒钟
     */
    private int connectionTimeout = 10000;

    /**
     * Post 类型包括为 File, InputStream, byte[]， String时的参数
     */
    private Map<String, ContentBody> content = Maps.newHashMap();

    /**
     * Post String类型的参数
     */
    private List<BasicNameValuePair> params = Lists.newArrayList();

    /**
     * 请求Header
     */
    private Map<String, String> header = new CaseInsensitiveMap<>();

    /**
     * 默认的请求编码方式
     */
    private String charset = "UTF-8";

    /**
     * 请求发起方的ip地址
     */
    private String clientIp;

    /**
     * 请求返回的方式
     */
    private HttpResultType resultType = HttpResultType.BYTES;

    public HttpClientRequest() {
        super();
    }

    public HttpClientRequest(HttpResultType resultType) {
        super();
        this.resultType = resultType;
    }

    public HttpClientRequest(String url) {
        super();
        this.url = url;
    }

    /**
     * @return Returns the clientIp.
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @param clientIp The clientIp to set.
     */
    public HttpClientRequest setClientIp(String clientIp) {
        this.clientIp = clientIp;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public HttpClientRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpClientMethod getMethod() {
        return method;
    }

    public HttpClientRequest setMethod(HttpClientMethod method) {
        this.method = method;
        return this;
    }

    public HttpClientRequest setMethod(String method) {
        this.method = HttpClientMethod.valueOf(method);
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public HttpClientRequest setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public HttpClientRequest setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * @return Returns the charset.
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @param charset The charset to set.
     */
    public HttpClientRequest setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public HttpResultType getResultType() {
        return resultType;
    }

    public HttpClientRequest setResultType(HttpResultType resultType) {
        this.resultType = resultType;
        return this;
    }

    /**
     * 添加参数
     *
     * @param map
     * @author songlin
     */
    public HttpClientRequest addParam(Map<String, ? extends Object> map) {
        if (MapUtil.isNotEmpty(map)) {
            for (Map.Entry<String, ? extends Object> s : map.entrySet()) {
                if (s.getValue() != null)
                    addParam(s.getKey(), s.getValue());
            }
        }
        return this;
    }

    /**
     * 添加各类型参数
     *
     * @param property
     * @param value
     * @author songlin
     */
    public HttpClientRequest addParam(String property, Object value) {
        ContentBody body = null;
        if (value instanceof File) {
            body = new FileBody((File) value);
        } else if (value instanceof byte[]) {
            body = new ByteArrayBody((byte[]) value, ContentType.DEFAULT_BINARY, "");
        } else if (value instanceof InputStream) {
            body = new InputStreamBody((InputStream) value, "");
        } else {
            body = new StringBody(value.toString(), ContentType.DEFAULT_TEXT);
            params.add(new BasicNameValuePair(property, value.toString()));
        }
        this.content.put(property, body);
        return this;
    }

    public HttpClientRequest setHeader(Map<String, String> header) {
        this.header = header;
        return this;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    /**
     * 添加header
     *
     * @param name
     * @param value
     * @author songlin
     */
    public HttpClientRequest addHeader(String name, String value) {
        if (null == header) {
            header = new CaseInsensitiveMap<>();
        }
        if (header.get(name) == null) {
            header.put(name, value);
        }
        return this;
    }

    /**
     * 上传请求参数
     *
     * @return
     * @author songlin
     */
    public Map<String, ContentBody> getContent() {
        return content;
    }

    /**
     * Post 请求参数
     *
     * @return
     * @author songlin
     */
    public List<BasicNameValuePair> getParams() {
        return params;
    }

    /**
     * get 请求参数
     *
     * @return
     * @author songlin
     */
    public String getQueryString() {
        return URLEncodedUtils.format(params, charset);
    }

    /**
     * 是否存在多媒体参数
     *
     * @return
     * @author songlin
     */
    public boolean isMultipart() {
        for (Map.Entry<String, ContentBody> c : content.entrySet()) {
            if (!StringUtils.equalsIgnoreCase(c.getValue().getMediaType(), "text")) {
                return true;
            }
        }
        return false;
    }

    public static HttpClientRequest create() {
        return new HttpClientRequest();
    }

    public static HttpClientRequest create(String url) {
        return create().setUrl(url);
    }

    public static HttpClientRequest get() {
        return create().setMethod(HttpClientMethod.GET);
    }

    public static HttpClientRequest post() {
        return create().setMethod(HttpClientMethod.POST);
    }

    public static HttpClientRequest delete() {
        return create().setMethod(HttpClientMethod.DELETE);
    }

    public static HttpClientRequest put() {
        return create().setMethod(HttpClientMethod.PUT);
    }

    public static HttpClientRequest get(String url) {
        return create(url).setMethod(HttpClientMethod.GET);
    }

    public static HttpClientRequest post(String url) {
        return create(url).setMethod(HttpClientMethod.POST);
    }

    public static HttpClientRequest delete(String url) {
        return create(url).setMethod(HttpClientMethod.DELETE);
    }

    public static HttpClientRequest put(String url) {
        return create(url).setMethod(HttpClientMethod.PUT);
    }

    /**
     * build
     *
     * @throws HttpClientException
     * @author songlin
     */
    public HttpClientResponse execute() throws HttpClientException {
        return HttpClientUtil.execute(this);
    }
}
