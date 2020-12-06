package com.ssdb.deparser;

import java.security.Key;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.ssdb.core.AddHomAlgorithm;
import com.ssdb.core.DETAlgorithm;
import com.ssdb.core.KeyManager;
import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;
import com.ssdb.core.OPEAlgorithm;
import com.ssdb.core.RNDOnion;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class UpdateDeparserV2 {

	public String updateReconstruct(Update update,Map<String,MetaDataManager> metaOfTable, JTextPane showArea, StringBuilder stringBuilder)
			throws Exception {

		MetaDataManager metaManager = metaOfTable.values().iterator().next();
		List<Column> columnList = new ArrayList<Column>();
		columnList = update.getColumns();
		update.setColumns(rewriteColumnList(columnList,metaManager));
		
		
		List<Expression> expressionList = new ArrayList<Expression>();
		expressionList = update.getExpressions();
		update.setExpressions(rewriteExpressionList(columnList,expressionList,metaManager,showArea,stringBuilder));
		

		if(update.getWhere() != null){
			//下面在重写where子句部分
			StringBuilder buffer = new StringBuilder();
			WhereExpressionDeparser whereDeparser = new WhereExpressionDeparser(metaOfTable,new SelectDeParser(),buffer);
			update.getWhere().accept(whereDeparser);
		}
		return update.toString()+";";

		
	}
	public List<Column> rewriteColumnList(List<Column> columnList,MetaDataManager metaManager) throws Exception{
		String plainColumnName =""; 
		String secretColumnName = ""; 
		List<Column> resultList = new ArrayList<Column>(); 
		for(int i = 0; i < columnList.size(); i++){
			plainColumnName = columnList.get(i).getColumnName();
			String dataType = metaManager.getDataType(plainColumnName);
			
			secretColumnName = NameHide.getSecretName(plainColumnName);
			//plainColumnList.get(i).setColumnName(NameHide.getDETName(secretColumnName));
			
			if(dataType.equals("int")||dataType.equals("double")||dataType.equals("float")){ 
				Column c_DET =new Column(NameHide.getDETName(secretColumnName));
				resultList.add(c_DET); 
				Column c_OPE = new Column(NameHide.getOPEName(secretColumnName)); 
				resultList.add(c_OPE);
				//下面要添加5个HOM列 
				for(int index_HOM = 0;index_HOM < 5;index_HOM++){
					Column c_HOM = new Column(NameHide.getHOMName(secretColumnName)+(index_HOM+1));
					resultList.add(c_HOM); 
				} 
			}else{
				if(dataType.equals("char")||dataType.equals("varchar")||dataType.equals("text")){ 
					Column c_DET =new Column(NameHide.getDETName(secretColumnName));
					resultList.add(c_DET); 
					/*暂时不考虑字符串的排序问题
					 * Column c_OPE = new Column(NameHide.getOPEName(secretColumnName)); 
					resultList.add(c_OPE);*/
				} 
			} 
		}
		return resultList;
		
	}
	public List<Expression> rewriteExpressionList(List<Column> columnList,List<Expression> expressionList,MetaDataManager metaManager,JTextPane showArea,StringBuilder stringBuilder) throws Exception {
		List<Expression> newExpressionList = new ArrayList<Expression>();
		
		for (int index_column = 0; index_column < columnList.size(); index_column++) {
			String columnName = columnList.get(index_column).getColumnName();
			//根据列名生成或者获取相应的密钥
			String dataType = metaManager.getDataType(columnName);

			Key detKey = KeyManager.generateDETKey("1234567812345678", columnName, "det");
			double[] opeKey = null;
			if ("int".equals(dataType) || "float".equals(dataType) || "double".equals(dataType)) {	
			
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
				//只有在数值类型时才需要获取ope密钥
				opeKey = metaManager.getOpeKey(columnName);
				OPEAlgorithm opeAlg = new OPEAlgorithm(opeKey[0], opeKey[1], opeKey[2]);
				double opeEnc = opeAlg.nindex(Double.valueOf(rightToStr), true);
				newExpressionList.add(new DoubleValue(String.valueOf(opeEnc)));
			
				//只有在数值类型时才需要获取hom密钥
				double[][] homKey = metaManager.getHomKey(columnName);
				AddHomAlgorithm homAlg = new AddHomAlgorithm(homKey, 5);
				double[] homEnc = homAlg.encrypt(Double.valueOf(rightToStr));
				for (int index_hom = 0; index_hom < 5; index_hom++) {
					newExpressionList.add(new DoubleValue(String.valueOf(homEnc[index_hom])));
				}
			} else {
				if (dataType.equals("char")||dataType.equals("varchar")||dataType.equals("text")) {
					String value = ((StringValue) expressionList.get(index_column)).getValue();
					String detEnc = DETAlgorithm.encrypt(value, detKey);
					newExpressionList.add(new StringValue("'"+detEnc+"'"));

				} else {
					stringBuilder.append("Update语句中出现不支持的数据类型");
					showArea.setText(stringBuilder.toString());
					
				}
			}
			
		}
		return newExpressionList;

	}

	public static void handler(Update update,Connection conn, JTextPane showArea, StringBuilder stringBuilder){

		try {
	
		Statement smt = conn.createStatement();
		UpdateDeparserV2 updateRec = new UpdateDeparserV2();
		String outputSQL="";
		//我们只允许单表的更新操作
		if(update.getTables().size() > 1){
			stringBuilder.append("本系统只允许单表的更新操作！\n");
			showArea.setText(stringBuilder.toString());
			
		}
		String tableName = update.getTables().get(0).getName();

		Map<String,MetaDataManager> metaOfTable = new HashMap<String,MetaDataManager>();
		MetaDataManager metaManager = new MetaDataManager();
		metaManager.fetchMetaData(tableName);
		metaOfTable.put(tableName, metaManager);
		outputSQL = updateRec.updateReconstruct(update,metaOfTable,showArea,stringBuilder);
//		stringBuilder.append("执行更新操作："+outputSQL+"\n"); //outputSql
//		showArea.setText(stringBuilder.toString());
		
		
		/*
		 * 必须对语句中当前表的所有包含DET的列进行RND层的处理
		 */
		
			//1.当前需要查询DET列，则先剥去RND层，再查询，最后再包上RND层
			String peelOff = RNDOnion.peelOffRND(tableName, metaOfTable.values().iterator().next().getAllDETColumnName(), "123456");
			smt.executeUpdate(peelOff);
			
			//2.下面将处理后的密文SQL语句提交给数据库执行，但是在此之前我们要剥去RND层
			smt.executeUpdate(outputSQL);
			
			//3.完成更新任务后，用下面这条语句包上RND层,需要注意的是，在包RND层时，必须将"set...where"中的那部分DET列也重新包上RND
			String packOn = RNDOnion.packOnRND(tableName, metaOfTable.values().iterator().next().getAllDETColumnName(), "123456");
			smt.executeUpdate(packOn);
			smt.close();
	

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
