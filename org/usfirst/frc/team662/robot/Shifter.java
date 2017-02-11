package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Joystick;

public class Shifter implements Component{
	public static final int SOLENOID_PORT = 0;
	Solenoid solenoid = new Solenoid(SOLENOID_PORT);
	boolean current = true;
	boolean past = false;
	
	public Shifter(){
		
		Recorder.addRecordable(() -> solenoid.get(), (extended) -> solenoid.set((boolean)extended), SOLENOID_PORT);
		
	}
	
	public void update(){
		current = Robot.stick.getRawButton(XboxMap.B);
		
		if(current == true && past == false && !Recorder.isRecordingPlaying){
			solenoid.set(!solenoid.get());
		}
		
		past = current;
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
