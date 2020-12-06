package com.ssdb.demo;

import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextPane;

import com.ssdb.core.ConnectionMySQL;
import com.ssdb.deparser.CreateTableDeparserV2;
import com.ssdb.deparser.DeleteDeparserV2;
import com.ssdb.deparser.InsertDeparserV2;
import com.ssdb.deparser.SelectDemoV2;
import com.ssdb.deparser.UpdateDeparserV2;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

public class ClientDemo {
	
	/*
	 * 这个变量用于收集语句中的DET类型的列名，用于处理RND层时使用
	 * 因为我们只对DET类型的列增加RND层，而在一条语句改写后含有
	 */
	public static List<String> tableNameList = new ArrayList<String>();
	public static List<String> encColumnNameList = new ArrayList<String>();

	
	
 	public static void RecvToParseSql(String inputSQL,JTextPane showArea,StringBuilder stringBuilder){
 			
 			Connection conn = ConnectionMySQL.openConnection();
			
			try {					
				
				CCJSqlParserManager parserManager = new CCJSqlParserManager();
				Statement statement = parserManager.parse(new StringReader(inputSQL));
				
				if (statement instanceof CreateTable) {
					long start = System.currentTimeMillis(); 
					CreateTable createTable = (CreateTable) statement;
					CreateTableDeparserV2.handler(createTable,conn,showArea,stringBuilder);
					long end = System.currentTimeMillis();
					stringBuilder.append("操作完成，本次创建表操作耗时:"+(end-start)+"ms\n");
					showArea.setText(stringBuilder.toString());
				} else {
					
					if (statement instanceof Insert) {
						long startInsert = System.currentTimeMillis(); 
						Insert insert = (Insert) statement;
						InsertDeparserV2.handler(insert, conn,showArea,stringBuilder);
						long endInsert = System.currentTimeMillis();
						stringBuilder.append("操作完成，本次插入操作耗时：:"+(endInsert-startInsert)+"ms\n");
						showArea.setText(stringBuilder.toString());
					} else {
						
						if (statement instanceof Select) {
							long startSelect = System.currentTimeMillis();
							Select select = (Select) statement;
							SelectDemoV2.handler(select,conn,showArea,stringBuilder);
							long endSelect = System.currentTimeMillis();
							stringBuilder.append("操作完成，本次查询操作耗时：:"+(endSelect-startSelect)+"ms\n");
							showArea.setText(stringBuilder.toString());
							/*
							 * 
							 * 下面这段代码用于将一条select语句执行多次得到平均值，忽略第一次的取值ֵ
							 * long total = 0;
							for(int i = 0;i < 11;i++){	
								tableNameList.clear();
								encColumnNameList.clear();
								long start = System.currentTimeMillis(); 
								Select select = (Select) statement;
								SelectDemoV2.handler(select,conn);
								long end = System.currentTimeMillis();
								Thread.sleep(1000);
								if(i!=0){
								total += (end - start);
								}
							}
							System.out.println("Average:"+total/10);*/
						} else {
							
							if (statement instanceof Delete) {
								long startDelete = System.currentTimeMillis();
								Delete delete = (Delete) statement;
								DeleteDeparserV2.handler(delete,conn);
								long endDelete = System.currentTimeMillis();
								stringBuilder.append("操作完成，本次删除操作耗时:"+(endDelete-startDelete)+"ms\n");
								showArea.setText(stringBuilder.toString());
							} else {
								
								if (statement instanceof Update) {
									long startUpdate = System.currentTimeMillis();
									Update update = (Update) statement;
									UpdateDeparserV2.handler(update,conn,showArea,stringBuilder);
									long endUpdate = System.currentTimeMillis();
									stringBuilder.append("操作完成，本次更新操作耗时:"+(endUpdate-startUpdate)+"ms\n");
									showArea.setText(stringBuilder.toString());
								} else {
									stringBuilder.append("不支持的语句类型"+"\n");
									showArea.setText(stringBuilder.toString());
									return;
								}
							}
						}
					}
				}
				//在执行完一条语句后，需要将静态列表清空
				tableNameList.clear();
				encColumnNameList.clear();

			} catch (JSQLParserException e) {
				showArea.setText("您输入的sql语句有错误，请检查！");
				System.out.println("您输入的sql语句有错误，请检查！");
			}
 		
 	}
 	
 	
 	
 	
 	
