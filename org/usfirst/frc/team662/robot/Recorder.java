package org.usfirst.frc.team662.robot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.google.gson.Gson;

public class Recorder implements Component{
	
	Gson gson = new Gson();
	
	//Timers used for recording and playing
	Timer GlobalTime = new Timer();
	static Timer playingTimer = new Timer();
	
	//The ui radio buttons. Also stores files with name.
	SendableChooser<File> autoChooser = new SendableChooser<File>();
	
	public static Map<String, File> allFound;

	//Some constants
	static final String ALL_FILES = "autoFiles";
	static final String DIRECTORY = "/home/lvuser/Records/";
	static final String FILE_NAME = "File name";
	static final String DO_RECORD = "Create recording";
	static final String DO_DELETE = "Delete rcording";
	public static final String DO_PLAY = "Play recording";
	static final String DO_REFRESH = "Refresh the available files";
	static final String DEFAULT_NAME = "defaultAuto";
	static final String MAKE_READABLE = "make readable";
			
	//Used for the recorder

	boolean hasFinished = false;
	
	//Used for the player
	public static boolean isRecordingPlaying = false;

	//These are the arrays used to store recording information
	
	//This one is used to store the functions to the hardware
	static ArrayList<Hardware> pieces = new ArrayList<Hardware>();
	
	//This one stores when the values change and what they change to
	static ArrayList<Timings> timers= new ArrayList<Timings>();
	
	//This one is for erasing the timers array, when required

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
		public int index = 0;
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

	//must be last to be constructed in the main robot code
	public Recorder() {
		//Put the ui elements
		SmartDashboard.putBoolean(DO_RECORD, false);
		SmartDashboard.putBoolean(DO_DELETE, false);
		SmartDashboard.putBoolean(DO_REFRESH, false);
		SmartDashboard.putBoolean(MAKE_READABLE, false);

		SmartDashboard.putString(FILE_NAME, DEFAULT_NAME);

		refreshFiles();
		
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
		
		//reload file choices, if needed
		if(SmartDashboard.getBoolean(DO_REFRESH, false)){
			refreshFiles();
			SmartDashboard.putBoolean(DO_REFRESH, false);
		}
		
		//Check if we need to record based on a dashboard
		if (SmartDashboard.getBoolean(DO_RECORD, false)) {
			record();
			//This will run as soon as the player changes the ui back to false to stop recording
		} else if (hasFinished) {
			GlobalTime.stop();
			GlobalTime.reset(); 
			saveRecording();
			//Reset hasFinished
			hasFinished = false;
			System.out.println("SAVED");
		}
		

		//play Recording
		if (SmartDashboard.getBoolean(DO_PLAY, false)) {
			if(isRecordingPlaying){
				play();
			} else {
				initializePlay(autoChooser.getSelected());				
			}
		}
		
		//delete recording
		if (SmartDashboard.getBoolean(DO_DELETE, false)) {
			delete();
		}
		
		if(SmartDashboard.getBoolean(MAKE_READABLE, false)) {
			makeReadable(timers);
			//makeReadable(pieces);
			SmartDashboard.putBoolean(MAKE_READABLE, false);
		}
	}
	
	//Load whatever the saved file is
	static ArrayList<Timings> loadSavedRecording(File fileToLoad) {
		//Create where we will save the loaded thing
		ArrayList<Timings> deSerialized = new ArrayList<Timings>();

		try {
			//File stuff. Create input stream based on the file from the chooser on smartDashboard
			FileInputStream fileIn = new FileInputStream(fileToLoad);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			deSerialized = (ArrayList<Timings>) in.readObject();
		} catch (IOException i) {
			System.err.println("There was a file retrieval error during the recording load sequence: " + i);
			SmartDashboard.putBoolean(DO_PLAY, false);
		} catch (ClassNotFoundException c) {
			System.err.println("There was a problem with the recording file: " + c);
			SmartDashboard.putBoolean(DO_PLAY, false);
		}
		//This sorts the array by port. Needed to ensure that the timings and hardwares line up even if the order they are added changes.
		//The play method checks this more.
		deSerialized.sort((a, b) -> a.port - b.port);
		System.out.println("The number of objects in timers after loading: " + deSerialized.get(5).times.size());

		return deSerialized;
	}
	
	//Save the timings arraylist to a file
	void saveRecording() {
		//The file name according to the user
		String fileName = SmartDashboard.getString(FILE_NAME, "dc");
		//This would be bad if it happened so change it to a real string.
		if (fileName.equals("")) {
			fileName = DEFAULT_NAME;
		}
		try {
			//All of the io and file stuff. Create a file, then a stream, then an object stream
			File fileLocation = new File(DIRECTORY + fileName + ".ser");
			if (!fileLocation.createNewFile()){
				System.out.println("Deleting the file");
				fileLocation.delete();
				fileLocation.createNewFile();
			}
			FileOutputStream fileOut = new FileOutputStream(fileLocation, false);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			System.out.println("The number of objects in timers is: " + timers.get(5).times.size());
			out.writeObject(timers);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			//Shouldn't happen
			System.err.println("There was an error during the save recording sequence:" + i);
		}
	}
	
