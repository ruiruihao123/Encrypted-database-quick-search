package com.ssdb.core;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/*
 * Ԫ���ݹ���ģ�飬�����ݿ��е�Ԫ���ݱ��л�ȡ����Ϣ������ڱ���
 *
 */
public class MetaDataManager {


	private Map<String,String> dataTypeMeta;
	private Map<String,String> opeKeyMeta;
	private Map<String,String> homKeyMeta;
	private List<String> allColumnName;
	
	public MetaDataManager(){	
			dataTypeMeta = new HashMap<String,String>();
			opeKeyMeta = new HashMap<String,String>();
			homKeyMeta = new HashMap<String,String>();
			allColumnName = new ArrayList<String>();		
	}

	
	/*
	 * ����������ڻ�ȡ���Ԫ���ݣ�
	 * ����ָ�����е��������������columnNameList�У���Щ������������ʽ��
	 */
	
	public  List<String> getAllDETColumnName() throws SQLException{
		List<String> allEncColumnName = new ArrayList<String>();
		for(String name : allColumnName){   //allColumnName ͨ������fetchMetaData�õ�����Ҫȷ�� fetchMetaDataִ����
			try {
				allEncColumnName.add(NameHide.getDETName(NameHide.getSecretName(name)));
			} catch (Exception e) {
				System.out.println("MetaDataManager�޷���ȡ������Ϣ");
				e.printStackTrace();
			}
		}
		return allEncColumnName;
	}
	public List<String> getAllPlainColumnName(){
		return allColumnName;  //��������������������  ��Ҫȷ�� fetchMetaDataִ����
	}
	
