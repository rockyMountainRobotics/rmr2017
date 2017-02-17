package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedController;

public class BallGrabber implements Component{

	//FIND WHAT PORT WE ARE SUPPOSED TO USE!!!
	final static int MANIPULATOR_MOTOR_PORT = 4;
	final static int OPENER_PORT = 1;
	final static double MULTIPLIER = -1;


	final static double GRAB_DEADZONE = 0.1;
	CANTalon ballGrab = new CANTalon(MANIPULATOR_MOTOR_PORT);
	Solenoid ballPlace = new Solenoid(OPENER_PORT);

	
	@Override
	public void update(){
	
		//set the ball collector speed to the value of the right trigger
		if(Robot.manipulatorStick.getRawAxis(XboxMap.RIGHT_TRIGGER) > GRAB_DEADZONE){
			ballGrab.set(Robot.manipulatorStick.getRawAxis(XboxMap.RIGHT_TRIGGER) * MULTIPLIER);
		}
		else{
			ballGrab.set(0);
		}
		
		//if the b button is held, open the ball holder. Otherwise, close it
		if(Robot.manipulatorStick.getRawButton(XboxMap.B)){
			ballPlace.set(true);
		}
		else{
			ballPlace.set(false);
		}
	}
	@Override
	public void autoUpdate() {
		// TODO Auto-generated method stub
	}
	@Override
	public void disable() {
		ballGrab.set(0);
		ballPlace.set(false);
	}
}
