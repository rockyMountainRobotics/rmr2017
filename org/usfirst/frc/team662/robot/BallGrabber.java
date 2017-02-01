package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedController;

@SuppressWarnings("unused")
public class BallGrabber implements Component{

	final static int MANIPULATOR_MOTOR_PORT = 0;
	CANTalon ballGrab = new CANTalon(1);

	
	@Override
	public void update(){
		//Uses the right trigger
		Robot.stick.getRawButton(XboxMap.RIGHT_TRIGGER);
		//If the right analog button is pressed, the ball grabber motor moves at .25, obviously.
		if(Robot.stick.getRawButton(XboxMap.R_ANALOG) == true){
			ballGrab.set(.25);
		}
	}
	@Override
	public void autoUpdate() {
		// TODO Auto-generated method stub
	}
	@Override
	public void disable() {
		ballGrab.set(0);
	}
}
