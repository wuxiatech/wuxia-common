package cn.wuxia.common.web.httpclient;

import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.MapUtil;
import cn.wuxia.common.util.reflection.BeanUtil;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.*;
import org.apache.http.message.BasicNameValuePair;
import org.nutz.lang.random.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author songlin
 */
@Data
public class HttpAsyncClientRequest extends HttpClientRequest {
    private String requestId;

    public HttpAsyncClientRequest(String requestId) {
        this.requestId = requestId;
    }

    public HttpAsyncClientRequest() {
        this.requestId = R.UU32();
    }

    public static HttpAsyncClientRequest create() {
        return new HttpAsyncClientRequest(R.UU32());
    }

    public HttpAsyncClientRequest(HttpClientRequest httpClientRequest) {
        this();
        BeanUtil.copyProperties(this, httpClientRequest);
        if (ListUtil.isNotEmpty(httpClientRequest.getParams())) {
            for (BasicNameValuePair basicNameValuePair : httpClientRequest.getParams()) {
                this.addParam(basicNameValuePair.getName(), basicNameValuePair.getValue());
            }
        }
    }

}
