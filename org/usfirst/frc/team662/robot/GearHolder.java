package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class GearHolder implements Component {
	final static int LIMIT_SWITCH_CHANNEL_TOP = 2;
	final static int LIMIT_SWITCH_CHANNEL_BOTTOM = 3;
	final static int LIMIT_SWITCH_CHANNEL_MIDDLE = 4;
	final static int TOP = 1;
	final static int BOTTOM = -1;
	final static int MIDDLE = 0;
	final static int JOYmanipulatorStick_PORT_1 = 0;
	final static int MANIPULATOR_MOTOR_PORT_2 = 7;
	

	final static double SPEED = .5;
	String state;
	final static String KEY = "Holder state";
	
	final static double DEADZONE_1 = -.5;
	final static double DEADZONE_2 = .5;
	
	CANTalon manipulatorMotor = new CANTalon(MANIPULATOR_MOTOR_PORT_2);
	
	
	public DigitalInput limitSwitchTop = new DigitalInput(LIMIT_SWITCH_CHANNEL_TOP);
	public DigitalInput limitSwitchBottom = new DigitalInput(LIMIT_SWITCH_CHANNEL_BOTTOM);
	public DigitalInput limitSwitchMiddle = new DigitalInput(LIMIT_SWITCH_CHANNEL_MIDDLE);

	double currentLocation = TOP;
	
	boolean isTraveling = false;
	boolean isManualOverride = false;
	
	double speed = 0;
	
	public GearHolder(){
		
		Recorder.addRecordable(() -> manipulatorMotor.get(), (speed) -> moveMotor((double)speed), MANIPULATOR_MOTOR_PORT_2);
		//Recorder.addRecordable(() -> manipulatorMotor.get(), (speed) -> this.speed = (double)speed, MANIPULATOR_MOTOR_PORT_2);
	}
	
	public void update(){
		boolean topLimit = !limitSwitchTop.get();
		
		boolean bottomLimit = !limitSwitchBottom.get();
		
		boolean middleLimit = !limitSwitchMiddle.get();
		
		SmartDashboard.putBoolean("Top", topLimit);
		SmartDashboard.putBoolean("Middle", middleLimit);
		SmartDashboard.putBoolean("Bottom", bottomLimit);
		
		if(!Recorder.isRecordingPlaying){		
			
			
			//If you press the X button, it checks whether you were last at topLimit or bottomLimit, 
			// and sets the motor to get you from the top/bottom to the middle.
			if(Robot.manipulatorStick.getRawButton(XboxMap.X)  && !isTraveling)
			{
				
				if(currentLocation == TOP)
				{
					speed = -SPEED;
					isTraveling = true;
					currentLocation = MIDDLE;
				}
				if(currentLocation == BOTTOM)
				{
					speed = SPEED;
					isTraveling = true;
					currentLocation = MIDDLE;
				}
				
			}
			//If you press the Y button, sets the motor to go up, towards topLimit, as well as setting isTraveling to true,
			// so the robot can't change direction mid-movement.
			if(Robot.manipulatorStick.getRawButton(XboxMap.Y) && currentLocation != TOP && !isTraveling)
			{
				speed = SPEED;
				isTraveling = true;
				currentLocation = TOP;
			}
			//If you press the A button, sets the motor to go down, towards bottomLimit, as well as setting isTraveling to true,
			//so the robot can't change direction mid-movement.
			if(Robot.manipulatorStick.getRawButton(XboxMap.A) && currentLocation != BOTTOM && !isTraveling)
			{
				speed = -SPEED;
				isTraveling = true;
				currentLocation = BOTTOM;
			}
		}
		if(isTraveling)
		{
			if(topLimit && currentLocation == TOP)
			{
				speed = 0;
				isTraveling = false;
					
			}
			if(bottomLimit && currentLocation == BOTTOM)
			{
				speed = 0;
				isTraveling = false;
					
			}
			if(middleLimit && currentLocation == MIDDLE)
			{
				speed = 0;
				isTraveling = false;
			}
		}
		
		//Manual Override using the joystick.
		if(Robot.manipulatorStick.getRawButton(XboxMap.R_ANALOG))
		{
			isManualOverride = true;
			
			speed = -Robot.manipulatorStick.getRawAxis(XboxMap.RIGHT_JOY_VERT);

			if(Math.abs(speed) <= DEADZONE_2)
			{
				speed = 0;
			} 
			else
			{
				speed = speed * SPEED;
			}
		}
		else if (isManualOverride){
			speed = 0;
			isManualOverride = false;
		}
		
		//Sending what state the robot is in to the smart dash board
		
		//Checking state
		if (currentLocation == TOP){
			state = "Top";
		}
		else if (currentLocation == MIDDLE){
			state = "Middle";
		}
		else if(currentLocation == BOTTOM){
			state = "Bottom";
		}
		else if(currentLocation == BOTTOM && isTraveling == true){
			state = "Traveling to bottom";
		}
		else if(currentLocation == MIDDLE && isTraveling == true){
			state = "Traveling to middle";
		}
		else if(currentLocation == TOP && isTraveling == true){
			state = "Traveling to top";
		}
		
		//Sending to smart dash board
		SmartDashboard.putString( KEY, state);
		
		
		//This actually moves the motors now. Motor should only move if called from here.
		if (!Recorder.isRecordingPlaying){
			moveMotor(speed);
		}
}
	public void autoUpdate(){
		//Recorder will move the motor but only call it once instead of every time. This prevents safety stopping. Calling it here fixes it and will stop motors
		//Any time they reach the switches. 
		double speed = manipulatorMotor.get();
		if(!limitSwitchTop.get() && speed > 0){
			manipulatorMotor.set(0);
			currentLocation = TOP;
			System.out.println("Emergency stopped the motor for going too high");
		}
		else if(!limitSwitchBottom.get() && speed < 0){
			manipulatorMotor.set(0);
			currentLocation = BOTTOM;
			System.out.println("Emergency stopped the motor for going too low");
		}
		
	}
	
	@Override
	public void disable() {
		manipulatorMotor.set(0);
		speed = 0;
		currentLocation = BOTTOM;
		isTraveling = false;
	}
	
	public void moveMotor(double speed) {
		//System.out.println("The current speed is: " + speed);
		if(!limitSwitchTop.get() && speed > 0){
			speed = 0;
			currentLocation = TOP;
			System.out.println("Emergency stopped the motor for going too high");
		}
		else if(!limitSwitchBottom.get() && speed < 0){
			speed = 0;
			currentLocation = BOTTOM;
			System.out.println("Emergency stopped the motor for going too low");
		}
		manipulatorMotor.set(speed);
	}
	
}
