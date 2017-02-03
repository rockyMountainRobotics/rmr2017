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
    final static int FINAL_HEIGHT = 2000000000;
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
	    		
	    	if (Robot.stick.getRawButton(XboxMap.START))
	    	{
	    		startRobot = true;
	    	}
	    	//Check if height is larger than width
	    	
	    	//Sets left and right rekts as null
			Rect left = null;
			Rect right = null;
			
			//Please comment what you guys did here 1=1=1=1=1=1=1=1=1=1=1
			if (pipeline.filterContoursOutput().size() >= 2){
				left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
				for(int i = 0 ; i < pipeline.filterContoursOutput().size(); i++)
				{
					//Checks how many rektangles the robot sees
					Rect checkRekt  = Imgproc.boundingRect(pipeline.filterContoursOutput().get(i));
					
					//Makes sure we are seeing the correct rektangles (not the ones on the furnace)
					if (checkRekt.height > checkRekt.width)
						{
							if (left == null)
							{
								left = checkRekt;
							}
							else
							{
								right = checkRekt;
							}
							
						}
					
				}
				
				//If the robot didnt see two rektangles, it sets both to null until Vision is called again
				if (left == null || right == null)
				{
					left = null;
					right = null;
				}
			}
			
			
			//Checks to make sure that the robot is not on yet, and if it isn't, it turns the robot on.
			if (state == State.WAITING && !Drive.isInUse && startRobot)
			{
				Drive.isInUse=true;
				state = State.START;
				
			}
			if (state == State.START)
			{
				
				//Makes sure that the robot sees the rectangles.
				if (left != null)
				{
					//Checks if the left rektangle is closer than the right triangle, and if it is, sets the robot's state to LEFT.
					if (left.height > right.height)
					{
						state = State.LEFT;
					}
					//Checks if the right rektangle is closer from the robot than the left triangle, and if it is, sets the robot's state to RIGHT.
					if (left.height < right.height)
					{
						state = State.RIGHT;
					}
					//Checks if the robot is perfectly centered between the two rectangles, and if it is, sets the robot's state to CENTERED.
					if (left.height == right.height)
					{
						state = State.CENTERED;
					}
				}
				//If the robot can't see either rektangle, this spins the robot until it can see the rektangles.
				else
				{
					Drive.left.set(driveMore);
					Drive.right.set(driveLess);
				}
				
			}// :)
			
			//If the robot is closer to the left rektangle than the right rektangle, its state becomes LEFT
			if (state == State.LEFT)
			{
				//As long as the robot can see the rektangles,
				if (left != null)
				{
					//If the robot sees between the left threshold (80 pixels) and the center (160 pixels), the robot turns to the right.
					if (left.x > LEFT_THRESHOLD){
						Drive.left.set(driveMore);
						Drive.right.set(driveLess);
					}
					//If the robot sees between the left threshold (80 pixels) and the left edge (0 pixels), the robot turns to the left.
					if (left.x <= LEFT_THRESHOLD)
					{
						Drive.left.set(driveLess);
						Drive.right.set(driveMore);
						
					}
					//If the robot is within a certain distance of the peg, the robot's state is set to FINISH, and the motors are set to stop. 
					if(leftHeight >= FINAL_HEIGHT)
					{
					   	//Play FINISH, which is a recorder that moves the robot to the peg.
						state = State.FINISH;
						Drive.left.set(0);
						Drive.right.set(0);
					}
				}	
			}
			//If the robot is closer to the right rektangle than the left rectangle, its state becomes RIGHT
			if (state == State.RIGHT){
				//As long as the robot can see the rektangles,
				if (left != null)
				{
					//If the robot sees between the right threshold (240 pixels) and the right edge (320 pixels), the robot turns to the right.
					if (right.x > RIGHT_THRESHOLD){
						Drive.left.set(driveMore);
						Drive.right.set(driveLess);
					}
					//If the robot sees between the center (160 pixels) and the right threshold (240 pixels), the robot turns to the left.
					if (right.x <= RIGHT_THRESHOLD)
					{
						Drive.left.set(driveLess);
						Drive.right.set(driveMore);
					}
					//If the robot is within a certain distance of the peg, the robot's state is set to FINISH, and the motors are set to stop. 
					if(rightHeight >= FINAL_HEIGHT)
					{
					   	//Go forward
						state = State.FINISH;
						Drive.left.set(0);
						Drive.right.set(0);
					}

				}

			}
			//If the robot is perfectly centered already, its state is set to CENTERED
			if (state == State.CENTERED)
			{
				//Moves the robot straight forward (its centered already).
				Drive.left.set(driveMore);
				Drive.right.set(driveMore);
				
				//If the robot gets within a certain distance of the peg, it runs a recorder program through the FINISH state.
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
		
		startRobot = true;
		
	}
	
	public void disable()
	{
		vthread.stop();
	}

}
