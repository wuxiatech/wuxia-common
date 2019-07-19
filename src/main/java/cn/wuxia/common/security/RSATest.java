/*
* Created on :Nov 8, 2014
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 songlin.li All right reserved.
*/
package cn.wuxia.common.security;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;

import org.springframework.util.Base64Utils;

import cn.wuxia.common.util.BytesUtil;


public class RSATest {

    static String publicKey;

    static String privateKey;

    static {
        try {
            Map<String, Object> keyMap = RSAUtils.genKeyPair(512);
            publicKey = RSAUtils.getPublicKey(keyMap);
            privateKey = RSAUtils.getPrivateKey(keyMap);
            //将密钥对写入到文件
            FileWriter pubfw = new FileWriter( "/app/publicKey.keystore");
            FileWriter prifw = new FileWriter( "/app/privateKey.keystore");
            BufferedWriter pubbw = new BufferedWriter(pubfw);
            BufferedWriter pribw = new BufferedWriter(prifw);
            pubbw.write(publicKey);
            pribw.write(privateKey);
            pubbw.flush();
            pubbw.close();
            pubfw.close();
            pribw.flush();
            pribw.close();
            prifw.close();
            System.err.println("公钥: \n\r" + publicKey);
            System.err.println("私钥： \n\r" + privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //test2();
        testSign();
//        byte[] password = RSAUtils.encryptByPublicKey("123456".getBytes());
//        String pw = Base64Utils.encodeToString(password);
//        System.out.println(pw);
//       password= Base64Utils.decodeFromString(pw);
//       password=  RSAUtils.decryptByPrivateKey(password);
////        System.out.println(BytesUtil.bytesToObject(password));
//       System.out.println(Base64Utils.encodeToString("123456".getBytes()));
        //System.out.println(DesUtils.createKey().);
        //System.out.println(EncodeUtils.hexEncode("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAI/PrAcZ1jVpI5GUz5CmJNSSiw/ltUV1G8gZOJGRK/A/QzFURZzVV2CI9k8ddbyPxTeMIb+eq7G1cz6sdrnlk4ECAwEAAQ==".getBytes()));
    }
    static void test2() throws Exception{
        System.err.println("公钥加密——私钥解密");
        String source = "fjlaskjflasdkjflsadf";
        System.out.println("\r加密前文字：\r\n" + source);
        byte[] data = source.getBytes();
        byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        
        byte[] decodedData = RSAUtils.decryptByPrivateKey(new String(encodedData,"ISO-8859-1").getBytes("ISO-8859-1"), privateKey);
        String target = new String(decodedData);
        System.out.println("解密后文字: \r\n" + target);
        
        
        String sign = RSAUtils.sign(encodedData, privateKey);
        System.out.println(RSAUtils.verify(encodedData, publicKey, sign));
        
    }
    static void test() throws Exception {
        System.err.println("公钥加密——私钥解密");
        String source = "这是一行没有任何意义的文字，你看完了等于没看，不是吗？";
        System.out.println("\r加密前文字：\r\n" + source);
        byte[] data = source.getBytes();
        byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
        System.out.println("加密后文字：\r\n" + new String(encodedData));
        byte[] decodedData = RSAUtils.decryptByPrivateKey(encodedData, privateKey);
        String target = new String(decodedData);
        System.out.println("解密后文字: \r\n" + target);
    }

    static void testSign() throws Exception {
        System.err.println("私钥加密——公钥解密");
        String source = "这是一行测试RSA数字签名的无意义文字";
        System.out.println("原文字：\r\n" + source);
        byte[] data = source.getBytes();
        byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);
        System.out.println("加密后：\r\n" + new String(encodedData));
        byte[] decodedData = RSAUtils.decryptByPublicKey(encodedData, publicKey);
        String target = new String(decodedData);
        System.out.println("解密后: \r\n" + target);
        System.err.println("私钥签名——公钥验证签名");
        String sign = RSAUtils.sign(encodedData, privateKey);
        System.err.println("签名:\r" + sign);
        boolean status = RSAUtils.verify(encodedData, publicKey, sign);
        System.err.println("验证结果:\r" + status);
    }

}
