package com.ssdb.core;

import java.text.DecimalFormat;


public class AddHomAlgorithm {
	private int n = 0;
	
	private double[][] key = new double[5][3];
	/*
	 * ������Կk�Ĳ�������,������Ҫ����Կ��ʽΪ[(k1,s1,t1),(k2,s2,t2),(k3,s3,t3)...(kn,sn,tn)]
	 * @param n ��Կ�ĸ���������secret share�ĸ��������ǵ���ȫ�Ժ�Ч�ʣ���ʱĬ��nΪ5(n>=3)�����������е����ۣ�nԽ�󣬰�ȫ��Խ�ߡ�
	 * @return �������м�����Կ��double��ά���顣
	 */

	
	public AddHomAlgorithm(double[][] key,int n){
		this.key = key;
		this.n = n;
	}
	
	
	/*
	 * ����������������ķ����������
	 * @param key ��Կ
	 * @param ri �������������������ֱ���������ķ��飬Ϊ�˱�֤̬ͬ�ԣ���Ҫ�������������д���
	 * @return ����������
	 */
	/*public double[] noisei(double[] key,double[] ri){
		return null;
	}*/


	public double[] encrypt(double plainValue){
		//����secretValue[i]���ڱ����i�����ķ�Ƭ
		double[] secretValue = new double[n];

//----------------����n-2���������-------------------------
		double[] randomNum = new double[n-2];
		double sum_randomNum = 0.0;
		double sens = 0.01;
		for(int i = 0; i < n-2 ; i++){
			//Ϊ�˱�֤��һ����Ƭ���б������ԣ�������Ҫ�޶���һ�������r�ķ�Χ��(-t1*sens,t1*sens],Ĭ��sens=0.01
			if(i == 0){
				randomNum[i] = (Math.random()*2-1.0)*key[0][2]*sens; 
			}else{
				randomNum[i] = Math.random()*20.0-10.0; 
			}
			sum_randomNum += randomNum[i];
		}
		
//----------------���ÿ��secretValue[i]-------------------
		//����1~n-2��secretValue[i]�����		
		for(int i = 0; i < n-2 ; i++){
			secretValue[i] = key[i][0] * key[i][2] * plainValue + key[i][1] + key[i][0] * randomNum[i];
		}
		secretValue[n-2] = key[n-2][0] * key[n-2][2] * sum_randomNum +key[n-2][1];
		secretValue[n-1] = key[n-1][0]+key[n-1][1]+key[n-1][2];
		return secretValue;    //�洢��n����Ƭ��ֵ

	}
	

	public double decrypt(double[] secretValue){
//-------------������ܺ����е�L������ֵ:k1+k2......k(n-1)��ע����n-1������n------------------
		double L = 0.0;
		for(int i = 0;i < n-2; i++){
			L += key[i][2];
		}
//-------------���S---------------------
		double S = 0;
		S = secretValue[n-1] / (key[n-1][0]+key[n-1][1]+key[n-1][2]);
	
//------------���I----------------------
		double I = 0.0;
		I = secretValue[n-2] - S *key[n-2][1];
		
//-----------���������������Ĳ������ָ�����--------------
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
		System.out.println("̬ͬ���ܲ������");
	}*/
	public static void main(String[] args){
		double[][] key = KeyManager.generateHomKey();		
		AddHomAlgorithm add  = new AddHomAlgorithm(key, 5);
		double[] c1 = add.encrypt(2.00);
		double[] c2 = add.encrypt(3.10);
		double[] sum = new double[5];
		System.out.println("����c1������:");
		for(double tmp : c1){
			System.out.print(tmp+"\t");
		}
		System.out.println();
		System.out.println("����c2������:");
		for(double tmp : c2){
			System.out.print(tmp+"\t");
		}
		System.out.println();
		System.out.println("c1+c2�����ĺ�:");
		for(int i = 0; i < 5; i++){
			sum[i] = c1[i] + c2[i];
			System.out.print(sum[i]+"\t");
		}
		System.out.println();
		
		System.out.println("c1+c2�����ĳ˻�:");
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
		System.out.println("�жϴ�С");
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
		System.out.println("���ܺ�:"+format.format(r1)+","+format.format(r2)+",��ӽ����"+format.format(r_sum)+",��˽����"+format.format(r_muti));		
	}
	
	//	����Ϊ�����㷨��ʱ��ƽ��ʱ�䣬���Һ��Ե�һ�ε�ʱ�䣩
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
				//System.out.println("���ܺ�ʱ��" + (lendTime-lStart));
				if(i!=0){
					totalOfEnc+=(lendTime-lStart);
				}
				//System.out.println(encryptResult);

				 
				// ����
				lStart = System.nanoTime();
				double plain = add.decrypt(result);
				lendTime = System.nanoTime();
				if(i!=0){
					totalOfDec+=(lendTime-lStart);
				}
				//System.out.println("���ܺ�ʱ��" + (lendTime-lStart) );
				//System.out.println("���ܺ�" + new String(decryptResult));
			
		}
		System.out.println("Enc Time = " + (totalOfEnc/10000000));
		System.out.println("Dec Time = " + (totalOfDec/10000000));
		}*/
}
