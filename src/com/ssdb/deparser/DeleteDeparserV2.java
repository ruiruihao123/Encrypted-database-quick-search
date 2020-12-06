package com.ssdb.deparser;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextArea;

import com.ssdb.core.MetaDataManager;
import com.ssdb.core.RNDOnion;
import com.ssdb.demo.ClientDemo;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.delete.Delete;

public class DeleteDeparserV2 {


	public DeleteDeparserV2(){
	} 
	
	public String deleteReconstruct(Delete delete,Map<String,MetaDataManager> metaOfTable) throws JSQLParserException{
		

		MetaDataManager metaManager = metaOfTable.values().iterator().next();
		String tableName = delete.getTable().getName();
		metaManager.fetchMetaData(tableName);
		StringBuilder buffer = new StringBuilder();

		//���ǰ�where�Ӿ�Ľ���ͳһ����WhereExpressionDeparser����ɡ�
		WhereExpressionDeparser whereExpressionDeparser = new WhereExpressionDeparser(metaOfTable,null,buffer);
		if(delete.getWhere() != null){
			delete.getWhere().accept(whereExpressionDeparser);
		}
		return delete.toString()+";";
	}
	

	public static void handler(Delete delete,Connection conn){
		try {
	
		Statement smt = conn.createStatement();
		DeleteDeparserV2 deleteRec = new DeleteDeparserV2();
		String tableName = delete.getTable().getName();
		Map<String,MetaDataManager> metaOfTable = new HashMap<String,MetaDataManager>();
		MetaDataManager metaManager = new MetaDataManager();
		metaManager.fetchMetaData(tableName);
		metaOfTable.put(tableName, metaManager);
		//�������sql���
		String outputSQL = deleteRec.deleteReconstruct(delete,metaOfTable);
		
		//�����ǰ����where��䣬�ͱ����Ƚ�where������漰����DET���а�ȥRND��
		if(ClientDemo.encColumnNameList.size() > 0){
			//1.�����ǰwhere�Ӿ�����Ҫƥ��DET�У����Ȱ�ȥRND�㣬��ɾ��
			String peelOff = RNDOnion.peelOffRND(tableName, ClientDemo.encColumnNameList, "123456");
			smt.executeUpdate(peelOff);
			//2.���潫����������SQL����ύ�����ݿ�ִ�У������ڴ�֮ǰ����Ҫ��ȥRND��
			smt.executeUpdate(outputSQL);
			
			/*3.�������ǵ������delete from peach where id = 3;
			 * ���ǰ�id=3��������¼ɾ���ˣ�����������¼�е�id�д���DET�㣬���Ǳ��뽫id_DET���¼���ΪRND
			 */
			
			String packOn = RNDOnion.packOnRND(tableName, ClientDemo.encColumnNameList, "123456");
			smt.executeUpdate(packOn);
			smt.close();
		}else{
			//�����ǰû��where�Ӿ䣬����Ҫ��ȥRND�㡣ֱ��ɾ������
			 
			// ִ��delete���
			smt.executeUpdate(outputSQL);
			smt.close();
			
		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

