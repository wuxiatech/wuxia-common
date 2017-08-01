/*
* Created on :2015年10月21日
* Author     :songlin
* Change History
* Version       Date         Author           Reason
* <Ver.No>     <date>        <who modify>       <reason>
* Copyright 2014-2020 武侠科技 All right reserved.
*/
package cn.wuxia.common.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * 
 * [ticket id]
 * 简单在一个系统中可使用
 * @author songlin
 * @ Version : V<Ver.No> <2015年10月21日>
 */
public class DesUtils {

    // 定义 加密算法,可用 DES,DESede,Blowfish  
    private final static String ALGORITHM = "DES";

    /** 
     * 创建密匙 
     *  
     * @param algorithm 
     *             
     * @return SecretKey 秘密（对称）密钥 
     */
    public static SecretKey createKey() {
        // 声明KeyGenerator对象  
        KeyGenerator keygen;
        SecretKey deskey = null;
        // 声明 密钥对象  
        try {
            // 返回生成指定算法的秘密密钥的 KeyGenerator 对象,可用 DES,DESede,Blowfish
            keygen = KeyGenerator.getInstance(ALGORITHM);
            // 生成一个密钥  
            deskey = keygen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return deskey;
    }

    /**
     * 一个简单的加密字符串代码，可被解密
     * 根据密匙进行DES加密 
     * @author songlin 
     *  
    
     * @param info 
     *            要加密的信息 
     * @return String 加密后的信息 
     */
    public String encrypt(SecretKey deskey, String word) {
        // 加密随机数生成器 (RNG),(可以不写)  
        SecureRandom sr = new SecureRandom();
        // 定义要生成的密文  
        byte[] cipherByte = null;
        try {
            // 得到加密/解密器  
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            // 用指定的密钥和模式初始化Cipher对象  
            // 参数:(ENCRYPT_MODE, DECRYPT_MODE, WRAP_MODE,UNWRAP_MODE)  
            c1.init(Cipher.ENCRYPT_MODE, deskey, sr);
            // 对要加密的内容进行编码处理,  
            cipherByte = c1.doFinal(word.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回密文的十六进制形式  
        return new String(cipherByte);
    }

    /** 
     * 根据密匙进行DES解密 
     *  
     * @param word 
     *            要解密的密文 
     * @return String 返回解密后信息 
     */
    public String decrypt(SecretKey deskey, String word) {
        // 加密随机数生成器 (RNG)  
        SecureRandom sr = new SecureRandom();
        byte[] cipherByte = null;
        try {
            // 得到加密/解密器  
            Cipher c1 = Cipher.getInstance(ALGORITHM);
            // 用指定的密钥和模式初始化Cipher对象  
            c1.init(Cipher.DECRYPT_MODE, deskey, sr);
            // 对要解密的内容进行编码处理  
            cipherByte = c1.doFinal(word.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // return byte2hex(cipherByte);  
        return new String(cipherByte);
    }
}
