package org.usfirst.frc.team662.robot;


import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;


public class ClimbingMotor implements Component {
	final static int PORT_CLIMING_MOTOR = 5, PORT_CONTROLLER = 1;
	CANTalon motor = new CANTalon(PORT_CLIMING_MOTOR);
	Joystick Joystick1 = new Joystick(PORT_CONTROLLER);
	final static double DeadZone=.2;
	
	public void update()
	{
		//Declaring the num double and setting it to hold the right joystick horizontal value
		double num;
		num = Joystick1.getRawAxis(XboxMap.RIGHT_JOY_HORIZ);
		
		//Checking if joystick is in the endzone
		if (num < DeadZone){
			num = 0;	
		}
		else{
			motor.set(num);
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
