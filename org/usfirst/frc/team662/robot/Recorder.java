package org.usfirst.frc.team662.robot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Recorder {

	Timer GlobalTime = new Timer();
	Timer playingTimer = new Timer();
	static final String DIRECTORY = "/home/luser/Records";

	static class Hardware {
		public Supplier getter;
		public Consumer setter;
		public int port;

		public Hardware(Supplier incGetter, Consumer incSetter, int incPort) {
			getter = incGetter;
			setter = incSetter;
			port = incPort;
		}
	}

	public static class Timings implements Serializable {
		public int port;
		public int index = 0;
		public static ArrayList<Double> times;

		public static ArrayList value;

		public Timings(int incPort) {
			port = incPort;

		}
	}

	static ArrayList<Hardware> pieces;
	static ArrayList<Timings> timers;

	//must be last to be constructed in the main robot code
	public Recorder() {
		SmartDashboard.putBoolean("record", false);
		SmartDashboard.putString("input", "defaultAuto");
		SmartDashboard.putBoolean("play recording", false);

		SendableChooser<File> autoChooser = new SendableChooser<File>();
		File records = new File(DIRECTORY);
		File[] foundRecords = records.listFiles();
		if (foundRecords.length != 0) {
			for (int i = 0; i < foundRecords.length; i++) {
				autoChooser.addObject(foundRecords[i].getName(), foundRecords[i]);
			}
			SmartDashboard.putData("Autonomous choices", autoChooser);
		}
		pieces.sort((a, b) -> a.port - b.port);
	}

	public static void addRecordable(Supplier newGet, Consumer newSet, int port) {
		//add a new hardware class to pieces and set up the timer
		pieces.add(new Hardware(newGet, newSet, port));
		timers.add(new Timings(port));
	}

	public void update() {
		//every piece should be ready
		double lastTime = -1;
		if (GlobalTime.get() - lastTime > .1) {
			lastTime = GlobalTime.get();
			boolean hasFinished = false;
			if (SmartDashboard.getBoolean("record", false)) {
				if (!hasFinished) {
					GlobalTime.start();
				}
				hasFinished = true;
				for (int i = 0; i <= pieces.size(); i++) {
					if (pieces.get(i).getter.get() == timers.get(i).value.get(i)) {
						timers.get(i).times.add(GlobalTime.get());
						timers.get(i).value.add((Double) pieces.get(i).getter.get());
					}
				}
			} else if (hasFinished) {
				GlobalTime.stop();
				GlobalTime.reset();
				saveRecording();
				hasFinished = false;
			}
		}

		//play Recording
		boolean hasLoaded = false;

		if (SmartDashboard.getBoolean("play recording", false)) {
			if (!hasLoaded) {
				timers = loadSavedRecording();
				if (pieces.size() == timers.size()) {
					for (int i = 0; i < pieces.size(); i++) {
						if (pieces.get(i).port != timers.get(i).port) {
							System.out.println("ERROR!!");
						}
					}
					hasLoaded = true;
					playingTimer.stop();
					playingTimer.reset();
					playingTimer.start();
				} else {
					System.out.println("ERROR!!");
				}
			}

		}
	}

	ArrayList loadSavedRecording() {
		//if a recording id found, load it
		ArrayList<Timings> deSerialized = new ArrayList<Timings>();

		try {
			FileInputStream fileIn = new FileInputStream((File) SmartDashboard.getData("Autonomous Choices"));
			ObjectInputStream in = new ObjectInputStream(fileIn);
			deSerialized = (ArrayList<Timings>) in.readObject();
		} catch (IOException i) {
			System.out.println(i);
		} catch (ClassNotFoundException c) {
			System.out.println(c);
		}
		deSerialized.sort((a, b) -> a.port - b.port);
		return deSerialized;
	}

	void saveRecording() {
		//serialize
		String input = SmartDashboard.getString("input", "defaultAuto");

		if (input.equals("")) {
			input = "defaultAuto";
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(DIRECTORY + input + ".ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(timers);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	public static void Play(ArrayList<Timings> input) {
		for (int i = 0; i < input.size(); i++) {
			pieces.get(input.get(i).port).setter.set(input.get(i).value);
		}
	}
}
