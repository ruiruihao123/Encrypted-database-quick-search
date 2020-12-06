package com.ssdb.core;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


public class OPEAlgorithm {
	private double a = 0.0;
	private double b = 0.0;
	private double sens = 0.0;
	//private double noise = 0.0;

	public OPEAlgorithm(double a, double b, double sens) {
		this.a = a;
		this.b = b;
		this.sens = sens;
	}

	private double function(double value) {
		return Math.abs(value);
	}

	private double noise(double value) {   //制造随即噪声
		Random random = new Random();
		return random.nextDouble() * (a * function(value + sens) * (value + sens) - a * function(value) * value);
	}

	public double nindex(double value, boolean isPutNoise) {
		if (isPutNoise == true) {
			return a * function(value) * value + b + noise(value);
		} else {
			return a * function(value) * value + b;
		}
	}
	
	public static void testOPEAlgorithm(){
		OPEAlgorithm opeAlg = new OPEAlgorithm(1,2,1);
		double d = opeAlg.nindex(10, true);
		System.out.println("保序加密模块测试完成:-->"+d);
	}
	
	//	以下为计算算法耗时（平均时间，并且忽略第一次的时间）
	 public static void main(String[] args) throws NoSuchAlgorithmException {
			/*long totalOfEnc = 0;
			OPEAlgorithm ope = new OPEAlgorithm(2.0,3.0,1);
		for(long i=0;i<10000001L;i++){	
			long lStart = System.nanoTime();
			double result = ope.nindex(100.1, true);
			long lendTime = System.nanoTime();
			if(i!=0){
				totalOfEnc+=(lendTime-lStart);
			}
		}
		System.out.println("Enc Time = " + (totalOfEnc/10000000L));
		}*/
	
		 OPEAlgorithm.testOPEAlgorithm();
		 
		 
		 
	 }
}
