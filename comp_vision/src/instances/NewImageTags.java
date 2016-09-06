package instances;
import image.ImageUtils;
import instances.RGBInstances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.w3c.dom.ls.LSInput;

public class NewImageTags {

	private static HashMap<String, String> url2keywords = new HashMap<String, String>();
	private static HashMap<String, List<String>> url2access = new HashMap<String, List<String>>();
	private static StringBuffer pageSource = new StringBuffer(2000);
	private static boolean stop = false;
	private static FloatCentroidsResult fcr = null;
	private static List<String> keyList = null;
	private static RGBInstances iRep = new RGBInstances();
	static int[] SIFThistogram = new int[1200];
	private static final String location = "/Users/Aurnob/Desktop/cotraining files/multiClass/";

	public static void run(File csvFile) throws Exception{

		File cbFile = new File(location+"codebook");
		File mdFile = new File(location+"metadata");
		File rgbFile = new File(location+"classifier_rgb.arff");

		// writes the header for the arffs
		iRep.addRGBHeader(rgbFile);
		addFeaturesHeader(location + "classifier_facial.arff", "facial",'f', 2);
		addFeaturesHeader(location + "classifier_edge.arff", "edge", 'e', 144);

		BufferedReader br = new BufferedReader(new FileReader(csvFile));
		String url = null;
		ArrayList<float[]> f2 = new ArrayList<float[]>();
		int urlCount=0;

		int numLines = 0;
		int numLines1 = ImageUtils.getNumberOfLines(cbFile, " ");
		int numLines2 = ImageUtils.getNumberOfLines(mdFile, "-");
		if (numLines1 != numLines2){
			numLines = 0;
		}
		else{
			numLines = numLines1;
		}

		while((url = br.readLine())!=null){
			String[] starr = url.trim().split(",");
			String staticUrl = starr[1];
			String viewAns = starr[2];
			String shareAns = starr[3];
			String tagAns = starr[4];
			int len = starr.length;

			// in triple we store the values for the privacy level for {view, share, tag}
			List<String> triple = new ArrayList<String>();
			triple.add(viewAns);
			triple.add(shareAns);
			triple.add(tagAns);

			url2access.put(staticUrl, triple);
			urlCount++;	
			if(urlCount<numLines){
				continue;
			}

			System.out.print(urlCount + " ");
			System.out.println(url);

			// takes the url of the page that contains the image
			url = "http://flickr.com/photo.gne?id="+ImageUtils.getUrlID(staticUrl);
			if(ImageUtils.canConnect(url)){
				boolean featuresPresent = true;
				// extracts the keywords from the page source
				String keyWords = ImageTags.populateKeywords(url, staticUrl);
				// if it has keywords, we extract features from RGB and SIFT
				if(keyWords != null){
					featuresPresent = iRep.getRGBRepresentation(staticUrl, rgbFile.getAbsolutePath(), triple, urlCount) 
							&&Codebook.loadCodebook(staticUrl, f2, cbFile,urlCount);
				}

				// if it has keywords si  the features' extraction was successful
				if(keyWords!=null && featuresPresent){
					url2keywords.put(starr[1], keyWords);
					iRep.getFacialFeatures(starr[1], location+"classifier_facial.arff", 
							triple, urlCount);
					// iRep.getEdgeDirectionCoherenceFeatures(starr[1], filename.getAbsolutePath() + "_edge",starr[2]);
					addToMD(starr[1], keyWords,triple, mdFile,urlCount);
				}
			}
			System.out.println("putting " + starr[1] + " with " + starr[len - 1]);
		}

		f2 = ImageTags.getCodeBookFloatList(cbFile);
		// puts in url2keywords the url and keywords+privacyTriple
		ImageTags.populateDictionary(mdFile);
		System.out.println("a total of " + url2keywords.size() + " lines");
		File fcFile = new File(csvFile.getAbsolutePath() + "_fcr");
		if(fcr == null){
			if(fcFile.exists()){
				fcr = new FloatCentroidsResult();
				Scanner sc= new Scanner(fcFile);
				fcr.readASCII(sc);
				sc.close();
			} else{
				fcr = Codebook.getCodeBook(f2, fcFile);
			}
		}

		// takes the url list from md file
		keyList = ImageTags.getURLList(mdFile);
		ImageTags.printSparseMatrix(location+"classifier_tags.arff");
		url2keywords.clear();
		File siftFile = new File(location + "classifier_sift.arff");
		// newList = keyList.subList(i * partLen, (i + 1) *
		// partLen);
		getAllSIFTFeatures(cbFile, keyList, fcr, siftFile);

		// newList = keyList.subList(divfactor * partLen, (divfactor)
		// * partLen + rem);
		// System.out.println("final iteration ");
		// iRep.getSIFTFeatures(newList, filename.getParentFile()
		// .getAbsolutePath(), filename.getAbsolutePath()
		// + "SIFT.arff", fcr);

		br.close();

	}

