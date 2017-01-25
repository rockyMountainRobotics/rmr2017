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

public class Recorder implements Component{

	//Timers used for recording and playing
	Timer GlobalTime = new Timer();
	Timer playingTimer = new Timer();
	//The ui radio buttons. Also stores files with name.
	SendableChooser<File> autoChooser = new SendableChooser<File>();
	SendableChooser<Boolean> recordChooser = new SendableChooser<Boolean>();

	//Some constants
	static final String ALL_FILES = "autoFiles";
	static final String DIRECTORY = "/home/lvuser/Records/";
	
	//Used for the recorder
	double lastTime = -1;
	boolean hasFinished = false;
	//Used for the player
	public static boolean hasLoaded = false;


	static class Hardware {
		//A supplier takes no arguments and returns something. It saves a method as a variable.
		//Warning is because it is a generic in raw form. We can't get any more specific because components will be of different types.
		public Supplier getter;
		//Consumer takes 1 argument and returns nothing. Consumer will take what supplier returns.
		public Consumer setter;
		//Used to match up Hardware with recordings
		public int port;

		public Hardware(Supplier incGetter, Consumer incSetter, int incPort) {
			getter = incGetter;
			setter = incSetter;
			port = incPort;
		}
	}

	public static class Timings implements Serializable {
		//Same reason as Hardware
		public int port;
		//When playing back, this value keeps track of which event we are on and where we are in the ArrayLists below
		public int index;
		//The list of times. We can actually use the non-raw form!
		public ArrayList<Double> times;
		//The list of values. Will be whatever type the supplier returns
		public ArrayList values;

		public Timings(int incPort) {
			port = incPort;
			times = new ArrayList<Double>();
			values = new ArrayList();

		}
	}

	//These are the arrays used to store recording information
	//This one is used to store the functions to the hardware
	static ArrayList<Hardware> pieces = new ArrayList<Hardware>();
	//This one stores when the values change and what they change to
	static ArrayList<Timings> timers= new ArrayList<Timings>();

	//must be last to be constructed in the main robot code
	public Recorder() {
		//Put the ui elements
		SmartDashboard.putBoolean("record", false);
		SmartDashboard.putString("input", "defaultAuto");

		//Make sure our folders really exist
		File records = new File(DIRECTORY);
		records.mkdirs();
		
		//Get all files in the directory
		File[] foundRecords = records.listFiles();
		
		//Did we find anything?
		if (foundRecords != null && foundRecords.length != 0) {
			//Add them as options if we did!
			for (int i = 0; i < foundRecords.length; i++) {
				if(i == 0){
					autoChooser.addDefault(DIRECTORY + foundRecords[i].getName(), foundRecords[i]);
				}
				else{
					autoChooser.addObject(foundRecords[i].getName(), foundRecords[i]);
				}
			}
			//Also, put play related things on the ui
			SmartDashboard.putData(ALL_FILES, autoChooser);
			SmartDashboard.putBoolean("play recording", false);
			System.out.println("LOADED THE THING");
		}
		//Sort the pieces and timers arraylists based on ports. This is why recorder has to run last. 
		//This is used when loading a recording.
		pieces.sort((a, b) -> a.port - b.port);
		timers.sort((a, b) -> a.port - b.port);
	}

	//Called by each component at least once if it has hardware to record
	public static void addRecordable(Supplier newGet, Consumer newSet, int port) {
		//add a new hardware class to pieces and set up the timer
		//newGet is (some class like CANTalon or Solenoid).get(). Set is same thing but .set()
		//Port is incredibly important and is used to identify a hardware component with its saved recording
		pieces.add(new Hardware(newGet, newSet, port));
		timers.add(new Timings(port));
		System.out.println(port);
	}

