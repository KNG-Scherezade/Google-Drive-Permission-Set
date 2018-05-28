import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ConsoleModel extends Observable{
	public DriveAPI drive;
	
	public String folder_to_set;
	public String role_level;
	public String type_of_permission;
	public String email_of_permission;
	
	public ArrayList<Observer> observers = new ArrayList<Observer>();
	
	public ConsoleModel(){	
		drive = new DriveAPI(this);
	}
	
	public String loggin(){
		return drive.authenticate();
	}
	public String setPermissions(){
		return drive.setPermissions(folder_to_set, role_level, type_of_permission, email_of_permission);
	}
	
	public void attatch(Observer obs){
		observers.add(obs);
	}
	
	public void notifyObservers(String message){
		for(Observer obs: observers){
			obs.update(this, message);
		}
	}	
}
