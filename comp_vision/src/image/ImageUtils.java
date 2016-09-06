package image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.ImageIcon;
import java.io.*;

public class ImageUtils {
	public static String[] readNames(String source) {	
		String[] input = new String[CONSTANT.numInput];
		try {
			BufferedReader in = new BufferedReader(new FileReader(source));
			String line = in.readLine();
			int count = 0;
			String temp;
			
			while (line != null) {
				temp = line;
				line = in.readLine();
				if (line != null) {			// 0 mod 3 slots hold the unused image ID
					input[count+1] = temp;
					input[count+2] = line;
				} else
					break;
				line = in.readLine();
				count = count + 3;
			}
			in.close();
		} catch(Exception e) {
			System.out.println("Cannot read the source file.  The file may not be in the correct format.");
			e.printStackTrace();
		}
		return input;
	}
	
    public static int[][] extractRGB(String picture) {
    	/* [0][] Red
    	 * [1][] Green
    	 * [2][] Blue
    	 * [3][] Hue
    	 * [4][] Saturation
    	 * [5][] brightness
    	 */
    	int[][] rgbValues = new int[6][256];
    	
        //URL url = Utils.class.getClassLoader().getResource(picture);
        //ImageIcon icon = new ImageIcon(url);
    	System.out.println("URL is - " + picture);
    	URL pic = null;
    	try {
			pic = new URL(picture);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ImageIcon icon = new ImageIcon(pic);
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);

        Image image = icon.getImage();
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        
        for (int y = 0; y < icon.getIconHeight(); y++) {
            for (int x = 0; x < icon.getIconWidth(); x++) {
            	
            	// rgb=an integer pixel in the default RGB color model and default RGB colorspace.
                int rgb = bufferedImage.getRGB(x, y);
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
        return rgbValues;
    }
    
    public static int extractNumPixels(String picture) {
    	int numPixels;
    	
        URL url = ImageUtils.class.getClassLoader().getResource(picture);
        ImageIcon icon = new ImageIcon(url);
        numPixels = icon.getIconHeight() * icon.getIconWidth();
        
    	return numPixels;
    }

    public static void output(int[][] values, int numPixels, String fileName, String label) {
    	int count = 0;
    	BufferedWriter out = null;
    	try {
    		out = new BufferedWriter(new FileWriter(fileName,true),8 * 1024);
    		for(int i=0;i<CONSTANT.numFeatures;i++) {
    			//out.append(i+1+" ");
    			out.append(label+" ");
    			for(int j=0;j<CONSTANT.numValues;j++) {
    				out.append(1+i*(1+j)+j+":"+values[i][j]+" ");
    				count++;
    			}
    		}
    		out.append("\n");
    		System.out.println(count);
    	}catch(Exception e) {
    		System.out.println("Error writing to file!");
    		e.printStackTrace();
    	}finally {
    		try {
    			out.flush();
    			out.close();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
	public static String getUrlID(String staticUrl) {
		String [] splitStrs = staticUrl.split("/");
		int numSplits = splitStrs.length;
		//System.out.println(splitStrs[numSplits-1]);
		
		String [] urlIdSplit = splitStrs[numSplits-1].split("_");
		return urlIdSplit[0].trim();
	}
	
	public static String privacyValues(List<String> privacyList, int index)
	{
		String retStr = "";
		for(String str:privacyList)
		{
			retStr += index+ " \"" + str + "\",";
			index++;
		}
		retStr += " }\n";
		return retStr;
	}
	
	public static int getNumberOfLines(File filename, String sep) {
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
	
	public static boolean canConnect(String url) {
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
	
    public static void output2(int[][] values, int numPixels, String fileName) {
    	int count = 0;
    	BufferedWriter out = null;
    	try {
    		out = new BufferedWriter(new FileWriter(fileName,true),8 * 1024);
    		for(int i=0;i<CONSTANT.numFeatures;i++) {
    			out.append(i+1+" ");
    			for(int j=0;j<CONSTANT.numValues;j++) {
    				out.append(1+j+":"+values[i][j]+" ");
    				count++;
    			}
    			out.append("\n");
    		}
    		out.append("\n");
    		System.out.println(count);
    	}catch(Exception e) {
    		System.out.println("Error writing to file!");
    		e.printStackTrace();
    	}finally {
    		try {
    			out.flush();
    			out.close();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
}