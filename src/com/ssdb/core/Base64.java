package com.ssdb.core;

public class Base64 {
	//BASE64������һ�ֳ��õĽ�����������ת��Ϊ�ɴ�ӡ�ַ��ı���
	/*
	 * ���룬�������ƴ�ת�����ַ���
	 */
	public static String encode(byte[] binary){
		return org.apache.commons.codec.binary.Base64.encodeBase64String(binary);
	}
	
	/*
	 * ���룬�����ַ���ת�������ƴ�
	 * 
	 */
	public static byte[] decode(String str){
		return org.apache.commons.codec.binary.Base64.decodeBase64(str);
	}
	
	
	
}