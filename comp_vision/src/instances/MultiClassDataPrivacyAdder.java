package instances;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import java.util.HashMap;

public class MultiClassDataPrivacyAdder {

	private static String getAccess(String string) {
		if (string.contains("private")) {
			return "private";
		}
		return "public";
	}
	
	private static HashMap<String,String> populateMap(File file) throws IOException{
		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String url = null;
		while((url = br.readLine())!=null){
			url = url.trim();
			String[] starr = url.split(",");
			map.put(starr[1].trim(), getAccess(starr[2]));
		}
		br.close();
		return map;
		
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String fileStr_10000 = "/Users/Aurnob/Downloads/images/src/data/sample/rahul/september9/10000/arff_multi/10000.csv";
		String fileStr_multi = "/Users/Aurnob/Downloads/images/src/data/sample/rahul/september9/10000/arff_multi/multilabelDataFinal.csv";
		String fileStr_multiout = "/Users/Aurnob/Downloads/images/src/data/sample/rahul/september9/10000/arff_multi/multilabelDataFinal_out.csv";

		File filename_10000 = new File(fileStr_10000);
		File filename_multi = new File(fileStr_multi);
		
		HashMap<String,String> map = null;
		if(filename_10000.exists()){
			// in map we have the link and the access
			map = populateMap(filename_10000);
		}
		
		if(filename_multi.exists()){
			FileWriter fw =  new FileWriter(fileStr_multiout, true);
			BufferedReader br = new BufferedReader(new FileReader(filename_multi));
			String url = null;
			while((url = br.readLine())!=null){
				url = url.trim();
				String[] starr = url.split(",");
				if(map.containsKey(starr[1])){
					fw.write(url+","+map.get(starr[1]));
				}
			}
			br.close();
			fw.close();
		}
	}

}
