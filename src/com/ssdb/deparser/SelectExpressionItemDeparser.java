package com.ssdb.deparser;

import java.util.List;
import java.util.Map;

import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/**
 * �ҰѶ���select...where���ֵĽ������ܷ�����һ�����������У�SelectExpressionItemDeparser
 * ר����������select...from�м�Ĳ���				 
 * 
 *
 */
public class SelectExpressionItemDeparser extends ExpressionDeParser{

	/*
	 * ����AllColumn��AllTableColumn������⣬selectItem���ᱻ����һ��ExpressionItem���Դ������е�
	 * Ԫ��������ExpressionDeparser������
	 */
	private StringBuilder buffer;
	//private SelectVisitor selectVisitor;
	private Map<String, MetaDataManager> metaOfTable;

	
	public SelectExpressionItemDeparser(Map<String, MetaDataManager> metaOfTable,StringBuilder buffer){
		this.metaOfTable = metaOfTable;
		this.buffer = buffer;
	}

	public void visit(Column tableColumn) {
		// TODO Auto-generated method stub
		try {
	      
			//����ǵ�������û���ṩ�����������
			if(metaOfTable.size() == 1 && tableColumn.getTable().getName() == null){
				String secretName = NameHide.getSecretName(tableColumn.getColumnName());
				//buffer.append(NameHide.getDETName(secretName));
				String detName = NameHide.getDETName(secretName);
				buffer.append(detName);
			}else{
				if((metaOfTable.size() != 1 && tableColumn.getTable().getName() == null)){
					System.out.println("���漰�����Ĳ���ʱ��������Ҫ���ṩ��Ӧ�ı�������employee.salary");
					
				}else{
					//����ṩ�˱�������Ҫ������һ�����
					String tableName = tableColumn.getTable().getName();
					String secretName = NameHide.getSecretName(tableColumn.getColumnName());
					//buffer.append(NameHide.getDETName(secretName));
					String detName = NameHide.getDETName(secretName);
					buffer.append(tableName+"."+detName);
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Ϊ���ܹ���������ʹ�üӷ�̬ͬ��������Ҫ��sum��avg����������д ���磺select sum(id) from test where id =
	 * 1; ������Ҫ��дΪ:SELECT
	 * sum(di_HOM1),sum(di_HOM2),sum(di_HOM3),sum(di_HOM4),sum(di_HOM5) FROM
	 * test WHERE di_DET = '8uNsSDheptE=';
	 * ���������һ��sum������չ��5�����������ǻ�Ҫ��ԭ�����������е������޸�Ϊ��Ӧ��HOM������
	 */
	public void visit(Function function) {
		// TODO Auto-generated method stub
		try {
			if (function.getName().toLowerCase().equals("sum") || function.getName().toLowerCase().equals("avg")) {
				//List<Function> functions = new ArrayList<Function>();
				ExpressionList parameters = function.getParameters();
				List<Expression> listExp = parameters.getExpressions();
				/*
				 * �����ǵĳ����У����Ǽ���SQL����е���sum(id)��avg(id)�ĺ������Դ�������Ҫ��ȡ���е�������
				 * ���и�д��sum(di_HOM1)...sum(di_HOM5)
				 * ����Ƕ���ѯ��������Ҫ�ж�
				 */
				Column columnPara = (Column) listExp.get(0);
				/*�ڽ���������ʱ������ֻ��Ҫ����������ȴû���ṩ��������������ʱ�������ʾ��Ϣ���˳�����
				 * ����������ǲ���Ҫ���⴦��
				 */
				if((metaOfTable.size() != 1 && columnPara.getTable().getName() == null)){
					System.out.println("���漰�����Ĳ���ʱ��sum()��avg()��������Ҫ���ṩ��������sum(employee.salary)");
					System.exit(0);
				}
				String secretColumnName = NameHide.getHOMName(NameHide.getSecretName(columnPara.getColumnName()));
				for (int index_fun = 0; index_fun < 5; index_fun++) {
					Function felement = new Function();
					// ���õ�ǰ��functionԪ�صĺ�������
					felement.setName(function.getName());
					// �������ĳ�di_HOM1��di_HOME2...
					columnPara.setColumnName(secretColumnName + (index_fun + 1));
					listExp.set(0, columnPara);
					parameters.setExpressions(listExp);
					// ��functionԪ���е�parameter���֣�Ҳ����sum()�������еĲ��֣�����Ϊ�����޸ĺ��ֵ����di_HOM1
					felement.setParameters(parameters);
					// ����ǰ��functionԪ����ӵ�List<function>��,ע��5��sum��������Ҫ���4�����š�
					if (index_fun == 4) {
						buffer.append(felement);
					} else {
						buffer.append(felement).append(",");
					}
				}
			} else {
				super.visit(function);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}



}