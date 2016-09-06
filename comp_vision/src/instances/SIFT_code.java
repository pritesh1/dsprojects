package instances;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openimaj.feature.ByteFV;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

public class SIFT_code {
	public static 	int[] SIFThistogram = new int[1200];

	public static FloatCentroidsResult loadCodebook() throws MalformedURLException, IOException{

		ArrayList<float[]> f2 = new ArrayList<float[]>();
		MBFImage mImage;
		List<Keypoint> keypoints = null;
		FImage image = null;

		File[] folder = new File("/Users/Andreea/Documents/Research/Data/images project/data/crawled_dataset/photos").listFiles();
		for(int i=0;i<folder.length;i++){
			mImage = ImageUtilities.readMBF(folder[i]);

			// returns a single band image containing the result; Flatten the bands into a single band using the average value of the pixels at each location.
			image = mImage.flatten();
			DoGSIFTEngine engine = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> keys = engine.findFeatures(image);

			if(keys.size()==0){
				continue;
			}
			keypoints = keys.subList(0, keys.size()-1);

			for(Keypoint key : keypoints){
				ByteFV vector = key.getFeatureVector();

				float[] ff2 = new float[vector.values.length];
				for(int j=0;j<vector.values.length;j++){
					ff2[j] = vector.values[j];
				}
				f2.add(ff2);
			}

			if(i%200==0){
				System.out.println("processed "+i+" images");
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

	// returns the "bag of codewords" representation for a photo
	public static void getSIFTFeaturesForPhotoUsingCodebook(File photo, String fileOut, FloatCentroidsResult codebook) throws IOException {

		FileWriter FW = new FileWriter(fileOut, true);
		try {
			FImage fmg = ImageUtilities.readF(photo);
			DoGSIFTEngine engine = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(fmg);
			if (queryKeypoints.size() == 0) {
				FW.write(photo.getName()+"\t"+"{ }");
				return;
			}
			List<Keypoint> keys = queryKeypoints.subList(0,
					queryKeypoints.size() - 1);

			// remember to set SIFThistogram to zero
			for (int i = 0; i < SIFThistogram.length; i++) {
				SIFThistogram[i] = 0;
			}
			FW.write(photo.getName()+"\t"+"{ ");
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
				}
			}
			FW.write("}\n");
		} catch (Exception e) {
			System.err.println("While processing URL: " + photo.getName());
			e.printStackTrace();
		}
		FW.close();
	}
	
	// returns for each keypoint, the sift values associated with it
	public static HashMap<Integer, ArrayList<Double>> getSIFTfeaturesForPhoto(File photo1) throws IOException{
		HashMap<Integer, ArrayList<Double>> siftValuesPerKeyPoint = new HashMap<Integer, ArrayList<Double>>();
		try{
			FImage fmg1 = ImageUtilities.readF(photo1);
			DoGSIFTEngine engine1 = new DoGSIFTEngine();
			LocalFeatureList<Keypoint> queryKeypoints1 = engine1.findFeatures(fmg1);
			if (queryKeypoints1.size() == 0) {
				System.out.println("niciun keypoint");
				return null;
			}
			List<Keypoint> keys1 = queryKeypoints1.subList(0,
					queryKeypoints1.size() - 1);

			int k=0;
			for (Keypoint kp1 : keys1) {
				ByteFV siftVector = kp1.getFeatureVector();
				ArrayList<Double> values = new ArrayList<Double>();
				for (int i = 0; i < siftVector.values.length; i++) {
					values.add((double) siftVector.values[i]);
				}
				siftValuesPerKeyPoint.put(k,values);
				k++;
			}
		} catch (Exception e) {
			System.err.println("While processing photo: " + photo1.getName());
			e.printStackTrace();
		}
		return siftValuesPerKeyPoint;
	}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
		FloatCentroidsResult centroids = loadCodebook();
		File photo = new File("/Users/Andreea/Documents/Research/Data/images project/data/crawled_dataset/photos/104958071.jpg");
		
		getSIFTFeaturesForPhotoUsingCodebook(photo,"/Users/Andreea/Documents/Research/Data/images project/data/crawled_dataset/SIFTfeatures.txt",centroids);
		HashMap<Integer, ArrayList<Double>> siftValuesPerKeyPoint = getSIFTfeaturesForPhoto(photo);
			
	}

}
