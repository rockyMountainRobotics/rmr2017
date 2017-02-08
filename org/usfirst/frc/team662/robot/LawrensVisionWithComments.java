package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.opencv.core.Rect;

import org.opencv.imgproc.Imgproc;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.vision.VisionThread;

public class LawrensVisionWithComments {
	//Variables correspond to the triangle screenshot titled VisionTriangle.png in ThisPC >> Pictures >> ScreenShot.
	
	public enum State {
		INIT, MOVE_FORWARD, TURN, STOP, END, TURN_ON_PEG
	}
	State state = State.INIT;
	
	boolean isFinished;
	double right1;
	double left1;
	double right2;
	double left2;
	final static String VISION_FILE_NAME = "vision";
	double robToLeftTape; //d
	double robotXDistanceToPeg; //g
	double robToRightTape; //e
	final double realTapeHeight = 5; //Height of tape in real life in inches
	final double sensorHeight = 0.68; //Height of the sensor above the ground
	final int imageHeight = 240; //Height of the image in pixels
	double robotYDistanceToPeg; //q
	double robotToPeg; //w
	double distanceToTurn; //How far the robot needs to turn
	double robotToEndOfPeg; //h
	final double FOV = Math.PI/3; //60 degree field of view in radians
	final double TARGET_SIZE = 147.06;
	final double TURN_SPEED = .1;
	final double STRAIGHT_SPEED = .5;
	int startPositionLeft;
	int startPositionRight;
	CANTalon frontRightMotor = new CANTalon(3);
	CANTalon frontLeftMotor = new CANTalon(4);
	CANTalon rearRightMotor = new CANTalon(5);
	CANTalon rearLeftMotor = new CANTalon(6);
	
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
			robToLeftTape = (realTapeHeight*imageHeight)/(leftHeight*sensorHeight);
			robToRightTape = (realTapeHeight*imageHeight)/(rightHeight*sensorHeight);
			
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
			distanceToTurn = ((Math.PI - Math.acos((Math.pow(robotToPeg, 2) - Math.pow(robotToEndOfPeg, 2) - 12)/(-2*robotToEndOfPeg)))/FOV)*imageHeight;
					
			if(state == State.INIT){
				state = State.TURN;
				startPositionLeft = left.x;
				startPositionRight = right.x;
			}
			if(state == State.TURN){
				
				
				if(robToLeftTape > robToRightTape){
					frontLeftMotor.set(TURN_SPEED);
					frontRightMotor.set(-TURN_SPEED);
					rearLeftMotor.set(TURN_SPEED);
					rearRightMotor.set(-TURN_SPEED);
					
					if(Math.abs(left.x - startPositionLeft) >= distanceToTurn){
						state = State.STOP;
					}
				}
				else{
					frontLeftMotor.set(-TURN_SPEED);
					frontRightMotor.set(TURN_SPEED);
					rearLeftMotor.set(-TURN_SPEED);
					rearRightMotor.set(TURN_SPEED);
					
					if(Math.abs(right.x - startPositionRight) == distanceToTurn){
						state = State.STOP;
					}
				}
			}
			if(state == State.STOP){
				frontLeftMotor.set(0);
				frontRightMotor.set(0);
				rearLeftMotor.set(0);
				rearRightMotor.set(0);
				if(left.y < TARGET_SIZE && right.y < TARGET_SIZE){
					state = State.MOVE_FORWARD;
				}
			}
			
			
			if(state == State.MOVE_FORWARD){
				frontLeftMotor.set(STRAIGHT_SPEED);
				frontRightMotor.set(STRAIGHT_SPEED);
				rearLeftMotor.set(STRAIGHT_SPEED);
				rearRightMotor.set(STRAIGHT_SPEED);
				if(left.y >= TARGET_SIZE || right.y >= TARGET_SIZE){
					state = State.TURN_ON_PEG;
				}
			}
			
			
			