	private static void getAllSIFTFeatures(File cbFile, List<String> urlList, FloatCentroidsResult fcr, File file) throws IOException{
		
		iRep.addSIFTHeader(file.getAbsolutePath(), 1200, "sift");
		BufferedReader br = new BufferedReader(new FileReader(cbFile));
		int count = 0;
		StringBuffer line = new StringBuffer(700000);
		
		while((line.append(br.readLine()).toString())!=null){
			System.out.println("iteration - " + count);
			String[] starr = line.toString().split(" ");
			if(starr.length==1){
				System.out.println(line);
				return;
			}
			getSIFTFeature(urlList.get(count),line.toString(),fcr,file);
			count++;
			line.delete(0, line.length()-1);
		}
	}
	
	private static void getSIFTFeature(String URL, String floatLine, FloatCentroidsResult fcr,File file) throws IOException{
		
		FileWriter FW = new FileWriter(file, true);
		String[] starr = floatLine.split(" ");
		if(starr.length==1){
			System.out.println(floatLine);
		}
		String floatVals = starr[1];
		// gets the features for each keypoint
		String[] floatArr = floatVals.split(";");
		
		HardAssigner<float[], ?, ?> assigner = fcr.defaultHardAssigner();
		for (int i = 0; i < SIFThistogram.length; i++) {
			SIFThistogram[i] = 0;
		}
		
		// creates histograma
		for(String floatVal : floatArr){
			// transforms the features for each keypoint in float array
			String[] floats = floatVal.split(",");
			ArrayList<Float> flist = new ArrayList<Float>();
			for(String flt : floats){
				flist.add(Float.parseFloat(flt));
			}
			float[] flarr = new float[flist.size()];
			for (int i = 0; i < flarr.length; i++){
				flarr[i] = flist.get(i);
			}
			// classLabel = cluster's index where a keypoint is assigned
			int classLabel = assigner.assign(flarr);
			// we mark in the histogram that we found another keypoint belonging to this cluster
			SIFThistogram[classLabel] = SIFThistogram[classLabel] + 1;
		}
		
		FW.write("{");
		for (int i = 0; i < SIFThistogram.length; i++) {
			if (SIFThistogram[i] > 0) {
				FW.write("" + i + " " + SIFThistogram[i] + ", ");
			}
		}
		FW.write( ImageUtils.privacyValues(url2access.get(URL),SIFThistogram.length) );
		FW.close();
	}
	public static void addToMD(String url, String keywords, List<String> privacyList, File mdFile, int urlCount) throws IOException{

		FileWriter fw = new FileWriter(mdFile);
		fw.write(urlCount+" - "+ url+":"+keywords+"_"+privacyList.toString()+"\n");
		fw.close();
	}

	private static void addFeaturesHeader(String file, String keyword, char attribute, int count) throws IOException{
		File fileout = new File(file);
		if(fileout.exists()){
			return;
		}

		FileWriter fw = new FileWriter(file);
		String lsp = "\n";
		fw.write("@relation "+keyword+" "+lsp);

		for(int i=0;i<count;i++){
			fw.write("@attribute "+attribute+i+" numeric"+ lsp);
		}
		fw.write("@attribute view"+attribute +" {{\"you\", \"everyone\", \"SocialNetwork\", \"Friends\", \"Family\"}} "+lsp);
		fw.write("@attribute share"+attribute +" {{\"you\", \"everyone\", \"SocialNetwork\", \"Friends\", \"Family\"}} "+lsp);
		fw.write("@attribute tag"+attribute +" {{\"you\", \"everyone\", \"SocialNetwork\", \"Friends\", \"Family\"}} "+lsp);

		fw.write("@data" + lsp);
		fw.close();

	}
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String imageFolder = location + "images/";
		File[] listOfFiles = new File(imageFolder).listFiles();
		for(int i=0; i< listOfFiles.length;i++){
			File file = listOfFiles[i];
			if (file.getName().endsWith("csv")) {
				run(file);
			}
		}

	}

}
