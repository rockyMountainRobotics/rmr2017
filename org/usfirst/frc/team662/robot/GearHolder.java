package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;

public class GearHolder implements Component {
	final static int LIMIT_SWITCH_CHANNEL_1 = 0;
	final static int LIMIT_SWITCH_CHANNEL_2 = 0;
	final static int LIMIT_SWITCH_CHANNEL_3 = 0;
	final static int TOP = 1;
	final static int BOTTOM = -1;
	final static int MIDDLE = 0;
	final static int JOYSTICK_PORT_1 = 0;
	final static int MANIPULATOR_MOTOR_PORT_2 = 7;
	
	final static double DEADZONE_1 = -.5;
	final static double DEADZONE_2 = .5;
	
	CANTalon manipulatorMotor = new CANTalon(MANIPULATOR_MOTOR_PORT_2);
	
	public Joystick ManipulatorStick = new Joystick(JOYSTICK_PORT_1 );
	
	public DigitalInput limitSwitchTop = new DigitalInput(LIMIT_SWITCH_CHANNEL_1);
	public DigitalInput limitSwitchBottom = new DigitalInput(LIMIT_SWITCH_CHANNEL_2);
	public DigitalInput limitSwitchMiddle = new DigitalInput(LIMIT_SWITCH_CHANNEL_3);

	double liftSpeed = 0;
	double currentLocation = 0;
	
	boolean isTraveling = false;
	
	public GearHolder(){
		
		Recorder.addRecordable(() -> manipulatorMotor.get(), (speed) -> check((double)speed), MANIPULATOR_MOTOR_PORT_2);
		
	}
	
	public void update(){
		boolean topLimit= true;
		topLimit = limitSwitchTop.get();
		
		boolean bottomLimit = true;
		bottomLimit = limitSwitchBottom.get();
		
		boolean middleLimit = true;
		middleLimit = limitSwitchMiddle.get();
		
		
		
		
		if(!Recorder.isRecordingPlaying){		
			
			//Prevents the robot from changing where it is moving to if it is currently moving.
			if(!isTraveling)
			{
				//If you press the X button, it checks whether you were last at topLimit or bottomLimit, and sets the motor to get you from the top/bottom to the middle.
				if(Robot.stick.getRawButton(XboxMap.X))
				{
					
					if(topLimit)
					{
						manipulatorMotor.set(-0.11);
						isTraveling = true;
						currentLocation = MIDDLE;
					}
					if(bottomLimit)
					{
						manipulatorMotor.set(0.11);
						isTraveling = true;
						currentLocation = MIDDLE;
					}
					
				}
				//If you press the Y button, sets the motor to go up, towards topLimit, as well as setting isTraveling to true, so the robot can't change direction mid-movement.
				if(Robot.stick.getRawButton(XboxMap.Y))
				{
			
					manipulatorMotor.set(0.11);
					isTraveling = true;
					currentLocation = TOP;
				}
				//If you press the A button, sets the motor to go down, towards bottomLimit, as well as setting isTraveling to true, so the robot can't change direction mid-movement.
				if(Robot.stick.getRawButton(XboxMap.A))
				{
					manipulatorMotor.set(-0.11);
					isTraveling = true;
					currentLocation = BOTTOM;
				}
			}
		}
		if(isTraveling)
		{
			if(topLimit && currentLocation == TOP)
			{
				
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
		
		//Use right joystick to do the thingy do
		
		double speed = Robot.stick.getRawAxis(XboxMap.RIGHT_JOY_VERT);
		if(Robot.stick.getRawButton(XboxMap.R_ANALOG))
		{
			if(speed <= DEADZONE_1 || speed >= DEADZONE_2)
			{
				manipulatorMotor.set(0);
			} 
			else
			{
				manipulatorMotor.set(speed);
				
			}
		}
}//robot.stick.getButton(xboxmap.X)
	public void autoUpdate(){
	//We wanted to add a rumble, but were too lazy. Also too lazy to add the ' in were
	}
	@Override
	public void disable() {
		manipulatorMotor.set(0);
	}
	public void check(double speed) {
		
		if(!limitSwitchTop.get() && liftSpeed > 0){
			speed = 0;
		}
		if(!limitSwitchBottom.get() && liftSpeed < 0){
			speed = 0;		
		}
		if(!limitSwitchMiddle.get() && liftSpeed < 0 || liftSpeed > 0){
			speed = 0;
		}
		manipulatorMotor.set(speed);
	}
	
}
