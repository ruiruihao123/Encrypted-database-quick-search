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
     * FromItem分为三种情况（有点类似于SelectItem）：
     * 1.Table:select...from table1
     * 2.subJoin:select...from table1 join table2
     * 3.subselect:select...from (select...from)
     * 这里的Table是FromItem的一种情况
     */
    public void visit(Table tableName) {
        //将表名添加到收集器中
    	 ClientDemo.tableNameList.add(tableName.getName());
    	
    }

    /**
     * 这个函数用于处理from...中出现的嵌套select语句，我们认为强制限定只能有两层嵌套，否则退出程序
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
			System.out.println("只允许两层嵌套！");
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
			System.out.println("join操作的左表达式必须是表名：employee join manager");
			System.exit(0);
		}*/
		 FromItem rightFromItem = (FromItem)join.getJoin().getRightItem();
		 System.out.println(rightFromItem.toString());
		 rightFromItem.accept(this);
		/*if(rightFromItem instanceof Table){
			Table rightJoinTable = (Table)rightFromItem;
			ClientDemo.tableNameList.add(rightJoinTable.getName());
		}else{
			System.out.println("join操作的右表达式必须是表名：employee(表名) inner join manager(表名)");
			System.exit(0);
		}*/
	}
    
}