	/**
	 * ��������������Ǹ��ݱ���tableName��metadata���л�ȡ����Ӧ�������е�Ԫ������Ϣ�������һ��map������
	 * �������������ִ������getXXX����֮ǰִ��
	 * @param tableName Ҫ��ȡ��Ϣ�ı���
	 * @return ����һ������<����,��������>��map����
	 */
	public void fetchMetaData(String tableName){
		try {		
			Connection conn = ConnectionMySQL.openConnection();
			Statement stmt = conn.createStatement();
			Key metaKey = KeyManager.generateDETKey("1234567812345678", "metadata", "det");			
			ResultSet rs = stmt.executeQuery("select * from metadata where tablename = '" + tableName + "';");
			//���Ա���������Ԫ������Ϣ��
			while(rs.next()){
				String columnName = new String(DETAlgorithm.decrypt(rs.getString("columnname"),metaKey));
				// <����,��������>�ŵ�map����--dataTypeMeta
				dataTypeMeta.put(columnName, new String(DETAlgorithm.decrypt(rs.getString("datatype"),metaKey)));
				String opeKeyEnc = rs.getString("opekey");
				String homKeyEnc = rs.getString("homkey");
				if(opeKeyEnc != null){
					opeKeyMeta.put(columnName,new String(DETAlgorithm.decrypt(rs.getString("opekey"),metaKey)));
				}
				if(homKeyEnc != null){
					homKeyMeta.put(columnName, new String(DETAlgorithm.decrypt(rs.getString("homkey"),metaKey)));	
				}				
				allColumnName.add(columnName);   //�����������������
			}
			conn.close();
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("��ȡԪ������Ϣʧ��");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ������������ڴ��������޸ı�Ľṹʱ������Ľṹ��Ϣ�洢��metadata���У����а��������֣������������������������͡�
	 * @param tableName ���Ǵ������Ǹ��������
	 * @param listColumn ����ʱ���е���Ϣ��������������������
	 */
	public static void storeMetaData(String tableName,List<ColumnDefinition> listColumn){	
		try {
			Connection conn = ConnectionMySQL.openConnection();
			Key metaKey = KeyManager.generateDETKey("1234567812345678", "metadata", "det");			
			String insertMetaData = "insert into metadata(tablename,columnname,datatype,opekey,homkey) values(?,?,?,?,?)";			
			PreparedStatement pstmt = conn.prepareStatement(insertMetaData);
			for(int i = 0;i < listColumn.size();i++){
				//�Ա��������м���
				pstmt.setString(1, tableName);
				String columnName = listColumn.get(i).getColumnName();
				pstmt.setString(2,  DETAlgorithm.encrypt(columnName,metaKey));
				String dataType = listColumn.get(i).getColDataType().getDataType();
				pstmt.setString(3, DETAlgorithm.encrypt(dataType,metaKey));
				//����Ҫ����ope�㷨����Կ�������������ֵ����ݣ�a,b,sens��������ַ��ͺ���ֵ�Ͷ���Ҫ
				//Ŀǰ����Ĭ����ֵ������int���ͣ��������ǵ�sensΪ1
				/*double[] opeKey = KeyManager.generateOpeKey(1.0);
				pstmt.setString(4, opeKey[0] + "," +opeKey[1] + "," + opeKey[2]);*/
				/*����hom����Կ��������Ҫ�������ĵ��������ͽ����жϣ��������ֵ�ͣ���Ҫ����һ��homkey
				      ������ַ��ͣ���homkey����ΪNULL;
				   homKeyʵ������һ��double�͵Ķ�ά���飬����������������һ���ַ���
				      Ŀǰ����Ĭ����ֵ��Ϊint�ͣ��Ժ����������һЩ��
				*/
				if(dataType.equals("int")){	
					//Ŀǰ����Ĭ����ֵ������int���ͣ��������ǵ�sensΪ1
					double[] opeKey = KeyManager.generateOpeKey(1.0);
					pstmt.setString(4, DETAlgorithm.encrypt(opeKey[0] + "," +opeKey[1] + "," + opeKey[2],metaKey));
					//��ά����Ĵ�СΪdouble[5][3]
					double[][] homKey = KeyManager.generateHomKey();
					StringBuilder keyBuffer = new StringBuilder();
					for(int index_row = 0; index_row < 5 ;index_row++){
						for(int index_col = 0;index_col < 3;index_col++){
							keyBuffer.append(homKey[index_row][index_col]);
							//����������һ��,�����һ��������Ϊ�зָ��
							if(index_col != 2){
								keyBuffer.append(",");
							}
						}
						//����������һ��,�����һ���ֺ���Ϊ�зָ��
						if(index_row != 4){
							keyBuffer.append(";");
						}
					}
					//��ת������Կ�ַ����洢�����ݿ���
					pstmt.setString(5,DETAlgorithm.encrypt(keyBuffer.toString(),metaKey));
				}else{
					if(dataType.equals("double")||dataType.equals("float")){
						//�����double�ͣ�sens��1E-8
						double[] opeKey = KeyManager.generateOpeKey(1E-8);
						pstmt.setString(4, DETAlgorithm.encrypt(opeKey[0] + "," +opeKey[1] + "," + opeKey[2],metaKey));
						//��ά����Ĵ�СΪdouble[5][3]
						double homKey[][] = KeyManager.generateHomKey();
						StringBuilder keyBuffer = new StringBuilder();
						for(int index_row = 0; index_row < 5 ;index_row++){
							for(int index_col = 0;index_col < 3;index_col++){
								keyBuffer.append(homKey[index_row][index_col]);
								//����������һ��,�����һ��������Ϊ�зָ��
								if(index_col != 2){
									keyBuffer.append(",");
								}
							}
							//����������һ��,�����һ���ֺ���Ϊ�зָ��
							if(index_row != 4){
								keyBuffer.append(";");
							}
						}
						//��ת������Կ�ַ����洢�����ݿ���
						pstmt.setString(5,DETAlgorithm.encrypt(keyBuffer.toString(),metaKey));						
				}else{
					//����12������"varchar"����,���java.sql.Types
					pstmt.setNull(4,12);
					pstmt.setNull(5,12);
				}
			}
				
				//���е�Ԥռλ���ľ���ֵ���Ѿ����ú��ˣ�����Ϳ���ִ������Ԥ���������
				//ע��������Ԥ����,����д��pstmt.executeUpdate(insertMetaData);����ᱨ��
				pstmt.executeUpdate();
			}
			conn.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * �������������������ȡ�������ͣ�������ִ��fetchMetaData(String tableName)
	 * @param columnName ����
	 * @return �����ж�Ӧ�����ĵ���������
	 */
	public String getDataType(String columnName){		
		//�����Ϊ��
		if(!dataTypeMeta.isEmpty()){
			return dataTypeMeta.get(columnName);
			
		}else{
			System.out.println("���Ȼ�ȡԪ����");
			return null;
		}
	}	
	/**
	 * ����������ڻ�ȡָ���е�OPE��Կ��������ִ��fetchMetaData(String tableName)
	 * @param columnName ����
	 * @return ����OPE��Կ
	 */
	public double[] getOpeKey(String columnName){
		if(!opeKeyMeta.isEmpty()){
			String[] opeKeyStr = opeKeyMeta.get(columnName).split(",");
			double[] opeKey = new double[3];
			opeKey[0] = Double.valueOf(opeKeyStr[0]);
			opeKey[1] = Double.valueOf(opeKeyStr[1]);
			opeKey[2] = Double.valueOf(opeKeyStr[2]);
			return opeKey;
		}else{
			return null;
		}
	}

	/**
	 * ����������ڻ�ȡָ�����HOM��Կ��������ִ��fetchMetaData(String tableName)
	 * ��ͨ��fetchMetaData(String tableName)��������Ԫ���ݱ��л�ȡ��HOM��Կ�ַ�����Ȼ������ͨ���������ת����double��
	 */
	public double[][] getHomKey(String columnName){
		if(!homKeyMeta.isEmpty()){
			double[][] homKey = new double[5][3];		
			//hom��Կ��double[5][3]�͵Ķ�ά���飬��ת�����ַ���ʱ����";"�����зָ��","�����зָ�
			if(homKeyMeta.get(columnName) != null){
				String[] homKeyStr_row = homKeyMeta.get(columnName).split(";");
				for(int index_row = 0;index_row < homKeyStr_row.length ; index_row++){
					String[] homKeyStr_col = homKeyStr_row[index_row].split(",");
					for(int index_col = 0;index_col < homKeyStr_col.length;index_col++){
						homKey[index_row][index_col] = Double.valueOf(homKeyStr_col[index_col]);
					}
				}		
				return homKey;
			}else{
				//�����ǰ�в�����ֵ�ͣ���ôHOMԪ�����ǿգ��������ʾ
				System.out.println("��ǰ�е�HOM��ԿΪ�գ�");
				return null;
				}
		}else{
			//���homKeyMeta�ǿյģ�˵����û�л�ȡԪ���ݣ���Ҫ��ʾ�û���ִ��fetchMetaData(String tableName)
			System.out.println("���Ȼ�ȡԪ����");
			return null;
		}
	}
	
}
