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
			//��������дwhere�Ӿ䲿��
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
	public List<Expression> rewriteExpressionList(List<Column> columnList,List<Expression> expressionList,MetaDataManager metaManager,JTextPane showArea,StringBuilder stringBuilder) throws Exception {
		List<Expression> newExpressionList = new ArrayList<Expression>();
		
		for (int index_column = 0; index_column < columnList.size(); index_column++) {
			String columnName = columnList.get(index_column).getColumnName();
			//�����������ɻ��߻�ȡ��Ӧ����Կ
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
				//ֻ������ֵ����ʱ����Ҫ��ȡope��Կ
				opeKey = metaManager.getOpeKey(columnName);
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
					String value = ((StringValue) expressionList.get(index_column)).getValue();
					String detEnc = DETAlgorithm.encrypt(value, detKey);
					newExpressionList.add(new StringValue("'"+detEnc+"'"));

				} else {
					stringBuilder.append("Update����г��ֲ�֧�ֵ���������");
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
		//����ֻ������ĸ��²���
		if(update.getTables().size() > 1){
			stringBuilder.append("��ϵͳֻ������ĸ��²�����\n");
			showArea.setText(stringBuilder.toString());
			
		}
		String tableName = update.getTables().get(0).getName();

		Map<String,MetaDataManager> metaOfTable = new HashMap<String,MetaDataManager>();
		MetaDataManager metaManager = new MetaDataManager();
		metaManager.fetchMetaData(tableName);
		metaOfTable.put(tableName, metaManager);
		outputSQL = updateRec.updateReconstruct(update,metaOfTable,showArea,stringBuilder);
//		stringBuilder.append("ִ�и��²�����"+outputSQL+"\n"); //outputSql
//		showArea.setText(stringBuilder.toString());
		
		
		/*
		 * ���������е�ǰ������а���DET���н���RND��Ĵ���
		 */
		
			//1.��ǰ��Ҫ��ѯDET�У����Ȱ�ȥRND�㣬�ٲ�ѯ������ٰ���RND��
			String peelOff = RNDOnion.peelOffRND(tableName, metaOfTable.values().iterator().next().getAllDETColumnName(), "123456");
			smt.executeUpdate(peelOff);
			
			//2.���潫����������SQL����ύ�����ݿ�ִ�У������ڴ�֮ǰ����Ҫ��ȥRND��
			smt.executeUpdate(outputSQL);
			
			//3.��ɸ������������������������RND��,��Ҫע����ǣ��ڰ�RND��ʱ�����뽫"set...where"�е��ǲ���DET��Ҳ���°���RND
			String packOn = RNDOnion.packOnRND(tableName, metaOfTable.values().iterator().next().getAllDETColumnName(), "123456");
			smt.executeUpdate(packOn);
			smt.close();
	

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
