package org.usfirst.frc.team662.robot;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.*;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.vision.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Vision implements Component{
	final Object imgLock = new Object();
	VisionThread vthread;
    UsbCamera camera;
    double ratiol = 0;
    double ratior = 0;
	CameraServer cameraServer = CameraServer.getInstance();
	

    final static double drive = .5;
    final static int DRIVE_STATE = 0;
    final static int TURN_STATE = 1;
    int state = 0;
    
    final static double driveMore = 0.25;
    final static double driveLess = -0.25;
    final static int CENTER_THRESHOLD = 160;
    final static int LEFT_THRESHOLD = 80;
    final static int RIGHT_THRESHOLD = 240;
    final static int FINAL_HEIGHT = 20000000;
    int leftHeight;

	public Vision() {
	    camera = CameraServer.getInstance().startAutomaticCapture();
	    
	    //camera.setExposureManual(0);
	    camera.setResolution(320,240);
	    cameraServer.addCamera(camera);
	    vthread = new VisionThread(camera, new GripPipeline(), pipeline -> {
	            SmartDashboard.putNumber("rectangles",pipeline.filterContoursOutput().size());        
	    });
	    vthread.start();
	    
	    
	    
	}
	public void update(){
		camera.setWhiteBalanceManual(0);
		
		int centerLeft;
		int centerRight;
		
		//VisionThread visthred = new VisionThread(camera, new GripPipeline(), pipeline -> {});
		
		 VisionThread visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
			 if (pipeline.filterContoursOutput().size() >= 2) {
				 Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				 Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));		            
				 SmartDashboard.putNumber("rectangles",pipeline.filterContoursOutput().size());
				 
				 centerLeft = left.x + (left.width / 2);
				 centerRight = right.x + (right.width / 2);
				 leftHeight = right.y;}
			
			
			//Drive motor1 is left, two is right
				 
				
				
				
				else if(centerLeft > LEFT_THRESHOLD && centerLeft < CENTER_THRESHOLD)
				{
				 
				 DriveMotor1.set(driveLess);
				 DriveMotor2.set(driveMore);
				 
				}
				else if(centerLeft < LEFT_THRESHOLD)
				{
				 DriveMotor1.set(driveMore);
				 DriveMotor2.set(driveLess);
				}
				else if(centerRight > RIGHT_THRESHOLD)
				{
				 DriveMotor1.set(driveLess);
				 DriveMotor2.set(driveMore);
				}
				else if(centerRight < RIGHT_THRESHOLD && centerRight > CENTER_THRESHOLD)
				{
				 DriveMotor1.set(driveMore);
				 DriveMotor2.set(driveLess);
				}
				 
				
				 //Stops the robot if it gets too close to the peg (peg pokes out 1 foot, maybe stop at 18 inches?)
				else if(leftHeight >= FINAL_HEIGHT)
				{
			   	//Go forward
					DriveMotor1.set(0);
					DriveMotor2.set(0);
				}
			 
			 else
			{
				
				DriveMotor1.set(driveLess);
				DriveMotor2.set(driveMore);
				
			}
				
			
		        
	
		        
		        
		        
	public void autoUpdate(){
		
	}

}
