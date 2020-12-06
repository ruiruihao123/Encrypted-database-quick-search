package com.ssdb.deparser;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.ssdb.core.AddHomAlgorithm;
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
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * 这个类用于解析用户输入的insert语句,并将语句中的明文使用加密模块加密后，重新构造SQL语句。
 * <p>
 * 功能简介：<br>
 * <li>1.由构造函数获取外部传入的sql语句；
 * <li>2.在插入数据前，先使用Blowfish类对敏感信息进行加密。
 * <li>3.利用JSQLParser重构SQL语句
 * <li>4.向外返回一个重构的含有密文信息的SQL语句字符串。
 * 
 */
public class InsertDeparserV2 {

	/**
	 * 该函数用户解析用户输入SQL语句，并将其中的敏感信息加密后，重新构造一个密文下的SQL语句
	 * @param stringBuilder 
	 * @param showArea 
	 * 
	 * @return 密文下的SQL语句
	 * @throws JSQLParserException
	 * @throws NoSuchAlgorithmException 
	 * @see com.ssdb.BlowFish
	 * @see com.ssdb.Base64
	 * @see net.sf.jsqlparser.util.deparser.InsertDeParser
	 */
	public String sqlReonstructor(Insert insert,JTextPane showArea,StringBuilder stringBuilder) throws JSQLParserException, NoSuchAlgorithmException {
		
		// 获取元数据
		MetaDataManager metaManager = new MetaDataManager();
		metaManager.fetchMetaData(insert.getTable().getName());

		List<Column> list = new ArrayList<Column>();
		list = insert.getColumns();
		try {
			insert.setColumns(rewriteColumnList(list,metaManager));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ExpressionList expressionList = (ExpressionList) insert.getItemsList();   //只能处理单条数据
		insert.setItemsList(rewriteExpressionList(list,expressionList.getExpressions(),metaManager,showArea,stringBuilder));

		return insert.toString() + ";";
	}



	/**
	 * 这个函数用于改写所有列名
	 * 
	 * @param plainColumnList
	 *            明文的列名列表
	 * @param tableName
	 *            表名
	 * @return 改写后的列名表
	 * @throws Exception
	 */
	public List<Column> rewriteColumnList(List<Column> plainColumnList,MetaDataManager metaManager) throws Exception {
		// 根据insert语句中的表名，从metadata表中获取相应的元数据信息
		
		String plainColumnName =""; 
		String secretColumnName = ""; 
		List<Column> resultList = new ArrayList<Column>(); 
		
		for(int i = 0; i < plainColumnList.size(); i++){
			plainColumnName = plainColumnList.get(i).getColumnName();
			String dataType = metaManager.getDataType(plainColumnName);
			
			secretColumnName = NameHide.getSecretName(plainColumnName);
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
			}else if(dataType.equals("char")||dataType.equals("varchar")||dataType.equals("text")){ 
					Column c_DET =new Column(NameHide.getDETName(secretColumnName));
					resultList.add(c_DET); 
					/*暂时不考虑字符串的排序问题
					 * Column c_OPE = new Column(NameHide.getOPEName(secretColumnName)); 
					resultList.add(c_OPE);*/
				 
			}else{
				System.out.println("列中出现了不支持的数据类型");
			}
		}
		return resultList;
	}

	
	
	public ExpressionList rewriteExpressionList(List<Column> columnList, List<Expression> expressionList,
			MetaDataManager metaManager, JTextPane showArea, StringBuilder stringBuilder) throws NoSuchAlgorithmException {
		ExpressionList resultList = new ExpressionList();
		List<Expression> newExpressionList = new ArrayList<Expression>();
		for (int index_column = 0; index_column < columnList.size(); index_column++) {
			String columnName = columnList.get(index_column).getColumnName();
			//根据列名生成或者获取相应的密钥
			String dataType = metaManager.getDataType(columnName);
			
			//通过提供用户主密钥，列名和加密类型来生成一个密钥
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

				//只有在数值类型时才需要获取hom密钥
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
					/*下面这句话的问题在于使用StringValue的构造函数，StringValue构造函数默认开始和结束符号是单引号，
					 * 于是在构造函数中先去掉首尾共两个字符
						newExpressionList.add(new StringValue(detEnc));
					*/
					newExpressionList.add(new StringValue("'"+detEnc+"'"));
				/*	暂时不考虑字符串的ope算法
				 * OPEAlgorithm opeAlg = new OPEAlgorithm(opeKey[0], opeKey[1], opeKey[2]);
					
					StringBuilder valueBuffer = new StringBuilder();
					for(int i = 0; i < value.length();i++){
						valueBuffer.append(Integer.valueOf(value.charAt(i)).toString());
					}
					
					double opeEnc = opeAlg.nindex(Integer.parseInt(valueBuffer.toString()), true);
					newExpressionList.add(new DoubleValue(String.valueOf(opeEnc)));
				*/

				} else {
					
					System.out.println("insert语句中出现不支持的数据类型");
				}
			}
			
		}
		resultList.setExpressions(newExpressionList);
		return resultList;		
	}
	
	
	public static void handler(Insert insert,Connection conn,JTextPane showArea,StringBuilder stringBuilder){ 
		try {
			Statement smt = conn.createStatement();
			InsertDeparserV2 ind = new InsertDeparserV2();
			String outputSQL = "";
			outputSQL = ind.sqlReonstructor(insert,showArea,stringBuilder);
			smt.executeUpdate(outputSQL,Statement.RETURN_GENERATED_KEYS);
			//执行结果之后会自动产生键值，可以获取
			ResultSet rsKey = smt.getGeneratedKeys();
			int rowid = 0;
			if(rsKey.next()){
				rowid = rsKey.getInt(1);	
			}	
 
			String tableName = insert.getTable().getName();
			List<String> columnNameList = new ArrayList<String>();
			String columnName = "";
			//我们改写之后的列名中既有DET，又有OPE、HOM，而我们只需要使用含有DET的列名
			for(Column column : insert.getColumns()){	
				columnName = column.getColumnName();
				if(columnName.contains("DET")){
					columnNameList.add(columnName);			
				}
			}
	
			//在构造的Update语句的基础上，添加一个条件语句，把当前插入的记录的rowid作为条件，目的是对新插入的行包上RND层
			String packOnSQL =  RNDOnion.packOnRND(tableName, columnNameList, "123456") + " where rowid = " + rowid;
			smt.executeUpdate(packOnSQL);
			smt.close();
		}catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
