import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.features2d.*;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.highgui.*;

public class ImageProcessing {
	
	public static void main(String[] args) {
		
		long start_time = System.nanoTime();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		File outputFile = null;
		try {
			outputFile = File.createTempFile("orbDetectorParams", ".YAML");
		    FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("%YAML:1.0\nhessianThreshold: 400.\noctaves: 3\noctaveLayers: 4\nupright: 0\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Mat img1 = Highgui.imread("C:/Users/Ibrahim/Pictures/test3.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
		Mat img2 = Highgui.imread("C:/Users/Ibrahim/Pictures/test5.jpg", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
	
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
		detector.read(outputFile.getPath());
		
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
		
		detector.detect(img1, keypoints1);
		detector.detect(img2, keypoints2);
		
		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		
		Mat descriptors1 = new Mat(), descriptors2 = new Mat();
		extractor.compute(img1, keypoints1, descriptors1);
		extractor.compute(img2, keypoints2, descriptors2);
		
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		
		MatOfDMatch matches = new MatOfDMatch();
		
		matcher.match(descriptors1, descriptors2, matches);
		
		double max_dist = 0; double min_dist = 100;
		
		for( int i = 0; i < descriptors1.rows(); i++ ){ 
			double dist = matches.toArray()[i].distance;
		    if( dist < min_dist ) min_dist = dist;
		    if( dist > max_dist ) max_dist = dist;
		}
		
		System.out.println("Minimum distance: "+min_dist);
		System.out.println("Maximum distance: "+max_dist);
		
		MatOfDMatch goodMatches = new MatOfDMatch();
		
		for( int i = 0; i < descriptors1.rows(); i++ ){ 
			if(matches.toArray()[i].distance < 2*min_dist){	
				Mat tempMat = new MatOfDMatch(matches.toArray()[i]);
				goodMatches.push_back(tempMat);
			}
		}
		
		System.out.println("goodMatches: "+goodMatches.size());
		
		Mat imageMatches = new Mat();
		Features2d.drawMatches(img1, keypoints1, img2, keypoints2, goodMatches, imageMatches);
		
		System.out.println("imageMatches: "+imageMatches.size());
		
		MatOfPoint2f obj = new MatOfPoint2f();
		MatOfPoint2f scene = new MatOfPoint2f();

		LinkedList<Point> objList = new LinkedList<Point>();
		LinkedList<Point> sceneList = new LinkedList<Point>();
		
		List<KeyPoint> keypoints_objectList = keypoints1.toList();
		List<KeyPoint> keypoints_sceneList = keypoints2.toList();
		
		for(int i = 0; i<goodMatches.toArray().length; i++){
		    objList.addLast(keypoints_objectList.get(goodMatches.toArray()[i].queryIdx).pt);
		    sceneList.addLast(keypoints_sceneList.get(goodMatches.toArray()[i].trainIdx).pt);
		}
			
		obj.fromList(objList);
		scene.fromList(sceneList);

		Mat H = Calib3d.findHomography(obj, scene);
		boolean result = niceHomography(H);
		
		System.out.println("Result: "+result);
		double score = imageMatches.cols()*0.3 + goodMatches.cols()*0.7;
		System.out.println("score: "+score);
		//imshow("Good Matches", imageMatches);
		
		long end_time = System.nanoTime();
		double difference = (end_time - start_time)/1e9;
		System.out.println("Time to calculate: "+difference);
	}
	
    public static void imshow(String title, Mat img) {
        
        // Convert image Mat to a jpeg
        MatOfByte imageBytes = new MatOfByte();
        Highgui.imencode(".jpg", img, imageBytes);
        
        try {
            // Put the jpeg bytes into a JFrame window and show.
            JFrame frame = new JFrame(title);
            frame.getContentPane().add(new JLabel(new ImageIcon(ImageIO.read(new ByteArrayInputStream(imageBytes.toArray())))));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean niceHomography(Mat H)
    {
      double det = H.get(0, 0)[0] * H.get(1, 1)[0] - H.get(1, 0)[0] * H.get(0, 1)[0];
      double N3 = Math.sqrt(H.get(2, 0)[0] * H.get(2, 0)[0] + H.get(2, 1)[0] * H.get(2, 1)[0]);
      double N2 = Math.sqrt(H.get(0, 1)[0] * H.get(0, 1)[0] + H.get(1, 1)[0] * H.get(1, 1)[0]);
      double N1 = Math.sqrt(H.get(0, 0)[0] * H.get(0, 0)[0] + H.get(1, 0)[0] * H.get(1, 0)[0]);
      
      System.out.println("det: "+det);
      System.out.println("N1: "+N1);
      System.out.println("N2: "+N2);
      System.out.println("N3: "+N3);
      
      if (det < -1){   	  
    	  return false;  
      }
   
      if (N1 > 4 || N1 < 0.1){
    	  return false;
      }
      
      if (N2 > 4 || N2 < 0.1){
    	  return false;
      }
      
      if (N3 > 0.015){  
    	  return false;
      }
      
      return true;
    }
    
    
}
