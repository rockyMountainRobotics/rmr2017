package org.usfirst.frc.team662.robot;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class LawrensVision {
	double distanceLeft; //d
	double distanceRight; //e
	double yDistance; //g
	final double realheight = 5; //Height of tape
	final int imageheight = 240; //Height of image (only good variable name)
	final double sensorheight = 0.68; //How high the camera is on the robot?
	double distanceToturn; //H to Peg?
	double xDistance; //W
	
	public LawrensVision() {
		
	
	CameraServer cameraServer = CameraServer.getInstance(); 
	UsbCamera camera = cameraServer.startAutomaticCapture();

		
		//f (inches)  real height(inches) * image height (pixels)
		//  :)		  object height(pixels) * sensor height (inches)
		
	//USE COMMENTS GOSH DANG IT
	//what in tarnation
	//I'm trying to jump into code with complicated math and terrible comments >:0
	
	
		 VisionThread visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
			 
			 //I ask what this does. "I'm not sure" is the response.
			 if (!pipeline.filterContoursOutput().isEmpty()) {
				 Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				 Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));		            
 
				 double leftHeight = left.y + left.height;
				 double rightHeight= right.y + right.height;
				 
				 //Distance
				 distanceLeft = (realheight*imageheight)/(leftHeight*sensorheight);
				 distanceRight = (realheight*imageheight)/(rightHeight*sensorheight);
		
				 //The distance between the edges of the tape: 10.25 inches
				 
				 //Checking what side of the triangle the robot is on
				 if(distanceLeft < 10.25 &&  10.25 < distanceRight){
					 
					 //Robot is on the left of the tape
					 yDistance = 5.125 + distanceLeft * Math.sin(Math.acos(( Math.pow(distanceLeft, 2) + Math.pow(10.25, 2) - distanceRight ) / (2 * distanceLeft * 10.25))) - 90;	
				 	
				 }	
				 else if(10.25 < distanceLeft &&  10.25 < distanceRight){
					 
					 //Robot is on the right of the tape
					 yDistance = 5.125 - (( Math.pow(distanceLeft, 2) + Math.pow(10.25, 2) - Math.pow(distanceRight, 2)) / (2 * 10.25));
				 
				 }
				 xDistance = 0.5*Math.sqrt(Math.pow(yDistance, 2) + Math.sqrt(2*(Math.pow(distanceLeft, 2)+Math.pow(distanceRight, 2))-Math.pow(10.25, 2)));
				 
			 
				 /* This code's comments turned me into a violent psychopath.
				  * 
				  * "Always code a if the guy who ends up maintaining your code is a violent psychopath who knows where you live" -Martin Golding
				  * 
				  */
			 
			 
			 }

		 });
				
}
	
}