		/*Connection conn = ConnectionMySQL.openConnection();
		try {
			 
			conn.setAutoCommit(false);
			List<String> columnNameList = new ArrayList<String>();
			columnNameList.add("name");
			//columnNameList.add("name_OPE");
			java.sql.Statement stmt = conn.createStatement();
			
			String udfSQL = RNDOnion.packOnRND("peach",columnNameList,"123456");
			System.out.println(udfSQL);
			stmt.executeUpdate(udfSQL);
			//udfSQL = peelOffRND("peach",columnNameList,"123456");
			//System.out.println(udfSQL);
			//stmt.executeUpdate(udfSQL);
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	*/
	//
	
	
			
/* 			Connection conn = ConnectionMySQL.openConnection();
			CCJSqlParserManager parserManager = new CCJSqlParserManager(); 
			long start = System.currentTimeMillis(); 
			try { 
			for(int i = 0 ; i < 10 ; i++){
				
					String inputSQL = "insert into grade(id,grade) values("+i+","+90+");"; 
					Statement statement = parserManager.parse(new StringReader(inputSQL)); 
					Insert insert = (Insert)statement;
					InsertDeparserV2.handler(insert,conn); 					 
			}
			} catch (JSQLParserException e) {
					// TODO Auto-generated catch block e.printStackTrace(); 
				}	
			long end = System.currentTimeMillis();
			System.out.println(end-start);
	}*/

	//����100���������ݲ���
/*	Connection conn = ConnectionMySQL.openConnection();
	try {
		java.sql.Statement statement = conn.createStatement();
		long start = System.currentTimeMillis(); 
		for(int i = 0 ; i < 100 ; i++){
			String inputSQL = "insert into temp(id,name) values("+i+",'wang'"+");";
			System.out.println(inputSQL);
			statement.executeUpdate(inputSQL);
		} 
		long end = System.currentTimeMillis();
		System.out.println(end-start);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	

}*/
	
 	
 	/*
 	 * 
 	 * create table student(id int,name varchar(20),age int,sex char(5));
 	 * create table grade(id int,grade double);
 	 * 
 	 * 
 	 * 
 	 * insert into student(id,name,age,sex) values(1,'chandler',20,'male');
 	 * insert into student(id,name,age,sex) values(2,'joe',22,'male');
 	 * insert into student(id,name,age,sex) values(3,'monica',19,'female');
 	 * insert into student(id,name,age,sex) values(4,'ross',20,'male');
 	 * insert into student(id,name,age,sex) values(5,'phebe',20,'female');
 	 * insert into student(id,name,age,sex) values(6,'rachel',20,'female');
 	 * 
 	 * insert into grade(id,grade) values(1,95);
 	 * insert into grade(id,grade) values(2,86);
 	 * insert into grade(id,grade) values(3,72);
 	 * insert into grade(id,grade) values(4,97);
 	 * insert into grade(id,grade) values(5,95);
 	 * insert into grade(id,grade) values(6,70);
 	 * 
 	 *
 	 * select id,name,age,sex from student;
 	 * select id from student where name = 'chandler';
 	 * 
 	 *
 	 * select name from student where age >= 20;
 	 * 
 	 * select id,grade from grade;
 	 * select id from grade where grade > 90;
 	 * select avg(grade) from grade;
 	 * 
 	 * select student.id from student inner join grade on student.id = grade.id where grade.grade >= 80;
 	 * 
 	 * 
 	 * select grade.grade from student inner join grade on student.id = grade.id where student.name = 'chandler';
 	 * 
 	 * 
 	 * select avg(grade.grade) from grade;
 	 * select avg(grade.grade) from student inner join grade on student.id = grade.id where grade.grade > 80;
 	 * 
 	 * 
 	 * update grade set grade = 100 where id = 1;
 	 * 
 	 *
 	 * delete from grade where grade < 80;
 	 * delete from grade where grade > 80;
 	 */
}
