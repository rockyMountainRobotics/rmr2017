package org.usfirst.frc.team662.robot;


import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;


public class ClimbingMotor implements Component {
	final static int PORT_CLIMING_MOTOR = 1, PORT_CONTROLLER = 1; //Another 0
	final static int Other_Motor = 2;
	DualTalon motor = new DualTalon(PORT_CLIMING_MOTOR, Other_Motor);
	Joystick Joystick1 = new Joystick(PORT_CONTROLLER);
	final static double DeadZone=.2;
	
	public void update()
	{
		//Declaring the num double and setting it to hold the right joystick horizontal value
		double num;
		num = Joystick1.getRawAxis(XboxMap.LEFT_JOY_HORIZ);
		
		//Checking if joystick is in the endzone
		if (!Recorder.isRecordingPlaying){
			if (num < DeadZone){
				num = 0;	
			}
			else{
				motor.set(num);
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
