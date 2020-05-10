package cn.wuxia.common.web.httpclient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author songlin
 */
@Getter
@Setter
@AllArgsConstructor
public class HttpAction {
    String url;

    HttpClientMethod method;

    public static HttpAction action(String url, HttpClientMethod method) {
        return new HttpAction(url, method);
    }

    public static HttpAction get(String url) {
        return new HttpAction(url, HttpClientMethod.GET);
    }

    public static HttpAction post(String url) {
        return new HttpAction(url, HttpClientMethod.POST);
    }

}
