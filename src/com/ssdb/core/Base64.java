package com.ssdb.core;

public class Base64 {
	//BASE64编码是一种常用的将二进制数据转换为可打印字符的编码
	/*
	 * 编码，将二进制串转换成字符串
	 */
	public static String encode(byte[] binary){
		return org.apache.commons.codec.binary.Base64.encodeBase64String(binary);
	}
	
	/*
	 * 解码，将成字符串转换二进制串
	 * 
	 */
	public static byte[] decode(String str){
		return org.apache.commons.codec.binary.Base64.decodeBase64(str);
	}
	
	
	
}