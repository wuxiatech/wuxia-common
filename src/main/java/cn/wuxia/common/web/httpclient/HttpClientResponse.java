package cn.wuxia.common.web.httpclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;

import cn.wuxia.common.util.StringUtil;

/* *
 * 类名：HttpResponse功能：Http返回对象的封装详细：封装Http返回信息版本：3.3日期：2011-08-17说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class HttpClientResponse {

    /**
     * 返回中的Header信息
     */
    private Header[] responseHeaders;

    /**
     * String类型的result
     */
    private String stringResult;

    /**
     * btye类型的result
     */
    private byte[] byteResult;

    /**
     * 返回的数据编码
     */
    private String charset;

    public Header[] getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Header[] responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public byte[] getByteResult() {
        if (byteResult != null) {
            return byteResult;
        }
        if (stringResult != null) {
            return stringResult.getBytes();
        }
        return null;
    }

    public void setByteResult(byte[] byteResult) {
        this.byteResult = byteResult;
    }

    public String getStringResult() {
        if (byteResult != null) {
            try {
                return new String(byteResult, charset);
            } catch (UnsupportedEncodingException e) {
            }
        }
        return null;
    }

    public String getStringResult(String charset) {
        if (byteResult != null) {
            try {
                return new String(byteResult, charset);
            } catch (UnsupportedEncodingException e) {
            }
        }
        return null;
    }
    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    public InputStream getContent() {
        return new ByteArrayInputStream(this.byteResult);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getHeader(String headerName) {
        for (Header header : responseHeaders) {
            if (StringUtil.equalsIgnoreCase(header.getName(), headerName)) {
                return header.getValue();
            }
        }
        return null;
    }
}
