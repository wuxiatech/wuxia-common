package cn.wuxia.common.web.httpclient;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @author songlin
 */
@Configuration
public class HttpClientSpringConfiguration {

    /**
     * 整个线程池中最大连接数
     */
    private final int MAX_CONNECTION_TOTAL = 800;
    /**
     * 路由到某台主机最大并发数，是MAX_CONNECTION_TOTAL（整个线程池中最大连接数）的一个细分
     */
    private final int ROUTE_MAX_COUNT = 500;
    /**
     * 重试次数，防止失败情况
     */
    private final int RETRY_COUNT = 3;
    /**
     * 客户端和服务器建立连接的超时时间
     */
    private final int CONNECTION_TIME_OUT = 5000;
    /**
     * 客户端从服务器读取数据的超时时间
     */
    private final int READ_TIME_OUT = 7000;
    /**
     * 从连接池中获取连接的超时时间
     */
    private final int CONNECTION_REQUEST_TIME_OUT = 5000;
    /**
     * 连接空闲超时，清楚闲置的连接
     */
    private final int CONNECTION_IDLE_TIME_OUT = 5000;
    /**
     * 连接保持存活时间
     */
    private final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000;

    @Bean
    @Lazy
    public ClientHttpRequestFactory clientHttpRequestFactory()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();

        httpClientBuilder.setSSLContext(sslContext)
                .setMaxConnTotal(MAX_CONNECTION_TOTAL)
                .setMaxConnPerRoute(ROUTE_MAX_COUNT)
                .evictIdleConnections(CONNECTION_IDLE_TIME_OUT, TimeUnit.MILLISECONDS);

        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_COUNT, true));
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        CloseableHttpClient client = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
        clientHttpRequestFactory.setConnectTimeout(CONNECTION_TIME_OUT);
        clientHttpRequestFactory.setReadTimeout(READ_TIME_OUT);
        clientHttpRequestFactory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT);
        clientHttpRequestFactory.setBufferRequestBody(false);
        return clientHttpRequestFactory;
    }

    @Lazy
    @Bean
    public RestTemplate restTemplate() throws HttpClientException {
        RestTemplate restTemplate = null;
        try {
            restTemplate = new RestTemplate(clientHttpRequestFactory());
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new HttpClientException(e);
        }
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        // 修改StringHttpMessageConverter内容转换器
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
