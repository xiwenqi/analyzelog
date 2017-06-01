package analyzelog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Grep {

	public static void main(String[] args) throws Exception {
		//输入解析不太好，后续改成listener看看
		if (args.length==0){
			System.out.println("args: 必须输入参数");
			return;
		}	
		List<String> argl = Arrays.asList(args);
		System.out.println("args:" + argl.toString());
		
		if (argl.contains("-h") || argl.contains("-help")){
			System.out.println("########  grep 模拟器                                 ########");
			System.out.println("参数：");
			System.out.println("-k[]：检索的key,可指定多个");
			System.out.println("-i：检索的key文件,指定一个检索文件，文件里每个key一行");
			System.out.println("-f[]：检索的log文件,可指定多个");
			System.out.println("-d：需要检索的log文件目录，将扫描该目录下所有文件(包含子目录)，如果不指定具体目录，则使用当前目录下的data目录");
			System.out.println("eg. Grep -k key1 key2 -f file1 file2");
			System.out.println("eg. Grep -k key1 key2 -d path");
			System.out.println("eg. Grep -k key1 key2 -d");
			System.out.println("eg. Grep -i keyfile -f file1 file2");
			System.out.println("eg. Grep -i keyfile -d path");
			System.out.println("eg. Grep -i keyfile -d");
			System.out.println("########  e-mail: wenqi.xi@bl.com  ########");
			return;
		}
		if (argl.contains("-i")&&argl.contains("-k")){
			System.out.println("不能同时指定-i（指定输入文件）和-k（指定key）");
			return;
		}
		if (argl.contains("-d")&&argl.contains("-f")){
			System.out.println("不能同时指定-d（指定log目录）和-f（指定log文件）");
			return;
		}
		if (!("-i").equals(argl.get(0)) && !("-k").equals(argl.get(0))){
			System.out.println("必须先指定 -i 或者 -k");
			return;
		}
		
	
		
		String defaultLogDataFolder = new File(".").getCanonicalPath() + "\\data";
		
		String path = new File(".").getCanonicalPath();
		String tmpFileName = path + "\\tmp_" + LocalDateTime.now().toString().replaceAll(":", "") + ".log";
		File tmp = new File(tmpFileName);
		if (!tmp.exists()){
			tmp.createNewFile();
		}
		FileWriter fw = new FileWriter(tmp);
		BufferedWriter bfw = new BufferedWriter(fw);
		
		CallBack printback =(String t)->{System.out.println(t);};
		CallBack fileBack = (String t)->{try {
												bfw.write(t);
												bfw.newLine();
										} catch (Exception e) {
												e.printStackTrace();
										}	
		};
//		grepSingleFile("1","D:\\analyze\\chongzhi.bl.com.log_20160506000301", fileBack);
//		grepFiles("1462464333321",new String[]{"D:\\analyze\\chongzhi.bl.com.log_20160506000301","D:\\analyze\\chongzhi.bl.com.log_20160506000301","D:\\analyze\\chongzhi.bl.com.log_20160508000301"}, printback);

		if (argl.contains("-i")&&argl.contains("-d")){
			int i = argl.indexOf("-i");
			int d = argl.indexOf("-d");
			//key文件 指定log目录模式
			if (d-i>2){
				System.out.println("暂不支持指定多个key文件");
				return;
			}else{
				if (argl.size()==d+1){
					grepKeysFileFromFolder(args[1],defaultLogDataFolder,fileBack);//默认目录
				}else{
					grepKeysFileFromFolder(args[1],args[3],fileBack);
				}		
			}			
		}
		
		if (argl.contains("-i")&&argl.contains("-f")){
			int i = argl.indexOf("-i");
			int d = argl.indexOf("-f");
			//key文件 指定log文件路径模式
			if (d-i>2){
				System.out.println("暂不支持指定多个key文件");
				return;
			}else{
				String[] param = new String[argl.size()-d-1] ;
				for(int a=0;a<argl.size()-1-d;a++){
					param[a] = argl.get(d+1+a);
				}
				grepKeysFileFromFiles(args[1],param,fileBack);
			}
			
			
		}		
		if (argl.contains("-k")&&argl.contains("-d")){
			int i = argl.indexOf("-k");
			int d = argl.indexOf("-d");
			//key值  指定log目录
			List<String> l = argl.subList(i+1, d);
			Set<String> param = new HashSet<String>(l);	
			if (argl.size()==d+1){
				grepkeysFromFolder(param,defaultLogDataFolder,fileBack);
			}else{
				grepkeysFromFolder(param,args[d+1],fileBack);
			}
			
		}
		if (argl.contains("-k")&&argl.contains("-f")){
			int i = argl.indexOf("-k");
			int d = argl.indexOf("-f");
			//key值 指定log文件路径
			List<String> l = argl.subList(i+1, d);
			Set<String> param = new HashSet<String>(l);	
			List<String> l2 = argl.subList(d+1, argl.size());
			String[] fileNames = (String[])l2.toArray(new String[l2.size()]);
			grepkeysFromFiles(param,fileNames,fileBack);
		}					
		bfw.flush();
		bfw.close();
		fw.close();
		System.out.println("运行完成");
	}

	private static String[] getFileNamesInFolder(String folder) throws IOException{
		List<String> l = new ArrayList<String>();
		File f = new File(folder);
		getFilesFromFolder(l,f);
		String[] strs = (String[])l.toArray(new String[l.size()]);
		return strs;
	}
	
	private static void  getFilesFromFolder(List<String> fileNames ,File folder) throws IOException {
		File[] fs = folder.listFiles();
		for(File f:fs){
			if (f.isDirectory()){
				getFilesFromFolder(fileNames,f);
			}else{
				fileNames.add(f.getCanonicalPath());
			}
		}
	}

	interface CallBack{
		public void print(String t);
	}
	
	/**
	 * 在单个文件中抓取单个关键字
	 * @param key
	 * @param fileName
	 * @param c
	 */
	private static void grepSingleFile(String key,String fileName,CallBack c){
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				if(tempString.indexOf(key)>-1){
					c.print(tempString);
				}
				
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
	}
	
	/**
	 * 在多个文件中抓取单个关键字
	 * @param key
	 * @param files
	 * @param c
	 */
	private static void grepFiles(String key,String[] files,CallBack c){
		for(String file:files){
			grepSingleFile(key,file,c);
		}
	}
	
	/**
	 * 从指定文件目录下读取所有文件来匹配关键字
	 * @param key
	 * @param folder
	 * @param c
	 * @throws IOException
	 */
	private static void grepFolder(String key,String folder,CallBack c) throws IOException{
		String fileNames[] = getFileNamesInFolder(folder);
		grepFiles(key,fileNames,c);
	}
	
	/**
	 * 在单个文件中抓取多个关键字
	 * @param keys
	 * @param file
	 * @param c
	 */
	private static void grepKeysFromSingleFile(Set<String> keys,String file,CallBack c){
		for(String key:keys){
			grepSingleFile(key,file,c);
		}
	}
	
	/**
	 * 多个关键字，按关键字分别在多个文件中抓取
	 * @param keys
	 * @param files
	 * @param c
	 */
	private static void grepkeysFromFiles(Set<String> keys,String[] files,CallBack c){
		for(String key:keys){
			grepFiles(key,files,c);
		}
	}
	
	/**
	 * 多个关键字，按关键字分别在指定目录下的多个文件中抓取
	 * @param keys
	 * @param folder
	 * @param c
	 * @throws IOException
	 */
	private static void grepkeysFromFolder(Set<String> keys,String folder,CallBack c) throws IOException{
		String fileNames[] = getFileNamesInFolder(folder);
		grepkeysFromFiles(keys,fileNames,c);
	}	
	
	/**
	 * 在单个文件中抓取一个key文件中的所有关键字，使用于key很多的情况。key文件中每个关键字一行。
	 * @param keyFile
	 * @param file
	 * @param c
	 */
	private static void grepKeysFileFromSingleFile(String keyFile,String file,CallBack c){
		Set<String> keys = readKeys2SetFromFile(keyFile);
		grepKeysFromSingleFile(keys,file,c);
	}
	
	/**
	 * 根据key文件中的关键字，分别在多个文件中抓取
	 * @param keyFile
	 * @param files
	 * @param c
	 */
	private static void grepKeysFileFromFiles(String keyFile,String[] files,CallBack c){
		Set<String> keys = readKeys2SetFromFile(keyFile);
		grepkeysFromFiles(keys,files,c);
	}	
	
	/**
	 * 根据key文件中的关键字，分别在指定目录下的文件中抓取
	 * @param keyFile
	 * @param folder
	 * @param c
	 * @throws IOException
	 */
	private static void grepKeysFileFromFolder(String keyFile,String folder,CallBack c) throws IOException{
		String fileNames[] = getFileNamesInFolder(folder);
		Set<String> keys = readKeys2SetFromFile(keyFile);
		grepkeysFromFiles(keys,fileNames,c);
	}
	
	private static Set<String> readKeys2SetFromFile(String keyFile){
		Set<String> set = new HashSet<String>();
		File file = new File(keyFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				set.add(tempString);				
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
		return set;
	}
	
	

	

}
