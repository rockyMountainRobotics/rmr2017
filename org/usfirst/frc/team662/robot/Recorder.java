package org.usfirst.frc.team662.robot;
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

public class Recorder {

	Timer GlobalTime = new Timer();

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
		public static ArrayList<Double> times;

		public static ArrayList<Double> value;

		public Timings(int incPort) {
			port = incPort;

		}
	}

	static ArrayList<Hardware> pieces;
	static ArrayList<Timings> timers;

	//must be last to be constructed in the main robot code
	public Recorder() {
		GlobalTime.start();
	}

	public static void addRecordable(Supplier newGet, Consumer newSet, int port) {
		//add a new hardware class to pieces and set up the timer
		pieces.add(new Hardware(newGet, newSet, port));
		timers.add(new Timings(port));
	}

	public void update() {
		//every piece should be ready
		for (int i = 0; i <= pieces.size(); i++) {
			if (!pieces.get(i).getter.equals(Timings.value.get(Timings.value.size()))) {
				Timings.times.add(GlobalTime.get());
				Timings.value.add((Double) pieces.get(i).getter.get());
			}
		}
	}

	ArrayList loadSavedRecording() {
		//if a recording id found, load it
		ArrayList deSerialized = new ArrayList();
		try {
			FileInputStream fileIn = new FileInputStream("/Desktop/Serialize/Record.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			deSerialized = (ArrayList) in.readObject();
		} catch (IOException i) {
			System.out.println("Error");
		} catch (ClassNotFoundException c) {
			System.out.println("error");
		}

		return deSerialized;
	}

	void saveRecording() {
		//serialize
		try {
			FileOutputStream fileOut = new FileOutputStream("/Desktop/Serialize/Record.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(timers);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			System.out.println("Error");
		}
	}
}

