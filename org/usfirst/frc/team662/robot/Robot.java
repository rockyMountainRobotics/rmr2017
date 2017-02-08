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

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 *
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 *
 * WARNING: While it may look like a good choice to use for your code if you're
 * inexperienced, don't. Unless you know what you are doing, complex code will
 * be much more difficult under this system. Use IterativeRobot or Command-Based
 * instead if you're new.
 */
@SuppressWarnings("unused")
public class Robot extends SampleRobot {
	
	
	public static Joystick stick;
	Compressor compress = new Compressor(0);
	ArrayList<Component> components;
	ArrayList<Component> disabled;
	
	public Robot() {
		stick = new Joystick(0);

		components = new ArrayList<Component>();
		//components.add(new GearHolder());
		components.add(new Drive());
		//components.add(new Recorder());
		
		//disable items
		disabled = new ArrayList<Component>();
		int counter = 0;
		for(Component i : components){
			SmartDashboard.putBoolean(i.getClass().getName(), true);
			counter++;
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
	
	//Air on 0 1 is auto
	public void autonomous() {
		while (isEnabled() && isAutonomous()) {
			for (int i = components.size() - 1; i >= 0; i--) {
				components.get(i).autoUpdate();
				if(!SmartDashboard.getBoolean(components.get(i).getClass().getName(), true)){
					disabled.add(components.get(i));
					components.get(i).disable();
					components.remove(i);
				}
			}for(int i = disabled.size(); i >= 0; i--){
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
	DigitalInput compressor = new DigitalInput(0);
	public void operatorControl() {
		while (isOperatorControl() && isEnabled()) {
			for (int i = components.size() - 1; i >= 0; i--) {
				components.get(i).update();
				if(!SmartDashboard.getBoolean(components.get(i).getClass().getName(), true)){
					disabled.add(components.get(i));
					components.get(i).disable();
					components.remove(i);
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
}
