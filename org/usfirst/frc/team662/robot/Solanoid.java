package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;

public class Solanoid implements Component {
	Solenoid arm = new Solenoid(1);
	Joystick pushstick1	= new Joystick(2);
	
	public void update() {
		arm.set(pushstick1.getRawButton(1));
	}
	
	public void autoUpdate() {
		
	}
}
