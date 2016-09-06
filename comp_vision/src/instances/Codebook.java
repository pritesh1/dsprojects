package instances;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;


public class Codebook {

	// writes in cbfile all the features corresponding to the keypoints
	public static boolean loadCodebook(String URL, ArrayList<float[]> f2, File cbFile, int count) throws IOException{

		List<Keypoint> keys = null;
		FImage image = null;

		try {
			image = ImageUtilities.readF(new URL(URL));
			DoGSIFTEngine engine = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> keypoints = engine.findFeatures(image);
			if(keypoints.size()==0){
				return false;
			}
			keys = keypoints.subList(0, keypoints.size()-1);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			return false;
		}
		FileWriter fw = new FileWriter(cbFile,true);
		String fpVal = count +" ";

		for(Keypoint key : keys){
			ByteFV featureVector = key.getFeatureVector();
			float[] ff2 = new float[featureVector.values.length];
			for(int i=0; i< featureVector.values.length; i++){
				ff2[i] = featureVector.values[i];
				fpVal += ff2[i]+",";
			}
			fpVal +=";";
			//for(int i=0;i< ff2.length;i++)
			//System.out.println(i +":  "+ff2[i]);
			f2.add(ff2);
		}
		fw.write(fpVal+"\n");
		fw.close();

		//System.out.println(f2.toArray(new float[1][1]));
		return true;
	}

	public static FloatCentroidsResult getCodeBook(ArrayList<float[]> f2, File fcrFile){
		// 1200 - NUMBER OF CLUSTERS; 20 - maximum number of iterations
		FloatKMeans cluster = FloatKMeans.createExact(1200, 20);

		// transforms the vector with all the features from keypoints in matrix, where o line contains the values for a keypoint
		float[][] f3array = f2.toArray(new float[1][1]);

		// check array - test
		for(float[] f : f3array){
			if(f.length!=128){
				throw new RuntimeException("dim wrong");
			}
		}
		// computes the centroids for 1200 clusters
		FloatCentroidsResult result = cluster.cluster(f3array);
		PrintWriter fw;
		try{
			fw = new PrintWriter(fcrFile);
			result.writeASCII(fw);
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done getting the centroid results");
		return result;
	}

	// features for the edge direction coherence feature (will further check if an image is city or landscape)	
	public static boolean loadEDGECodebook(String URL, ArrayList<float[]> f2, File cbFile, int count) throws IOException{
		FileWriter fw = new FileWriter(cbFile,true);
		String fpVal = count + " ";

		try{
			FImage image = ImageUtilities.readF(new URL(URL));
			EdgeDirectionCoherenceVector edch = new EdgeDirectionCoherenceVector();
			edch.analyseImage(image);

			DoubleFV vec = edch.getFeatureVector();

			float[] ff2 = new float[vec.values.length];
			for(int i=0;i<vec.length();i++){
				ff2[i] = (float) vec.values[i];
				fpVal += ff2[i]+",";
			}
			fpVal+=";";
			f2.add(ff2);
		} catch (Exception e){
			System.err.println("While processing URL: " + URL);
			fw.close();
			return false;
		}

		fw.write(fpVal+"\n");
		fw.close();
		return true;
	}

	public static FloatCentroidsResult loadCodebook(List<String> urls, String image_path, String fileOut_unsupported) throws MalformedURLException, IOException{

		ArrayList<float[]> f2 = new ArrayList<float[]>();
		MBFImage mImage;
		List<Keypoint> keypoints = null;
		FImage image = null;

		for(String url : urls){
			mImage = ImageUtilities.readMBF(new URL(url));
			// returns a single band image containing the result; Flatten the bands into a single band using the average value of the pixels at each location.
			image = mImage.flatten();
			DoGSIFTEngine engine = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> keys = engine.findFeatures(image);

			if(keys.size()==0){
				continue;
			}
			System.out.println(keys.size());
			keypoints = keys.subList(0, keys.size()-1);

			for(Keypoint key : keypoints){
				ByteFV vector = key.getFeatureVector();

				float[] ff2 = new float[vector.values.length];
				for(int i=0;i<vector.values.length;i++){
					ff2[i] = vector.values[i];
				}
				f2.add(ff2);
			}
		}

		FloatKMeans cluster = FloatKMeans.createExact(128, 120);

		float[][] f3 = f2.toArray(new float[1][1]);
		// check array (test)
		for (float[] f : f3)
		{
			if (f.length != 128)
			{
				throw new RuntimeException("dim wrong");
			}

		}
		FloatCentroidsResult result = cluster.cluster(f3);
		System.out.println("done getting FloatCentroidsResult.");
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
