package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;

public class Drive implements Component{
	Timer autoTimer = new Timer();
	RobotDrive driver; 
	public static DualTalon left;
	public static DualTalon right;
	public static boolean isInUse = false;
	final static double LEFT_MULTIPLIER = 1;
	final static double RIGHT_MULTIPLIER = 1;
	final static double LEFT_DEADZONE = 0.15;
	final static double RIGHT_DEADZONE = 0.15;
	final static int FRONT_RIGHT_MOTOR = 5;
	final static int REAR_RIGHT_MOTOR = 3;
	final static int FRONT_LEFT_MOTOR = 6;
	final static int REAR_LEFT_MOTOR = 8;
	final static double AUTO_SPEED = -.375;
	final static double AUTO_FORWARD = 3;
	boolean reverseDrive;
	boolean toggleHeld;
	final static String KEY = "Toggle Held";

	public Drive(){
		
		left = new DualTalon(FRONT_LEFT_MOTOR,REAR_LEFT_MOTOR);
		right = new DualTalon(FRONT_RIGHT_MOTOR,REAR_RIGHT_MOTOR);
		
		reverseDrive = false;
		toggleHeld = false;
		
		left.setMultiplier(LEFT_MULTIPLIER);
		right.setMultiplier(RIGHT_MULTIPLIER);
		
		//driver = new RobotDrive(left,right);
		
		
		
		
				
	}
	public void autoUpdate(){
		//Current auto
		if (autoTimer.get() == 0){
			autoTimer.start();
		}
		if (autoTimer.get() <= AUTO_FORWARD){
			left.set(AUTO_SPEED);
			right.set(AUTO_SPEED);
		}
		else{
			left.set(0);
			right.set(0);
		}
	}
	
	public void update(){
		//drive train code by James S.
    	
           
       //set variables to defaults
       double rightMotorPower = 0;
       double leftMotorPower = 0;
       double highInput = 1;
       double leftInput = Robot.stick.getRawAxis(XboxMap.LEFT_JOY_VERT);
       double rightInput = Robot.stick.getRawAxis(XboxMap.RIGHT_JOY_HORIZ);
       
       if (rightInput < 0) {
    	   rightInput *= -rightInput;
       }
       else {
    	   rightInput *= rightInput;
       }
       
       //check if the reverse drive mode should be toggled
       if(Robot.stick.getRawButton(XboxMap.A)){
    	   if(!toggleHeld){
    		   reverseDrive = !reverseDrive;
    		   toggleHeld = true;
    	   }
       }
       else{
    	   toggleHeld = false;
       }
              //Checking and sending the toggle held state
       if (reverseDrive){
    	   SmartDashboard.putString( KEY, "Toggle Held is true");
       }
       else{
    	   SmartDashboard.putString(KEY, "Toggle Held is false");
       }
    	   
       
       
       //set motor power variables to left stick value
       if(Math.abs(leftInput) > LEFT_DEADZONE){
    	   leftMotorPower = leftInput;
    	   rightMotorPower = leftInput;
       }
       
       //modify motor power variables by right stick values
       if(Math.abs(rightInput) > RIGHT_DEADZONE){
    	   leftMotorPower = leftMotorPower + rightInput;
    	   rightMotorPower = rightMotorPower - rightInput;
       }
       
       //determine the highest input received
       highInput = findHighInput(leftInput, rightInput);
       
       //limit left motor power to highest input received
       leftMotorPower = limitMotor(leftMotorPower, highInput);
       
       //limit right motor power to highest input received
       rightMotorPower = limitMotor(rightMotorPower, highInput);
       
       //set the motors to power variables
       if(!Recorder.isRecordingPlaying && !reverseDrive && !isInUse){
    	   left.set(leftMotorPower);
    	   right.set(rightMotorPower);
       }
       
       //if reverse drive is enabled, set the motors to the negative value of the opposite side
       if(!Recorder.isRecordingPlaying && reverseDrive && !isInUse){
    	   left.set(-rightMotorPower);
    	   right.set(-leftMotorPower);
       }
              
	}
	
	//determines which of two inputs is greater, and returns it
	public double findHighInput(double input1, double input2){
		double resultInput = 0;
        if(Math.abs(input1) >= Math.abs(input2)){
    	    resultInput = input1;
        }else{
    	    resultInput = input2;
        }
		return resultInput;
	}
	
	//limits a motor power value to a limiting value
	public double limitMotor(double inputPower, double limiter){
		double outputPower = inputPower;
		if(Math.abs(inputPower) > Math.abs(limiter)){
	    	   if(inputPower < 0){
	    		   outputPower = -Math.abs(limiter);
	    	   }else{
	    		   outputPower = Math.abs(limiter);
	    	   }
	       }
		return outputPower;
	}

	@Override
	public void disable() {
		left.set(0);
		right.set(0);
		isInUse = false;
		autoTimer.stop();
		autoTimer.reset();
	}
	
}
