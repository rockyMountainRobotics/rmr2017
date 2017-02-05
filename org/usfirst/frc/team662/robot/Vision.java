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
	final static String VISION_FILE_NAME = "vision";
	
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
	    	
	    	//Sets left and right rects as null
			Rect left = null;
			Rect right = null;
			
			//Please comment what you guys did here 
			if (pipeline.filterContoursOutput().size() >= 2){
				for(int i = 0 ; i < pipeline.filterContoursOutput().size(); i++)
				{
					//Checks how many rectangles the robot sees
					Rect checkrect  = Imgproc.boundingRect(pipeline.filterContoursOutput().get(i));
					
					//Makes sure we are seeing the correct rectangles (not the ones on the furnace)
					if (checkrect.height > checkrect.width)
						{
							if (left == null)
							{
								left = checkrect;
							}
							else
							{
								right = checkrect;
							}
							
						}
					
				}
				
				//If the robot didnt see two rectangles, it sets both to null until Vision is called again
				if (left == null || right == null)
				{
					left = null;
					right = null;
				}
			}
			
			
			//Checks to make sure that the robot is not on yet, and if it isn't, it turns the robot on.
			if (state == State.WAITING && !Drive.isInUse && startRobot && !Recorder.isRecordingPlaying)
			{
				Drive.isInUse=true;
				state = State.START;
				
			}
			if (state == State.START)
			{
				
				//Makes sure that the robot sees the rectangles.
				if (left != null)
				{
					//Checks if the left rectangle is closer than the right triangle, and if it is, sets the robot's state to LEFT.
					if (left.height > right.height)
					{
						state = State.LEFT;
					}
					//Checks if the right rectangle is closer from the robot than the left triangle, and if it is, sets the robot's state to RIGHT.
					if (left.height < right.height)
					{
						state = State.RIGHT;
					}
					//Checks if both widths are the same, and if they are, sets state to CENTERED.
					if (left.width == right.width)
					{
						state = State.CENTERED;
					}
				}
				//If the robot can't see either rectangle, this spins the robot until it can see the rectangles.
				else
				{
					Drive.left.set(driveMore);
					Drive.right.set(driveLess);
				}
				
			}// :)
			
			//If the robot is closer to the left rectangle than the right rectangle, its state becomes LEFT
			if (state == State.LEFT)
			{
				//As long as the robot can see the rectangles,
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
					
					if (left.width <= right.width)
					{
						state = State.CENTERED;
					}
				}	
			}
			//If the robot is closer to the right rectangle than the left rectangle, its state becomes RIGHT
			if (state == State.RIGHT){
				//As long as the robot can see the rectangles,
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
					
					if (left.width >= right.width)
					{
						state = State.CENTERED;
					}
					
				

				}

			}
			//If the robot is perfectly centered already, its state is set to CENTERED
			if (state == State.CENTERED)
			{
				//If the robot is facing too far to the right, it will turn to the left
				if (Math.abs(CENTER_THRESHOLD - left.x) < Math.abs(right.x - CENTER_THRESHOLD))
				{
					Drive.left.set(driveMore);
					Drive.right.set(driveLess);
				}
				//If the robot is facing too far to the left, it will turn to the right
				if (Math.abs(CENTER_THRESHOLD - left.x) > Math.abs(right.x - CENTER_THRESHOLD))
				{
					Drive.left.set(driveLess);
					Drive.right.set(driveMore);
				}
				//If the robot is centered, it will drive forward
				if (Math.abs(CENTER_THRESHOLD - left.x) == Math.abs(right.x - CENTER_THRESHOLD))
				{
					Drive.left.set(driveMore);
					Drive.right.set(driveMore);
				}
				
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
				//Plays file "vision", waits, sets isInUse to false
			if (state == State.FINISH)
			{
				//If the robot is facing right, it will spin to the left
				if (Math.abs(CENTER_THRESHOLD - left.x) < Math.abs(right.x - CENTER_THRESHOLD))
				{
					Drive.left.set(-driveMore);
					Drive.right.set(driveMore);
				}
				//If the robot is facing left, it will spin to the right
				if (Math.abs(CENTER_THRESHOLD - left.x) > Math.abs(right.x - CENTER_THRESHOLD))
				{
					Drive.left.set(driveMore);
					Drive.right.set(-driveMore);
				}
				//If the robot is facing the center, it stops
				if (Math.abs(CENTER_THRESHOLD - left.x) == Math.abs(right.x - CENTER_THRESHOLD))
				{
					Recorder.initializePlay(Recorder.allFound.get(VISION_FILE_NAME));
					SmartDashboard.putBoolean("playing", true);
					state = State.WAITING;
					Drive.isInUse = false;
				}
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
