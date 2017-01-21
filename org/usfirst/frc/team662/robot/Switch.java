package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Switch implements Component{
	
	DigitalInput limitSwitch;
	
	public Switch(){
		limitSwitch = new DigitalInput(9);
	}
	
	public void update(){
		SmartDashboard.putBoolean("Switch", limitSwitch.get());
	}
	public void autoUpdate(){
		
	}
}
