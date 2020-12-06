package com.performance.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class CryptDBTest {

	/**���ݿ����Ա�û���*/
	//private static final String USERNAME = "ssdb";
	private static final String USERNAME = "root";
	/**����Ա����*/
	private static final String PASSWORD = "letmein";
	/**���ݿ����ӵ�URL*/
	//private static final String DB_URL = "jdbc:mysql://192.168.199.192:3306/college";
	private static final String DB_URL = "jdbc:mysql://192.168.199.93:3307/college";
	/**���ݿ����Ӻ���
	 * @return ����һ�����ݿ������
	 * */
	public Connection openConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("���ݿ�����ʧ��");
		return null;
	}
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		CryptDBTest ct = new CryptDBTest();
		Connection conn = ct.openConnection();
		Statement st = conn.createStatement();
		
		
		Scanner sc =null;
		Scanner tbc = null;
		System.out.println("Please Input SQL Statement:");
		sc = new Scanner(System.in);
		String sqlStatement = sc.nextLine();		
		while(true){						
			long startTime = System.currentTimeMillis();
			boolean endMark = st.execute(sqlStatement);
			long endTime = System.currentTimeMillis();
			System.out.println(endMark+"----CryptDBִ��ʱ��Ϊ:"+(endTime-startTime));
			System.out.println("To Be Continued? ---'exit' for exit from this program,if not,please input another statement---");			
			tbc = new Scanner(System.in);
			sqlStatement = tbc.nextLine();	
			if(sqlStatement.equals("exit")){
				System.out.println("ByeBye!");
				break;
			}
		}
		sc.close();
		tbc.close();
		//Test create SQL statement
		/*SQLGenerator sg = new SQLGenerator();
		String createStudentTest = sg.sqlCreateStudent();
		long startCreateStudent = System.currentTimeMillis();
		boolean endMark1 = st.execute(createStudentTest);
		long endCreateStudent = System.currentTimeMillis();
		System.out.println(endMark1+"----CryptDBִ��createStudentʱ��Ϊ:"+(endCreateStudent-startCreateStudent));
		
		String createGradeTest = sg.sqlCreateGradeCryptDB();
		long startCreateGrade = System.currentTimeMillis();
		boolean endMark2 = st.execute(createGradeTest);
		long endCreateGrade = System.currentTimeMillis();
		System.out.println(endMark2+"----CryptDBִ��createGradeʱ��Ϊ:"+(endCreateGrade-startCreateGrade));
		
		String insertStudentTest = sg.sqlInsertToStudent(1000);
		long startInsertStudent = System.currentTimeMillis();
		boolean endMark3 = st.execute(insertStudentTest);
		long endInsertStudent = System.currentTimeMillis();
		System.out.println(endMark3+"----CryptDBִ��insertStudent(1000)ʱ��Ϊ:"+(endInsertStudent-startInsertStudent));
		
		String insertGradeTest = sg.sqlInsertToGrade(1000);
		long startInsertGrade = System.currentTimeMillis();
		boolean endMark4 = st.execute(insertGradeTest);
		long endInsertGrade = System.currentTimeMillis();
		System.out.println(endMark4+"----CryptDBִ��insertGrade(1000)ʱ��Ϊ:"+(endInsertGrade-startInsertGrade));*/
		
	}

}
