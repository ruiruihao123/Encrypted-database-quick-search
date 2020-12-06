package com.ssdb.core;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;


public class BlowFish {
 
	/*  Blowfish��һ��64λ���鼰�ɱ���Կ���ȵķ��������㷨
	 * ��Կ������������������һ���ӽ�����Կ
	 * 
	 */
	
	public static Key keyGenerator() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("Blowfish");
	    keyGenerator.init(128);
	    return keyGenerator.generateKey();
	}
	
	/*
	 * ���ܺ�������������������ַ������ܳ�Ϊ�����ַ���
	 * 
	 * @param key ������Կ
	 * @param text �����ַ���
	 * @return �����ַ���
	 */
	public static String encrypt(Key key,String text) throws Exception  {
         Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
		 cipher.init(Cipher.ENCRYPT_MODE, key);
		 //ʹ��Base64���뽫�����ƴ�ת�����ַ���   ����BlowFish������������
		 return Base64.encode(cipher.doFinal(text.getBytes()));
		 
	}
	
	/*
	 * ����
	 * 
	 * @param key ������Կ
	 * @param text ����
	 * @return ����
	 */	
	public static String decrypt(Key key,String encrypedData) throws Exception  {
	    Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    return new String(cipher.doFinal(Base64.decode(encrypedData)));
	}	
}