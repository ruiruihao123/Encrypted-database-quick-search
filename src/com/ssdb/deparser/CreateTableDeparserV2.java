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
		 * 将表中的元数据信息保存下来，存储在metadata表中
		 */
		MetaDataManager.storeMetaData(createTable.getTable().getName(), listColumn);
		
		String plainColumnName = "";
		String secretColumnName = "";
		//新建一个ColDataType类来定义double类型和text类型
		ColDataType numericType = new ColDataType();
		numericType.setDataType("double");
		ColDataType stringType = new ColDataType();
		stringType.setDataType("text");
		
		//newListColumn是保存新生成的ColumnDefinition列表
		List<ColumnDefinition> newListColumn = new ArrayList<ColumnDefinition>();
		
		for(int i = 0; i < listColumn.size();i++){
			plainColumnName = listColumn.get(i).getColumnName();
			secretColumnName = NameHide.getSecretName(plainColumnName);
			//对于数值型数据我们需要扩展为三列，对于字符型，仅改写为一列
			String columnDataType = listColumn.get(i).getColDataType().getDataType().toLowerCase();
			String regexNum = "int|double|float";
			String regexStr = "char|varchar|text";
			Pattern pNum = Pattern.compile(regexNum,Pattern.CASE_INSENSITIVE);
			Matcher mNum = pNum.matcher(columnDataType);
			if(mNum.find()){				
						//对于数字类型(这里我们先用int类型做为测试)，先添加一个DET列
						ColumnDefinition element0 = new ColumnDefinition();
						element0.setColumnName(NameHide.getDETName(secretColumnName));
						element0.setColDataType(stringType);
						newListColumn.add(element0);
						//再添加一个OPE列
						ColumnDefinition element1 = new ColumnDefinition();
						element1.setColumnName(NameHide.getOPEName(secretColumnName));
						element1.setColDataType(numericType);
						newListColumn.add(element1);
						//我们将生成5个HOM列，用于保存5个密文分片，分别是XXX_HOM1、XXX_HOM2、XXX_HOM3、XXX_HOM4、XXX_HOM5
						for(int index_HOM = 0 ; index_HOM < 5 ; index_HOM++){
							ColumnDefinition elementHOM  = new ColumnDefinition();
							elementHOM.setColumnName(NameHide.getHOMName(secretColumnName)+(index_HOM+1));
							elementHOM.setColDataType(numericType);
							newListColumn.add(elementHOM);
						}
			}else{
				//用正则表达式判断当前的类型是不是字符类型
				Pattern pStr = Pattern.compile(regexStr,Pattern.CASE_INSENSITIVE);
				Matcher mStr = pStr.matcher(columnDataType);
				if(mStr.find()){
					//对于字符串类型（我们这里先用char作为测试），添加一个DET列
					ColumnDefinition element = new ColumnDefinition();
					element.setColumnName(NameHide.getDETName(secretColumnName));
					element.setColDataType(stringType);
					newListColumn.add(element);				
				}else{
					stringBuilder.append("您创建的表中存在不支持的字段类型，我们当前仅支持(int.double,float)(char,varchar,text)");
					showArea.setText(stringBuilder.toString());
				}
			}
		}

		/*
		 * 我们的表中需要增加一列(rowid primary key auto_increment)
		 */
		
		//先设置该列的名字是rowid
		ColumnDefinition rowId = new ColumnDefinition();
		rowId.setColumnName("rowid");
		//设置该列的类型为int型
		ColDataType intType = new ColDataType();		
		intType.setDataType("int");
		rowId.setColDataType(intType);
		//设置该列的约束条件
		List<String> constrainOfRowId = new ArrayList<String>();
		constrainOfRowId.add("primary key");
		constrainOfRowId.add("auto_increment");
		constrainOfRowId.add("not null");
		rowId.setColumnSpecStrings(constrainOfRowId);
		//将rowid添加到newListColumn
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
