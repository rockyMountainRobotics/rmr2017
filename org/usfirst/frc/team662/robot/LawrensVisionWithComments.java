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
			
			Rect left = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
			Rect right = Imgproc.boundingRect(pipeline.filterContoursOutput().get(1));
			
			double leftHeight = left.y + left.height;
			double rightHeight= right.y + right.height;
			
			//Finding the distance between the robot from the left and right tape
			robToLeftTape = (realTapeHeight*imageHeight)/(leftHeight*sensorHeight);
			robToRightTape = (realTapeHeight*imageHeight)/(rightHeight*sensorHeight);
			
			//The distance between the edge of the tape is 10.25inches
			
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
			
			//turning to face point a
			while (distanceToTurn != Math.abs(right.x-left.x)){
				//Checking which side of the peg the robot is on
				if (robToLeftTape<robToRightTape){
					//Robot is on the left side of the peg
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(-0.5);
					rearRightMotor.set(-0.5);
				
				
				
				}
				if (robToLeftTape>robToRightTape){
					//Robot is on the right side of the peg
					frontLeftMotor.set(-0.5);
					rearLeftMotor.set(-0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
				
				
				
				}
			}
			frontLeftMotor.set(0);
			rearLeftMotor.set(0);
			frontRightMotor.set(0);
			rearRightMotor.set(0);
			
			
			if(robToLeftTape<robToRightTape){
				//Robot is on the right side of the peg
				while (right.height < 147.06){
					//The rectangle is smaller than we want
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
				}
				while(right.height != left.height)
				{
					//turn to the right to face the peg
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(-0.5);
					rearRightMotor.set(-0.5);
				}
			}
			
			
			
			
			if(robToLeftTape>robToRightTape){
				//Robot is on the left side of the peg
				while(left.height < 147.06){
					//The rectangle is smaller than we want
					frontLeftMotor.set(0.5);
					rearLeftMotor.set(0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
				}
				while(right.height != left.height)
				{
					//turn to the left to face the peg
					frontLeftMotor.set(-0.5);
					rearLeftMotor.set(-0.5);
					frontRightMotor.set(0.5);
					rearRightMotor.set(0.5);
				}
				Recorder.initializePlay(Recorder.allFound.get(VISION_FILE_NAME));
				SmartDashboard.putBoolean("playing", true);
				Drive.isInUse = false;
			}
		}
		
	});
		
		
}
	
	
	


