package com.ssdb.core;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class DETAlgorithm {

	public static String encrypt(String content, Key key) {
		try {

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");  //返回实现指定转换的 Cipher对象。
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);	// 用密钥初始化此 Cipher
			byte[] result = cipher.doFinal(byteContent);  //按单部分操作加密数据
			return Base64.encode(result);  //编码，将加密后的字节数组转换成字符串
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static byte[] decrypt(String content, Key key) {
		try {
			byte[] byteContent  = Base64.decode(content);
			if (byteContent != null) {
				Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] result = cipher.doFinal(byteContent);
				return result; 
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void testDETAlgorithm() {
		try {
		String content = "12345678";
		String password = "1234567812345678";
		// 
		Key detKey = KeyManager.generateDETKey(password, "id", "det");
		//byte[] contentByte = content.getBytes("utf-8");
		//long startTime = System.nanoTime();
		String encryptResult = DETAlgorithm.encrypt(content, detKey);
		//byte[] encryptResult = DETAlgorithm.encryptByte(contentByte, detKey);
		//long endTime = System.nanoTime();
		//System.out.println("DET encrypt Time : "+ (endTime-startTime));
		//System.out.println(encryptResult);
		/*
		 * SecureRandom sr = new SecureRandom(password.getBytes()); byte[] b =
		 * new byte[16]; sr.nextBytes(b);
		 * System.out.println(Base64_2.encode(b));
		 */
		// 
		// long startDecTime = System.nanoTime();
		byte[] decryptResult = DETAlgorithm.decrypt(encryptResult,detKey);
		// long endDecTime = System.nanoTime();
		// System.out.println("DET DecryptTime : " + (endDecTime-startDecTime));
		// System.out.println("���ܺ�" + new String(decryptResult));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}

/*  public static void main(String[] args) throws NoSuchAlgorithmException {
		long totalOfEnc = 0;
		long totalOfDec = 0;
	for(int i=0;i<10000001;i++){	
		try {
			
			String content = "12345678";
			String password = "1234567812345678";
			Key detKey = KeyManager.generateDETKey(password, "id", "det");
			long lStart = System.nanoTime();
			String encryptResult = encrypt(content, detKey);
			long lendTime = System.nanoTime();
			//System.out.println("���ܺ�ʱ��" + (lendTime-lStart));
			if(i!=0){
				totalOfEnc+=(lendTime-lStart);
			}
			//System.out.println(encryptResult);

			 
	
			lStart = System.nanoTime();
			byte[] decryptResult = decrypt(encryptResult, detKey);
			lendTime = System.nanoTime();
			if(i!=0){
				totalOfDec+=(lendTime-lStart);
			}
			//System.out.println("���ܺ�ʱ��" + (lendTime-lStart) );
			//System.out.println("���ܺ�" + new String(decryptResult));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	System.out.println("Enc Time = " + (totalOfEnc/10000000));
	System.out.println("Dec Time = " + (totalOfDec/10000000));
	}*/
}
