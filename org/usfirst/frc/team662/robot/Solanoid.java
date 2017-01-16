package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;

public class Solanoid implements Component {
	
	static final int SOLE_PORT = 0;
	boolean active = false;
	
	DoubleSolenoid arm = new DoubleSolenoid(SOLE_PORT, 1);
	Compressor compress = new Compressor(1);
	
	public void update() {
		if (Robot.stick.getRawButton(XboxMap.LB)){
			arm.set(DoubleSolenoid.Value.kForward);
		}
	
		if (Robot.stick.getRawButton(XboxMap.RB)){
			arm.set(DoubleSolenoid.Value.kReverse);
		}
		
		if (Robot.stick.getRawButton(XboxMap.START)){
		}
	}
		
	
	public void autoUpdate() {
		
	}
}
