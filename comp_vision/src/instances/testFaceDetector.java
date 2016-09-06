package instances;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;


public class testFaceDetector {

	public static void main(String[] args) throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
		String path = "/Users/Andreea/Desktop/";
		String url = "http://farm3.staticflickr.com/2399/2171151605_203053fdbe_m.jpg";
		MBFImage mmg = ImageUtilities.readMBF(new URL(url));		
		FImage fm = ImageUtilities.readF(new URL(url));
		FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
		List<DetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity(mmg) );
		for( DetectedFace face : faces ) {
			mmg.drawShape(face.getBounds(), RGBColour.RED);
		}
	//	fm.flatten();
	}

}
