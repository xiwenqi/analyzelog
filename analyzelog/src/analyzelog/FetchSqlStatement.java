package analyzelog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ʹ�ó����������ڴ���־���Ų�������ʱ��sql�������ܶ࣬������ƴ���ͣ���������������������Ϣ�����硰 3(String), -1(String), 1000(BigDecimal),�� 
 * ���ֳ����»�ԭsql��PLSQL�в�ѯ�ܺ�ʱ��С�����ṩת������
 * ʹ�÷�����
 * 1.	����־��sql��䱣����sqlĿ¼������Ϊstmt.txt;����־�в���������sqlĿ¼������Ϊparam.txt;(��Ҫ�����������ӦĿ¼���ļ���)
 * 2.	ִ��main����ӡת��������
 * 3.	Ŀǰ֧����String��BigDecimal��ת�������������������������Ӧ��replaceXXX����
 * @author XWQ72
 *
 */
public class FetchSqlStatement {

	public static void main(String[] args) throws Exception {
		String defaultLogDataFolder = new File(".").getCanonicalPath() + "\\sql";
		String stmtFileName = defaultLogDataFolder + "\\stmt.txt";
		String stmtReg = "\\?";
		String paramFileName = defaultLogDataFolder + "\\param.txt";
		String paramReg = ",";
		String tmpSqlFile = defaultLogDataFolder + "\\tmp_" + LocalDateTime.now().toString().replaceAll(":", "") + ".sql";
		String stmtStr = getLineFromFile(stmtFileName);
		String paramStr = getLineFromFile(paramFileName);
		List<String> stmtSplits = Arrays.asList(stmtStr.split(stmtReg));
		List<String> paramSplits = Arrays.asList(paramStr.split(paramReg));
		if (stmtSplits.size()!=paramSplits.size()+1){
			throw new Exception("stmt param num:" + stmtSplits.size() + "is not match param size:" + paramSplits.size());
		}
		String result = generateSql(stmtSplits,paramSplits,tmpSqlFile);
		System.out.println(result);
	}
	

	/**
	 * ����sql,д�Ĳ�̫�ã����ڸ���stream������д
	 * @param stmtSplits
	 * @param paramSplits
	 * @param tmpSqlFile
	 */
	private static String generateSql(List<String> stmtSplits, List<String> paramSplits, String tmpSqlFile) {
		List<String> resultSql = new ArrayList<String>();  
		List<String> resultParams = replaceBigDecimal(replaceString(paramSplits));// ��������б�
//		resultParams.forEach(str -> System.out.println(str));
		int j = resultParams.size();
		for(int i=0;i<stmtSplits.size();i++){
			resultSql.add(stmtSplits.get(i));
			if (i<j){
				resultSql.add(resultParams.get(i));
			}	
		}
		return resultSql.stream().collect(Collectors.joining());
	}
	
	private static List<String> replaceString(List<String> paramSplits) {
		String key = "(String)";
		String regex = "\\(String\\)";
		String prefix = "'";
		String suffix = "'";
		String replacement = "";
		return replaceByRegex(paramSplits,key,regex,replacement,prefix,suffix);
	}

	private static List<String> replaceBigDecimal(List<String> paramSplits) {
		String key = "(BigDecimal)";
		String regex = "\\(BigDecimal\\)";
		String prefix = "";
		String suffix = "";
		String replacement = "";
		return replaceByRegex(paramSplits,key,regex,replacement,prefix,suffix);
	}


	
	private static List<String> replaceByRegex(List<String> paramSplits,String key,String regex,String replacement,String prefix,String suffix) {		
		List<String> list = paramSplits.stream().map(param->{return param.indexOf(key)>-1?prefix + param.trim().replaceAll(regex, replacement)+ suffix:param;}).collect(Collectors.toList());
		return list;
	}



//  �ļ���ʽ����stream
//	private void getStreamFromFile(){
//		try(Stream lines = Files.lines(Paths.get(���ļ�·������),Charset.defaultCharset())){ 
//			//�ɶ�lines��һЩ���� 
//			}catch(IOException e){ 
//			} 
//	}
	


	private static String getLineFromFile(String fileName) {
		StringBuffer bf = new StringBuffer("");
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				bf.append(tempString);//�����־����,�ϲ���һ��			
				line++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					System.out.println(e1);
				}
			}
		}	
		return bf.toString();
	}
	

}
