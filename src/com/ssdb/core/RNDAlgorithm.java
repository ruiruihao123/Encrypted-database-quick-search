package com.ssdb.core;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

public class RNDAlgorithm {
	// �Զ����ƽ���Base64����
	/**
	 * DETAlgorithm�ǽ���ȷ�����ܵ�
	 * 
	 * @param content
	 *            ��Ҫ���ܵ�����
	 * @param key
	 *            ������Կ����Ҫͨ��KeyManager������
	 * @return
	 */
	public String encrypt(String content, Key key, IvParameterSpec ivspec) {
		try {

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// ����������

			// �������ַ������͵�����ת�����ֽ����ͣ���utf-8���б���

			/*
			 * ע��������䣬��������ȶ����Ľ���DET���ܣ�����һ����Base64������ַ������������ʹ��utf-8���룬�᲻��
			 * �������ڻ�δ֪��
			 */
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);// ��ʼ��
			byte[] result = cipher.doFinal(byteContent);
			return Base64.encode(result); // ����
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
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param content
	 *            ���������ݣ����ݿ��е����ݶ�����Base64�������ַ���
	 * @param key
	 *            ������Կ
	 * @return ���ؽ�����Զ����Ʊ�ʾ
	 */
	public byte[] decrypt(String content, Key key, IvParameterSpec ivspec) {
		try {
			byte[] byteContent = Base64.decode(content);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// ����������
			cipher.init(Cipher.DECRYPT_MODE, key, ivspec);// ��ʼ��
			byte[] result = cipher.doFinal(byteContent);
			return result; // ����
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
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

//	����Ϊ�����㷨��ʱ��ƽ��ʱ�䣬���Һ��Ե�һ�ε�ʱ�䣩
/* public static void main(String[] args) throws NoSuchAlgorithmException {
		long totalOfEnc = 0;
		long totalOfDec = 0;
	for(int i=0;i<10000001;i++){	
		try {
			
			String content = "12345678";
			String password = "1234567812345678";
			// ����
			// ����һ��������������ڶ�CBCģʽ���������
			//long lStart = System.currentTimeMillis();
			byte[] iv = new byte[16];
			// System.out.println(Base64.encode(iv));
			SecureRandom rand = new SecureRandom();
			rand.nextBytes(iv);
			// System.out.println(Base64.encode(iv));
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			//System.out.println("����ǰ��" + content);
			RNDAlgorithm rndAlg = new RNDAlgorithm();
			Key rndKey = KeyManager.generateDETKey(password, "id", "det");
			long lStart = System.nanoTime();
			String encryptResult = rndAlg.encrypt(content, rndKey, ivspec);
			long lendTime = System.nanoTime();
			//System.out.println("���ܺ�ʱ��" + (lendTime-lStart));
			if(i!=0){
				totalOfEnc+=(lendTime-lStart);
			}
			//System.out.println(encryptResult);
			 
			// ����
			lStart = System.nanoTime();
			byte[] decryptResult = rndAlg.decrypt(encryptResult, rndKey, ivspec);
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
