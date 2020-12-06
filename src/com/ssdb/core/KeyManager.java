package com.ssdb.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.SecretKeySpec;


/*
 * ���������������Կ��������Կ�������ļ���.
 */
public class KeyManager {

	/*
	 * ����������ڻ�ȡBlowFish�㷨��Ҫ����Կ
	 * 1.���ȼ����Կ�ļ��Ƿ���ڣ����������������һ������Կ����ʹ�ö������л��ķ�ʽ������key.dat�ļ��У�
	 * 2.�����Կ�ļ�key.dat�ļ��Ѿ����ڣ���ֱ�Ӷ�ȡ�ļ��е���Կ����.

	 */
	public static Key blowfishKey() throws Exception{
		
		File f = new File("key.dat");
		//�����Կ�ļ�������˵����û����Կ����Ҫ����һ����Կ
		if(!f.exists()){
			Key key = BlowFish.keyGenerator();
			FileOutputStream file = new FileOutputStream("key.dat");
			//�Զ������л��ķ�ʽ�洢��key.dat��
			ObjectOutputStream objOutput = new ObjectOutputStream(file);
			objOutput.writeObject(key);
			objOutput.close();
			return key;
		}else{
			//����ļ����ڣ����ʾ�Ѿ�����Կ������Ҫ��ȡ�����Կ
			FileInputStream fileIn = new FileInputStream("key.dat");
			//��ȡkey.dat�е���Կ���������л�������װΪ����key��
			ObjectInputStream objInput = new ObjectInputStream(fileIn);
			Key key = (Key)objInput.readObject();
			objInput.close();
			return key;
		}

	}
	
	//������ֵ������Կ   password   onionType  ����û��
	public static Key generateDETKey(String password,String columnName,String onionType) throws NoSuchAlgorithmException{
    	byte[] colNameBytes=columnName.getBytes();
    	//�����ɵ�16λbytes���飬��ʼΪmasterKey��bytes��֮�����ڴ洢������key
    	byte[] keyBytes=password.getBytes();
    	//�������ɶ�����16λ��byte����
    	int row=(int) Math.ceil((double)colNameBytes.length/16);
    	byte[][] bytesArray=new byte[row][16];
    	//�������byte��ά����
    	for(int i=0,k=0;i<row;i++){
    		for(int j=0;j<16;j++){
    			//��colName���Ȳ��㣬���ͷ��ʼ���
    			if(k==colNameBytes.length){
    				k=0;
    			}
    			bytesArray[i][j]=colNameBytes[k++];
    			}
    		}
    	//������
    	for(int i=0;i<row;i++){
    		for(int j=0;j<16;j++){
    			keyBytes[j]=(byte)(keyBytes[j]^bytesArray[i][j]);
    		}
    	}
    	SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
    	return key;
    	

	}
	
	
	//�������������Կ
	public static double[] generateOpeKey(double sens){
		double[] opeKey = new double[3];
		opeKey[0] = Math.random()*1000;
		opeKey[1] = Math.random()*1000;
		//opeKey�ĵ���������������sens
		opeKey[2] = sens;
		return opeKey;
		
	}
	
	
	//����̬ͬ������Կ
	public static double[][] generateHomKey(){
		double[][] homKey = new double[5][3];
		for(int i = 0 ; i < 5-1 ; i++ ){
			//����k1...kn-1,��-100��+100�в����������
			if(i == 0){
				//����ǵ�һ��k��Կ�����������
				homKey[i][0] = Math.random()*20;
			}else{
				homKey[i][0] = Math.random()*20-10;
			}
			
			//����si:s1+...s(n-2) != 0,s(n-1) !=0�����ǲ�ȡһ���򵥵Ľ���취����sʼ��Ϊ����
			homKey[i][1] = Math.random()*10;
			
			//����t��Լ������ֻ��һ����kn+sn+tn != 0,��t1...t(n-1)û��Ҫ��,���������ݶ�t�ķ�Χ�ǣ�(200~500)֮��������
			homKey[i][2] = Math.random();
		}
		//ע�⣺kn+sn+tn != 0
		homKey[4][0] = Math.random()*20-10;
		homKey[4][1] = Math.random()*10;
		//Ϊ������kn+sn+tn != 0��������������tn����������������õ�,������kn+sn+tn = 1000�����ǻ�������kn+sn+tn = randomNumber
		homKey[4][2] = 10.0-homKey[4][0]-homKey[4][1];
		//System.out.println("��ʹ��KeyManager�е�generateHomKey()����������Կ���飡");
		return homKey;		
	}
	
	//����
	public void testKeyManager() throws NoSuchAlgorithmException{
		generateDETKey("12345678", "xing", "det");
		generateHomKey();
		generateOpeKey(1);
		System.out.println("��Կ����ģ��������");		
	}
}
