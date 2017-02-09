package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;

public class GearHolder implements Component {
	final static int LIMIT_SWITCH_CHANNEL_TOP = 2;
	final static int LIMIT_SWITCH_CHANNEL_BOTTOM = 3;
	final static int LIMIT_SWITCH_CHANNEL_MIDDLE = 4;
	final static int TOP = 1;
	final static int BOTTOM = -1;
	final static int MIDDLE = 0;
	final static int JOYmanipulatorStick_PORT_1 = 0;
	final static int MANIPULATOR_MOTOR_PORT_2 = 7;
	
	final static double DEADZONE_1 = -.5;
	final static double DEADZONE_2 = .5;
	
	CANTalon manipulatorMotor = new CANTalon(MANIPULATOR_MOTOR_PORT_2);
	
	
	public DigitalInput limitSwitchTop = new DigitalInput(LIMIT_SWITCH_CHANNEL_TOP);
	public DigitalInput limitSwitchBottom = new DigitalInput(LIMIT_SWITCH_CHANNEL_BOTTOM);
	public DigitalInput limitSwitchMiddle = new DigitalInput(LIMIT_SWITCH_CHANNEL_MIDDLE);

	double liftSpeed = 0;
	double currentLocation = BOTTOM;
	
	boolean isTraveling = false;
	
	public GearHolder(){
		
		Recorder.addRecordable(() -> manipulatorMotor.get(), (speed) -> check((double)speed), MANIPULATOR_MOTOR_PORT_2);
		
	}
	
	public void update(){
		boolean topLimit= true;
		topLimit = !limitSwitchTop.get();
		
		boolean bottomLimit = true;
		bottomLimit = !limitSwitchBottom.get();
		
		boolean middleLimit = true;
		middleLimit = !limitSwitchMiddle.get();
		
		
		//System.out.println(topLimit + " " + bottomLimit + " " + middleLimit);

		
		if(!Recorder.isRecordingPlaying){		
			
			//Prevents the robot from changing where it is moving to if it is currently moving.
			if(!isTraveling)
			{
				//If you press the X button, it checks whether you were last at topLimit or bottomLimit, and sets the motor to get you from the top/bottom to the middle.
				if(Robot.manipulatorStick.getRawButton(XboxMap.X))
				{
					
					if(currentLocation == TOP)
					{
						manipulatorMotor.set(-0.1);
						isTraveling = true;
						currentLocation = MIDDLE;
					}
					if(currentLocation == BOTTOM)
					{
						manipulatorMotor.set(0.1);
						isTraveling = true;
						currentLocation = MIDDLE;
					}
					
				}
				//If you press the Y button, sets the motor to go up, towards topLimit, as well as setting isTraveling to true, so the robot can't change direction mid-movement.
				if(Robot.manipulatorStick.getRawButton(XboxMap.Y) && currentLocation != TOP)
				{
					manipulatorMotor.set(0.1);
					isTraveling = true;
					currentLocation = TOP;
				}
				//If you press the A button, sets the motor to go down, towards bottomLimit, as well as setting isTraveling to true, so the robot can't change direction mid-movement.
				if(Robot.manipulatorStick.getRawButton(XboxMap.A) && currentLocation != BOTTOM)
				{
					manipulatorMotor.set(-0.1);
					isTraveling = true;
					currentLocation = BOTTOM;
				}
			}
		}
		if(isTraveling)
		{
			if(topLimit && currentLocation == TOP)
			{
				System.out.println("Hit the top");
				manipulatorMotor.set(0);
				isTraveling = false;
					
			}
			if(bottomLimit && currentLocation == BOTTOM)
			{
				manipulatorMotor.set(0);
				isTraveling = false;
					
			}
			if(middleLimit && currentLocation == MIDDLE)
			{
				manipulatorMotor.set(0);
				isTraveling = false;
			}
		}
		//Use right joymanipulatorStick to do the thingy do
		
		double speed = Robot.manipulatorStick.getRawAxis(XboxMap.RIGHT_JOY_VERT);
		if(Robot.manipulatorStick.getRawButton(XboxMap.R_ANALOG))
		{
			if(Math.abs(speed) <= DEADZONE_2 || topLimit || bottomLimit)
			{
				manipulatorMotor.set(0);
			} 
			else
			{
				manipulatorMotor.set(speed * .1);
				
			}
		}
}//robot.manipulatorStick.getButton(xboxmap.X)
	public void autoUpdate(){
	//We wanted to add a rumble, but were too lazy. Also too lazy to add the ' in were
	}
	@Override
	public void disable() {
		manipulatorMotor.set(0);
		liftSpeed = 0;
		currentLocation = BOTTOM;
		isTraveling = false;
	}
	public void check(double speed) {
		
		if(!limitSwitchTop.get() && liftSpeed > 0){
			speed = 0;
		}
		if(!limitSwitchBottom.get() && liftSpeed < 0){
			speed = 0;		
		}
		manipulatorMotor.set(speed);
	}
	
}
