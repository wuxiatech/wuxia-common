package cn.wuxia.common.web.httpclient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HttpAction {
    String url;

    HttpClientMethod method;

    @Deprecated
    public static HttpAction Action(String url, HttpClientMethod method) {
        return new HttpAction(url, method);
    }

    @Deprecated
    public static HttpAction Action(String url) {
        return new HttpAction(url, HttpClientMethod.GET);
    }

    public static HttpAction action(String url, HttpClientMethod method) {
        return new HttpAction(url, method);
    }

    public static HttpAction action(String url) {
        return new HttpAction(url, HttpClientMethod.GET);
    }


}
