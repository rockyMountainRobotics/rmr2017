package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class motor implements Component{
	
	static final int CANMOT = 0;
	static final int OTHERMOT = 1;
	

	public  CANTalon DriveMotor1= new CANTalon(CANMOT);
	public CANTalon DriveMotor2= new CANTalon(OTHERMOT);
	
	DigitalInput limitSwitch;
	
	public motor(){
		limitSwitch = new DigitalInput(9);
	}
	public void motoring(double speed, CANTalon DriveMotor1)
	{
		
		if((speed >= .2 || speed <= -.2) && limitSwitch.get())
		{
			DriveMotor1.set(speed);
		}
		else
		{
			System.out.println("Stopped MotorThingy");
			DriveMotor1.set(0);
		}
		
	}
	

	public void update()
	{
		Robot.stick.getRawAxis(XboxMap.RIGHT_JOY_VERT);
		motoring(Robot.stick.getRawAxis(XboxMap.RIGHT_JOY_VERT), DriveMotor1);
		motoring(Robot.stick.getRawAxis(XboxMap.LEFT_JOY_VERT), DriveMotor2);
	}
	
	public void autoUpdate()
	{
		
		
		
	}
	
	
	
}
