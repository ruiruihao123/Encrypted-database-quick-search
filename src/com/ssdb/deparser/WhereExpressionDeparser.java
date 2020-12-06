package com.ssdb.deparser;

import java.security.Key;
import java.util.Map;

import com.ssdb.core.DETAlgorithm;
import com.ssdb.core.KeyManager;
import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;
import com.ssdb.core.OPEAlgorithm;
import com.ssdb.demo.ClientDemo;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
/**
 * �̳���Expression�࣬����д�����еķ�������������������������expression���ֵ�,ר�����ڴ���where�Ӿ�Ľ�����
 * ���캯���е�buffer�������ĵ�buffer��ͬ���ģ����Ƕ�ָ��ͬһ���ڴ�ռ䡣
 */

public class WhereExpressionDeparser extends ExpressionDeParser{

	Map<String, MetaDataManager> metaOfTable;
	
	/**
	 * 
	 * @param key ͨ�����캯����ȡ��Կ
	 * @param selectVisitor ������where�Ӿ��г���selectǶ�����ʱ��Ӧ�ý���˭������
	 * @param buffer ����ַ���
	 */
	public WhereExpressionDeparser(Map<String, MetaDataManager> metaOfTable,SelectVisitor selectVisitor,StringBuilder buffer){
		//��������Ĺ��캯��ǰ�������ȵ��ø���Ĺ��캯��
		super(selectVisitor,buffer);
	
		this.metaOfTable = metaOfTable;

	}
	/**
	 * ����������ڴ���where����� ����" a > 5"�����
	 * @param greaterThan ���ڱ��ʽ
	 */
	@Override
	public void visit(GreaterThan greaterThan) {
	
		//�ȴ�greaterThan�л�ȡ�������������������޸�
		//greaterThan.getLeftExpression().accept(this);
		Column c = (Column)greaterThan.getLeftExpression();
		String columnName = c.getColumnName();		
		//����tableName.columnName��������Կ
		/*�����ǰֻ�漰һ������ô��������where�Ӿ��е���������Ҫд�ɣ�tableName.columnName����ʽ
		 * ���������ǰ��ѯ�漰���������ǿ��Ҫ��where�Ӿ�ĸ�ʽ
		 */
		double[] opeKey = new double[3] ;
		if(metaOfTable.size() == 1 && c.getTable().getName() == null){	
			opeKey = metaOfTable.values().iterator().next().getOpeKey(columnName);
		}else{
			if(metaOfTable.size() != 1 && c.getTable().getName() == null){
				System.out.println("���漰�����Ĳ���ʱ��������where�Ӿ���ָ����������employee(����).salary(����)");
				System.exit(0);
			}else{
				String tableName = c.getTable().getName();
				opeKey = metaOfTable.get(tableName).getOpeKey(columnName);
			}			
		}		

		//�ұ��ʽ�а�����ope��������Ҫ������
		Expression rightExpression = greaterThan.getRightExpression();
		double rightValue = 0.0;
		if(rightExpression instanceof DoubleValue){
			rightValue = ((DoubleValue)rightExpression).getValue();
		}else{
			if(rightExpression instanceof LongValue){
				rightValue = ((LongValue)rightExpression).getValue();
			}else{
				System.out.println("a > b�ıȽ������д��ڲ�֧�ֵ���������");
			}
		}
		//��ǰ���ǵĲ���ʱ��Ĭ�ϲ���ʽ���ұ������������sensΪ1
		
		OPEAlgorithm ope = new OPEAlgorithm(opeKey[0],opeKey[1],opeKey[2]);
		DoubleValue encDoubleValue  = new DoubleValue(Double.toString(ope.nindex(rightValue+1,false)));
		String secretName;
		try {
			secretName = NameHide.getSecretName(c.getColumnName());
			/*ע�����ﲻ��������buffer.append(NameHide.getOPEName(secretName));
			 * ��Ȼ�ᵼ���ظ������
			 */
			c.setColumnName(NameHide.getOPEName(secretName));					
			greaterThan.setRightExpression(encDoubleValue);
			visitOldOracleJoinBinaryExpression(greaterThan, " >= ");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�滻����ʽ����ʧ��!");
		}		        
	}

	
	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		// TODO Auto-generated method stub
		Column c = (Column)greaterThanEquals.getLeftExpression();
		String columnName = c.getColumnName();
		
		
		//����tableName.columnName��������Կ
		/*�����ǰֻ�漰һ������ô��������where�Ӿ��е���������Ҫд�ɣ�tableName.columnName����ʽ
		 * ���������ǰ��ѯ�漰���������ǿ��Ҫ��where�Ӿ�ĸ�ʽ
		 */
		double[] opeKey = new double[3];
		if(metaOfTable.size() == 1 && c.getTable().getName() == null){
			opeKey = metaOfTable.values().iterator().next().getOpeKey(columnName);
		}else{
			if(metaOfTable.size() != 1 && c.getTable().getName() == null){
				System.out.println("���漰�����Ĳ���ʱ��������where�Ӿ���ָ����������employee(����).salary(����)");
				return;
			}else{
				String tableName = c.getTable().getName();
				opeKey = metaOfTable.get(tableName).getOpeKey(columnName);
			}			
		}		

