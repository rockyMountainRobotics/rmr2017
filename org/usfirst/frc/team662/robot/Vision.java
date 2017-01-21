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


	public Vision() {
	    camera = CameraServer.getInstance().startAutomaticCapture();
	    
	    //camera.setExposureManual(0);
	    camera.setResolution(320,240);
	    cameraServer.addCamera(camera);
	    vthread = new VisionThread(camera, new GripPipeline(), pipeline -> {
	            SmartDashboard.putNumber("dankmemes",pipeline.filterContoursOutput().size());        
	    });
	    vthread.start();
	    
	    
	    
	}
	public void update(){/*
		camera.setWhiteBalanceManual(0);

		 VisionThread visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
		        if (!pipeline.filterContoursOutput().isEmpty()) {
		            Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
		            Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));		            
		     
		            int centerLeft = left.x + (left.width / 2);
		            int centerRight = right.x + (right.width / 2);
        
		            

		          /*if(centerLeft < 160 && centerRight < 160)
		          {
		       		DriveMotor1.set(driveLess);
		       		DriveMotor2.set(driveMore);
		           }
		     
		          if(centerLeft > 160 && centerRight > 160)
		          {
		        	DriveMotor1.set(driveMore);
		        	DriveMotor2.set(driveLess);
		          }
		        
		          //
		          if(centerLeft ==  80 && centerRight == 240)
		          {
		        	  
		        	  
		        	  
		        	  
		          }
		        }
		        *//*
		            
		            //Using the left rectangle as the reference point
		            if(state == DRIVE_STATE) 
		            {
		            	if (centerLeft > 80 || centerLeft < 240) {
		            		DriveMotor1.set(drive);
			            	DriveMotor2.set(drive);
		            	}
		            	
		            	else 
		            	{
		            		state = TURN_STATE;
		            	}
		            	
		            } else if (state == TURN_STATE)
		            {		
		            	if(centerLeft  ){
		            		
		            		
		            	}
		            }
		            	
		            	DriveMotor1.set(-0.25);
		            	DriveMotor2.set(0.25);
		            } else
		            {
		            	state = DRVE_STATE;

		            }
		           
		            
		            
		          
		            	
		            	/*if(centerLeft < 80 || centerLeft > 240)
		            	{
		            		DriveMotor1.set(drive);
		            		DriveMotor2.set(drive);
		            	}
		            	else
		            	{
		            		state = TURN_STATE;
		            	}
		            } 
		            else if(state == TURN_STATE)
		            {
		            	state = DRIVE_STATE;
		            	DriveMotor1.set(0.25);
		            	DriveMotor2.set(-0.25);*/
		            }
		            
		            
		           
		            
		        
		        
		        
	public void autoUpdate(){}

}
