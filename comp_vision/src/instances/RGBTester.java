package instances;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.ml.clustering.FloatCentroidsResult;


public class RGBTester {

	// extracts the rgb values from a given image
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		String path = "/Users/Andreea/Desktop/before.jpg";	
		FImage image = ImageUtilities.readF(new File(path));
		ImageIcon icon = new ImageIcon(path);

		//ImageUtilities.write(image, new File("/Users/Andreea/Desktop/after.jpg"));

		int[][] rgbValues = new int[6][256];

		BufferedImage buff = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = buff.createGraphics();
		Image imag = icon.getImage();
		graphics.drawImage(imag, 0, 0, null);

		for(int y=0;y<icon.getIconHeight();y++){
			for(int x=0;x<icon.getIconWidth();x++){
				// rgb=an integer pixel in the default RGB color model and default sRGB colorspace.
				int rgb = buff.getRGB(x, y);
				String hex = Integer.toHexString(rgb);

				rgbValues[0][Integer.parseInt(hex.substring(2,4),16)]++;
				rgbValues[1][Integer.parseInt(hex.substring(4,6),16)]++;
				rgbValues[2][Integer.parseInt(hex.substring(6,8),16)]++;

				float[] hsbValues = new float[3];
				hsbValues = Color.RGBtoHSB(Integer.parseInt(hex.substring(2,4),16), Integer.parseInt(hex.substring(4,6),16), Integer.parseInt(hex.substring(6,8),16), hsbValues);
				rgbValues[3][Math.round(hsbValues[0]*255)]++;
				rgbValues[4][Math.round(hsbValues[1]*255)]++;
				rgbValues[5][Math.round(hsbValues[2]*255)]++;
			}
		}
		for(int x=0;x<rgbValues.length;x++){
			for(int y=0;y<rgbValues[x].length;y++){
			//	System.out.print(rgbValues[x][y]+" ");
			}
		//	System.out.println("\n");
		}
		List<String> urls = new ArrayList<String>();
		urls.add("http://farm4.staticflickr.com/3296/2618833302_babbe89fd8_m.jpg");
		urls.add("http://farm5.staticflickr.com/4068/4654120186_f7fb504392_m.jpg");
		RGBInstances iRep = new RGBInstances();
		iRep.getRGBRepresentation("http://farm4.staticflickr.com/3296/2618833302_babbe89fd8_m.jpg", "/Users/Andreea/Desktop/before1.txt", "private",1);
		iRep.getEdgeDirectionCoherenceFeatures("http://farm4.staticflickr.com/3296/2618833302_babbe89fd8_m.jpg", "/Users/Andreea/Desktop/edge_detection.txt", "private");
		FloatCentroidsResult codeb = Codebook.loadCodebook(urls, "http://farm4.staticflickr.com/3296/2618833302_babbe89fd8_m.jpg", "/Users/Andreea/Desktop/codebook.txt");
		iRep.getSIFTFeaturesForURL("http://farm4.staticflickr.com/3296/2618833302_babbe89fd8_m.jpg", "http://farm4.staticflickr.com/3296/2618833302_babbe89fd8_m.jpg", "/Users/Andreea/Desktop/siftFeatures.txt",codeb);	
		}

}
