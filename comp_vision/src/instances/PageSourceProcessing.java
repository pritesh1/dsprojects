package instances;
import image.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class PageSourceProcessing {

	public static File csvFile= new File("/Users/Andreea/Documents/Research/Data/images project/data/3559to4559.csv");
	public static ArrayList<String> imagesId = new ArrayList<String>();
	public static ArrayList<String> pageSourceUrls = new ArrayList<String>();
	public static File PageSourceUrlsFile= new File("/Users/Andreea/Documents/Research/Data/images project/data/3559to4559_PageSource.csv");

	public static void csvProcessing() throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		String line = null;
		FileWriter fw = new FileWriter(PageSourceUrlsFile);
		while((line = br.readLine())!=null){
			String[] parts = line.split(",");
			imagesId.add(ImageUtils.getUrlID(parts[1]));
			fw.write(parts[0]+","+"https://flickr.com/photo.gne?id="+parts[0]+","+parts[2]+"\n");
		}
		br.close();
		fw.close();
	}

	public static void getPageSourcesUrls() throws IOException{
		FileWriter fw = new FileWriter(PageSourceUrlsFile);
		for(String id : imagesId){
			fw.write("https://flickr.com/photo.gne?id="+id+"\n");
			pageSourceUrls.add("https://flickr.com/photo.gne?id="+id);
		}

		fw.close();
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
				System.out.println();

				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	public static String getPageSourceFromUrl(String url) throws MalformedURLException, IOException{

		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) new URL(url).openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try{
			con.connect();			
			//url = con.getURL();
			if(con.getInstanceFollowRedirects()){
				String redirUrl = con.getURL().toString();
				System.out.println("original url: " + con.getURL().toString());
				System.out.println("redirected url: " + redirUrl);

				String pageSource = "";
				InputStream in;
				try{
					in = new BufferedInputStream(con.getInputStream());
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String line="";
					while((line = br.readLine())!=null){
						pageSource += line+ System.getProperty("line.separator");
					}
					br.close();
					PrintWriter pw = new PrintWriter(new File("/Users/Andreea/Desktop/pageSource1.txt"));
					pw.print(pageSource);
					pw.close();
					return pageSource;
					
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}

		return null;
	}
	
	private static String getKeywordsFromUrl(String url) throws MalformedURLException, IOException {
		String pageSource = getPageSourceFromUrl(url);
		try {
			String[] lines = pageSource.toString().split(
					System.getProperty("line.separator"));
			for (String line : lines) {
				if (line.contains("<meta") && line.contains("keywords")) {
					pageSource =line ;
				}
			}
			if (pageSource != null) {
				int st = pageSource.indexOf("content");
				int en = pageSource.indexOf(">", st);
				String imageLink = pageSource.substring(st + 9, en - 1);
				return imageLink;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//csvProcessing();
		//getPageSourcesUrls();
		//String url = "https://flickr.com/photo.gne?id=4338661716";
		//System.out.println(getPageSourceFromUrl(url));
		//System.out.println(getKeywordsFromUrl(url));
		String page = getKeywordsFromUrl("https://flickr.com/photo.gne?id=146875949");
	}


}
