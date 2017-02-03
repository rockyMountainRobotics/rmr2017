package org.usfirst.frc.team662.robot;

import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.*;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.vision.*;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Vision implements Component
{
	final Object imgLock = new Object();
	VisionThread vthread;
    UsbCamera camera;
    double ratiol = 0;
    double ratior = 0;
	CameraServer cameraServer = CameraServer.getInstance();
	
	public static boolean startRobot = false;
    final static double drive = .5;
    final static int DRIVE_STATE = 0;
    final static int TURN_STATE = 1;
    public enum State
    {
    	LEFT, RIGHT, START, FINISH, CENTERED, WAITING
    }
    State state = State.WAITING;
    final static double driveMore = 0.25;
    final static double driveLess = .1;
    final static int CENTER_THRESHOLD = 160;
    final static int LEFT_THRESHOLD = 80;
    final static int RIGHT_THRESHOLD = 240;
    final static int FINAL_HEIGHT = 20000000;
    int leftHeight;
    int rightHeight;

	public Vision() {
	    camera = CameraServer.getInstance().startAutomaticCapture();
	    //camera.setExposureManual(0);
	    camera.setResolution(320,240);
	    cameraServer.addCamera(camera);
	    vthread = new VisionThread(camera, new GripPipeline(), pipeline -> {
	        SmartDashboard.putNumber("rectangles",pipeline.filterContoursOutput().size());
			camera.setWhiteBalanceManual(0);
			
			int centerLeft;
			int centerRight;
	    		
	    	Robot.stick.getRawButton(XboxMap.START);
	    		
	    		
			Rect left = null;
			Rect right = null;
			if (pipeline.filterContoursOutput().size() >= 2){
				left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
			}
			//Needs check a variable/button
			
			
			if (state == State.WAITING && Drive.isInUse==false)
			{
				Drive.isInUse=true;
				state = State.START;
				
			}
			if (state == State.START)
			{

				if (left != null)
				{
					if (left.height > right.height)
					{
						state = State.LEFT;
					}
					if (left.height < right.height)
					{
						state = State.RIGHT;
					}
					if (left.height == right.height)
					{
						state = State.CENTERED;
					}
				}
				else
				{
					Drive.left.set(driveMore);
					Drive.right.set(driveLess);
				}
				
			}// :)
			
			if (state == State.LEFT)
			{
				if (left != null)
				{
					if (left.x > LEFT_THRESHOLD){
						Drive.left.set(driveLess);
						Drive.right.set(driveMore);
					}
					if (left.x <= LEFT_THRESHOLD)
					{
						Drive.left.set(driveMore);
						Drive.right.set(driveLess);
						
					}
					if(leftHeight >= FINAL_HEIGHT)
					{
					   	//Go forward
						state = State.FINISH;
						Drive.left.set(0);
						Drive.right.set(0);
					}
				}	
			}
			if (state == State.RIGHT){
				if (left != null)
				{
					if (right.x > RIGHT_THRESHOLD){
						Drive.left.set(driveLess);
						Drive.right.set(driveMore);
					}
					if (right.x <= RIGHT_THRESHOLD)
					{
						Drive.left.set(driveMore);
						Drive.right.set(driveLess);
					}
					if(rightHeight >= FINAL_HEIGHT)
					{
					   	//Go forward
						state = State.FINISH;
						Drive.left.set(0);
						Drive.right.set(0);
					}

				}
				
				
				
				
			}
			if (state == State.CENTERED){
				Drive.left.set(driveMore);
				Drive.right.set(driveMore);
				
				if(leftHeight >= FINAL_HEIGHT)
				{
				   	//Go forward
					state = State.FINISH;
					Drive.left.set(0);
					Drive.right.set(0);
				}
			}
				
			if (state == State.FINISH)
			{
				//Finished state
				//Play finished recording where the gear is dropped and the robot goes back beeping and booping
			}
	    			    
	    });
	    vthread.start();
	    
	    
	    
	}
	public void update()
	{

	}
	public void autoUpdate()
	{
		
	}
	
	public void disable()
	{
		vthread.stop();
	}

}
