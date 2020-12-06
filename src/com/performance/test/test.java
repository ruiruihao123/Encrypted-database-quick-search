package com.performance.test;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int num = 12345;
		int count = 0;
		while(num > 0){
			count++;
			num &= (num-1);
			
		}
		System.out.println(count);
	}

}