			if(state == State.TURN_ON_PEG){
				if(left.y >= TARGET_SIZE){
					frontLeftMotor.set(-TURN_SPEED);
					frontRightMotor.set(TURN_SPEED);
					rearLeftMotor.set(-TURN_SPEED);
					rearRightMotor.set(TURN_SPEED);
				}else{
					frontLeftMotor.set(TURN_SPEED);
					frontRightMotor.set(-TURN_SPEED);
					rearLeftMotor.set(TURN_SPEED);
					rearRightMotor.set(-TURN_SPEED);
				}
			}
			//turning to face point a
			/*if(distanceToTurn != 0 && state == State.INIT)
				state = State.TURN;
			if(state == State.TURN){
				right1 = right.x;
				left1 = left.x;
				
				
				if(robToLeftTape < robToRightTape){
					//robot is on the left side
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(-0.5);
					rearRightMotor.set(-0.5);
					right2 = right.x;
					left2 = left.x;
				}
				
				if(robToLeftTape > robToRightTape){
					//robot is on the right side
					frontLeftMotor.set(-0.5);
					rearLeftMotor.set(-0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
					right2 = right.x;
					left2 = left.x;
				}
			}
			if(Math.abs(right2-right1) >= distanceToTurn && state == State.TURN){
				state = State.STOP;
			}*/
			
			
			
			
			
			/////////BLOCK OF CODE IS BEING RENOVATED ;) BAD!!!! @@@@@ at all of the things
			/*if (distanceToTurn != Math.abs(right.x-left.x) && state == State.INIT){
				state = State.TURN;
			}
			
			
			if(state == State.TURN){
				//Checking which side of the peg the robot is on
				Drive.isInUse = true;
				if (robToLeftTape < robToRightTape){
					//Robot is on the left side of the peg
					
					//Turn right
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(-0.5);
					rearRightMotor.set(-0.5);
				
				
				
				}
				if (robToLeftTape>robToRightTape){
					
					//Robot is on the right side of the peg; turn left
					frontLeftMotor.set(-0.5);
					rearLeftMotor.set(-0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
				
				
				}
			}
			
			
			if(distanceToTurn == Math.abs(right.x-left.x) && state == State.TURN){
				state = State.STOP;
				
			}*/
			
			//Stop the robot
			/*if(state == State.STOP){
				frontLeftMotor.set(0);
				rearLeftMotor.set(0);
				frontRightMotor.set(0);
				rearRightMotor.set(0);
	
				
			}	
			
			if((right.height< TARGET_SIZE && left.height < TARGET_SIZE) && state == State.STOP)
			{
				state = State.MOVE_FORWARD;
			}
			
			if(state == State.MOVE_FORWARD)
			{
				if(right.height < TARGET_SIZE && robToLeftTape > robToRightTape)
				{
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(0.5);
					rearLeftMotor.set(0.5);
				}
				if(left.height < TARGET_SIZE)
				{
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(0.5);
					rearLeftMotor.set(0.5);
				}
			}
			
			if((right.height >= TARGET_SIZE || left.height >= TARGET_SIZE))
			
			///////////CODE IS BEING RENOVATED- PROCEED WITH CAUTION
			if(robToLeftTape < robToRightTape){
				//Robot is on the right side of the peg
				if (right.height < 147.06 && state == State.STOP){
					state = State.MOVE_FORWARD;
					isFinished = false;
				}
				if (state == State.MOVE_FORWARD){
					//The rectangle is smaller than we want, so move closer so it gets bigger
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
					
				}
				
				if(right.height != left.height && state == State.MOVE_FORWARD){
					state = State.TURN;
					
				}
				
				
				if(state == State.TURN){
					//turn to the right to face the peg
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(-0.5);
					rearRightMotor.set(-0.5);
					
				}
			}
			
			
			
			
			if(robToLeftTape > robToRightTape){
				//Robot is on the left side of the peg
				if(left.height < 147.06 && state == State.STOP){
					state = State.MOVE_FORWARD;
				}
					if (state == State.MOVE_FORWARD){
						//The rectangle is smaller than we want, so move the robot closer
						frontLeftMotor.set(0.5);
						rearLeftMotor.set(0.5);
						frontRightMotor.set(0.5);
						rearRightMotor.set(0.5);
				}
				if(right.height != left.height && state == State.MOVE_FORWARD){
					state = State.TURN;
				}
				
				
					if (state == State.TURN){
						//turn to the left to face the peg
						frontLeftMotor.set(-0.5);
						rearLeftMotor.set(-0.5);
						frontRightMotor.set(0.5);
						rearRightMotor.set(0.5);
				}
				
			}*/
			//Connect it to the recorder
			Recorder.initializePlay(Recorder.allFound.get(VISION_FILE_NAME));
			SmartDashboard.putBoolean("playing", true);
			Drive.isInUse = false;
		}
		
	});
		
		
}
	
	
	


