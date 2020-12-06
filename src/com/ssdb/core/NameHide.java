package com.ssdb.core;

/**
 * ������ݿ�������������<br>
 * Ŀǰֻ��һ����ܣ����еļӽ��ܵĺ����У�ֻ�ǲ��ý��ַ����ߵ��ķ�ʽ��ģ�⡱���ܣ����������ڼӽ��ܺ��������Ӽ����㷨<br>
 * @author WANGHAIWEI
 *
 */
public class NameHide {

	//private String tableName;
	/**
	 * ��������������ǽ����ĵ���������Ϊ�����µ���������Ŀǰʹ�õ��Ǽ򵥵�Base64�㷨<br>
	 * ��telephoneΪ����������������Ĵ���֮�󣬵õ�����dGVsZXBob25l�����ǻ���Ҫʹ�ú��漸��getXXXTableName����дΪ<br>
	 * dGVsZXBob25l_DET��dGVsZXBob25l_OPE����ʽ������������ʽ��������ݿ��С�<br>
	 * @param tableName ��������������µ�����
	 * @return ���ؼ��ܺ����������
	 * @throws Exception
	 */
	public static String getSecretName(String name) throws Exception{
		return EncryptAlgorithm(name);
		}
	
	/**
	 * ��������������ǽ������µ��������ܳ����ĵ�����<br>
	 * �����ݿ��е���������"secretTableName_type",���е�type��ָ��DET��OPE��JOIN��SEARCH����HOM<br>
	 * ���Ƚ��ַ�����"_"�ָ�ΪsecretTableName��type�����֣�ǰ�߾���Base64�Ľ��������������Ժ���á�<br>
	 * �Ժ�İ汾�б�������޸ģ���ΪҪ���⡰emp_id_DET�������
	 * @param secretTableNameWithType ���м������͵���������
	 * @return ���ĵ�����
	 * @throws Exception
	 */
	public static String getPlainName(String secretNameWithType) throws Exception{
		//��������Ϊ��di_DET������ָ�Ϊdi��DET��ǰ�������ĵ������������ǵ�ǰ�еļ��ܷ�ʽ��
		String[] temp = secretNameWithType.split("_");
		//String secretNameWithoutType = temp[0];
		//String type = temp[1];
		/*Key key = KeyManager.blowfishKey();
		return BlowFish.decrypt(key,secretTableNameWithoutType);	
		*/
		//���Ƕ����ĵ��������н���
		return new String(DecryptAlgorithm(temp[0]));
	}
	
	/**
	 * ����String getSecretTableName(String tableName)�����Ĵ���֮�󣬻�Ҫ����Щ����<br>
	 * ��telephoneΪ����������������Ĵ���֮�󣬵õ�����dGVsZXBob25l�����ǻ���Ҫʹ�ú��漸��getXXXTableName����дΪ<br>
	 * dGVsZXBob25l_DET��dGVsZXBob25l_OPE����ʽ������������ʽ��������ݿ��С�<br>
	 * @param secretTableName
	 * @return
	 */
	public static String getDETName(String secretName){
		
		return secretName+"_DET";
		
	}
	public static String getOPEName(String secretName){
		return secretName+"_OPE";
		
	}
	public static String getJOINName(String secretName){
		return secretName+"_JOIN";
		
	}
	public static String getHOMName(String secretName){
		return secretName+"_HOM";
		
	}
	public static String getSEARCHName(String secretName){
		return secretName+"_SEARCH";		
	}
	
	private static String EncryptAlgorithm(String str){
		StringBuffer strBuffer = new StringBuffer(str).reverse();
		return strBuffer.toString();
	}
	private static String DecryptAlgorithm(String str){
		StringBuffer strBuffer = new StringBuffer(str).reverse();
		return strBuffer.toString();
	}
	
	/**
	 * �����main������������
	 * @param args
	 * @throws Exception
	 */
	public void testNameHide() throws Exception {
		// TODO Auto-generated method stub
		String secretName = getSecretName("telephone");
		//System.out.println("���ĵ�������"+secretName);
		
		String secretNameWithDET = getDETName(secretName);
		//System.out.println("DET�е����֣�"+secretNameWithDET);
		
		String secretNameWithOPE = getOPEName(secretName);
		//System.out.println("OPE�е����֣�"+secretNameWithOPE);
		
		String secretNameWithJOIN = getJOINName(secretName);
		//System.out.println("JOIN�е����֣�"+secretNameWithJOIN);
		
		String plainName = getPlainName(secretNameWithJOIN);
		//System.out.println("���������ǣ�"+plainName);
		System.out.println("��������ģ��������");
	}

}
