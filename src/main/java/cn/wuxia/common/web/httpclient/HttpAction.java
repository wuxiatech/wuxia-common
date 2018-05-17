package cn.wuxia.common.web.httpclient;

public class HttpAction {
    String url;

    HttpClientMethod method;

    public HttpAction(String url, HttpClientMethod method) {
        this.url = url;
        this.method = method;
    }

    public static HttpAction Action(String url, HttpClientMethod method) {
        return new HttpAction(url, method);
    }

    public static HttpAction Action(String url) {
        return new HttpAction(url, HttpClientMethod.GET);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpClientMethod getMethod() {
        return method;
    }

    public void setMethod(HttpClientMethod method) {
        this.method = method;
    }
}