	public void update() {
		//Check if we need to record based on a dashboard
		if (SmartDashboard.getBoolean("record", false)) {
			record();
			//This will run as soon as the player changes the ui back to false to stop recording
		} else if (hasFinished) {
			GlobalTime.stop();
			GlobalTime.reset();
			lastTime = -1;
			saveRecording();
			//Reset hasFinished
			hasFinished = false;
			System.out.println("SAVED");
		}
		

		//play Recording
		if (SmartDashboard.getBoolean("play recording", false)) {
			play();
		}
		
	}
	//Load whatever the saved file is
	ArrayList loadSavedRecording() {
		//Create where we will save the loaded thing
		ArrayList<Timings> deSerialized = new ArrayList<Timings>();

		try {
			//File stuff. Create input stream based on the file from the chooser on smartDashboard
			FileInputStream fileIn = new FileInputStream(autoChooser.getSelected());
			ObjectInputStream in = new ObjectInputStream(fileIn);
			deSerialized = (ArrayList<Timings>) in.readObject();
		} catch (IOException i) {
			System.out.println(i);
		} catch (ClassNotFoundException c) {
			System.out.println(c);
		}
		//This sorts the array by port. Needed to ensure that the timings and hardwares line up even if the order they are added changes.
		//The play method checks this more.
		deSerialized.sort((a, b) -> a.port - b.port);
		return deSerialized;
	}
	//Save the timings arraylist to a file
	void saveRecording() {
		//The file name according to the user
		String fileName = SmartDashboard.getString("input", "dc");
		//This would be bad if it happened so change it to a real string.
		if (fileName.equals("")) {
			fileName = "defaultAuto";
		}
		try {
			//All of the io and file stuff. Create a file, then a stream, then an object stream
			File fileLocation = new File(DIRECTORY + fileName + ".ser");
			fileLocation.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(fileLocation);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(timers);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			//Shouldn't happen
			System.out.println(i);
		}
	}
	//Records movements done based on component values.
	public void record(){
		//This adds a .1 second delay since components shouldn't change much quicker.
		if (GlobalTime.get() - lastTime > .1) {
			lastTime = GlobalTime.get();
			//If this is the first time we are running the record code...
			if (!hasFinished) {
				GlobalTime.start();
			}
			//Will remain true until the recording finishes
			hasFinished = true;
			//Record every single hardware piece
			for (int i = 0; i < pieces.size(); i++) {
				//Store the actual object we are using from the ArrayList
				Timings gottenTimer = timers.get(i);
				Hardware gottenPiece = pieces.get(i);
				
				//Store the object for the previous timer value so we only have to save when it changes.
				Object previousTimerValue = -1;
				if (gottenTimer.values.size() != 0){
					previousTimerValue = gottenTimer.values.get(gottenTimer.values.size() - 1);
				}
				//Store the current value for the motor
				Object currentMotorValue = gottenPiece.getter.get();
				
				
				//Basically, we can't actually compare two plain objects because it will always be false. This casts it to whatever the subclass is
				//There should be no time when the motor and its recordings should be of different type making this safe
				/*System.out.println("Timer is: " + previousTimerValue.getClass().cast(previousTimerValue));
				System.out.println("Motor is: " + currentMotorValue.getClass().cast(currentMotorValue));*/
				if (gottenTimer.values.size() == 0 || !currentMotorValue.getClass().cast(currentMotorValue).equals(previousTimerValue.getClass().cast(previousTimerValue))) {
					//Add the current time and the value to the array.
					gottenTimer.times.add(GlobalTime.get());
					gottenTimer.values.add(gottenPiece.getter.get());
				}
			}
		}
				
	}

	//Called every time the robot updates and SmartDashboard button set. replays any action scheduled for the specific time
	public void play() {
		//Checks if this is the first time we have run this code since clicking the button.
		if (!hasLoaded) {
			//Set our timers object equal to whatever gets loaded.
			timers = loadSavedRecording();
			//A check to ensure that we have the same hardware as we had when we recorded it
			if (pieces.size() == timers.size()) {
				for (int i = 0; i < pieces.size(); i++) {
					if (pieces.get(i).port != timers.get(i).port) {
						System.out.println("ERROR!!");
					}
				}
				//Will be true until all playback has finished
				hasLoaded = true;
				playingTimer.stop();
				playingTimer.reset();
				playingTimer.start();
				
				
			} else {
				System.out.println("ERROR!!");
			}
		} else {
			//If allDone is true at the end of the for loop, then everything is done.
			boolean allDone = true;
			for(int i = 0; i < pieces.size(); i++){
				//The Timings object currently checking in the arrayList		
				Timings currentTimeChecking = timers.get(i);
				
				//If we haven't already done every action that was recorded, keep playing.
				if (currentTimeChecking.index < currentTimeChecking.times.size()){
					//Since we still have stuff to do, we aren't all done
					allDone = false;
					//Check if we have reached the time for the next event
					if(currentTimeChecking.times.get(currentTimeChecking.index) <= playingTimer.get()){
						//Set the motor value to whatever the recording was at this time. Also add one to index.
						pieces.get(i).setter.accept(currentTimeChecking.values.get(currentTimeChecking.index));
						currentTimeChecking.index++;
						
					}
				}
			}
			//If this is true, then every component has passed the final event.
			if (allDone){
				//Reset everything back
				hasLoaded = false;
				SmartDashboard.putBoolean("play recording", false);
			}
		}
			
	}
	
	@Override
	public void autoUpdate() {
		// TODO Auto-generated method stub
		
	}
	
}
