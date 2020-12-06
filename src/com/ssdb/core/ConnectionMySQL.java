package com.ssdb.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySQL数据库连接模块
 */
public class ConnectionMySQL {

	static String driverClass = null;
	static String url = null;
	static String name = null;
	static String password= null;
	
	static{
		try {
			//1. 创建一个属性配置对象
			Properties properties = new Properties();
			InputStream is = new FileInputStream("data.properties");
			
			//使用类加载器，去读取src底下的资源文件。 
			//InputStream is = JDBCUtil.class.getClassLoader().getResourceAsStream("jdbc.properties");
			//导入输入流。
			properties.load(is);
			
			//读取属性
			driverClass = properties.getProperty("driverClass");
			url = properties.getProperty("url");
			name = properties.getProperty("user");
			password = properties.getProperty("password");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**数据库连接函数
	 * @return 返回一个数据库的连接
	 * */
	public static Connection openConnection(){
		Connection conn = null;
		try {
			Class.forName(driverClass);
			//静态代码块 ---> 类加载了，就执行。 java.sql.DriverManager.registerDriver(new Driver());
			//DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			//DriverManager.getConnection("jdbc:mysql://localhost/test?user=monty&password=greatsqldb");
			//2. 建立连接 参数一： 协议 + 访问的数据库 ， 参数二： 用户名 ， 参数三： 密码。
			conn = DriverManager.getConnection(url, name, password);
			return conn;
		} catch (Exception e) {
			System.out.println("数据库连接失败");
			e.printStackTrace();
		}
		return null;
	}
	
	
}
