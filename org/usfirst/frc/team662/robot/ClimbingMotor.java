package org.usfirst.frc.team662.robot;


import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;


public class ClimbingMotor implements Component {
	final static int PORT_CLIMING_MOTOR = 1;
	final static int PORT_CONTROLLER = 1;
	final static int OTHER_MOTOR = 0;
	final static double MULTIPLIER = .6;
	DualTalon motor = new DualTalon(PORT_CLIMING_MOTOR, OTHER_MOTOR);
	//CANTalon motor = new CANTalon(PORT_CLIMING_MOTOR, OTHER_MOTOR);

	final static double DeadZone = .2;
	
	
	public void update()
	{
		/*
		//Declaring the num double and setting it to hold the left trigger value
		double speed;
		speed = -Robot.manipulatorStick.getRawAxis(XboxMap.LEFT_TRIGGER);
		
		//Checking if joystick is in the endzone
		if (!Recorder.isRecordingPlaying){
			if (Math.abs(speed) < DeadZone){
				speed = 0;	
			}
			motor.set(speed * MULTIPLIER);
		}*/
		boolean isLeftDown = true;
		boolean isRightDown = true;
		if (!Recorder.isRecordingPlaying){
			//Decide which to move
			double leftSpeed = -Robot.manipulatorStick.getRawAxis(XboxMap.LEFT_TRIGGER);
			if (Math.abs(leftSpeed) < DeadZone){
				isLeftDown = false;
			}
			double rightSpeed = -Robot.manipulatorStick.getRawAxis(XboxMap.RIGHT_TRIGGER);
			if (Math.abs(rightSpeed) < DeadZone){
				isRightDown = false;
			}
			//Actually move motors
			if (!isRightDown && !isLeftDown){
				motor.set(0);
			}
			else if (isRightDown){
				motor.set(rightSpeed);
			}
			else if (isLeftDown){
				motor.set(leftSpeed * MULTIPLIER);
			}
		}
		
	}

	@Override
	public void autoUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disable() {
		motor.set(0);
	}
	
}
