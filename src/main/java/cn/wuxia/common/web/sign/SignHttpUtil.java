/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.wuxia.common.web.sign;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wuxia.common.web.MediaTypes;
import cn.wuxia.common.web.httpclient.HttpAsyncClientUtil;
import cn.wuxia.common.web.httpclient.HttpClientRequest;
import cn.wuxia.common.web.httpclient.HttpClientResponse;
import cn.wuxia.common.web.httpclient.HttpClientUtil;



/**
 * Http工具类
 */
public class SignHttpUtil {
    public static Logger logger = LoggerFactory.getLogger("httpclient");

    /**
     * HTTP GET
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static HttpClientResponse httpGet(String host, String path, Map<String, String> headers, Map<String, String> querys,
            List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        HttpClientRequest param = initialRequest(HttpGet.METHOD_NAME, host, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);

        return HttpClientUtil.get(param);
    }

    /**
     * HTTP POST表单
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static HttpClientResponse httpPost(String host, String path, Map<String, String> headers, Map<String, String> querys,
            Map<String, String> bodys, List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put(SignHeader.HTTP_HEADER_CONTENT_TYPE, MediaTypes.FORM_UTF_8);
        HttpClientRequest param = initialRequest(HttpPost.METHOD_NAME, host, path, headers, querys, bodys, signHeaderPrefixList, appKey, appSecret);
        return HttpClientUtil.post(param);
    }

    public static List<HttpClientResponse> httpPost(String host, String path, Map<String, String> headers, List<Map<String, String>> bodys,
            String appKey, String appSecret) throws Exception {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
//        return HttpAsyncClientUtil.posts();
        return null;
    }

    /**
     * Http POST 字符串
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param body
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static HttpClientResponse httpPost(String host, String path, Map<String, String> headers, Map<String, String> querys, String body,
            List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        HttpClientRequest param = initialRequest(HttpPost.METHOD_NAME, host, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        return HttpClientUtil.post(param);
    }

    /**
     * HTTP POST 字节数组
     * @param host
     * @param path
     * @param connectTimeout
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws Exception
     */
    public static HttpClientResponse httpPost(String host, String path, Map<String, String> headers, Map<String, String> querys, byte[] bodys,
            List<String> signHeaderPrefixList, String appKey, String appSecret) throws Exception {
        HttpClientRequest param = initialRequest(HttpPost.METHOD_NAME, host, path, headers, querys, null, signHeaderPrefixList, appKey, appSecret);
        return HttpClientUtil.post(param);
    }

    private static String initUrl(String host, String path, Map<String, String> querys) throws UnsupportedEncodingException {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);
        if (!StringUtils.isBlank(path)) {
            sbUrl.append(path);
        }
        if (null != querys) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, String> query : querys.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append(SignConstants.SPE3);
                }
                if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
                    sbQuery.append(query.getValue());
                }
                if (!StringUtils.isBlank(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StringUtils.isBlank(query.getValue())) {
                        sbQuery.append(SignConstants.SPE4);
                        sbQuery.append(URLEncoder.encode(query.getValue(), SignConstants.ENCODING));
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append(SignConstants.SPE5).append(sbQuery);
            }
        }

        return sbUrl.toString();
    }

    /**
     * 初始化基础Header
     * @param method
     * @param path
     * @param headers
     * @param querys
     * @param bodys
     * @param signHeaderPrefixList
     * @param appKey
     * @param appSecret
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException 
     */
    private static HttpClientRequest initialRequest(String method, String host, String path, Map<String, String> headers, Map<String, String> querys,
            Map<String, String> bodys, List<String> signHeaderPrefixList, String appKey, String appSecret) {
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        if (bodys == null) {
            bodys = new HashMap<String, String>();
        }
        HttpClientRequest param = null;
        try {
            headers = SignUtil.initialSignHeader(method, path, headers, querys, bodys, signHeaderPrefixList, appKey, appSecret);
            param = new HttpClientRequest(initUrl(host, path, querys));
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Map.Entry<String, String> header : headers.entrySet()) {
            param.addHeader(header.getKey(), header.getValue());
        }
        for (Map.Entry<String, String> body : bodys.entrySet()) {
            param.addParam(body.getKey(), body.getValue());
        }
        return param;
    }

    /**
     * 将流转换为字符串
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static String readStreamAsStr(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WritableByteChannel dest = Channels.newChannel(bos);
        ReadableByteChannel src = Channels.newChannel(is);
        ByteBuffer bb = ByteBuffer.allocate(4096);

        while (src.read(bb) != -1) {
            bb.flip();
            dest.write(bb);
            bb.clear();
        }
        src.close();
        dest.close();

        return new String(bos.toByteArray(), SignConstants.ENCODING);
    }

}
