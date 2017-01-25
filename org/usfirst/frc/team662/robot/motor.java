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
	
	static final int CANMOT = 5;
	static final int OTHERMOT = 3;
	static final double MODIFIER = .2;
	

	CANTalon DriveMotor1= new CANTalon(CANMOT);
	CANTalon DriveMotor2= new CANTalon(OTHERMOT);
	
	public motor(){
		Recorder.addRecordable(() -> DriveMotor2.get(), (speed) -> DriveMotor2.set((double)speed), OTHERMOT);
		Recorder.addRecordable(() -> DriveMotor1.get(), (speed) -> DriveMotor1.set((double)speed), CANMOT);
	}
	
	public void motoring(double speed, CANTalon DriveMotor1)
	{
		if((speed > .2 || speed < -.2) && !Recorder.hasLoaded)
		{
			DriveMotor1.set(speed*MODIFIER);
		}
		else if (!Recorder.hasLoaded){
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
