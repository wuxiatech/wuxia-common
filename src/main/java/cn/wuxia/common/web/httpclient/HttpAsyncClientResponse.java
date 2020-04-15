package cn.wuxia.common.web.httpclient;

import cn.wuxia.common.util.StringUtil;
import lombok.Data;
import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * 类名：HttpResponse功能：Http返回对象的封装详细：封装Http返回信息版本：3.3日期：2011-08-17说明：
 * 以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 * 该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 *
 * @author songlin
 */
@Data
public class HttpAsyncClientResponse extends HttpClientResponse {
    private String requestId;

}
