package com.ssdb.core;

import java.text.DecimalFormat;


public class AddHomAlgorithm {
	private int n = 0;
	
	private double[][] key = new double[5][3];
	/*
	 * 计算密钥k的产生函数,我们需要的密钥形式为[(k1,s1,t1),(k2,s2,t2),(k3,s3,t3)...(kn,sn,tn)]
	 * @param n 密钥的个数，等于secret share的个数，考虑到安全性和效率，暂时默认n为5(n>=3)，按照论文中的理论，n越大，安全性越高。
	 * @return 包含所有计算密钥的double二维数组。
	 */

	
	public AddHomAlgorithm(double[][] key,int n){
		this.key = key;
		this.n = n;
	}
	
	
	/*
	 * 这个函数用于向密文分组添加噪声
	 * @param key 密钥
	 * @param ri 随机数，这个随机数不能直接用于密文分组，为了保证同态性，需要对这个随机数进行处理。
	 * @return 处理后的噪声
	 */
	/*public double[] noisei(double[] key,double[] ri){
		return null;
	}*/


	public double[] encrypt(double plainValue){
		//数组secretValue[i]用于保存第i个密文分片
		double[] secretValue = new double[n];

//----------------产生n-2个随机噪声-------------------------
		double[] randomNum = new double[n-2];
		double sum_randomNum = 0.0;
		double sens = 0.01;
		for(int i = 0; i < n-2 ; i++){
			//为了保证第一个分片具有保序特性，我们需要限定第一个随机数r的范围：(-t1*sens,t1*sens],默认sens=0.01
			if(i == 0){
				randomNum[i] = (Math.random()*2-1.0)*key[0][2]*sens; 
			}else{
				randomNum[i] = Math.random()*20.0-10.0; 
			}
			sum_randomNum += randomNum[i];
		}
		
//----------------求解每个secretValue[i]-------------------
		//对于1~n-2的secretValue[i]的求解		
		for(int i = 0; i < n-2 ; i++){
			secretValue[i] = key[i][0] * key[i][2] * plainValue + key[i][1] + key[i][0] * randomNum[i];
		}
		secretValue[n-2] = key[n-2][0] * key[n-2][2] * sum_randomNum +key[n-2][1];
		secretValue[n-1] = key[n-1][0]+key[n-1][1]+key[n-1][2];
		return secretValue;    //存储着n个分片的值

	}
	

	public double decrypt(double[] secretValue){
//-------------求出解密函数中的L参数的值:k1+k2......k(n-1)，注意是n-1，不是n------------------
		double L = 0.0;
		for(int i = 0;i < n-2; i++){
			L += key[i][2];
		}
//-------------求出S---------------------
		double S = 0;
		S = secretValue[n-1] / (key[n-1][0]+key[n-1][1]+key[n-1][2]);
	
//------------求出I----------------------
		double I = 0.0;
		I = secretValue[n-2] - S *key[n-2][1];
		
//-----------根据以上求解出来的参数，恢复明文--------------
		double plainValue = 0.0;
		for(int i = 0; i < n-2 ; i++){
			plainValue += (secretValue[i] - S * key[i][1]) / (L * key[i][0]); 
		}
		plainValue = plainValue - I / (L * key[n-2][0] * key[n-2][2]);
		return plainValue;		
}
/*	public void testAddHomeAlgorithm(){
		double[][] key = new double[5][3];
		for(int i = 0;i < 5;i++){
			for(int j = 0;j < 3; j++){
				key[i][j]=1;
			}
		}		
		AddHomAlgorithm add  = new AddHomAlgorithm(key, 5);
		double[] result = add.encrypt(10.00);	
		add.decrypt(result);
		System.out.println("同态加密测试完成");
	}*/
	public static void main(String[] args){
		double[][] key = KeyManager.generateHomKey();		
		AddHomAlgorithm add  = new AddHomAlgorithm(key, 5);
		double[] c1 = add.encrypt(2.00);
		double[] c2 = add.encrypt(3.10);
		double[] sum = new double[5];
		System.out.println("加密c1的密文:");
		for(double tmp : c1){
			System.out.print(tmp+"\t");
		}
		System.out.println();
		System.out.println("加密c2的密文:");
		for(double tmp : c2){
			System.out.print(tmp+"\t");
		}
		System.out.println();
		System.out.println("c1+c2的密文和:");
		for(int i = 0; i < 5; i++){
			sum[i] = c1[i] + c2[i];
			System.out.print(sum[i]+"\t");
		}
		System.out.println();
		
		System.out.println("c1+c2的密文乘积:");
		double[][] multiply = new double[5][5];		
		for(int i = 0; i < 5; i++){
			for(int j = 0;j < 5; j++){
				multiply[i][j] = c1[i]*c2[j];
				if(j == 0){
					System.out.print("["+multiply[i][j]+"=====");
				}else{
					if(j == 4){
						System.out.println(multiply[i][j]+"]");
					}else{
						System.out.print(multiply[i][j]+"=====");
					}
				}
			}
		}
		
		System.out.println();
		System.out.println("判断大小");
		if(c1[0]>c2[0]){
			System.out.println("c1 > c2");
		}else{
			if(c1[0]<c2[0]){
				System.out.println("c1 < c2");
			}
		}
		double r1 = add.decrypt(c1);
		double r2 = add.decrypt(c2);
		double r_sum = add.decrypt(sum);
		double[] r_multiOfRow = new double[5];		
		for(int k = 0;k < 5;k++){
			r_multiOfRow[k] = add.decrypt(multiply[k]);
		}
		double r_muti = add.decrypt(r_multiOfRow);
		DecimalFormat format = new DecimalFormat("#.00");
		System.out.println("解密后:"+format.format(r1)+","+format.format(r2)+",相加结果："+format.format(r_sum)+",相乘结果："+format.format(r_muti));		
	}
	
	//	以下为计算算法耗时（平均时间，并且忽略第一次的时间）
/*	 public static void main(String[] args){
			long totalOfEnc = 0;
			long totalOfDec = 0;
			double[][] key = new double[5][3];
			for(int i = 0;i < 5;i++){
				for(int j = 0;j < 3; j++){
					key[i][j]=1;
				}
			}
			AddHomAlgorithm add  = new AddHomAlgorithm(key, 5);
		for(int i=0;i<10000001;i++){					
				long lStart = System.nanoTime();
				double[] result = add.encrypt(10);	
				long lendTime = System.nanoTime();
				//System.out.println("加密耗时：" + (lendTime-lStart));
				if(i!=0){
					totalOfEnc+=(lendTime-lStart);
				}
				//System.out.println(encryptResult);

				 
				// 解密
				lStart = System.nanoTime();
				double plain = add.decrypt(result);
				lendTime = System.nanoTime();
				if(i!=0){
					totalOfDec+=(lendTime-lStart);
				}
				//System.out.println("解密耗时：" + (lendTime-lStart) );
				//System.out.println("解密后：" + new String(decryptResult));
			
		}
		System.out.println("Enc Time = " + (totalOfEnc/10000000));
		System.out.println("Dec Time = " + (totalOfDec/10000000));
		}*/
}
