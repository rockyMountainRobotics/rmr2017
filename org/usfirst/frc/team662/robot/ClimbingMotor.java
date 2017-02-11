package org.usfirst.frc.team662.robot;


import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;


public class ClimbingMotor implements Component {
	final static int PORT_CLIMING_MOTOR = 1;
	final static int PORT_CONTROLLER = 1;
	final static int OTHER_MOTOR = 2;
	DualTalon motor = new DualTalon(PORT_CLIMING_MOTOR, OTHER_MOTOR);
	final static double DeadZone=.2;
	
	public void update()
	{
		
		//Declaring the num double and setting it to hold the right joystick horizontal value
		double speed;
		speed = Robot.manipulatorStick.getRawAxis(XboxMap.LEFT_JOY_HORIZ);
		
		//Checking if joystick is in the endzone
		if (!Recorder.isRecordingPlaying){
			if (Math.abs(speed) < DeadZone){
				speed = 0;	
			}
			motor.set(speed);
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
