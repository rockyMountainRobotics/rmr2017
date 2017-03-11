package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.opencv.imgproc.Imgproc;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.cscore.UsbCamera;

public class CameraSwitch implements Component{
	final static int FRONT_CAMERA_PORT = 0;
	final static int TOP_CAMERA_PORT = 1;
	
	public static CameraServer camServer = CameraServer.getInstance();
	public static UsbCamera currentCamera;
	public final UsbCamera FRONT_CAMERA = camServer.startAutomaticCapture(FRONT_CAMERA_PORT);
	//public final UsbCamera TOP_CAMERA = camServer.startAutomaticCapture(TOP_CAMERA_PORT);
	
	
	public CameraSwitch() {
		currentCamera = FRONT_CAMERA;
		NetworkTable.getTable("").putString("Current Camera", currentCamera.getName());
	}
	
	boolean currentExposure = false; //False means manual. True is auto
	public void update() {
		try {
			if (!currentExposure){
				currentCamera.setExposureAuto();
			}
		}
		catch (Exception e){
			return;
		}
		currentExposure = true;
		/*
		if(Robot.stick.getRawButton(XboxMap.BACK)) {
			currentCamera = TOP_CAMERA;
			NetworkTable.getTable("").putString("Current Camera", currentCamera.getName());
		}
		if(Robot.stick.getRawButton(XboxMap.START)) {
			currentCamera = FRONT_CAMERA;
			NetworkTable.getTable("").putString("Current Camera", currentCamera.getName());
		}
		*/
	}

	public void autoUpdate() {
	    //currentCamera.setExposureManual(0);

	}
	public void disable() {
		
	}
	
}
