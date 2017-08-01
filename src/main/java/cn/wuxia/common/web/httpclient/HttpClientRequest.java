package cn.wuxia.common.web.httpclient;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/* *
 * 类名：HttpRequest功能：Http请求对象的封装详细：封装Http请求版本：3.3日期：2011-08-17说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class HttpClientRequest {

    /** HTTP GET method */
    public static final String METHOD_GET = HttpGet.METHOD_NAME;

    /** HTTP POST method */
    public static final String METHOD_POST = HttpPost.METHOD_NAME;

    /**
     * 待请求的url
     */
    private String url = null;

    /**
     * 默认的请求方式
     */
    private String method = METHOD_GET;

    /** 回应超时时间, 由bean factory设置，缺省为30秒钟 */
    private int socketTimeout = 30000;

    /** 连接超时时间，由bean factory设置，缺省为10秒钟 */
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
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
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
    public void setCharset(String charset) {
        this.charset = charset;
    }

    public HttpResultType getResultType() {
        return resultType;
    }

    public void setResultType(HttpResultType resultType) {
        this.resultType = resultType;
    }

    /**
     * 添加参数
     * @author songlin
     * @param map
     */
    public void addParam(Map<String, Object> map) {
        for (Map.Entry<String, Object> s : map.entrySet()) {
            addParam(s.getKey(), s.getValue());
        }
    }

    /**
     * 添加各类型参数
     * @author songlin
     * @param property
     * @param value
     */
    public void addParam(String property, Object value) {
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
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    /**
     * 添加header
     * @author songlin
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        if (null == header) {
            header = new CaseInsensitiveMap<>();
        }
        if (header.get(name) == null) {
            header.put(name, value);
        }
    }

    /**
     * 上传请求参数
     * @author songlin
     * @return
     */
    public Map<String, ContentBody> getContent() {
        return content;
    }

    /**
     * Post 请求参数
     * @author songlin
     * @return
     */
    public List<BasicNameValuePair> getParams() {
        return params;
    }

    /**
     * get 请求参数
     * @author songlin
     * @return
     */
    public String getQueryString() {
        return URLEncodedUtils.format(params, charset);
    }

    /**
     * 是否存在多媒体参数
     * @author songlin
     * @return
     */
    public boolean isMultipart() {
        for (Map.Entry<String, ContentBody> c : content.entrySet()) {
            if (!StringUtils.equalsIgnoreCase(c.getValue().getMediaType(), "text")) {
                return true;
            }
        }
        return false;
    }
}
