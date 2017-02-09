package org.usfirst.frc.team662.robot;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Compressor;

public class CompressorSwitch implements Component{

	final static int DIGITAL_INPUT_PORT = 0;
	DigitalInput input = new DigitalInput(DIGITAL_INPUT_PORT);
	Compressor compressor = new Compressor();
	
	public void update(){
		if(input.get()){
			compressor.start();
		}
		else{
			compressor.stop();
		}
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
