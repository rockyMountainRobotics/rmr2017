package org.usfirst.frc.team662.robot;

import edu.wpi.first.wpilibj.*;
import com.ctre.CANTalon;

public class DualTalon implements SpeedController {
	
	CANTalon left, right;
	int leftPort;
	int rightPort;
	
	double currentSpeed;
	double leftSpeedMultiplier;
	double rightSpeedMultiplier;
	
	public DualTalon(int leftChannel, int rightChannel) {
		leftPort = leftChannel;
		rightPort = rightChannel;
		initDualTalon(new CANTalon(leftChannel), new CANTalon(rightChannel));
	}
	
	private void initDualTalon(CANTalon left, CANTalon right) {
		this.left = left;
		this.right = right;
		
		Recorder.addRecordable(() -> left.get(), (speed) -> left.set((double)speed), leftPort);
		Recorder.addRecordable(() -> right.get(), (speed) -> right.set((double)speed), rightPort);
		
		currentSpeed = 0;
		leftSpeedMultiplier = rightSpeedMultiplier = 1.0;
	}
	
	public void setMultiplier(double multiplier) {
		setMultiplier(true, multiplier);
		setMultiplier(false, multiplier);
	}
	
	public void setMultiplier(boolean left, double multiplier) {
		if (multiplier > 1.0) {
			multiplier = 1.0;
		}
		else if (multiplier < -1.0) {
			multiplier = -1.0;
		}
		
		if (left) {
			leftSpeedMultiplier = multiplier;
		}
		else {
			rightSpeedMultiplier = multiplier;
		}
	}
	
	public void invert() {
		invert(true);
		invert(false);
	}
	
	public void invert(boolean left) {
		if (left) {
			leftSpeedMultiplier = -leftSpeedMultiplier;
		}
		else {
			rightSpeedMultiplier = -rightSpeedMultiplier;
		}
	}

	@Override
	public void pidWrite(double output) {
	}

	@Override
	public double get() {
		return currentSpeed;
	}
	

	@Override
	public void set(double speed) {
		if (speed < -1) {
			speed = -1;
		}
		else if (speed > 1) {
			speed = 1;
		}
		
		currentSpeed = speed;
		
		left.set(leftSpeedMultiplier * currentSpeed);
		right.set(rightSpeedMultiplier * currentSpeed);
	}

	// Does nothing
	@Override
	public void setInverted(boolean isInverted) {
		
	}

	// Does nothing
	@Override
	public boolean getInverted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disable() {
		leftSpeedMultiplier = rightSpeedMultiplier = 0.0;
	}

	@Override
	public void stopMotor() {
		left.set(0);
		right.set(0);
		
	}

}