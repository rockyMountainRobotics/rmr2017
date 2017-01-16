package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class Gyroscope implements Component {

	
	AnalogGyro gyro1 = new AnalogGyro(0);
	
	
	public Gyroscope(){
		
		//gyro1.initGyro();
		//gyro1.calibrate();
		//gyro1.setSensitivity(1);
		//gyro1.reset();
		
	}
	
	public void update(){
		
		double RobotAngle = gyro1.getAngle();
		double RobotRate = gyro1.getRate();
		
		SmartDashboard.putNumber("Angle", RobotAngle);
		SmartDashboard.putNumber("Rate", RobotRate);
	}
	
	public void autoUpdate(){
		
	}
	
	
}
