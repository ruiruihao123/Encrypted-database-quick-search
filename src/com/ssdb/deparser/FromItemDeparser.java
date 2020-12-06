package com.ssdb.deparser;

import com.ssdb.demo.ClientDemo;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class FromItemDeparser extends SelectDeParser{
	   /**
     * FromItem��Ϊ����������е�������SelectItem����
     * 1.Table:select...from table1
     * 2.subJoin:select...from table1 join table2
     * 3.subselect:select...from (select...from)
     * �����Table��FromItem��һ�����
     */
    public void visit(Table tableName) {
        //��������ӵ��ռ�����
    	 ClientDemo.tableNameList.add(tableName.getName());
    	
    }

    /**
     * ����������ڴ���from...�г��ֵ�Ƕ��select��䣬������Ϊǿ���޶�ֻ��������Ƕ�ף������˳�����
     * @param subSelect
     */
	public void visit(SubSelect subSelect) {
		// TODO Auto-generated method stub
		PlainSelect plainSelect = (PlainSelect)subSelect.getSelectBody();
		FromItem fromItem = plainSelect.getFromItem();
		if(fromItem instanceof Table){
			Table table = (Table)fromItem;
			ClientDemo.tableNameList.add(table.getName());
		}else{
			System.out.println("ֻ��������Ƕ�ף�");
			System.exit(0);
		}
	}

	public void visit(SubJoin join) {
		// TODO Auto-generated method stub
		FromItem leftFromItem = join.getLeft();
		System.err.println(leftFromItem.toString());
		leftFromItem.accept(this);
		/*if(leftFromItem instanceof Table){
			Table leftJoinTable = (Table)leftFromItem;
			ClientDemo.tableNameList.add(leftJoinTable.getName());
		}else{
			System.out.println("join����������ʽ�����Ǳ�����employee join manager");
			System.exit(0);
		}*/
		 FromItem rightFromItem = (FromItem)join.getJoin().getRightItem();
		 System.out.println(rightFromItem.toString());
		 rightFromItem.accept(this);
		/*if(rightFromItem instanceof Table){
			Table rightJoinTable = (Table)rightFromItem;
			ClientDemo.tableNameList.add(rightJoinTable.getName());
		}else{
			System.out.println("join�������ұ��ʽ�����Ǳ�����employee(����) inner join manager(����)");
			System.exit(0);
		}*/
	}
    
}
