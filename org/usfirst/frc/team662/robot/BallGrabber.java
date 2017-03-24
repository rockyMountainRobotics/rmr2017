package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SpeedController;

public class BallGrabber implements Component{

	//FIND WHAT PORT WE ARE SUPPOSED TO USE!!!
	final static int MANIPULATOR_MOTOR_PORT = 4;
	final static int OPENER_PORT = 1;
	final static double BALL_GRAB_SPEED = -.9;
	boolean isMoving = false;
	boolean wereMoving = false;
	final static int BALLSERVO = 90;
	final static int BALLSERVOMIN = 0;


	final static double GRAB_DEADZONE = 0.1;
	CANTalon ballGrab = new CANTalon(MANIPULATOR_MOTOR_PORT);
	Solenoid ballPlace = new Solenoid(OPENER_PORT);
	Servo ballPlaceTwo = new Servo(OPENER_PORT);
	
	@Override
	public void update(){
	
		//If the collector is not moving and the left trigger is pressed, turn it on
		if(Robot.manipulatorStick.getRawButton(XboxMap.LB)){
			if (!wereMoving){
				if (isMoving){
					ballGrab.set(0);
				}
				else{
					ballGrab.set(BALL_GRAB_SPEED);
				}
				wereMoving = true;
				isMoving = !isMoving;
			}
		}
		else {
			wereMoving = false;
		}
		
		//if the b button is held, open the ball holder. Otherwise, close it
		if(Robot.manipulatorStick.getRawButton(XboxMap.B)){
			ballPlace.set(true);
		}
		else{
			ballPlace.set(false);
		}
		
		//if the b button is held, open the ball holder. Otherwise, close it
		if(Robot.manipulatorStick.getRawButton(XboxMap.B)){
			ballPlaceTwo.setAngle(BALLSERVO);
		}
		else{
			ballPlaceTwo.setAngle(BALLSERVOMIN);
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
