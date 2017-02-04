package org.usfirst.frc.team662.robot;
import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;

public class GearHolder implements Component {
	final static int LIMIT_SWITCH_CHANNEL_1 = 0;
	final static int LIMIT_SWITCH_CHANNEL_2 = 0;
	final static int JOYSTICK_PORT_1 = 0;
	final static int MANIPULATOR_MOTOR_PORT_2 = 7;
	
	final static double DEADZONE_1 = -.5;
	final static double DEADZONE_2 = .5;
	
	CANTalon manipulatorMotor = new CANTalon(MANIPULATOR_MOTOR_PORT_2);
	
	public Joystick ManipulatorStick = new Joystick(JOYSTICK_PORT_1 );
	
	public DigitalInput limitSwitchTop = new DigitalInput(LIMIT_SWITCH_CHANNEL_1);
	public DigitalInput limitSwitchBottom = new DigitalInput(LIMIT_SWITCH_CHANNEL_2);

	double liftSpeed = 0;
	
	public GearHolder(){
		
		Recorder.addRecordable(() -> manipulatorMotor.get(), (speed) -> check((double)speed), MANIPULATOR_MOTOR_PORT_2);
		
	}
	
	public void update(){
		boolean topLimit= true;
		topLimit = limitSwitchTop.get();
		
		boolean bottomLimit = true;
		bottomLimit = limitSwitchBottom.get();
		
		liftSpeed = ManipulatorStick.getRawAxis(XboxMap.RIGHT_JOY_HORIZ);
		if(Recorder.hasLoaded){
			if (liftSpeed < DEADZONE_1|| liftSpeed > DEADZONE_2){
				manipulatorMotor.set(liftSpeed);
			}
			
			if (!topLimit && liftSpeed > 0){
				manipulatorMotor.set(0);
			}
			
			if (!bottomLimit && liftSpeed < 0){
				manipulatorMotor.set(0);
			}
		}
	}
	public void autoUpdate(){
	//We wanted to add a rumble, but were too lazy.
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
		manipulatorMotor.set(speed);
	}
	
}
