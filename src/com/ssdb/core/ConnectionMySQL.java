package com.ssdb.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySQL���ݿ�����ģ��
 */
public class ConnectionMySQL {

	static String driverClass = null;
	static String url = null;
	static String name = null;
	static String password= null;
	
	static{
		try {
			//1. ����һ���������ö���
			Properties properties = new Properties();
			InputStream is = new FileInputStream("data.properties");
			
			//ʹ�����������ȥ��ȡsrc���µ���Դ�ļ��� 
			//InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
			//������������
			properties.load(is);
			
			//��ȡ����
			driverClass = properties.getProperty("driverClass");
			url = properties.getProperty("url");
			name = properties.getProperty("user");
			password = properties.getProperty("password");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**���ݿ����Ӻ���
	 * @return ����һ�����ݿ������
	 * */
	public static Connection openConnection(){
		Connection conn = null;
		try {
			Class.forName(driverClass);
			//��̬����� ---> ������ˣ���ִ�С� java.sql.DriverManager.registerDriver(new Driver());
			//DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			//DriverManager.getConnection("jdbc:mysql://localhost/test?user=monty&password=greatsqldb");
			//2. �������� ����һ�� Э�� + ���ʵ����ݿ� �� �������� �û��� �� �������� ���롣
			conn = DriverManager.getConnection(url, name, password);
			return conn;
		} catch (Exception e) {
			System.out.println("���ݿ�����ʧ��");
			e.printStackTrace();
		}
		return null;
	}
	
	
}
