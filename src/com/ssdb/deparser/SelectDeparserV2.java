package com.ssdb.deparser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ssdb.core.MetaDataManager;
import com.ssdb.core.NameHide;
import com.ssdb.demo.ClientDemo;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SelectDeparserV2 implements SelectVisitor, OrderByVisitor, SelectItemVisitor, FromItemVisitor, PivotVisitor{

    private StringBuilder buffer;
    private ExpressionVisitor expressionVisitor;
    Map<String, MetaDataManager> metaOfTable;
    
    public SelectDeparserV2(){
    	
    }
    public SelectDeparserV2(Map<String, MetaDataManager> metaOfTable) {
    	this.metaOfTable = metaOfTable;
    }
    
    public SelectDeparserV2(Map<String, MetaDataManager> metaOfTable,ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        this.buffer = buffer;
        this.expressionVisitor = expressionVisitor;
        this.metaOfTable = metaOfTable;
    }

    @Override
    public void visit(Pivot pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT (")
                .append(PlainSelect.getStringList(pivot.getFunctionItems()))
                .append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN ")
                .append(PlainSelect.getStringList(pivot.getInItems(), true, true))
                .append(")");
    }

    @Override
    public void visit(PivotXml pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT XML (")
                .append(PlainSelect.getStringList(pivot.getFunctionItems()))
                .append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN (");
        if (pivot.isInAny()) {
            buffer.append("ANY");
        } else if (pivot.getInSelect() != null) {
            buffer.append(pivot.getInSelect());
        } else {
            buffer.append(PlainSelect.getStringList(pivot.getInItems()));
        }
        buffer.append("))");
    }


    /**
     * FromItem��Ϊ����������е�������SelectItem����
     * 1.Table:select...from table1
     * 2.subJoin:select...from table1 join table2
     * 3.subselect:select...from (select...from)
     * �����Table��FromItem��һ�����
     */
    @Override
    public void visit(Table tableName) {
        buffer.append(tableName.getFullyQualifiedName());
        //��������ӵ��ռ�����
        ClientDemo.tableNameList.add(tableName.getName());
        Pivot pivot = tableName.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }
    }
    
    @Override
    public void visit(SubSelect subSelect) {
        buffer.append("(");
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                withItem.accept(this);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        //����Select�������Ϊ��ǰ�Ľ�����
        subSelect.getSelectBody().accept(this);
        buffer.append(")");
        Pivot pivot = subSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            buffer.append(alias.toString());
        }
    }


    @Override
    public void visit(SubJoin subjoin) {
        buffer.append("(");
        subjoin.getLeft().accept(this);
        deparseJoin(subjoin.getJoin());
        buffer.append(")");

        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        buffer.append(lateralSubSelect.toString());
    
    }
    
    @Override
    public void visit(ValuesList valuesList) {
        buffer.append(valuesList.toString());
    }

	@Override
	public void visit(AllColumns allColumns) {
		// TODO Auto-generated method stub
/*		List<String> encColumnName;
		try {
			encColumnName = metaManager.getAllEncColumnName();		
			int size =  encColumnName.size();
			for(int index = 0; index < size;index++){
					buffer.append(encColumnName.get(index));
					if(index != (size-1)){
						buffer.append(",");
					}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		 buffer.append("*");
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
		// TODO Auto-generated method stub
		 buffer.append(allTableColumns.getTable().getFullyQualifiedName()).append(".*");
	}
 
	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		// TODO Auto-generated method stub
		SelectExpressionItemDeparser selectExpressionItemDeparser = new SelectExpressionItemDeparser(metaOfTable,buffer);
    	selectExpressionItem.getExpression().accept(selectExpressionItemDeparser);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }
	}

	@Override
	//��дOrderBy��䣬��������������ָ�дΪOPE����
	public void visit(OrderByElement orderBy) {
	        orderBy.getExpression().accept(expressionVisitor);
	        if (!orderBy.isAsc()) {
	            buffer.append(" DESC");
	        } else if (orderBy.isAscDescPresent()) {
	            buffer.append(" ASC");
	        }
	        if (orderBy.getNullOrdering() != null) {
	            buffer.append(' ');
	            buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
	        }
}


	@Override
	public void visit(PlainSelect plainSelect) {
		// TODO Auto-generated method stub
		//�Ƿ�ʹ������
    	if (plainSelect.isUseBrackets()) {
            buffer.append("(");
        }
        buffer.append("SELECT "); 
        
        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            buffer.append(skip).append(" ");
        }

        First first = plainSelect.getFirst();
        if (first != null) {
            buffer.append(first).append(" ");
        }
        
        if (plainSelect.getDistinct() != null) {
            buffer.append("DISTINCT ");
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append("ON (");
                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter.hasNext();) {
                    SelectItem selectItem = iter.next();
                    selectItem.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
                buffer.append(") ");
            }

        }
        Top top = plainSelect.getTop();
        if (top != null) {
            buffer.append(top).append(" ");
        }

        //��δ����Ǵ���Item���
        for (Iterator<SelectItem> iter = plainSelect.getSelectItems().iterator(); iter.hasNext();) {
            SelectItem selectItem = iter.next();
            selectItem.accept(this);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }

        if (plainSelect.getIntoTables() != null) {
            buffer.append(" INTO ");
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext();) {
                visit(iter.next());
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getFromItem() != null) {
            buffer.append(" FROM ");
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                deparseJoin(join);
            }
        }

        //��ȡwhere�Ӿ��������������޸�
        if (plainSelect.getWhere() != null) {
            buffer.append(" WHERE ");
            WhereExpressionDeparser whereDeparser = new WhereExpressionDeparser(metaOfTable,this,buffer);
            plainSelect.getWhere().accept(whereDeparser);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor);
        }

        if (plainSelect.getGroupByColumnReferences() != null) {
            buffer.append(" GROUP BY ");
            for (Iterator<Expression> iter = plainSelect.getGroupByColumnReferences().iterator(); iter.hasNext();) {
                Expression columnReference = iter.next();
                columnReference.accept(expressionVisitor);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getHaving() != null) {
            buffer.append(" HAVING ");
            plainSelect.getHaving().accept(expressionVisitor);
        }

        if (plainSelect.getOrderByElements() != null) {
            deparseOrderBy(plainSelect.isOracleSiblings(), plainSelect.getOrderByElements());
        }

        if (plainSelect.getLimit() != null) {
            deparseLimit(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            deparseOffset(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            deparseFetch(plainSelect.getFetch());
        }
        if (plainSelect.isForUpdate()) {
            buffer.append(" FOR UPDATE");
            if (plainSelect.getForUpdateTable() != null) {
                buffer.append(" OF ").append(plainSelect.getForUpdateTable());
            }
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(")");
        }
	}


    /**
     * �ú������ڴ���Join���ֵģ������ǵ�ϵͳ��û�����ر������������ֻ��Ҫ�޸�ON���֮����������־Ϳ�����
     * ����ķ������Զ���һ��ON���ʽ�Ľ������������н�������������
     * @see Join
     * @param join
     */
    public void deparseJoin(Join join) {
        if (join.isSimple()) {
            buffer.append(", ");
        } else {

            if (join.isRight()) {
                buffer.append(" RIGHT");
            } else if (join.isNatural()) {
                buffer.append(" NATURAL");
            } else if (join.isFull()) {
                buffer.append(" FULL");
            } else if (join.isLeft()) {
                buffer.append(" LEFT");
            } else if (join.isCross()) {
                buffer.append(" CROSS");
            }

            if (join.isOuter()) {
                buffer.append(" OUTER");
            } else if (join.isInner()) {
                buffer.append(" INNER");
            }

            buffer.append(" JOIN ");

        }

        
        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.getOnExpression() != null) {
            buffer.append(" ON ");
            /*����Ŀǰ��֧�ֵ�ֵ���ӣ����ֻ��Ҫ�޸����е�visit(EqualTo)��������
             * ������on table1.id = table2.id
             * ��д��on table1.di_DET = table2.di_DET
             */
            ExpressionDeParser onExpressionDeparser = new ExpressionDeParser(this,buffer){

				@Override
				public void visit(EqualsTo equalsTo) {
					// TODO Auto-generated method stub
					try {
					Column leftColumn = (Column)equalsTo.getLeftExpression();
					String leftColumnName = leftColumn.getColumnName();	
					String detNameLeft = NameHide.getDETName(NameHide.getSecretName(leftColumnName));
					leftColumn.setColumnName(detNameLeft);
					
					Column rightColumn = (Column)equalsTo.getRightExpression();
					String rightColumnName = rightColumn.getColumnName();
					String detNameRight = NameHide.getDETName(NameHide.getSecretName(rightColumnName));
					rightColumn.setColumnName(detNameRight);
					
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					super.visit(equalsTo);
				}
            	           	
            };
            join.getOnExpression().accept(onExpressionDeparser);
        }
        if (join.getUsingColumns() != null) {
            buffer.append(" USING (");
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext();) {
                Column column = iterator.next();
                buffer.append(column.getFullyQualifiedName());
                if (iterator.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }

    }
    /**
     * ����OrderByԪ�ص�,���ǵ�Ŀ���ǽ�OrderBy�����������дΪOPE����
     */
    public void deparseOrderBy(List<OrderByElement> orderByElements) {
        deparseOrderBy(false, orderByElements);
    }

    /**
     * ���������룺
     * XXXX order by id,salary;
     * ��дΪ��XXXX order by di_OPE,yralas_OPE;
     * 
     * 
     */
    public void deparseOrderBy(boolean oracleSiblings, List<OrderByElement> orderByElements){
        if (oracleSiblings) {
            buffer.append(" ORDER SIBLINGS BY ");
        } else {
            buffer.append(" ORDER BY ");
        }

       
        for (Iterator<OrderByElement> iter = orderByElements.iterator(); iter.hasNext();) {
        	try {	 
        		OrderByElement orderByElement = iter.next();                 
        		Column column = (Column)orderByElement.getExpression();
        		String columnName = column.getColumnName();
        		//System.out.println(columnName);
        		column.setColumnName(NameHide.getOPEName(NameHide.getSecretName(columnName)));		
        		orderByElement.accept(this);
        		if (iter.hasNext()) {
        			buffer.append(", ");
        		}
        	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        	}
        }
    }

    public void deparseLimit(Limit limit) {
        // LIMIT n OFFSET skip
        if (limit.isRowCountJdbcParameter()) {
            buffer.append(" LIMIT ");
            buffer.append("?");
        } else if (limit.getRowCount() >= 0) {
            buffer.append(" LIMIT ");
            buffer.append(limit.getRowCount());
        } else if (limit.isLimitNull()) {
            buffer.append(" LIMIT NULL");
        }

        if (limit.isOffsetJdbcParameter()) {
            buffer.append(" OFFSET ?");
        } else if (limit.getOffset() != 0) {
            buffer.append(" OFFSET ").append(limit.getOffset());
        }

    }

    public void deparseOffset(Offset offset) {
        // OFFSET offset
        // or OFFSET offset (ROW | ROWS)
        if (offset.isOffsetJdbcParameter()) {
            buffer.append(" OFFSET ?");
        } else if (offset.getOffset() != 0) {
            buffer.append(" OFFSET ");
            buffer.append(offset.getOffset());
        }
        if (offset.getOffsetParam() != null) {
            buffer.append(" ").append(offset.getOffsetParam());
        }

    }

    public void deparseFetch(Fetch fetch) {
        // FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
        buffer.append(" FETCH ");
        if (fetch.isFetchParamFirst()) {
            buffer.append("FIRST ");
        } else {
            buffer.append("NEXT ");
        }
        if (fetch.isFetchJdbcParameter()) {
            buffer.append("?");
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(" ").append(fetch.getFetchParam()).append(" ONLY");

    }
    
    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }
	
    @Override
	public void visit(SetOperationList setOperationList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WithItem withItem) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * ��������������Զ��ģ�ר����������Order by��Group by�Ӿ��е��������������ط���Column����Ч��
	 * @param column
	 */
	/*public void visit(Column column) {
		try {
	    String columnName = column.getColumnName();	    
	    column.setColumnName(NameHide.getOPEName(NameHide.getSecretName(columnName)));		
		buffer.append(column.getFullyQualifiedName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }*/

}
