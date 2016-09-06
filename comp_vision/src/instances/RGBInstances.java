package instances;
import image.CONSTANT;
import image.ImageUtils;

import java.awt.RenderingHints.Key;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.feature.ByteFV;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;

import structure.Sequences;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class RGBInstances {

	int nf = CONSTANT.numFeatures * CONSTANT.numValues;
	int[] SIFThistogram = new int[1200];
	int[] vector = new int[nf];
	int[][] values = new int[CONSTANT.numFeatures][CONSTANT.numValues];
	Instances header;
	public HashMap<String, String> url2access = new HashMap<String, String>();
	public RGBInstances() {
	}

	public void setNF(int nf) {
		this.nf = nf;
	}

	public void setHeader() {

		// attrInfo - vector with all the attributes
		FastVector attrInfo = new FastVector();

		// defines the attribute CLASSATR with the access - public/privat
		FastVector classVals = new FastVector();
		classVals.addElement( "public" );
		classVals.addElement("private");
		Attribute classAttr = new Attribute("class", classVals);

		for (int i = 0; i < nf; i++) {
			Attribute a = new Attribute(new String("a" + i));
			attrInfo.addElement(a);
		}
		attrInfo.addElement(classAttr);

		header = new Instances("RGB", attrInfo, 0);
		header.setClassIndex(nf);
	}

	public Instances getHeader() {
		return header;
	}

	private String getAccess(String string) {
		if (string.contains("private")) {
			return "private";
		}

		return "public";
	}

	public List<String> getStaticURL(File filename) {
		List<String> URls = new ArrayList<String>();
		if (filename.exists()) {
			try {
				FileInputStream inputStream = new FileInputStream(filename);
				DataInputStream in = new DataInputStream(inputStream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String line = null;
				while ((line = br.readLine()) != null) {
					String[] splitline = line.split(",");
					URls.add(splitline[1]);
					String access = getAccess(splitline[2]);
					System.out.println("static url - " + splitline[1]);
					url2access.put(splitline[1], access);
				}
				in.close();
				br.close();
			}

			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return URls;
	}

	public void getEdgeDirectionCoherenceFeatures(String URL, String fileOut, String privacy) throws IOException {

		FileWriter FW = new FileWriter(fileOut, true);
		try {
			FImage query = ImageUtilities.readF(new URL(URL));
			EdgeDirectionCoherenceVector edch = new EdgeDirectionCoherenceVector();
			edch.analyseImage(query);
			DoubleFV featureFV = edch.getFeatureVector();
			String fpVal = "";
			int[] ff2 = new int[featureFV.values.length];
			System.out.println(featureFV.values.length);
			FW.write("{");
			for (int i = 0; i < featureFV.values.length; i++) {
				ff2[i] = (int) featureFV.values[i];
				if(ff2[i] != 0)
					fpVal += "" + i + " " + ff2[i] + ",";
			}
			FW.write(fpVal);
			FW.write(featureFV.values.length + " \"" + getAccess(privacy) + "\"}\n");
			// FW.write("?}\n");
		} catch (Exception e) {
			System.err.println("While processing URL: " + URL);
			e.printStackTrace();
		}
		FW.close();
	}

	public void getFacialFeatures(String URL, String fileOut, String privacy, int count) throws IOException{
		FileWriter fw = new FileWriter(fileOut, true);
		FImage image = ImageUtilities.readF(new URL(URL));
		FaceDetector<KEDetectedFace, FImage> fd = new FKEFaceDetector();
		List<KEDetectedFace> faces = fd.detectFaces(image);

		fw.write("{");
		double area =0;
		if(!faces.isEmpty()){
			for(KEDetectedFace face : faces){
				Rectangle rect = face.getBounds();
				area += rect.getHeight() * rect.getWidth();
			}
			double area_ratio = area / (image.height * image.width);
			fw.write("1 " + new Double(area_ratio).toString() + ", ");
		}
		fw.write(" 2 \"" + getAccess(privacy) + "\"}\n");

		fw.close();
	}

	private String privacyValues(List<String> privacy, int index){
		String retStr = "";
		for(String str : privacy){
			retStr += index +" \"" + str+"\",";
			index++;
		}
		retStr += " }\n";
		return retStr;
	}
	// here it uses the privacy for view, share, tag
	public void getFacialFeatures(String URL, String fileOut, List<String> privacyList, int count) throws IOException{
		FileWriter fw = new FileWriter(fileOut,true);
		FImage image = ImageUtilities.readF(new URL(URL));
		FaceDetector<KEDetectedFace, FImage> fd = new FKEFaceDetector();
		List<KEDetectedFace> faces = fd.detectFaces(image);
		fw.write("{");
		double area=0;
		if(!faces.isEmpty()){
			for(KEDetectedFace face : faces){
				Rectangle rect = face.getBounds();
				area += rect.getHeight() * rect.getWidth();
			}
			double area_ratio = area/(image.getHeight()*image.getWidth());
			fw.write("1 " + new Double(area_ratio).toString() + ", ");
		}
		String privacyValues = privacyValues(privacyList,2);
		fw.write(privacyValues);

		fw.close();
	}

	// detects the keypoints and for each keypoint has a feature vector
	// added FloatCentroidsResult codebook as input - need to check syntax
	public void getSIFTFeatures(List<String> URLs, String img_path,
			String fileOut, FloatCentroidsResult codebook) throws IOException {
		//addSIFTHeader(fileOut, 12000,"sift");
		//FileWriter FW1 = new FileWriter(img_path+"SIFT_TMP", true);
		FileWriter FW = new FileWriter(fileOut, true);

		for(String url : URLs){
			FImage image = ImageUtilities.readF(new URL(url));
			DoGSIFTEngine engine = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> keypoints = engine.findFeatures(image);

			if(keypoints.size()==0){
				FW.write("{ ");
				FW.write(new Integer(SIFThistogram.length).toString() + " ");

				FW.write(" \"" + getAccess(url2access.get(url)) + "\"}\n");
				continue; // skip URL
			}
			List<Keypoint> keys = keypoints.subList(0,keypoints.size() - 1);
			// remember to set SIFThistogram to zero
			for (int i = 0; i < SIFThistogram.length; i++) {
				SIFThistogram[i] = 0;
			}

			FW.write("{");
			HardAssigner<float[], ?, ?> assigner = codebook.defaultHardAssigner();
			for(Keypoint key : keypoints){
				ByteFV siftvector = key.getFeatureVector();
				float[] ff2 = new float[siftvector.values.length];
				for(int i=0;i<siftvector.values.length;i++){
					ff2[i] = siftvector.values[i];
				}

				// returns the index of selected centroid 				int classLabel = assigner.assign(ff2);
				SIFThistogram[classLabel] = SIFThistogram[classLabel]+1;

				// outputting individual labels
				// FW.write("" + j + " " +classLabel + ", ");
			}
			// for ( Keypoint k : keys)
			// {
			// write SIFThistogram to file
			// }
			// write histogram to file
			for(int i=0;i<SIFThistogram.length;i++){
				FW.write(""+i+" "+SIFThistogram[i]+", ");
			}
			FW.write(new Integer(SIFThistogram.length).toString() + " ");
			FW.write(" \"" + getAccess(url2access.get(url)) + "\"}\n");
		}
		FW.close();
	}

	public void getSIFTFeaturesForURL(String URL, String img_path,
			String fileOut, FloatCentroidsResult codebook) throws IOException {
		
		//addSIFTHeader(fileOut, 12000,"sift");
		FileWriter FW = new FileWriter(fileOut, true);
		//FileWriter FW1 = new FileWriter(img_path+"_SIFT_TMP", true);
		try {
			FImage fmg = ImageUtilities.readF(new URL(URL));
			DoGSIFTEngine engine = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> queryKeypoints = engine
					.findFeatures(fmg);



			if (queryKeypoints.size() == 0) {
				FW.write("{ ");
				FW.write(new Integer(SIFThistogram.length).toString() + " ");

				FW.write(" \"" + getAccess(url2access.get(URL)) + "\"}\n");

			//	FW1.write("{ ");
			//	FW1.write(new Integer(SIFThistogram.length).toString() + " ");
			//	FW1.write(" \"" + getAccess(url2access.get(URL)) + "\"}\n");
				return;
			}

			List<Keypoint> keys = queryKeypoints.subList(0,
					queryKeypoints.size() - 1);

			// remember to set SIFThistogram to zero

			for (int i = 0; i < SIFThistogram.length; i++) {
				SIFThistogram[i] = 0;
			}

			FW.write("{ ");


			HardAssigner<float[], ?, ?> assigner = codebook
					.defaultHardAssigner();

			for (Keypoint kp : keys) {

				ByteFV siftVector = kp.getFeatureVector();

				float[] ff2 = new float[siftVector.values.length];
				for (int i = 0; i < siftVector.values.length; i++) {
					ff2[i] = siftVector.values[i];
				}
				int classLabel = assigner.assign(ff2);
				SIFThistogram[classLabel] = SIFThistogram[classLabel] + 1;
			}
			for (int i = 0; i < SIFThistogram.length; i++) {
				if (SIFThistogram[i] > 0) {
					FW.write("" + i + " " + SIFThistogram[i] + ", ");
					//FW1.write("" + i + " " + SIFThistogram[i] + ", ");
				}
			}
			FW.write(new Integer(SIFThistogram.length).toString() + " ");
			//FW.write(" \"" + getAccess(url2access.get(URL)) + "\"}\n");
			FW.write(" \"" + "private" + "\"}\n");
		//	FW1.write(new Integer(SIFThistogram.length).toString() + " ");
		//	FW1.write(" \"" + getAccess(url2access.get(URL)) + "\"}\n");
			//FW.write("?}\n");
		} catch (Exception e) {
			System.err.println("While processing URL: " + URL);
			e.printStackTrace();
		}

		FW.close();
	//	FW1.close();
	}

	public int[] getVector(String image_path){
		// [0][]=red
		// [1][]=green
		// [2][]=blue
		// etc
		System.out.println("get vector - " + image_path);
		values = ImageUtils.extractRGB(image_path);

		System.out.println(CONSTANT.numValues);
		
		int k=0;
		for(int i = 0; i < CONSTANT.numFeatures; i++){
			for(int j=0;j<CONSTANT.numValues;j++){
				vector[k]= values[i][j];
				k++;
			}
		}
	
		return vector;
	}

	// rgb representation and the access(private/public)
	public void getRGBRepresentation(List<String> URLs, String img_path, String fileOut) throws IOException{
		FileWriter fw = new FileWriter(fileOut);
		fw.write(header.toString()+"\n\n");

		for(String URL : URLs){
			// RGB features
			int[] vector = this.getVector(URL);
			for (int j = 0; j < vector.length; j++) {
				fw.write(vector[j] + ", ");
			}
			fw.write(" \"" + getAccess( url2access.get(URL)) + "\"\n");
		}
		fw.close();
	}

	public void addRGBHeader(File fileOut) throws IOException{
		if(fileOut.exists()){
			return;
		}
		FileWriter fw = new FileWriter(fileOut);
		setHeader();
		fw.write(header.toString()+"\n\n");
		fw.close();
	}

	//for a single url
	public boolean getRGBRepresentation(String URL, String fileOut, String privacy, int count) throws IOException{
		FileWriter fw = new FileWriter(fileOut, true);

		try{
			int[] vector = this.getVector(URL);
			if(vector.length==0){
				fw.close();
				return false;
			}
			fw.write("{");
			for(int j=0;j<vector.length;j++){
				fw.write(j+" "+vector[j]+",");
			}
			fw.write(vector.length+" "+getAccess(privacy)+"\"}\n");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fw.write(" \"" + getAccess(privacy) + "\"\n");
			fw.close();
			System.err.println("While processing URL: " + URL);
			e.printStackTrace();
		}
		fw.close();
		return true;
	}

	// for a url, when we use privacy for {tag, share, view} 
	public boolean getRGBRepresentation(String URL, String fileOut, List<String> privacyList, int count) throws IOException{

		FileWriter fw = new FileWriter(fileOut,true);
		try{
		fw.write("{");
		int[] vector = this.getVector(URL);
		if(vector.length==0){
			fw.close();
			return false;
		}
		for(int i=0;i<vector.length;i++){
			fw.write(i+" "+ vector[i]+", ");
		}
		String privacyValues = privacyValues(privacyList, vector.length);
		fw.write(privacyValues);
		} catch (Exception e) {
			String privacyValues = privacyValues(privacyList, vector.length);
			fw.write(privacyValues);
			fw.close();
			System.err.println("While processing URL: " + URL);
			e.printStackTrace();
		}
		
		fw.close();
		return true;
	}

	public void addSIFTHeader(String string, int dataCount, String relation) 
	{
		try {
			FileWriter fw =  new FileWriter(string);
			String lsp = "\n";
			fw.write("@relation "+relation + lsp);

			for( int i = 0; i < dataCount ; i++)
			{
				fw.write("@attribute a" + i+" numeric" + lsp);
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
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String path = "/Users/Aurnob/Downloads/images/src/data/sample/rahul/september9/10000/arff_facial/";
		File csvFile = new File("/Users/Aurnob/Downloads/images/src/data/sample/rahul/september9/10000/arff_facial/10000.csv");
		String home_path = "/Users/Aurnob/Downloads/images/src/data/";
		String fileTrain = home_path + "/train_images.txt";
		String fileOut = home_path + "/sample/rahul/test/train_images.arff";

		System.out.println("Loading train!");
		Sequences train = new Sequences();
		try {
			train.loadSequences(fileTrain);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done loading train!");

		RGBInstances iRep = new RGBInstances();
		// writes the header for arff with the features and the access (public/private)
		iRep.setHeader();
		List<String> URLs = iRep.getStaticURL(csvFile);

		//		Codebook Codebook = new Codebook();
		//			String file = "SIFT_unlabelled.arff";
		//			FloatCentroidsResult fcr = Codebook.loadCodebook(URLs, path, path+file );

		//			iRep.getRGBRepresentation(URLs, path, fileOut);
		//			iRep.getSIFTFeatures(URLs, path, path + file, fcr);


		for(String URL : URLs){
			iRep.getEdgeDirectionCoherenceFeatures(URL, path, path + "EDGC.arff");
		}
		//			iRep.getFacialFeatures(URLs, path, path + "Facial.arff", path
		//					+ "FacialFeatures.op");
		System.out.println("DONE");

	}

}