	//Records movements done based on component values.
	public void record(){
		//If this is the first time we are running the record code...
		if (!hasFinished) {
			GlobalTime.stop();
			GlobalTime.reset(); 
			GlobalTime.start();
			for (Timings timeObject : timers){
				timeObject.times = new ArrayList<Double>();
				timeObject.values = new ArrayList();
				timeObject.index = 0;
			}
			System.out.println("Just reset timers: " + timers.get(5).times.size());
	
		}
		//Will remain true until the recording finishes
		hasFinished = true;
		//Record every single hardware piece
		for (int i = 0; i < pieces.size(); i++) {
			//Store the actual object we are using from the ArrayList
			Timings gottenTimer = timers.get(i);
			Hardware gottenPiece = pieces.get(i);
			
			//Store the object for the previous timer value so we only have to save when it changes.
			Object previousTimerValue = -2;
			if (gottenTimer.values.size() != 0){
				previousTimerValue = gottenTimer.values.get(gottenTimer.values.size() - 1);
			}
			//Store the current value for the motor
			Object currentHardwareValue = gottenPiece.getter.get();
			
			
			//Basically, we can't actually compare two plain objects because it will always be false. This casts it to whatever the subclass is
			//There should be no time when the motor and its recordings should be of different type making this safe
			if (gottenTimer.values.size() == 0 || !currentHardwareValue.getClass().cast(currentHardwareValue).equals(previousTimerValue.getClass().cast(previousTimerValue))) {
				//Add the current time and the value to the array.
				System.out.println("The current time is: " + GlobalTime.get());
				gottenTimer.times.add(GlobalTime.get());
				gottenTimer.values.add(gottenPiece.getter.get());
			}
			
		}
				
	}

	public static void initializePlay(File fileToLoad){
		
		//Set our timers object equal to whatever gets loaded.
		timers = new ArrayList<Timings>(loadSavedRecording(fileToLoad));
		isRecordingPlaying = true;
		//A check to ensure that we have the same hardware as we had when we recorded it
		if (pieces.size() == timers.size()) {
			for (int i = 0; i < pieces.size(); i++) {
				if (pieces.get(i).port != timers.get(i).port) {
					System.err.println("The port of piece number " + i + " does not match that of the recording.");
					SmartDashboard.putBoolean(DO_PLAY, false);
					isRecordingPlaying = false;
					return;
				}
			}
			//Will be true until all playback has finished
			playingTimer.stop();
			playingTimer.reset();
			playingTimer.start();
			
			
		} else {
			System.err.println("The number of pieces on the robot does not match that of the recording." + pieces.size() +" " + timers.size());
			SmartDashboard.putBoolean("play recording", false);
			isRecordingPlaying = false;
		}
	

	}
	
	//Called every time the robot updates and SmartDashboard button set. replays any action scheduled for the specific time
	public void play() {
		 
		System.out.println("Playing timer value: " + playingTimer.get());
		
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
					System.out.println("recorded time: " + currentTimeChecking.times.get(currentTimeChecking.index));
					System.out.println("Playing timer value: " + playingTimer.get());
					//Set the motor value to whatever the recording was at this time. Also add one to index.
					pieces.get(i).setter.accept(currentTimeChecking.values.get(currentTimeChecking.index));
					currentTimeChecking.index++;
					
				}
			}
		}
		//If this is true, then every component has passed the final event.
		if (allDone){
			//Reset everything back
			isRecordingPlaying = false;
			SmartDashboard.putBoolean(DO_PLAY, false);
		}	
	}
	
	public void delete() {
			//Delete the specified file
			File deletionFile = autoChooser.getSelected();
			deletionFile.delete();
			SmartDashboard.putBoolean(DO_DELETE, false);
	}
	
	@Override
	public void autoUpdate() {
		//reload file choices, if needed
		if(SmartDashboard.getBoolean(DO_REFRESH, false)){
			refreshFiles();
			SmartDashboard.putBoolean(DO_REFRESH, false);
		}
		
		//play Recording
		if (SmartDashboard.getBoolean(DO_PLAY, false)) {
			if(isRecordingPlaying){
				play();
			} else {
				initializePlay(autoChooser.getSelected());
			}
		}
	}

	@Override
	public void disable() {
		SmartDashboard.putBoolean(DO_PLAY, false);
		SmartDashboard.putBoolean(DO_RECORD, false);

		isRecordingPlaying = false;
		hasFinished = false;
	}
	
	public void refreshFiles(){
		//Make sure our folders really exist
				File records = new File(DIRECTORY);
				records.mkdirs();
				
				//Get all files in the directory
				File[] foundRecords = records.listFiles();
				allFound = new HashMap<String, File>();
				
				//Did we find anything?
				if (foundRecords != null && foundRecords.length != 0) {
					//Add them as options if we did!
					for (int i = 0; i < foundRecords.length; i++) {
						if(i == 0){
							autoChooser.addDefault(foundRecords[i].getName(), foundRecords[i]);
						}
						else{
							autoChooser.addObject(foundRecords[i].getName(), foundRecords[i]);
						}
						allFound.put(foundRecords[i].getName(), foundRecords[i]);
					}
					//Also, put play related things on the ui
					SmartDashboard.putData(ALL_FILES, autoChooser);
					SmartDashboard.putBoolean(DO_PLAY, false);
					System.out.println("LOADED THE Recording");
				}

				//Sort the pieces and timers arraylists based on ports. This is why recorder has to run last. 
				//This is used when loading a recording.
				pieces.sort((a, b) -> a.port - b.port);
				timers.sort((a, b) -> a.port - b.port);
			
	}
	public void makeReadable(ArrayList thingToPrint) {
		System.out.println(gson.toJson(thingToPrint));
	}
	
}
