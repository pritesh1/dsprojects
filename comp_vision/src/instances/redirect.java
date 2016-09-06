package instances;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;



public class redirect {

	// here we extract information from page sources - image's url, keywords ...
	private static final String URL_HEADER = "";
	// template-ul image-tag-ului
	private static final String IMAGETAG_TEMPLATE =  "<p><img src= alt=\"some_text\" width=\"304\" height=\"228\" /></p>";
	private static HashMap<String, String> url2keywords = new HashMap<String, String>();
	private static HashMap<String, String> url2access = new HashMap<String, String>();

	private static String getPageSource(HttpURLConnection con){

		String pageSource = "";
		InputStream in;
		try{
			in = new BufferedInputStream(con.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = br.readLine())!=null){
				pageSource += line+ System.getProperty("line.separator");
			}
			return pageSource;

		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private static String getImageKeywords(String pageSource){
		String[] lines = pageSource.split("line.separator");
		try{
		for(String line : lines){
			if(line.contains("<meta")&& line.contains("keywords")){
				pageSource = line;
			}
		}
		if(pageSource!=null){
			int st = pageSource.indexOf("content");
			int en = pageSource.indexOf(">",st);
			String imageLink = pageSource.substring(st+9,en-1);
			return imageLink;
		}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getStaticImageURL(String pageSource){
		String[] lines = pageSource.split("\n");
		// finds the image url from the page source
		try{
		for(String line : lines){
			if(line.contains("image-src")){
				pageSource = line;
			}
		}
		int st = pageSource.indexOf("http");
		int en = pageSource.indexOf("jpg");
		String imageLink = pageSource.substring(st, en + 3);
		return imageLink;
		} catch(Exception e){
			e.printStackTrace();
		}
				
		return null;
	}
	private static String getStaticURL(String url){
		HttpURLConnection con = null;
		String staticUrl = null;
		try {
			con = (HttpURLConnection) new URL(url).openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("orignal url: " + con.getURL());
		System.out.println("connected url: " + con.getURL());
		try{
			con.connect();
			if(con.getInstanceFollowRedirects()){
				// extracts the page source from url
				String pageSource = getPageSource(con);
				System.out.println("PAGINA: "+pageSource);
				//extracts keywords from page source
				String keyWords = getImageKeywords(pageSource);
				System.out.println("Keywords of the image are - " + keyWords);

				//returns image's url
				staticUrl = getStaticImageURL(pageSource);
				System.out.println("Image URL - " + staticUrl);

				// puts the url and the keywords
				if (keyWords != null)
					url2keywords.put(staticUrl,keyWords);
				else
				{
					url2keywords.put(staticUrl,"");
				}
			}
			else
			{
				staticUrl = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String redirUrl = con.getURL().toString();
		System.out.println("redirected url: " + redirUrl);

		return staticUrl;
	}

	// writes on the first line the keywords separated by ","
	public static void printSparseMatrix(){
		HashMap<String, Integer> kw2int = new HashMap<String, Integer>();
		int count = 0;
		for(String url : url2keywords.keySet()){
			String keywords = url2keywords.get(url);
			String[] words = keywords.split(",");
			for(String word : words){
				if(!kw2int.containsKey(word)){
					count++;
					kw2int.put(word, count);
				}
			}	
		}
		
		String location = "/Users/Andreea/Documents/Research/Data/images project/";
		File filename = new File(location + "3000-4000_static_tags.arff");
		PrintWriter out = null;
		try {
			out = new PrintWriter(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String keyword : kw2int.keySet()){
			System.out.println("keyword - " + keyword + " number in map - " + kw2int.get(keyword));
			out.write(keyword+", ");
		}
		for(String url : url2keywords.keySet()){
			out.write("{ "+url+" }");
			String keywords = url2keywords.get(url);
			String [] words = keywords.split(",");
			for(String word : words){
				out.write(new Integer(kw2int.get(word)) .toString() + " 1 ,");
			}
			out.write( " \"" + getAccess(url2access.get(url)) + "\"}\n");
		}
		out.close();
	}
	
	private static String getAccess(String string) {
		if ( string.contains("private"))
		{
			return "private";
		}
		
		return "public";
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String location = "/Users/Andreea/Documents/Research/Data/images project/data/";
		File filename = new File(location + "3559to4559_PageSource.csv");
		File fileOut = new File(location + "lol.csv");
		//File htmltemplate = new File(location + "templ.txt");
		//File htmlOut = new File(location + "templ_out.txt");

		PrintWriter out = new PrintWriter(fileOut);
		//PrintWriter out1 = new PrintWriter(htmlOut);

		if(filename.exists()){
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String url = null;
			String stUrl = null;
			//String htmlText = gethtmlText(htmltemplate);

			while((url = br.readLine())!=null && !url.split(",")[0].equals("4872919289")){
				System.out.println(url);
				String[] starr = url.split(",");
				url = starr[1];
				// returneaza url-ul imaginii
				stUrl = getStaticURL(URL_HEADER + url);
				url2access.put(stUrl, starr[2]);
				System.out.println("putting " + stUrl + " with " + starr[2]);
				if (stUrl == null)
				{
					continue;
				}
				String outStr = "";
				for(int i=0;i<starr.length;i++){
					if(i==1){
						outStr+=stUrl+", ";
					} else{
						outStr += starr[i] + ", ";
					}
				}
				out.println(outStr);
				//writeOut(getImageTag(stUrl)/* + htmlText*/,  out1);
				stUrl = null;
			}
			printSparseMatrix();
			out.close();
			//out1.close();
			br.close();
		}

	}

}
