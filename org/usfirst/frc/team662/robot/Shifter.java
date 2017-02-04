package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Joystick;

public class Shifter implements Component{
	public static final int SOLENOID_PORT = 0;
	Solenoid solenoid = new Solenoid(SOLENOID_PORT);
	boolean one = true;
	boolean past = false;
	
	public void update(){
		one = Robot.stick.getRawButton(XboxMap.B);
		
		if(one == true && past == false){
			solenoid.set(!solenoid.get());
		}
		
		past = one;
	}

	@Override
	public void autoUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disable() {
		// TODO Auto-generated method stub
		
	}
	
}
