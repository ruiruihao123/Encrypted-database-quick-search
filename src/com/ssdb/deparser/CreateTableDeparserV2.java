package com.ssdb.deparser;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;

import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public class CreateTableDeparserV2 {

	
	public String createTableReconstruct(CreateTable createTable,JTextPane showArea,StringBuilder stringBuilder) throws Exception{
		
		List<ColumnDefinition> listColumn = createTable.getColumnDefinitions();
		
		/*
		 * �����е�Ԫ������Ϣ�����������洢��metadata����
		 */
		MetaDataManager.storeMetaData(createTable.getTable().getName(), listColumn);
		
		String plainColumnName = "";
		String secretColumnName = "";
		//�½�һ��ColDataType��������double���ͺ�text����
		ColDataType numericType = new ColDataType();
		numericType.setDataType("double");
		ColDataType stringType = new ColDataType();
		stringType.setDataType("text");
		
		//newListColumn�Ǳ��������ɵ�ColumnDefinition�б�
		List<ColumnDefinition> newListColumn = new ArrayList<ColumnDefinition>();
		
		for(int i = 0; i < listColumn.size();i++){
			plainColumnName = listColumn.get(i).getColumnName();
			secretColumnName = NameHide.getSecretName(plainColumnName);
			//������ֵ������������Ҫ��չΪ���У������ַ��ͣ�����дΪһ��
			String columnDataType = listColumn.get(i).getColDataType().getDataType().toLowerCase();
			String regexNum = "int|double|float";
			String regexStr = "char|varchar|text";
			Pattern pNum = Pattern.compile(regexNum,Pattern.CASE_INSENSITIVE);
			Matcher mNum = pNum.matcher(columnDataType);
			if(mNum.find()){				
						//������������(������������int������Ϊ����)�������һ��DET��
						ColumnDefinition element0 = new ColumnDefinition();
						element0.setColumnName(NameHide.getDETName(secretColumnName));
						element0.setColDataType(stringType);
						newListColumn.add(element0);
						//�����һ��OPE��
						ColumnDefinition element1 = new ColumnDefinition();
						element1.setColumnName(NameHide.getOPEName(secretColumnName));
						element1.setColDataType(numericType);
						newListColumn.add(element1);
						//���ǽ�����5��HOM�У����ڱ���5�����ķ�Ƭ���ֱ���XXX_HOM1��XXX_HOM2��XXX_HOM3��XXX_HOM4��XXX_HOM5
						for(int index_HOM = 0 ; index_HOM < 5 ; index_HOM++){
							ColumnDefinition elementHOM  = new ColumnDefinition();
							elementHOM.setColumnName(NameHide.getHOMName(secretColumnName)+(index_HOM+1));
							elementHOM.setColDataType(numericType);
							newListColumn.add(elementHOM);
						}
			}else{
				//��������ʽ�жϵ�ǰ�������ǲ����ַ�����
				Pattern pStr = Pattern.compile(regexStr,Pattern.CASE_INSENSITIVE);
				Matcher mStr = pStr.matcher(columnDataType);
				if(mStr.find()){
					//�����ַ������ͣ�������������char��Ϊ���ԣ������һ��DET��
					ColumnDefinition element = new ColumnDefinition();
					element.setColumnName(NameHide.getDETName(secretColumnName));
					element.setColDataType(stringType);
					newListColumn.add(element);				
				}else{
					stringBuilder.append("�������ı��д��ڲ�֧�ֵ��ֶ����ͣ����ǵ�ǰ��֧��(int.double,float)(char,varchar,text)");
					showArea.setText(stringBuilder.toString());
				}
			}
		}

		/*
		 * ���ǵı�����Ҫ����һ��(rowid primary key auto_increment)
		 */
		
		//�����ø��е�������rowid
		ColumnDefinition rowId = new ColumnDefinition();
		rowId.setColumnName("rowid");
		//���ø��е�����Ϊint��
		ColDataType intType = new ColDataType();		
		intType.setDataType("int");
		rowId.setColDataType(intType);
		//���ø��е�Լ������
		List<String> constrainOfRowId = new ArrayList<String>();
		constrainOfRowId.add("primary key");
		constrainOfRowId.add("auto_increment");
		constrainOfRowId.add("not null");
		rowId.setColumnSpecStrings(constrainOfRowId);
		//��rowid��ӵ�newListColumn
		newListColumn.add(rowId);
		
 		createTable.setColumnDefinitions(newListColumn);
		return createTable.toString();
	}	

	public static void handler(CreateTable createTable,Connection conn,JTextPane showArea,StringBuilder stringBuilder){
		try {
		String outputSQL = "";		
		CreateTableDeparserV2 createReconstructor = new CreateTableDeparserV2();
		outputSQL = createReconstructor.createTableReconstruct(createTable,showArea,stringBuilder);
		Statement smt = conn.createStatement();
		smt.executeUpdate(outputSQL);			
		smt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
