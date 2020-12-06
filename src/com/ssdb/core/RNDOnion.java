package com.ssdb.core;
import java.util.List;


//�������ģ�ͽ��д���
public class RNDOnion {
	//����������ܲ�
	public static String packOnRND(String tableName ,List<String> columnNameList,String password){
		
		StringBuilder udfBuffer = new StringBuilder();
		udfBuffer.append("update "+tableName + " set ");
		String columnName = "";

		for(int index = 0; index < columnNameList.size(); index++){
				columnName = columnNameList.get(index);
				udfBuffer.append(columnName + " = to_base64(aes_encrypt("+columnName+",concat(rowid,"+"'"+password+"')))" );
				if(index != (columnNameList.size() - 1)){
					udfBuffer.append(",");
				}			  
			}
		return udfBuffer.toString();	
	}
	
	public static String peelOffRND(String tableName ,List<String> columnNameList,String password){
		StringBuilder udfBuffer = new StringBuilder();
		udfBuffer.append("update "+tableName + " set ");
		String columnName = "";

		for(int index = 0; index < columnNameList.size(); index++){
			columnName = columnNameList.get(index);
			udfBuffer.append(columnName + " = aes_decrypt(from_base64("+columnName+"),concat(rowid,"+"'"+password+"'"+"))");
			if(index != (columnNameList.size() - 1)){
				udfBuffer.append(",");
			}
		}
	
		return udfBuffer.toString();	
	}
	public void testRNDOnion(){
		System.out.println("�������ģ��������");
	}
/*	public static void main(String[] args) throws SQLException, NoSuchAlgorithmException{
		String content = "12345678";
		Connection conn = ConnectionMySQL.openConnection();
		Statement smt = conn.createStatement();
		Key detKey = KeyManager.generateDETKey("123456", "test", "det");
		String encContent = DETAlgorithm.encrypt(content, detKey);
		smt.execute("insert into test(name) values('"+encContent+"');");
		List<String> columnNameList =new ArrayList<String>();
		columnNameList.add("name");
		//String packOnContent = packOnRND("test", columnNameList, "123456");
		int result = 0;
		long start = System.currentTimeMillis();
		//result = smt.executeUpdate("update test set name = to_base64(aes_encrypt(name,'123456'));");
		//result = smt.executeUpdate(packOnContent);
		long end = System.currentTimeMillis();
		System.out.println("result = "+result+"Time = "+(end - start));
		
	}*/
}