		Expression rightExpression = greaterThanEquals.getRightExpression();
		double rightValue = 0.0;
		if(rightExpression instanceof DoubleValue){
			rightValue = ((DoubleValue)rightExpression).getValue();
		}else{
			if(rightExpression instanceof LongValue){
				rightValue = ((LongValue)rightExpression).getValue();
			}else{
				System.out.println("a >= b���Ҳ��������ڲ�֧�ֵ���������");
			}
		}	
		OPEAlgorithm ope = new OPEAlgorithm(opeKey[0],opeKey[1],opeKey[2]);
		DoubleValue encDoubleValue  = new DoubleValue(Double.toString(ope.nindex(rightValue,false)));
		String secretName;
		try {
			secretName = NameHide.getSecretName(c.getColumnName());
			c.setColumnName(NameHide.getOPEName(secretName));					
			greaterThanEquals.setRightExpression(encDoubleValue);
			super.visit(greaterThanEquals);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�滻����ʽ����ʧ��!");
		}		        
	}
	@Override
	public void visit(MinorThan minorThan) {
		// TODO Auto-generated method stub
		//�ȴ�greaterThan�л�ȡ�������������������޸�
		//greaterThan.getLeftExpression().accept(this);
		Column c = (Column)minorThan.getLeftExpression();
		String columnName = c.getColumnName();
		
		//����tableName.columnName��������Կ
		/*�����ǰֻ�漰һ������ô��������where�Ӿ��е���������Ҫд�ɣ�tableName.columnName����ʽ
		 * ���������ǰ��ѯ�漰���������ǿ��Ҫ��where�Ӿ�ĸ�ʽ
		 */
		double[] opeKey = new double[3];
		if(metaOfTable.size() == 1 && c.getTable().getName() == null){
			opeKey = metaOfTable.values().iterator().next().getOpeKey(columnName);
		}else{
			if(metaOfTable.size() != 1 && c.getTable().getName() == null){
				System.out.println("���漰�����Ĳ���ʱ��������where�Ӿ���ָ����������employee(����).salary(����)");
				return;
			}else{
				String tableName = c.getTable().getName();
				opeKey = metaOfTable.get(tableName).getOpeKey(columnName);
			}			
		}		

		//�ұ��ʽ�а�����ope��������Ҫ������
		Expression rightExpression = minorThan.getRightExpression();
		double rightValue = 0.0;
		if(rightExpression instanceof DoubleValue){
			rightValue = ((DoubleValue)rightExpression).getValue();
		}else{
			if(rightExpression instanceof LongValue){
				rightValue = ((LongValue)rightExpression).getValue();
			}else{
				System.out.println("a < b���Ҳ��������ڲ�֧�ֵ���������");
			}
		}	
		//��ǰ���ǵĲ���ʱ��Ĭ�ϲ���ʽ���ұ������������sensΪ1
		
		OPEAlgorithm ope = new OPEAlgorithm(opeKey[0],opeKey[1],opeKey[2]);
		DoubleValue encDoubleValue  = new DoubleValue(Double.toString(ope.nindex(rightValue,false)));
		String secretName;
		try {
			secretName = NameHide.getSecretName(c.getColumnName());
			/*ע�����ﲻ��������buffer.append(NameHide.getOPEName(secretName));
			 * ��Ȼ�ᵼ���ظ������
			 */
			c.setColumnName(NameHide.getOPEName(secretName));					
			minorThan.setRightExpression(encDoubleValue);
			super.visit(minorThan);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�滻����ʽ����ʧ��!");
		}		        
	}
	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		// TODO Auto-generated method stub
		Column c = (Column)minorThanEquals.getLeftExpression();
		String columnName = c.getColumnName();
		
		//����tableName.columnName��������Կ
		/*�����ǰֻ�漰һ������ô��������where�Ӿ��е���������Ҫд�ɣ�tableName.columnName����ʽ
		 * ���������ǰ��ѯ�漰���������ǿ��Ҫ��where�Ӿ�ĸ�ʽ
		 */
		double[] opeKey = new double[3];
		if(metaOfTable.size() == 1 && c.getTable().getName() == null){
			opeKey = metaOfTable.values().iterator().next().getOpeKey(columnName);
		}else{
			if(metaOfTable.size() != 1 && c.getTable().getName() == null){
				System.out.println("���漰�����Ĳ���ʱ��������where�Ӿ���ָ����������employee(����).salary(����)");
				return;
			}else{
				String tableName = c.getTable().getName();
				opeKey = metaOfTable.get(tableName).getOpeKey(columnName);
			}			
		}		

		Expression rightExpression = minorThanEquals.getRightExpression();
		double rightValue = 0.0;
		if(rightExpression instanceof DoubleValue){
			rightValue = ((DoubleValue)rightExpression).getValue();
		}else{
			if(rightExpression instanceof LongValue){
				rightValue = ((LongValue)rightExpression).getValue();
			}else{
				System.out.println("a <= b���Ҳ��������ڲ�֧�ֵ���������");
			}
		}
		OPEAlgorithm ope = new OPEAlgorithm(opeKey[0],opeKey[1],opeKey[2]);
		DoubleValue encDoubleValue  = new DoubleValue(Double.toString(ope.nindex(rightValue+1,false)));
		String secretName;
		try {
			secretName = NameHide.getSecretName(c.getColumnName());
			c.setColumnName(NameHide.getOPEName(secretName));					
			minorThanEquals.setRightExpression(encDoubleValue);
			super.visit(minorThanEquals);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�滻����ʽ����ʧ��!");
		}		        
	}
	/**
	 * �����������Ӧ������"a = ?"�����
	 * @param equalsTo
	 */
	@Override
	public void visit(EqualsTo equalsTo) {
		// TODO Auto-generated method stub
		try {
		Column c = (Column)equalsTo.getLeftExpression();
		String columnName = c.getColumnName();
		//��ֵ�ļӽ��ܵ���Կ����Ҫ����
		Key key = KeyManager.generateDETKey("1234567812345678", columnName, "det");		
		Expression rightExp = equalsTo.getRightExpression();
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
		
		/*
		 * ע����JSQLParserԴ���У�StringValue�Ĺ��캯����Щ��֡�
		 * public StringValue(String escapedValue) {
   		 *		// romoving "'" at the start and at the end
		 * 		value = escapedValue.substring(1, escapedValue.length() - 1);
		 *}
		 * ע�⿴�����ַ������ݸ����캯����ʱ�򣬹��캯��Ĭ��ȥ����ͷ�ͽ�β��һ���ַ�
		 * ������Ϊ����select�����StringValue�ǰ��������ŵģ��ڹ��캯������Ҫ��ȥ����Ե����š�
		 * ����StringValue.getValue()/setValue()���ǲ����������ŵģ������ȡ�������м��ֵ��
		 * ����������ʹ�ã�
		 *			StringValue encryptedRightValue = new StringValue("test");
		 * ʵ�ʱ������"es",������"test"����Ϊ��ʹû�е����ţ����캯����Ȼ��ȥ����ͷ�ͽ�β��һ���ַ���
		 * ������������ҪStringValue�б����"test"�Ļ���������Ҫ���ݸ����캯���Ĳ�����"'test'"�����ֶ����ϵ����š�
		 */

		String value = DETAlgorithm.encrypt(rightToStr, key);
		equalsTo.setRightExpression(new StringValue("'"+value+"'"));								
		String secretName;
		secretName = NameHide.getSecretName(c.getColumnName());		
		String detName = NameHide.getDETName(secretName);
		ClientDemo.encColumnNameList.add(detName);
		c.setColumnName(detName);	
		super.visit(equalsTo);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("�滻��ʽ��ʧ��!");
		}
		
	}

	
}
