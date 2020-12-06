package com.ssdb.deparser;

import java.io.StringReader;
import java.security.Key;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JTextPane;

import com.ssdb.core.AddHomAlgorithm;
import com.ssdb.core.DETAlgorithm;
import com.ssdb.core.KeyManager;
import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;
import com.ssdb.core.RNDOnion;
import com.ssdb.demo.ClientDemo;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SelectDemoV2 {



 	public  void print(Map<String,MetaDataManager> metaOfTable,ResultSet rs,List<SelectItem> plainItemList,JTextPane showArea,StringBuilder stringBuilder) throws Exception{
		Map<String,Key> detKeyMap = new HashMap<String,Key>();
		Map<String,double[][]> homKeyMap = new HashMap<String,double[][]>();
		int size = plainItemList.size();
		
		//��������join���ʱ����������,
		if(plainItemList.toString().contains("*")){
			//�õ�����������
			Iterator<String> iterator = metaOfTable.keySet().iterator();
			String tableName1 = iterator.next();
			MetaDataManager metaManager = metaOfTable.get(tableName1);
			//�����������Կ
			for(String columnName : metaManager.getAllPlainColumnName()){
				stringBuilder.append(columnName+'\t');
				Key key = KeyManager.generateDETKey("1234567812345678", columnName, "det");
				detKeyMap.put(columnName, key);
			}
			stringBuilder.append("\n\n");
			showArea.setText(stringBuilder.toString());
			//��������
			while(rs.next()){
				for(String columnName : metaManager.getAllPlainColumnName()){
					String detColumnName = NameHide.getDETName(NameHide.getSecretName(columnName));
					byte[] resultDET = DETAlgorithm.decrypt(rs.getString(detColumnName), detKeyMap.get(columnName));
					stringBuilder.append(new String(resultDET) + "\t");
				}
				stringBuilder.append('\n');
				showArea.setText(stringBuilder.toString());
			}
		}else{
			for(int index_list = 0; index_list < size;index_list++){
				Expression item = ((SelectExpressionItem)plainItemList.get(index_list)).getExpression();   //��仰���²���select *;
				/*
				 * �ж�selectItem�Ǻ�����sum()��avg()��������ͨ����
				 */
				if(item instanceof Function){
					Function functionItem = (Function)item;
					//����Ĭ��sum()����avg()�ڲ�ֻ��һ������
					Column columnOfFunction = (Column)functionItem.getParameters().getExpressions().get(0);
					String columnName = columnOfFunction.getColumnName();
					double[][] homKey  =  new double[5][3];
					/*
					 * ������ж�����Ϊ��
					 * 1.��ǰΪ�����ѯ��metaOfTable.size() == 1������ʹ�� "select ���� from ...";
					 * 		���1������
					 * 2.��ǰΪ����ѯ��metaOfTable.size() != 1������û��ʹ�� "select ����.���� from ...";
					 * 		���2��������
					 * 3.��ǰΪ�����ѯ����ʹ������select ����.���� from ...�����߶���ѯ����ʹ����"select ����.���� from ..."
					 * 		���3������
					 * 
					 * ���ϵ�������Ը���Ϊ������ѯʱ����������ǰָ����Ӧ�ı���������.����
					 */
					if(metaOfTable.size() == 1 && columnOfFunction.getTable().getName() == null){
							homKey = metaOfTable.values().iterator().next().getHomKey(columnName);
							
							homKeyMap.put(columnName, homKey);	
						}else{
							if(metaOfTable.size() != 1 && columnOfFunction.getTable().getName() == null){
								stringBuilder.append("���漰�����Ĳ���ʱ��sum()��avg()��������Ҫ���ṩ��������sum(employee.salary)");
								showArea.setText(stringBuilder.toString());
								
							}else{	
								//������ִ�е�����������ǰָ���˱���:tableName.columnName
									String tableName = columnOfFunction.getTable().getName();
									/*�����ѯ����ж�����д�����ͬ����������ô��
									 * ����:select sum(employee.salary),avg(manager.salary) ...
									 * ���ֻ�ǰ��������ķ�ʽ�洢hom��Կ����Ȼ���ָ��ǵ�����
									 * ���Զ��ڶ����������뽫homKeyMap�ļ�����Ϊ������+������
									 * ����������Բ���ô������һ��if��䣩
									 */
									homKey = metaOfTable.get(tableName).getHomKey(columnName);
									homKeyMap.put(tableName+columnName, homKey);	
							}			
						}					
							
					}else{
						/*�����ǰ��SelectItem����ͨ���У���Ϊ����Ҫ��Ԫ���ݱ��в�����Կ��ֻҪͨ����������det��Կ����.
						 * ������Ҫ��������һ�����������Ƕ��������������ͬ�����������ָ����Ӧ�ı�������select employee.id��manager.id...
						 * ͬʱ�����ȡ��Կ��ʱ��Ҳ��Ҫ��������ΪtableName+columnName
						 * ���ڵ�����ֻ��Ҫ����ΪcolumnName����
						 */
						Column columnItem  = (Column)item;
						String columnName = columnItem.getColumnName();
						Key key = KeyManager.generateDETKey("1234567812345678", columnName, "det");
						if(metaOfTable.size() == 1 && columnItem.getTable().getName() == null){
							detKeyMap.put(columnName, key);					
						}else{
							if(metaOfTable.size() != 1 && columnItem.getTable().getName() == null){
								
								stringBuilder.append("���漰�����Ĳ���ʱ��������Ҫ���ṩ��Ӧ�ı�������sum(employee.salary)");
								showArea.setText(stringBuilder.toString());
						
							}else{	
								//������ִ�е�����������ǰָ���˱���:tableName.columnName	
								String tableName = columnItem.getTable().getName();
								detKeyMap.put(tableName+columnName, key);	
							}			
						}	
					}
			}
				
				/*
				 * ������select sum(grade),sum(age) from gradeΪ��
				 * rs�е�����Ϊsum(grade_HOM1),sum(grade_HOM2),sum(grade_HOM3),sum(grade_HOM4),sum(grade_HOM5),
				 * sum(age_HOM1),sum(age_HOM2),sum(age_HOM3),sum(age_HOM4),sum(age_HOM5)
				 * ����������rs.getDouble���ֱ��ȡgrade��age��sum���ʱ������Ҫָ���ֶε����֣�����������ôȷ����ȡ����grade_HOM1,����age_HOM1�أ�
				 * ����Ľ�������ǣ��������ĵ�selectItem��Ϣ���Ƚ�������дΪ������Ҫ����ʽ���ٴ�rs�л�ȡ��Ӧ��ֵ
				 */
				while(rs.next()){
					for(int index_list = 0; index_list < size;index_list++){
						//�����ǰ��selectItem��һ�������Ļ�(SUM����AVG)
						Expression item = ((SelectExpressionItem)plainItemList.get(index_list)).getExpression();
						if(item instanceof Function){
							Function functionItem = (Function)item;
							//����Ĭ��sum()����avg()�ڲ�ֻ��һ������
							Column columnOfFunction = (Column)functionItem.getParameters().getExpressions().get(0);
							String columnName = columnOfFunction.getColumnName();
							String secretColumnName = NameHide.getHOMName(NameHide.getSecretName(columnName));
							double[] secretShare = new double[5];
							//����������Ҫ�жϱ���
							/*
							 * �������ǲ����ж��Ƕ���ǵ�����Ϊ֮ǰ��for������Ѿ����������Ĺ��������������ִ��������
							 * ֮ǰ�ͻ��˳����򣬼�Ȼ��ִ�е������Ȼ������ִ�������ģ�����ֻ��Ҫ�ж��ǲ��Ǻ��б����Ϳ����ˡ�
							 * ����ֻ�����������һ���Ǵ�������һ���ǲ�������
							 * ���ڲ��������ģ�����ͨ��������HOM��ʽ���Ҽ��ɣ�����homKeyMap�ļ�Ҳ��������
							 * ���ڴ������ģ�������Ҫ����+��������ʽ���в����Լ�����Կ
							 */
							if(columnOfFunction.getTable().getName() == null){
								for(int hom_index = 0; hom_index < 5;hom_index++){
									secretShare[hom_index] = rs.getDouble(functionItem.getName() + "(" +secretColumnName + (hom_index+1) + ")");
								}
								AddHomAlgorithm homAlg = new AddHomAlgorithm(homKeyMap.get(columnName), 5);
								double result = homAlg.decrypt(secretShare);
//								System.out.println(result + "\t");
								stringBuilder.append(result + "\t");
								showArea.setText(stringBuilder.toString());
							}else{
								if(columnOfFunction.getTable().getName() != null){
									String tableName = columnOfFunction.getTable().getName();
									for(int hom_index = 0; hom_index < 5;hom_index++){
										secretShare[hom_index] = rs.getDouble(functionItem.getName() + "(" +tableName+"."+secretColumnName + (hom_index+1) + ")");
									}
									AddHomAlgorithm homAlg = new AddHomAlgorithm(homKeyMap.get(tableName+columnName), 5);
									double result = homAlg.decrypt(secretShare);
//									System.out.println(result + "\t");
									stringBuilder.append(result + "\t");
									showArea.setText(stringBuilder.toString());
								}else{
									stringBuilder.append("��ѯ����!");
									showArea.setText(stringBuilder.toString());
									System.out.println("��ѯ����SelectDemoV2.print����");
								}
							}
						}else{
							//������ʽ���Ǻ���
							Column columnItem  = (Column)item;
							String columnName = columnItem.getColumnName();
							String detColumnName = NameHide.getDETName(NameHide.getSecretName(columnName));	
							if(metaOfTable.size() == 1 && columnItem.getTable().getName() == null){
								byte[] resultDET = DETAlgorithm.decrypt(rs.getString(detColumnName), detKeyMap.get(columnName));
								if (resultDET != null) {
//									System.out.println(new String(resultDET) + "\t");
									stringBuilder.append(new String(resultDET) + "\t");
									showArea.setText(stringBuilder.toString());
								}
							}else{
								if(metaOfTable.size() != 1 && columnItem.getTable().getName() == null){
									stringBuilder.append("���漰�����Ĳ���ʱ��������Ҫ���ṩ��Ӧ�ı�������sum(employee.salary)");
									showArea.setText(stringBuilder.toString());
									
								}else{	
									//������ִ�е�����������ǰָ���˱���:tableName.columnName	
									String tableName = columnItem.getTable().getName();
									byte[] resultDET = DETAlgorithm.decrypt(rs.getString(detColumnName), detKeyMap.get(tableName+columnName));
//									System.out.println(new String(resultDET+ "\t"));
									stringBuilder.append(new String(resultDET) + "\t");
									showArea.setText(stringBuilder.toString());
								}			
							}	
		
						}		
					}
					stringBuilder.append("\n");
					showArea.setText(stringBuilder.toString());
				}
		}
		
	}

	
	public String selectReconstruct(Select select,Map<String,MetaDataManager> metaOfTable) throws JSQLParserException{

		//buffer�������ع�SQL����ʱ�ַ�����
		StringBuilder buffer = new StringBuilder();
		/*
		 * ���������Ƕ�SQL�����н�����������еĸ������ֽ�����Ϊһ��������
		 * Select --> SelectBody --> PlainSelect
		 */
		//���µ������ǽ�select�е�SelectItem������select��from֮��Ĳ��֣���Ҳ����Ҫ��ѯ����Щ����ȡ��list�У�����ͨ��getItemsList()��ȡ���list
		PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
		//����һ��SelectDeparserV2���󣬸ö����buffer��Ϊ��������һ��ExpressionDeParser����
		SelectDeparserV2 selectDeparser = new SelectDeparserV2(metaOfTable);
		ExpressionDeParser expressionDeparser = new ExpressionDeParser(selectDeparser,buffer);
		selectDeparser.setBuffer(buffer);
		selectDeparser.setExpressionVisitor(expressionDeparser);
		plainSelect.accept(selectDeparser);
		return buffer.toString()+";";
		
	}
	
	public static void handler(Select select,Connection conn,JTextPane showArea,StringBuilder stringBuilder){
		try {
			conn.setAutoCommit(false);
			Statement smt = conn.createStatement();
			SelectDemoV2 selectDep = new SelectDemoV2();
			CCJSqlParserManager parserManager = new CCJSqlParserManager();
			Select orgSelect = (Select)parserManager.parse(new StringReader(select.toString()));
			/*���µ������ǽ�select�е�SelectItem������select��from֮��Ĳ��֣���Ҳ����Ҫ��ѯ����Щ����ȡ��list�У�����ͨ��getItemsList()��ȡ���list
			���ǻ�ȡ���ĵ�SelectItem��Ŀ����Ϊ����print������ʹ�á�
			 */
			PlainSelect plainSelectOrg = (PlainSelect)orgSelect.getSelectBody();
			List<SelectItem> plainItemList = plainSelectOrg.getSelectItems(); 				

			/*�����Զ���һ��fromitem�Ľ������������ǻ�ȡfrom...�е����б��������ڹ���metaOfTable
			 */
			FromItem fromItem = plainSelectOrg.getFromItem();
			FromItemDeparser fromItemDeparser = new FromItemDeparser();
			fromItem.accept(fromItemDeparser);   //������from����ı������tableNameList������
			if(plainSelectOrg.getJoins() != null){
				if(plainSelectOrg.getJoins().size() == 1){
					String joinTableName = ((Table)plainSelectOrg.getJoins().get(0).getRightItem()).getName();
					ClientDemo.tableNameList.add(joinTableName);
				}else{
					stringBuilder.append("join�����ұ��ʽ������,��������д��ڳ���һ����join����");
					showArea.setText(stringBuilder.toString());
					
				}
			}
			//���ǵ�������漰�ı�ֹһ����������Ҫ����һ��Map����<������������Ӧ��Ԫ����>  ��ȡ˳��һ��
			Map<String,MetaDataManager> metaOfTable = new LinkedHashMap<String,MetaDataManager>();
			
			for(int tableIndex = 0; tableIndex < ClientDemo.tableNameList.size();tableIndex++){			
				MetaDataManager metaManager = new MetaDataManager();
				String tableName = ClientDemo.tableNameList.get(tableIndex);
				metaManager.fetchMetaData(tableName);
				metaOfTable.put(tableName,metaManager);			
			}		
			//����б�
			ClientDemo.tableNameList.clear();
			
			//��ʼ��ʽselect������
			String encSQL = selectDep.selectReconstruct(select,metaOfTable);	
			
			for(int tableIndex = 0; tableIndex < ClientDemo.tableNameList.size();tableIndex++){			
				//�����ǰ��Ҫ��ѯDET�У����Ȱ�ȥRND�㣬�ٲ�ѯ������ٰ���RND��
				String tableName = ClientDemo.tableNameList.get(tableIndex);
				String peelOff = RNDOnion.peelOffRND(tableName, metaOfTable.get(tableName).getAllDETColumnName(), "123456");
				smt.executeUpdate(peelOff);
			}
			
			ResultSet rs = null;
			try{	
				//���潫����������SQL����ύ�����ݿ�ִ�У������ڴ�֮ǰ����Ҫ��ȥRND��	
				if (encSQL != null) {
					rs = smt.executeQuery(encSQL);  
					stringBuilder.append("\n��ѯ���Ϊ��\n");
					showArea.setText(stringBuilder.toString());
					if (plainItemList != null && metaOfTable!=null) {
						selectDep.print(metaOfTable,rs,plainItemList,showArea,stringBuilder);    
					}else {
						System.out.println("plainItemList"+" "+"metaOfTable����Ϊ��");
					}
					
					
				}
				conn.setAutoCommit(true);
			}catch(Exception e){
				conn.rollback();
				showArea.setText("�﷨��������");
				System.out.println("SelectDemoV2�����������쳣");
				e.printStackTrace();
				return;
			}
			
			//��ɲ�ѯ���ܺ�����������������RND��
			for(int tableIndex = 0; tableIndex < ClientDemo.tableNameList.size();tableIndex++){			
				//�����ǰ��Ҫ��ѯDET�У����Ȱ�ȥRND�㣬�ٲ�ѯ������ٰ���RND��
				String tableName = ClientDemo.tableNameList.get(tableIndex);
				String packOn = RNDOnion.packOnRND(tableName, metaOfTable.get(tableName).getAllDETColumnName(), "123456");
				smt.executeUpdate(packOn);
			}
			smt.close();
			rs.close();
			
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
