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
 * 使用场景：适用于从日志中排查问题有时候sql语句参数很多，尤其是拼接型，并且往往附加了类型信息，例如“ 3(String), -1(String), 1000(BigDecimal),” 
 * 这种场景下还原sql到PLSQL中查询很耗时，小程序提供转换功能
 * 使用方法：
 * 1.	将日志中sql语句保存在sql目录下命名为stmt.txt;将日志中参数保存在sql目录下命名为param.txt;(如要改名请调整相应目录和文件名)
 * 2.	执行main将打印转换后的语句
 * 3.	目前支持了String和BigDecimal的转换，如有其它类型自行添加相应的replaceXXX方法
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
	 * 生成sql,写的不太好，后期根据stream操作重写
	 * @param stmtSplits
	 * @param paramSplits
	 * @param tmpSqlFile
	 */
	private static String generateSql(List<String> stmtSplits, List<String> paramSplits, String tmpSqlFile) {
		List<String> resultSql = new ArrayList<String>();  
		List<String> resultParams = replaceBigDecimal(replaceString(paramSplits));// 整理参数列表
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



//  文件方式生成stream
//	private void getStreamFromFile(){
//		try(Stream lines = Files.lines(Paths.get(“文件路径名”),Charset.defaultCharset())){ 
//			//可对lines做一些操作 
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
				bf.append(tempString);//如果日志多行,合并成一行			
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
