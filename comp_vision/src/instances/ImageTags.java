package instances;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;


public class ImageTags {
	private static HashMap<String, String> url2keywords = new HashMap<String, String>();
	private static HashMap<String, String> url2access = new HashMap<String, String>();
	private static StringBuffer pageSource = new StringBuffer(2000);
	private static boolean stop = false;
	private static FloatCentroidsResult fcr = null;
	private static List<String> keyList = null;
	private static RGBInstances iRep = new RGBInstances();
	static int[] SIFThistogram = new int[1200];
	public static String location = System
			.getProperty("DOWNLOAD_LOCATION",
					"/Users/Andreea/Documents/Research/Data/images project/data/cotraining files/arff_newdata/");

	private static void runSift(File filename) throws IOException{
		int testCount = 10;
		int i=0;
		int lines = getTotalLines(filename.getParentFile().getAbsolutePath()+"_SIFT_TMP");
		for(; i<keyList.size()&&testCount>0;i++){
			if(i<lines){
				continue;
			}
			System.out.println("iteration - "+i);
			try{
				iRep.getSIFTFeaturesForURL(keyList.get(i), filename.getParentFile().getAbsolutePath(), filename.getAbsolutePath() + "SIFT.arff", fcr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			testCount--;
		}
		if(i == keyList.size())
			stop = true;
	}

	private static int getTotalLines(String filestr) throws IOException{

		File file = new File(filestr);
		if(!file.exists()){
			return 0;
		}
		String url;
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(file));
		while((url = br.readLine())!=null){
			count++;
		}
		return count;
	}
	private static File printUnique(File file) throws IOException{
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			Set<String> lines = new HashSet<String>();
			String line = null;
			int count = 0;
			while ((line = (br.readLine())) != null) {
				if (!lines.contains(line.toString())) {
					lines.add(line);
				}
			}
			String fileName = file.getAbsolutePath();
			fileName = fileName.substring(0, fileName.length() - 4);
			File nFile = new File(fileName + "_unique");

			FileWriter fw = new FileWriter(nFile);
			for (String str : lines) {
				fw.write(str + "\n");
			}
			fw.close();
			br.close();
			return nFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public static void addFeaturesHeader(String file, String keyword, char attribute, int count){

		File fileOut = new File(file);
		if(fileOut.exists()){
			return;
		}

		try{
			FileWriter fw = new FileWriter(file);
			String lsp = "\n";
			fw.write("@relation " + keyword + " " + lsp);

			for (int i = 0; i < count; i++)

			{
				fw.write("@attribute "+ attribute + i + " numeric" + lsp);
			}

			fw.write("@attribute class" + attribute +" {\"public\", \"private\"}" + lsp);
			fw.write("@data" + lsp);

			fw.close();	
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int getNumberOfLines(File filename, String sep) {
		if (!filename.exists())
			return 0;
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String url;
			String lastUrl = null;
			while ((url = br.readLine()) != null) {
				lastUrl = url;
			}
			if (lastUrl == null)
				return 0;

			String[] strarr = lastUrl.split(sep);

			return Integer.parseInt(strarr[0]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public static String getUrlID(String staticUrl){
		String [] splitStrs = staticUrl.split("/");
		int numSplits = splitStrs.length;
		//System.out.println(splitStrs[numSplits-1]);

		String[] urlIdSplit = splitStrs[numSplits-1].split("_");
		//System.out.println(urlIdSplit[0]);
		return urlIdSplit[0].trim();
	}

	public static void getPageSource(HttpURLConnection con){
		if(pageSource.length()>0){
			pageSource.delete(0, pageSource.length()-1);
		}
		InputStream in;
		try{
			in = new BufferedInputStream(con.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = br.readLine())!=null){
				pageSource.append(line+"\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getImageKeyWords(){
		try{
			String[] lines = pageSource.toString().split(System.getProperty("line.separator"));
			for(String line : lines){
				// pastreaza din pagina sursa doar partea cu keywords
				if (line.contains("<meta") && line.contains("keywords")) {
					pageSource.delete(0, pageSource.length() - 1);
					pageSource.append(line);
				}
				if(pageSource!=null){
					int st = pageSource.indexOf("content");
					int en = pageSource.indexOf(">", st);
					String imageLink = pageSource.substring(st + 9, en - 1);
					return imageLink;
				}
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String populateKeywords(String url, String staticUrl){
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) new URL(url).openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("orignal url: " + con.getURL());
		System.out.println("connected url: " + con.getURL());
		String keywords = null;

		try{
			con.connect();
			if(con.getInstanceFollowRedirects()){
				// downloads the page source
				getPageSource(con);
				// extract the keywords
				keywords = getImageKeyWords();
				System.out.println("Keywords of the image are - " + keywords);
			} else{
				staticUrl = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return keywords;
	}

	private static boolean canConnect(String url) {
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) new URL(url).openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			con.connect();
			if (con.getInstanceFollowRedirects()) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}

	private static void addToMD(String string, String keyWords, String privacy,
			File mdFile, int urlCount) {
		try {
			FileWriter fw = new FileWriter(mdFile, true);
			fw.write(urlCount + "-" + string + ":" + keyWords + "_" + privacy
					+ "\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static ArrayList<float[]> getCodeBookFloatList(File cbFile){
		FileInputStream inputStream;
		ArrayList<float[]> f2 = new ArrayList<float[]>();
		try{
			inputStream = new FileInputStream(cbFile);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer line = new StringBuffer(700000);
			int count = 0;
			while((line.append(br.readLine())).toString()!=null){
				String[] strarr = line.toString().split(" ");
				if (strarr.length == 1) {
					System.out.println(line);
					return f2;
				}
				String floatVals = strarr[1];
				// values for each attribute
				String[] floatArr = floatVals.split(";");
				for(String floatVal : floatArr){
					String[] floats = floatVal.split(",");
					// flist contains the feature for an attribute
					ArrayList<Float> flist = new ArrayList<Float>();
					for (String flt : floats) {
						flist.add(Float.parseFloat(flt));
					}
					//transforms arraylist in float vector
					float[] flarr = new float[flist.size()];
					for (int i = 0; i < flarr.length; i++)
						flarr[i] = flist.get(i);
					f2.add(flarr);
				}
				line.delete(0, line.length() - 1);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return f2;
	}

	public static void populateDictionary(File mdFile) {
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(mdFile);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer url = new StringBuffer(2000);
			int count = 0;
			while ((url.append(br.readLine())).toString() != null) {
				//System.out.println(count + " ");
				String[] starr = url.toString().split("-");
				String keyval = "";
				for (int i = 1; i < starr.length; i++){
					keyval += starr[i];
				}

				String[] kvp = keyval.split("jpg:");
				if (kvp.length == 1)
					break;
				String urlkey = kvp[0] + "jpg";
				urlkey = urlkey.trim();
				count++;
				if(url2keywords.containsKey(urlkey))
					System.out.println("collision key - " + urlkey);
				url2keywords.put(urlkey.trim(), kvp[1]);
				url.delete(0, url.length() - 1);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	public static List<String> getURLList(File mdFile) {
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(mdFile);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer url = new StringBuffer(2000);
			List<String> list = new ArrayList<String>();
			while ((url.append(br.readLine())).toString() != null) {
				String[] starr = url.toString().split("-");
				String keyval = "";
				for (int i = 1; i < starr.length; i++)
					keyval += starr[i];

				String[] kvp = keyval.split("jpg:");
				if (kvp.length == 1)
					break;
				String urlkey = kvp[0] + "jpg";
				list.add(urlkey);
				url.delete(0, url.length() - 1);
			}
			return list;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private static void addImageTagsHeader(String string, int dataCount) {
		try {
			FileWriter fw = new FileWriter(string);
			String lsp = "\n";
			fw.write("@relation keywords" + lsp);
			for (int i = 0; i < dataCount; i++)
			{
				fw.write("@attribute a" + i + " numeric" + lsp);
			}
			fw.write("@attribute class {\"public\", \"private\"}" + lsp);
			fw.write("@data" + lsp);
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printSparseMatrix(String fileName) {
		HashMap<String, Integer> kw2int = new HashMap<String, Integer>();
		int count = 0;
		// extracts all the KEYWORDS and their frequency
		for (String url : keyList) {
			url = url.trim();
			String keywords = url2keywords.get(url);
			String[] words = keywords.split(",");
			for (String word : words) {
				if (!kw2int.containsKey(word)) {
					kw2int.put(word, count);
					count++;
				}
			}
		}

		File filename = new File(fileName);
		//puts ARFF HEADER 
		addImageTagsHeader(filename.getAbsolutePath(), kw2int.size());
		FileWriter fw = null;
		String lsp = "\n";
		try {
			fw = new FileWriter(filename, true);
			for (String url : keyList) {
				fw.write("{ ");
				url = url.trim();
				// extracts KEYWORDS
				String keywords = url2keywords.get(url);
				String[] words = keywords.split(",");
				// PUTS IN SET KEYWORDS FREQUENCY IN THEIR NATURAL ORDER
				Set<Integer> wordset = new TreeSet<Integer>();
				for (String word : words) {
					wordset.add(kw2int.get(word));
				}
				// WRITES THE INDEX FROM WORDSET
				for (Integer index : wordset) {
					fw.write(index.toString() + " 1 ,");
				}
				fw.write(new Integer(kw2int.size()).toString());
				fw.write(" \"" + url2access.get(url) + "\"}\n");
				// fw.write(" ?}\n");
			}
			fw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void getSIFTFeature(String URL, String floatLine, FloatCentroidsResult fcr, File file) throws IOException {

		FileWriter FW = null;
		try {
			FW = new FileWriter(file, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] strarr = floatLine.toString().split(" ");
		if (strarr.length == 1) {
			System.out.println(floatLine);
			// return f2;
		}
		String floatVals = strarr[1];
		String[] floatArr = floatVals.split(";");

		HardAssigner<float[], ?, ?> assigner = fcr
				.defaultHardAssigner();
		for (int i = 0; i < SIFThistogram.length; i++) {
			SIFThistogram[i] = 0;
		}
		for (String floatval : floatArr) {
			String[] floats = floatval.split(",");
			ArrayList<Float> flist = new ArrayList<Float>();
			for (String flt : floats) {
				flist.add(Float.parseFloat(flt));
			}
			float[] flarr = new float[flist.size()];
			for (int i = 0; i < flarr.length; i++)
				flarr[i] = flist.get(i);
			int classLabel = assigner.assign(flarr);
			SIFThistogram[classLabel] = SIFThistogram[classLabel] + 1;
		}
		FW.write("{ ");
		for (int i = 0; i < SIFThistogram.length; i++) {
			if (SIFThistogram[i] > 0) {
				FW.write("" + i + " " + SIFThistogram[i] + ", ");
			}
		}
		FW.write(new Integer(SIFThistogram.length).toString() + " ");

		FW.write(" \"" + url2access.get(URL) + "\"}\n");
		FW.close();
	}


	private static void getAllSIFTFeatures(File cbFile, List<String> urlList,FloatCentroidsResult fcr, File file )
	{
		iRep.addSIFTHeader(file.getAbsolutePath() ,
				1200, "sift");
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(cbFile);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			StringBuffer line = new StringBuffer(700000);
			int count = 0;
			while ((line.append(br.readLine())).toString() != null) {
				// System.out.println(count++);
				System.out.println("iteration - " + count);
				String[] strarr = line.toString().split(" ");
				if (strarr.length == 1) {
					System.out.println(line);
					return;
				}
				getSIFTFeature(urlList.get(count), line.toString(), fcr, file);
				count++;
				line.delete(0, line.length() - 1);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void run(File filename) throws IOException{
		if(filename.exists()){
			try{
				FileInputStream inputStream = new FileInputStream(filename);
				DataInputStream in = new DataInputStream(inputStream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String url = null;
				ArrayList<float[]> f2 = new ArrayList<float[]>();
				int urlCount = 	0;

				File cbFile = new File(filename.getAbsolutePath() + "_codebook");
				File mdFile = new File(filename.getAbsolutePath() + "_metadata");
				File rgbFile = new File(filename.getAbsolutePath() + "_rgb");
				//iRep = rgb instance
				iRep.addRGBHeader(rgbFile);
				addFeaturesHeader(filename.getAbsolutePath() + "_facial", "facial", 'f', 2);
				addFeaturesHeader(filename.getAbsolutePath() + "_edge", "edge", 'e', 144);

				int numLines = 0;
				int numLines1 = getNumberOfLines(cbFile, " ");
				int numLines2 = getNumberOfLines(mdFile, "-");

				if (numLines1 != numLines2)
				{
					System.err.println("invalid number of lines in the arff files");
					in.close();
					br.close();
					return;
				}
				else{
					numLines = numLines1;
				}

				while((url = br.readLine())!=null){
					url = url.trim();
					String[] starr = url.split(",");
					String staticUrl = starr[1].trim();
					staticUrl = staticUrl.replace("\"", "");
					String privacy = starr[3].trim();
					privacy = privacy.replace("\"", "");
					url2access.put(staticUrl, privacy);
					if (urlCount < numLines) {
						urlCount++;
						continue;
					}

					urlCount++;
					System.out.print(urlCount + " ");
					System.out.println(url);

					//constructs the url of the page that contins the image
					url = "http://flickr.com/photo.gne?id=" + getUrlID(staticUrl);
					if(canConnect(url)){
						boolean featuresPresent = true;
						String keyWords = populateKeywords(url, staticUrl);
						if(keyWords!=null){
							{
								try{
									featuresPresent = instances.Codebook.loadCodebook(
											staticUrl, f2, cbFile, urlCount);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							// if it has keywords and the codebook is loaded
							//puts the keywords
							url2keywords.put(staticUrl, keyWords);
							// loads the data in the metadata file
							addToMD(staticUrl, keyWords, privacy, mdFile, urlCount);
						}
					}
					System.out.println("putting " + staticUrl + " with "+ privacy);
				}

				f2 = getCodeBookFloatList(cbFile);
				populateDictionary(mdFile);
				System.out.println("a total of " + url2keywords.size() + " lines");
				//FC FILE = FLOAT CENTROIDS
				File fcFile = new File(filename.getAbsolutePath() + "_fcr");

				if (fcr == null) {
					if (fcFile.exists()) {
						fcr = new FloatCentroidsResult();
						// A Scanner breaks its input into tokens using a delimiter pattern, which by default matches whitespace. 
						Scanner sc = new Scanner(fcFile);
						fcr.readASCII(sc);
						sc.close();
					} else {
						fcr = instances.Codebook.getCodeBook(f2, fcFile);
					}
				}
				keyList = getURLList(mdFile);
				// writes ARFF for TAGS
				printSparseMatrix(filename.getAbsolutePath() + "TAGS.arff");

				url2keywords.clear();
				iRep.url2access = url2access;

				File siftFile = new File(filename.getAbsolutePath() + "SIFT.arff");
				getAllSIFTFeatures(cbFile, keyList, fcr, siftFile);
				in.close();
				br.close();

			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static List<String> getURLList1(File metaFile) {
		List<String> urlList = new ArrayList<String>();
		if (!metaFile.exists())
			return urlList;
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(metaFile);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String url;
			while ((url = br.readLine()) != null) {
				String[] urlLines = url.split("jpg:");
				String urlLine = urlLines[0];
				String[] ulrkp = urlLine.split("-");
				int key = Integer.parseInt( ulrkp[0] );
				String val = ulrkp[1] + "jpg";
				urlList.add(val.trim());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return urlList;
	}

	@SuppressWarnings("resource")
	private static int getNumLines(File filename) {
		if (!filename.exists())
			return 0;
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String url;
			String lastUrl = null;
			int lineCount = 0;
			while ((url = br.readLine()) != null) {
				lineCount++;
				lastUrl = url;
			}
			if (lastUrl == null)
				return 0;

			return lineCount - 147;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public static void run_edge(File metaFile)
	{

		System.out.println("adding edge feature");
		addFeaturesHeader(metaFile.getAbsolutePath() + "_edge", "edge", 'e', 144);
		List<String> urls = getURLList1(metaFile);
		int urlCount = 0;
		int numLines = getNumLines(new File( metaFile.getAbsolutePath() + "_edge"));
		for (String url : urls) {
			urlCount++;
			if(urlCount < numLines)
				continue;
			else if (canConnect(url)) {
				// String keyWords = "";

				try {
					iRep.getEdgeDirectionCoherenceFeatures(url,
							metaFile.getAbsolutePath() + "_edge",
							url2access.get(url));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("error in url - " + url + " - count:" + urlCount);
					System.exit(0);
					//e.printStackTrace();
				}
			}
		}
	}


	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File folder = new File(location);

		File[] listOfFiles = folder.listFiles();
		for(int i=0;i<listOfFiles.length;i++){
			File file = listOfFiles[i];
			if(file.getName().endsWith("csv")){
				File nFile = printUnique(file);
				run(nFile);
			}
		}
	}

}
