package org.usfirst.frc.team662.robot;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class LawrensVision {
	double distanceLeft;
	double distanceRight;
	double yDistance;
	final double realheight = 5;
	final int imageheight = 240;
	final double sensorheight = 0.68;
	double distanceToturn;
	double xDistance;
	public LawrensVision() {
		
	
	CameraServer cameraServer = CameraServer.getInstance(); 
	UsbCamera camera = cameraServer.startAutomaticCapture();

		
		//f (inches)  real height(inches) * image height (pixels)
		//  		  object height(pixels) * sensor height (inches)
		
	//USE COMMENTS GOSH DANG IT
	//what in ternation
	
		 VisionThread visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
			 if (!pipeline.filterContoursOutput().isEmpty()) {
				 Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				 Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));		            
 
				 double leftHeight = left.y + left.height;
				 double rightHeight= right.y + right.height;
				 
				 //Distance
				 distanceLeft = (realheight*imageheight)/(leftHeight*sensorheight);
				 distanceRight = (realheight*imageheight)/(rightHeight*sensorheight);
		
				 //10.25 inches
				 if(distanceLeft < 10.25 &&  10.25 < distanceRight){
					 yDistance = 5.125 + distanceLeft * Math.sin(Math.acos(( Math.pow(distanceLeft, 2) + Math.pow(10.25, 2) - distanceRight ) / (2 * distanceLeft * 10.25))) - 90;	
				 	
				 }	
				 else if(10.25 < distanceLeft &&  10.25 < distanceRight){
					 yDistance = 5.125 - (( Math.pow(distanceLeft, 2) + Math.pow(10.25, 2) - Math.pow(distanceRight, 2)) / (2 * 10.25));
				 
				 }
				 xDistance = 0.5*Math.sqrt(Math.pow(yDistance, 2) + Math.sqrt(2*(Math.pow(distanceLeft, 2)+Math.pow(distanceRight, 2))-Math.pow(10.25, 2)));
				 
			 
				 
			 
			 
			 }

		 });
				
}
	
}
