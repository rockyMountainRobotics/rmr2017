package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class motor implements Component{

	Joystick drivestick1= new Joystick(1);
	Talon DriveMotor1= new Talon(2);
	Talon DriveMotor2= new Talon(3);
	
	public void motoring(double speed, Talon DriveMotor1)
	{
		if(speed >= .2 || speed <= -.2)
		{
			DriveMotor1.setSpeed(speed);
		}
		
	}
	

	public void update()
	{
		motoring(drivestick1.getRawAxis(1), DriveMotor1);
		motoring(drivestick1.getRawAxis(2), DriveMotor2);
		
	
	}
	
	public void autoUpdate()
	{
		
		
		
	}
	
	
	
}
