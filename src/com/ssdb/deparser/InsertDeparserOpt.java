package com.ssdb.deparser;

import java.io.StringReader;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.ssdb.core.AddHomAlgorithm;
import com.ssdb.core.ConnectionMySQL;
import com.ssdb.core.DETAlgorithm;
import com.ssdb.core.KeyManager;
import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;
import com.ssdb.core.OPEAlgorithm;
import com.ssdb.core.RNDOnion;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * ��������ڽ����û������insert���,��������е�����ʹ�ü���ģ����ܺ����¹���SQL��䡣
 * 1.�ɹ��캯����ȡ�ⲿ�����sql��䣻
 * 2.�ڲ�������ǰ����ʹ��Blowfish���������Ϣ���м��ܡ�
 * 3.����JSQLParser�ع�SQL���
 * 4.���ⷵ��һ���ع��ĺ���������Ϣ��SQL����ַ�����
 * 
 */
//�����ڽ������������ʱ��ʹ��
public class InsertDeparserOpt{

	/*
	 * �ú����û������û�����SQL��䣬�������е�������Ϣ���ܺ����¹���һ�������µ�SQL���
	 * 
	 * @return �����µ�SQL���
	 * @throws JSQLParserException
	 * @throws NoSuchAlgorithmException 
	 * @see com.ssdb.BlowFish
	 * @see com.ssdb.Base64
	 * @see net.sf.jsqlparser.util.deparser.InsertDeParser
	 */
	public String sqlReonstructor(Insert insert,MetaDataManager metaManager) throws JSQLParserException, NoSuchAlgorithmException {
		// ������Ҫʹ�õ�JSQLParser��
		//CCJSqlParserManager parserManager = new CCJSqlParserManager();
		// Ҫ��insert���ת����insert����
		//Insert insert = (Insert) parserManager.parse(new StringReader(sql));
		// ��ȡԪ����
		List<Column> list = new ArrayList<Column>();
		list = insert.getColumns();
		try {
			insert.setColumns(rewriteColumnList(list,metaManager));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ExpressionList expressionList = (ExpressionList) insert.getItemsList();
		insert.setItemsList(rewriteExpressionList(list,expressionList.getExpressions(),metaManager));
		
		return insert.toString() + ";";
	}



	/**
	 * ����������ڸ�д��������
	 * 
	 * @param plainColumnList
	 *            ���ĵ������б�
	 * @param tableName
	 *            ����
	 * @return ��д���������
	 * @throws Exception
	 */
	public List<Column> rewriteColumnList(List<Column> plainColumnList,MetaDataManager metaManager) throws Exception {
		// ����insert����еı�������metadata���л�ȡ��Ӧ��Ԫ������Ϣ
		
		String plainColumnName =""; 
		String secretColumnName = ""; 
		List<Column> resultList = new ArrayList<Column>(); 
		for(int i = 0; i < plainColumnList.size(); i++){
			plainColumnName = plainColumnList.get(i).getColumnName();
			String dataType = metaManager.getDataType(plainColumnName);
			
			secretColumnName = NameHide.getSecretName(plainColumnName);
			//plainColumnList.get(i).setColumnName(NameHide.getDETName(secretColumnName));
			if(dataType.equals("int")||dataType.equals("double")||dataType.equals("float")){ 
				Column c_DET =new Column(NameHide.getDETName(secretColumnName));
				resultList.add(c_DET); 
				Column c_OPE = new Column(NameHide.getOPEName(secretColumnName)); 
				resultList.add(c_OPE);
				//����Ҫ���5��HOM�� 
				for(int index_HOM = 0;index_HOM < 5;index_HOM++){
					Column c_HOM = new Column(NameHide.getHOMName(secretColumnName)+(index_HOM+1));
					resultList.add(c_HOM); 
				} 
			}else{
				if(dataType.equals("char")||dataType.equals("varchar")||dataType.equals("text")){ 
					Column c_DET =new Column(NameHide.getDETName(secretColumnName));
					resultList.add(c_DET); 
					/*��ʱ�������ַ�������������
					 * Column c_OPE = new Column(NameHide.getOPEName(secretColumnName)); 
					resultList.add(c_OPE);*/
				} 
			} 
		}
		return resultList;
	}


	public ExpressionList rewriteExpressionList(List<Column> columnList, List<Expression> expressionList,
		MetaDataManager metaManager) throws NoSuchAlgorithmException {
		ExpressionList resultList = new ExpressionList();
		List<Expression> newExpressionList = new ArrayList<Expression>();
		for (int index_column = 0; index_column < columnList.size(); index_column++) {
			String columnName = columnList.get(index_column).getColumnName();
			//�����������ɻ��߻�ȡ��Ӧ����Կ
			String dataType = metaManager.getDataType(columnName);
			
			//ͨ���ṩ�û�����Կ�������ͼ�������������һ����Կ
			Key detKey = KeyManager.generateDETKey("1234567812345678", columnName, "det");
			if (dataType.equals("int")||dataType.equals("double")||dataType.equals("float")) {				
				Expression rightExp = expressionList.get(index_column);
				String rightToStr = new String();
				if(rightExp instanceof LongValue){
					rightToStr = ((LongValue) rightExp).getStringValue();
				}else{
					if(rightExp instanceof DoubleValue){
						rightToStr = String.valueOf(((DoubleValue) rightExp).getValue());
					}else{
						if(rightExp instanceof StringValue){
							rightToStr = ((StringValue)rightExp).getValue();
						}
					}
				}
				
				
				String detEnc = DETAlgorithm.encrypt(rightToStr, detKey);
				newExpressionList.add(new StringValue("'"+detEnc+"'"));
				
				double[] opeKey = metaManager.getOpeKey(columnName);
				OPEAlgorithm opeAlg = new OPEAlgorithm(opeKey[0], opeKey[1], opeKey[2]);
				double opeEnc = opeAlg.nindex(Double.valueOf(rightToStr), true);
				newExpressionList.add(new DoubleValue(String.valueOf(opeEnc)));

				//ֻ������ֵ����ʱ����Ҫ��ȡhom��Կ
				double[][] homKey = metaManager.getHomKey(columnName);
				AddHomAlgorithm homAlg = new AddHomAlgorithm(homKey, 5);
				double[] homEnc = homAlg.encrypt(Double.valueOf(rightToStr));
				for (int index_hom = 0; index_hom < 5; index_hom++) {
					newExpressionList.add(new DoubleValue(String.valueOf(homEnc[index_hom])));
				}
			} else {
				if (dataType.equals("char")||dataType.equals("varchar")||dataType.equals("text")) {
					String value = ((StringValue)expressionList.get(index_column)).getValue();
					//DETAlgorithm detAlg = new DETAlgorithm();
					String detEnc = DETAlgorithm.encrypt(value, detKey);
					/*������仰����������ʹ��StringValue�Ĺ��캯����StringValue���캯��Ĭ�Ͽ�ʼ�ͽ��������ǵ����ţ�
					 * �����ڹ��캯������ȥ����β�������ַ�
						newExpressionList.add(new StringValue(detEnc));
					*/
					newExpressionList.add(new StringValue("'"+detEnc+"'"));
				/*	��ʱ�������ַ�����ope�㷨
				 * OPEAlgorithm opeAlg = new OPEAlgorithm(opeKey[0], opeKey[1], opeKey[2]);
					
					StringBuilder valueBuffer = new StringBuilder();
					for(int i = 0; i < value.length();i++){
						valueBuffer.append(Integer.valueOf(value.charAt(i)).toString());
					}
					
					double opeEnc = opeAlg.nindex(Integer.parseInt(valueBuffer.toString()), true);
					newExpressionList.add(new DoubleValue(String.valueOf(opeEnc)));
				*/

				} else {
					System.out.println("insert����г��ֲ�֧�ֵ���������");
				}
			}
			
		}
		resultList.setExpressions(newExpressionList);
		return resultList;		
	}
	
	
	
	public void insertToStudent(int n,JTextPane showArea,StringBuilder stringBuilder){
		try {
			long start = System.currentTimeMillis(); 
			String tableName = "student";
			MetaDataManager metaManager = new MetaDataManager();
			metaManager.fetchMetaData(tableName);
			
			Connection conn = ConnectionMySQL.openConnection();
			conn.setAutoCommit(false); 
			Statement stmt = conn.createStatement();
			CCJSqlParserManager parserManager = new CCJSqlParserManager(); 
			for(int i = 0 ; i < n ; i++){				
				String inputSQL = "insert into student(id,name,age,sex) values("+i+","+ "'wang"+i+"'," +(i+1)+",'male'"+");"; 
				Insert insert = (Insert) parserManager.parse(new StringReader(inputSQL));
				String encSQL = this.sqlReonstructor(insert,metaManager);
				stmt.addBatch(encSQL);
			}
			
			stmt.executeBatch();   //����ִ��

			List<String> columnNameList = new ArrayList<String>();

			//���Ǹ�д֮��������м���DET������OPE��HOM��������ֻ��Ҫʹ�ú���DET������
			for(String column : metaManager.getAllDETColumnName()){					
					columnNameList.add(column);							
			}
			//System.out.println(columnNameList.toString());
			//�ڹ����Update���Ļ����ϣ����һ��������䣬�ѵ�ǰ����ļ�¼��rowid��Ϊ������Ŀ���Ƕ��²�����а���RND��
			
			String packOnSQL =  RNDOnion.packOnRND(tableName, columnNameList, "123456") + " where rowid > 0 ";
			stmt.executeUpdate(packOnSQL);
			long end = System.currentTimeMillis();
			stringBuilder.append("���β��ԣ�����ִ��"+n+"��Insert�����ɣ�����ʱ��"+(end-start)+"ms\n");
			showArea.setText(stringBuilder.toString());
			conn.setAutoCommit(true);			
			stmt.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void insertToGrade(int n,JTextArea showArea,StringBuilder sBuilder){
		try {
			String tableName = "grade";
			MetaDataManager metaManager = new MetaDataManager();
			metaManager.fetchMetaData(tableName);
			InsertDeparserOpt insertDep = new InsertDeparserOpt();
			Connection conn = ConnectionMySQL.openConnection();
			conn.setAutoCommit(false); 
			Statement stmt = conn.createStatement();
			CCJSqlParserManager parserManager = new CCJSqlParserManager(); 
			long start = System.currentTimeMillis(); 
			for(int i = 0 ; i < n ; i++){				
				String inputSQL = "insert into grade(id,grade) values("+i+","+i+");"; 
				Insert insert = (Insert) parserManager.parse(new StringReader(inputSQL));
				String encSQL = insertDep.sqlReonstructor(insert,metaManager);
				stmt.addBatch(encSQL);
			}
			
			stmt.executeBatch();

			List<String> columnNameList = new ArrayList<String>();

			//���Ǹ�д֮��������м���DET������OPE��HOM��������ֻ��Ҫʹ�ú���DET������
			for(String column : metaManager.getAllDETColumnName()){					
					columnNameList.add(column);							
			}
			//System.out.println(columnNameList.toString());
			//�ڹ����Update���Ļ����ϣ����һ��������䣬�ѵ�ǰ����ļ�¼��rowid��Ϊ������Ŀ���Ƕ��²�����а���RND��
			
			String packOnSQL =  RNDOnion.packOnRND(tableName, columnNameList, "123456") + " where rowid > 0 ";
			stmt.executeUpdate(packOnSQL);
			long end = System.currentTimeMillis();
			System.out.println("InsertDeparserOPT----Time:"+(end-start));
			conn.setAutoCommit(true);			
			stmt.close();
			conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
				
}
