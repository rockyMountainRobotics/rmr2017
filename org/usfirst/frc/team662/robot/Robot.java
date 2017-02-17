package org.usfirst.frc.team662.robot;

import java.io.File;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends SampleRobot {
	
	
	public static Joystick stick;
	public static Joystick manipulatorStick;

	final static int AUTONOMOUS_PORT = 1;
	DigitalInput autonomousSwtich = new DigitalInput(AUTONOMOUS_PORT);
	ArrayList<Component> components;
	ArrayList<Component> disabled;
	
	public Robot() {
		stick = new Joystick(0);
		manipulatorStick = new Joystick(1);
		components = new ArrayList<Component>();
		
		components.add(new GearHolder());
		components.add(new Drive());
		components.add(new Shifter());
		components.add(new CompressorSwitch());

		components.add(new ClimbingMotor());
		components.add(new BallGrabber());

		components.add(new Vision());
		components.add(new Recorder());
		
		//disabled items list. Stored separately from enabled stuff
		disabled = new ArrayList<Component>();
		for(Component i : components){
			SmartDashboard.putBoolean(i.getClass().getName(), true);
		}
		
		
	}

	public void robotInit() {
		
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * if-else structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	
	public void autonomous() {
		while (isEnabled() && isAutonomous() && autonomousSwtich.get()) {
			for (int i = components.size() - 1; i >= 0; i--) {
				
				//Disabling components from SmartDashboard. True if disabled.
				if(!SmartDashboard.getBoolean(components.get(i).getClass().getName(), true)){
					disabled.add(components.get(i));
					components.get(i).disable();
					components.remove(i);
				}
				else{
					components.get(i).autoUpdate();
				}
			}
			for(int i = disabled.size() - 1; i >= 0; i--){
				if(SmartDashboard.getBoolean(disabled.get(i).getClass().getName(), true)){
					components.add(disabled.get(i));
					disabled.remove(i);
				}
			}
		}
	}
	/**
	 * Runs the motors with arcade steering.
	 */
	public void operatorControl() {
		while (isOperatorControl() && isEnabled()) {
			for (int i = components.size() - 1; i >= 0; i--) {
				
				//Disabling components from SmartDashboard. True if disabled.
				if(!SmartDashboard.getBoolean(components.get(i).getClass().getName(), true)){
					disabled.add(components.get(i));
					components.get(i).disable();
					components.remove(i);
				}
				else{
					components.get(i).update();

				}
			}
			for(int i = disabled.size() - 1; i >= 0; i--){
				if(SmartDashboard.getBoolean(disabled.get(i).getClass().getName(), true)){
					components.add(disabled.get(i));
					disabled.remove(i);
				}
			}
			
		}
	}

	/**
	 * Runs during test mode
	 */
	public void test() {
	}
	
	@Override
	public void disabled(){
		components.forEach((x) -> x.disable());
	}
}
