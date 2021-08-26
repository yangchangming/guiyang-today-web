package org.b3log.symphony.util;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;


/**
 * AES解密与加密程序
 * 
 * @author peng
 * 
 */
public class AES {

	// KeyGenerator 提供对称密钥生成器的功能，支持各种算法
	private KeyGenerator keygen;
	// SecretKey 负责保存对称密钥
	private SecretKey deskey;
	// Cipher负责完成加密或解密工作
	private Cipher c;
	// 该字节数组负责保存加密的结果
	private byte[] cipherByte;
	private static String CI_KEY = "iuweirnmv";
	// 对称密钥
	private String passwd = CI_KEY;
	private static AES _instance = new AES();

	public static AES getInstance() {
		return _instance;
	}

	private AES() {
		try {
			Security.addProvider(new com.sun.crypto.provider.SunJCE());
			// 实例化支持DES算法的密钥生成器(算法名称命名需按规定，否则抛出异常)
			keygen = KeyGenerator.getInstance("AES");
			// 生成密钥
			SecureRandom secure = SecureRandom.getInstance("SHA1PRNG");
			secure.setSeed(passwd.getBytes("utf-8"));
			keygen.init(128, secure);
			deskey = keygen.generateKey();

			// 生成Cipher对象,指定其支持的DES算法
			c = Cipher.getInstance("AES");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 对字符串加密
	 * 
	 * @param str
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] Encrytor(String str) {
		try {
			// 根据密钥，对Cipher对象进行初始化，ENCRYPT_MODE表示加密模式
			c.init(Cipher.ENCRYPT_MODE, deskey);
			byte[] src = str.getBytes();
			// 加密，结果保存进cipherByte
			cipherByte = c.doFinal(src);
			return cipherByte;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 对字符串解密
	 * 
	 * @param buff
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public byte[] Decryptor(byte[] buff) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，DECRYPT_MODE表示加密模式
		c.init(Cipher.DECRYPT_MODE, deskey);
		cipherByte = c.doFinal(buff);
		return cipherByte;
	}

	/**
	 * @param args
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 */
	public static void main(String[] args) throws Exception {
		AES de1 = new AES();
		String msg = "郭XX-搞笑相声全集";
		byte[] encontent = de1.Encrytor(msg);
		byte[] decontent = de1.Decryptor(encontent);
		System.out.println("明文是:" + msg);
		System.out.println("加密后:" + new String(encontent));
		System.out.println("解密后:" + new String(decontent));
	}

}
