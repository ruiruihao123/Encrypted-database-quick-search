package com.performance.test;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ssdb.core.ConnectionMySQL;
import com.ssdb.deparser.CreateTableDeparserV2;
import com.ssdb.deparser.InsertDeparserV2;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;

public class SSDBTest {

	public static void main(String[] args) throws SQLException, JSQLParserException {
		// TODO Auto-generated method stub
		Connection conn = ConnectionMySQL.openConnection();
		//java.sql.Statement stmt = conn.createStatement();
		ResultSet rs = conn.getMetaData().getTables(null, null, "metadata", null);
		if(!rs.next()){
			java.sql.Statement createMetadataTable = conn.createStatement();
			createMetadataTable.execute("create table metadata(tablename varchar(50),columnname varchar(200),datatype varchar(100),opekey text,homkey text,primary key(tablename,columnname));");	
		}
		
//		SQLGenerator sg =new SQLGenerator();
//		String inputSQL = sg.sqlCreateStudent();
//		CCJSqlParserManager parserManager = new CCJSqlParserManager();
//		Statement statement = parserManager.parse(new StringReader(inputSQL));
//		long start = 0;
//		long end = 0;
//		start = System.currentTimeMillis(); 
//		CreateTable createStudent = (CreateTable) statement;
//		CreateTableDeparserV2.handler(createStudent,conn);
//		end = System.currentTimeMillis();
//		System.out.println("CreateStudentInSSDBTime:"+(end-start));
//		
//		inputSQL = sg.sqlCreateGradeSSDB();
//		start = System.currentTimeMillis(); 
//		CreateTable createGrade = (CreateTable) statement;
//		CreateTableDeparserV2.handler(createGrade,conn);
//		end = System.currentTimeMillis();
//		System.out.println("CreateGradeInSSDBTime:"+(end-start));
//		
//		inputSQL = sg.sqlInsertToStudent(1000);
//		start = System.currentTimeMillis(); 
//		Insert insert = (Insert) statement;
//		InsertDeparserV2.handler(insert, conn);
//		end = System.currentTimeMillis();
//		System.out.println("InsertStudentTime:"+(end-start));
//		
//		insert = (Insert) statement;
//		start = System.currentTimeMillis(); 		
//		InsertDeparserV2.handler(insert, conn);
//		end = System.currentTimeMillis();
//		System.out.println("InsertGradeTime:"+(end-start));
	}

}
