package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.opencv.core.Rect;

import org.opencv.imgproc.Imgproc;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class LawrensVisionWithComments extends Component{
	//Variables correspond to the triangle screenshot titled VisionTriangle.png in ThisPC >> Pictures >> ScreenShot.
	
	public enum State {
		INIT, MOVE_FORWARD, TURN, STOP, END, TURN_ON_PEG, FINISH, DO_NOTHING, CENTER
	}
	State state = State.DO_NOTHING;
	
	boolean isFinished;
	boolean prevButton = false;
	double right1;
	double left1;
	double right2;
	double left2;
	final static String VISION_FILE_NAME = "vision";
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
	final static double TARGET_SIZE = 147.06;
	final static double TURN_SPEED = .1;
	final static double STRAIGHT_SPEED = .5;
	int startPositionLeft;
	int startPositionRight;
	DualTalon right = Drive.right;
	DualTalon left = Drive.left;
	
	CameraServer cameraServer = CameraServer.getInstance(); 
	UsbCamera camera = cameraServer.startAutomaticCapture();
	
	
	VisionThread visionThread = new VisionThread(camera, new GripPipeline(), pipeline -> {
		
		//Check to see if the contours are empty or not.
		if (!pipeline.filterContoursOutput().isEmpty()) 
		{
			
			//Create rectangles things from the vision
			Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
			Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
			
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
				if (left.x > 320 - right.x){
					left.set(TURN_SPEED);
					right.set(-TURN_SPEED);
					if (left.x <= 320 - right.x){
						state = State.INIT;
					}
				}
				else{
					left.set(-TURN_SPEED);
					right.set(TURN_SPEED);
					if (left.x >= 320 - right.x){
						state = State.INIT;
					}
				}
				
			}
			if(state == State.INIT){
				//during initialize state - set the left and right values originally, then start the next state
				state = State.TURN;
				startPositionLeft = left.x;
				startPositionRight = right.x;
			}
			if(state == State.TURN){
				//Turn state
				
				if(robToLeftTape > robToRightTape){
					//Turn right because we are on the right side of the tape
					left.set(TURN_SPEED);
					right.set(-TURN_SPEED);
					
					if(Math.abs(left.x - startPositionLeft) >= distanceToTurn){
						//If we've turned far enough, move to the next state
						state = State.STOP;
					}
				}
				else{
					//Turn left because we are on the left side of the tape
					left.set(-TURN_SPEED);
					right.set(TURN_SPEED);
					
					if(Math.abs(right.x - startPositionRight) == distanceToTurn){
						//If we've turned far enough, move to the next state
						state = State.STOP;
					}
				}
			}
			if(state == State.STOP){
				//During the stop state, stop all motors and then move to the next state
				left.set(0);
				right.set(0);
				//Used for testing pruposed. Replace with MOVE_FORWARD to continue
				state = State.DO_NOTHING;
			}
			
			if(state == State.MOVE_FORWARD){
				//Move forward state moves the robot forward
				left.set(STRAIGHT_SPEED);
				right.set(STRAIGHT_SPEED);
				if(left.y >= TARGET_SIZE || right.y >= TARGET_SIZE){
					//If the robot is close enough to the peg on either side, move on to the next state
					state = State.TURN_ON_PEG;
				}
			}
			
			
			
			if(state == State.TURN_ON_PEG){
				if (left.x > 320 - right.x){
					left.set(TURN_SPEED);
					right.set(-TURN_SPEED);
					if (left.x <= 320 - right.x){
						state = State.FINISH;
					}
				}
				else{
					left.set(-TURN_SPEED);
					right.set(TURN_SPEED);
					if (left.x >= 320 - right.x){
						state = State.FINISH;
					}
				}
			}
			if (state == State.FINISH){
				/*Recorder.initializePlay(Recorder.allFound.get(VISION_FILE_NAME));
				SmartDashboard.putBoolean("playing", true);
				Drive.isInUse = false;*/
				System.out.println("We are now finished and would be playing the recording");
				state = State.DO_NOTHING;
			}
			
			
		}
		
	});
	public void update(){
		if (!prevButton && Robot.stick.START){
			state = STATE.INIT;
		}
		prevButton = Robot.stick.START;
	}
	public void autoUpdate(){

	}
	public void disable(){
		state = STATE.DO_NOTHING;
		prevButton = false;
	}
}
