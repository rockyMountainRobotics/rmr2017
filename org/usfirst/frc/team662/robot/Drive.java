package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.lang.Math;
import edu.wpi.first.wpilibj.RobotDrive;

public class Drive{
		RobotDrive driver; 
		DualTalon left;
		DualTalon right;
		final static double LEFT_MULTIPLIER = 0.7;
		final static double RIGHT_MULTIPLIER = -0.7;
		final static double LEFT_DEADZONE = 0.15;
		final static double RIGHT_DEADZONE = 0.15;
		final static int FRONT_LEFT_MOTOR = 4;
		final static int REAR_LEFT_MOTOR = 6;
		final static int FRONT_RIGHT_MOTOR = 3;
		final static int REAR_RIGHT_MOTOR = 5;

		public Drive(){
			left = new DualTalon(FRONT_LEFT_MOTOR,REAR_LEFT_MOTOR);
			right = new DualTalon(FRONT_RIGHT_MOTOR,REAR_RIGHT_MOTOR);
			
			left.setMultiplier(LEFT_MULTIPLIER);
			right.setMultiplier(RIGHT_MULTIPLIER);
					
			//driver = new RobotDrive(left,right);
			
			
		}
		
		public void autoUpdate(){
			
		}
		
		public void update(){
			//experimental drive train code by James S.
	    	
	           
	       //set variables to defaults
	       double rightMotorPower = 0;
	       double leftMotorPower = 0;
	       double highInput = 1;
	       double leftInput = -Robot.stick.getRawAxis(XboxMap.LEFT_JOY_VERT);
	       double rightInput = Robot.stick.getRawAxis(XboxMap.RIGHT_JOY_HORIZ);
	       
	       if (rightInput < 0) {
	    	   rightInput *= -rightInput;
	       }
	       else {
	    	   rightInput *= rightInput;
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
	       left.set(leftMotorPower);
	       right.set(rightMotorPower);
		
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
		
	}
}
