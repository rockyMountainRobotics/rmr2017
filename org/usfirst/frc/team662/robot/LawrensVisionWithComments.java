package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.opencv.core.Rect;

import org.opencv.imgproc.Imgproc;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class LawrensVisionWithComments implements Component{
	//Variables correspond to the triangle screenshot titled VisionTriangle.png in ThisPC >> Pictures >> ScreenShot.
	
	public enum State {
		INIT, MOVE_FORWARD, TURN, STOP, END, TURN_ON_PEG, FINISH, DO_NOTHING, CENTER, WAIT
	}
	State state = State.WAIT;
	
	VisionThread vthread;

	
	boolean isFinished;
	boolean prevButton = false;
	//-1 is left, 0 center, 1 is right
	int isInCenter = 0;
	
	double right1;
	double left1;
	double right2;
	double left2;
	final static String VISION_FILE_NAME = "vision.ser";
	double robToLeftTape; //d
	double robotXDistanceToPeg; //g
	double robToRightTape; //e
	final static double REAL_TAPE_HEIGHT = 5; //Height of tape in real life in inches
	final static double SENSOR_HEIGHT = 0.68; //Height of the sensor above the ground
	final static int IMAGE_HEIGHT = 240; //Height of the image in pixels
	double robotYDistanceToPeg; //q
	double robotToPeg; //w
	double distanceToTurn; //How far the robot needs to turn
	double robotToEndOfPeg; //h
	final static double FOV = Math.PI/3; //60 degree field of view in radians
	final static double TARGET_SIZE = 43;
	final static double TURN_SPEED = .3;
	final static double STRAIGHT_SPEED = -.3;
	int startPositionLeft;
	int startPositionRight;
	DualTalon rightMotor = Drive.right;
	DualTalon leftMotor = Drive.left;
	 
	UsbCamera camera = CameraSwitch.currentCamera;
	//VisionThread vthread1;
	public LawrensVisionWithComments(){
	    camera.setExposureManual(0);
	    //camera.setBrightness(0);
	    camera.setResolution(320,240);
	    CameraSwitch.camServer.addCamera(camera);
	    
	    vthread = new VisionThread(camera, new GripPipeline(), pipeline -> {		
			//Check to see if the contours are empty or not.

			//System.out.println(pipeline.filterContoursOutput().size() + " Is the number of targest found");
	    	SmartDashboard.putNumber("Number of targets", pipeline.filterContoursOutput().size());
			if (pipeline.filterContoursOutput().size() == 2) 
			{
				//System.out.println(state + " is the state for vision");
				//Create rectangles things from the vision
				Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
				Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
				if (left.x > right.x){
					Rect temp = left;
					left = right;
					right = temp;
				}
				SmartDashboard.putNumber("Size right ", right.width);
				SmartDashboard.putNumber("Size left", left.width);


				
				double leftHeight = left.y + left.height;
				double rightHeight= right.y + right.height;
				
				//Finding the distance between the robot from the left and right tape
				robToLeftTape = (REAL_TAPE_HEIGHT*IMAGE_HEIGHT)/(leftHeight*SENSOR_HEIGHT);
				robToRightTape = (REAL_TAPE_HEIGHT*IMAGE_HEIGHT)/(rightHeight*SENSOR_HEIGHT);
				
				//The distance between the edges of the tape is 10.25inches
				
				//Checking to see which side of the tape the robot is on
				if(robToLeftTape < 10.25 &&  10.25 < robToRightTape)
				{
					//Robot is on the left side
					robotXDistanceToPeg = 5.125 + robToLeftTape * Math.sin(Math.acos(( Math.pow(robToLeftTape, 2) + Math.pow(10.25, 2) - robToRightTape ) / (2 * robToLeftTape * 10.25))) - 90;
				}
				else if(10.25 < robToLeftTape &&  10.25 < robToRightTape)
				{
					//Robot is on the right side
					robotXDistanceToPeg = 5.125 - (( Math.pow(robToLeftTape, 2) + Math.pow(10.25, 2) - Math.pow(robToRightTape, 2)) / (2 * 10.25));
				}
				
				//Equations, so many equations:
				//Robots distance to peg
				robotToPeg = 0.5*Math.sqrt(2*(Math.pow(robToRightTape, 2)+Math.pow(robToLeftTape, 2))-Math.pow(10.25,2));
				
				//Robots y distance to peg
				robotYDistanceToPeg = Math.sqrt(Math.pow(robotToPeg, 2) + Math.pow(robotXDistanceToPeg, 2));
				
				//Robot to end of peg
				robotToEndOfPeg = Math.sqrt(Math.pow(robotYDistanceToPeg-12, 2) + Math.pow(robotXDistanceToPeg, 2));
				
				//Peg to H distance in pixels
				distanceToTurn = ((Math.PI - Math.acos((Math.pow(robotToPeg, 2) - Math.pow(robotToEndOfPeg, 2) - 12)/(-2*robotToEndOfPeg)))/FOV)*IMAGE_HEIGHT;
				if(state == State.CENTER){
					//System.out.println("In the center state. Left is: " + left.x + " and right is: " + right.x + " right width is " + right.width + " center is: " + isInCenter);
					if (left.x > 320 - (right.x + right.width) && isInCenter == 0){
						leftMotor.set(TURN_SPEED);
						rightMotor.set(-TURN_SPEED);
						isInCenter = -1;
						
					}
					else if (isInCenter == 0){
						leftMotor.set(-TURN_SPEED);
						rightMotor.set(TURN_SPEED);
						isInCenter = 1;
						
					}
					
					if (left.x <= 320 - (right.x + right.width) && isInCenter == -1){
						state = State.STOP;
					}
					if (left.x >= 320 - (right.x + right.width) && isInCenter == 1){
						state = State.STOP;
					}
					
				}
				if(state == State.INIT){
					//during initialize state - set the left and right values originally, then start the next state
					state = State.TURN;
					rightMotor.set(0);
					leftMotor.set(0);

					startPositionLeft = left.x;
					startPositionRight = right.x;
				}
				if(state == State.TURN){
					//Turn state
					//System.out.println("In turn state");
					if(robToLeftTape > robToRightTape){
						//Turn right because we are on the right side of the tape
						leftMotor.set(TURN_SPEED);
						rightMotor.set(-TURN_SPEED);
						
						if(Math.abs(left.x - startPositionLeft) >= distanceToTurn){
							//If we've turned far enough, move to the next state
							state = State.STOP;
						}
					}
					else{
						//Turn left because we are on the left side of the tape
						leftMotor.set(-TURN_SPEED);
						rightMotor.set(TURN_SPEED);
						
						if(Math.abs(right.x - startPositionRight) == distanceToTurn){
							//If we've turned far enough, move to the next state
							state = State.STOP;
						}
					}
				}
				if(state == State.STOP){
					//During the stop state, stop all motors and then move to the next state
					leftMotor.set(0);
					rightMotor.set(0);
					//System.out.println("It has stopped");
					//Used for testing pruposed. Replace with MOVE_FORWARD to continue
					state = State.MOVE_FORWARD;
				}
				
				if(state == State.MOVE_FORWARD){
					//System.out.println("Moving forward");
					//Move forward state moves the robot forward
					
					if(left.width >= TARGET_SIZE || right.width >= TARGET_SIZE){
						//If the robot is close enough to the peg on either side, move on to the next state
						state = State.FINISH;
					}
					if (left.width < right.width){
						leftMotor.set(STRAIGHT_SPEED + .05);
						rightMotor.set(STRAIGHT_SPEED);
						//System.out.println("Left higher");
					}
					else if (left.width > right.width){
						leftMotor.set(STRAIGHT_SPEED);
						rightMotor.set(STRAIGHT_SPEED + .05);
						//System.out.println("right higher");
					}
					else {
						leftMotor.set(STRAIGHT_SPEED);
						rightMotor.set(STRAIGHT_SPEED);
						//System.out.println("neither higher");
					}
				}
				
				
				
				if(state == State.TURN_ON_PEG){
					if (left.x > 320 - right.x){
						leftMotor.set(TURN_SPEED);
						rightMotor.set(-TURN_SPEED);
						if (left.x <= 320 - right.x){
							state = State.FINISH;
						}
					}
					else{
						leftMotor.set(-TURN_SPEED);
						rightMotor.set(TURN_SPEED);
						if (left.x >= 320 - right.x){
							state = State.FINISH;
						}
					}
				}
				if (state == State.FINISH){
					leftMotor.set(0);
					rightMotor.set(0);
					//System.out.println("Before it is: " + Recorder.isRecordingPlaying);
					Recorder.initializePlay(Recorder.allFound.get(VISION_FILE_NAME));
					//System.out.println("After it is: " + Recorder.isRecordingPlaying);
					SmartDashboard.putBoolean(Recorder.DO_PLAY, true);
					Drive.isInUse = false;
					//System.out.println("We are now finished and would be playing the recording");
					state = State.DO_NOTHING;
				}
				if (state == State.DO_NOTHING){
					Drive.isInUse = false;
					isInCenter = 0;
				}
				
				
			}
			else{
				leftMotor.set(0);
				rightMotor.set(0);
				isInCenter = 0;
				//System.out.println("Couldn't find two targets");
			}
		
		});
	    vthread.start();	    
	}
	public void update(){
		if (!prevButton && Robot.stick.getRawButton(XboxMap.START)){
			if (state == State.DO_NOTHING){
				state = State.CENTER;
				Drive.isInUse = true;
			}
			else{
				state = State.DO_NOTHING;
				Drive.isInUse = false;
			}
		}
		prevButton = Robot.stick.getRawButton(XboxMap.START);
		SmartDashboard.putString("Vision State", state.name());

	}
	boolean prevRecorderState = false;
	public void autoUpdate(){
		if (prevRecorderState && !Recorder.isRecordingPlaying && state == State.WAIT){
			state = State.CENTER;
			Drive.isInUse = true;
		}
		prevRecorderState = Recorder.isRecordingPlaying;
	}
	public void disable(){
		state = State.DO_NOTHING;
		prevButton = false;
		isInCenter = 0;
		//vthread.stop();
	}
}
