package com.performance.test;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.ssdb.core.ConnectionMySQL;
import com.ssdb.demo.ClientDemo;
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

public class PreProcessTest {

	/**
	 * 这个变量用于收集语句中的DET类型的列名，用于处理RND层时使用 因为我们只对DET类型的列增加RND层，而在一条语句改写后含有
	 */
	//public static List<String> tableNameList = new ArrayList<String>();
	//public static List<String> encColumnNameList = new ArrayList<String>();

	public void testSSDB(JTextPane showArea,StringBuilder stringBuilder) {
		try {
			Connection conn = ConnectionMySQL.openConnection();
			ResultSet rs = conn.getMetaData().getTables(null, null, "metadata", null);
			stringBuilder.append("请等待测试完成"+"\n");
			showArea.setText(stringBuilder.toString());
			if (!rs.next()) {
				java.sql.Statement createMetadataTable = conn.createStatement();
				createMetadataTable.execute(
						"create table metadata(tablename varchar(50),columnname varchar(200),datatype varchar(100),opekey text,homkey text,primary key(tablename,columnname));");
			   
			    stringBuilder.append("元数据表创建模块测试完成"+"\n");
//			    showArea.setText(stringBuilder.toString());
			}
			try {
				CCJSqlParserManager parserManager = new CCJSqlParserManager();
				String inputSQL = "create table test(id int);";
				Statement statement = parserManager.parse(new StringReader(inputSQL));
				CreateTable createTable = (CreateTable) statement;
				CreateTableDeparserV2.handler(createTable, conn,showArea,stringBuilder);
			    stringBuilder.append("创建表模块测试完成"+"\n");
//			    showArea.setText(stringBuilder.toString());
				inputSQL = "insert into test(id) values(1);";
				statement = parserManager.parse(new StringReader(inputSQL));
				Insert insert = (Insert) statement;
				InsertDeparserV2.handler(insert, conn,showArea,stringBuilder);
				stringBuilder.append("插入表模块测试完成"+"\n");
//				showArea.setText(stringBuilder.toString());
				inputSQL = "select id from test where id = 0;";
				statement = parserManager.parse(new StringReader(inputSQL));
				Select select = (Select) statement;
				SelectDemoV2.handler(select, conn,showArea,stringBuilder);
				stringBuilder.append("查询表模块测试完成"+"\n");
//				showArea.setText(stringBuilder.toString());
				
				inputSQL = "update test set id = 2 where id =1";
				statement = parserManager.parse(new StringReader(inputSQL));
				Update update = (Update) statement;
				UpdateDeparserV2.handler(update, conn,showArea,stringBuilder);
				stringBuilder.append("更新表模块测试完成"+"\n");
				
//				showArea.setText(stringBuilder.toString());
				inputSQL = "delete from test where id > 0;";
				statement = parserManager.parse(new StringReader(inputSQL));
				Delete delete = (Delete) statement;
				DeleteDeparserV2.handler(delete, conn);
				stringBuilder.append("删除表模块测试完成"+"\n");
//				showArea.setText(stringBuilder.toString());
				java.sql.Statement smt = conn.createStatement();
				smt.execute("drop table test;");
				smt.execute("delete from metadata where tablename = 'test';");
				stringBuilder.append("全部测试完成,系统正常！"+"\n");
				showArea.setText(stringBuilder.toString());
				smt.close();
				ClientDemo.tableNameList.clear();
				ClientDemo.encColumnNameList.clear();
			} catch (JSQLParserException e) {
				showArea.setText("sql语句解析异常！");
				e.printStackTrace();
			}
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			showArea.setText("系统出现异常，请检查！");
			e.printStackTrace();
		}
	}

}