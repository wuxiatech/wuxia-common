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

/**
 * 系统HTTP头常量
 */
public class SignHeader {
    //签名Header
    public static final String X_CA_SIGNATURE = "x-ca-signature";
    //所有参与签名的Header
    public static final String X_CA_SIGNATURE_HEADERS = "x-ca-signature-headers";
    //请求时间戳
    public static final String X_CA_TIMESTAMP = "x-ca-timestamp";
    //请求放重放Nonce,15分钟内保持唯一,建议使用UUID
    public static final String X_CA_NONCE = "x-ca-nonce";
    //APP KEY
    public static final String X_CA_KEY = "x-ca-key";
    
    //请求Header Accept
    public static final String HTTP_HEADER_ACCEPT = "accept";
    //请求Body内容MD5 Header
    public static final String HTTP_HEADER_CONTENT_MD5 = "content-md5";
    //请求Header Content-Type
    public static final String HTTP_HEADER_CONTENT_TYPE = "content-type";
    //请求Header UserAgent
    public static final String HTTP_HEADER_USER_AGENT = "user-agent";
    //请求Header Date
    public static final String HTTP_HEADER_DATE = "date";
}
