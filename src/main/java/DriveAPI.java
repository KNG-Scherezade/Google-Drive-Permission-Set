import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import javafx.animation.PauseTransition;

//template from Java quickstart code and https://developers.google.com/drive/api/v3/batch
public class DriveAPI implements Runnable{
	private NetHttpTransport request_transport = new NetHttpTransport();
	private Drive service;
	private ConsoleModel attatchment;
	String folder_to_set;
	String role_level;
	String parent_id;
	String type_of_permission;
	String email_of_permission;
	
	int cooldown_time = 5000;
	int batch_size = 35;
	
	public DriveAPI(ConsoleModel cm){
		this.attatchment = cm;

	}
	
	public void setParam(String ft, String rl, String pi, String tp, String ep){
		folder_to_set = ft;
		role_level = rl;
		parent_id = pi;
		type_of_permission = tp;
		email_of_permission = ep;
	}

	@Override
	public void run() {
		setPermissions(folder_to_set, role_level, parent_id, type_of_permission, email_of_permission);
	}
	
	public String setPermissions(String folder_to_set, String role_level, String parent_id, String type_of_permission, String email_of_permission){
		type_of_permission = type_of_permission.toLowerCase();
		role_level = role_level.toLowerCase();
        // Print the names and IDs for up to 10 files.
        FileList result;
        
        
        String[] message1 = {"Getting all files of name " + folder_to_set + " to set root folder ID " + parent_id , "c"};
        attatchment.notifyObservers(message1);	
        
        
		try {
		    PauseTransition pause = new PauseTransition();
		    pause.durationProperty();
			System.out.println(folder_to_set);
			if(parent_id == ""){
				result = service.files().list()
						.setSupportsTeamDrives(true)
						.setIncludeTeamDriveItems(true)
				        .setPageSize(1000)
				        .setOrderBy("modifiedTime")
				        .setFields("files(id, name, teamDriveId, mimeType)")
				        .setQ("name=\"" + folder_to_set + "\"")
				        .execute();
			}
			else{
				result = service.files().list()
						.setSupportsTeamDrives(true)
						.setIncludeTeamDriveItems(true)
				        .setPageSize(1000)
				        .setOrderBy("modifiedTime")
				        .setFields("files(id, name, teamDriveId, mimeType, parents)")
				        .setQ("name=\"" + folder_to_set + "\"")
				        .execute();
			}
	        List<File> files = result.getFiles();
	        
	        
	        String[] message2 = {"List obtained " + parent_id , "c"};
	        attatchment.notifyObservers(message2);	
	        
	        if (files == null || files.isEmpty()) {
	        	return "No files matched";	
	        } else {
	            System.out.println("Files: " + files.size());
	            int total_files = files.size();
	            
	            if(total_files == 1000){
		            String[] failmsg = {"1000 files detected(API limit is 1000 files). This program will not run since some data could potentially be missed. Rename your files and try again", "r"};
		            attatchment.notifyObservers(failmsg);	
					return "Permissions set finished";	
	            }
	            else{
	            	String[] count_msg = {"Checking " + total_files + " files.", "c"};
		            attatchment.notifyObservers(count_msg);	
	            }
	            
	            Object[] obj = buildPermissionBatchRequest();
	            BatchRequest batch = (BatchRequest)obj[0];
	            @SuppressWarnings("unchecked")
				JsonBatchCallback<Permission> callback = (JsonBatchCallback<Permission>)obj[1];
	            int counter = 0;
	            int batch_counter = 0;       
	            long now = System.currentTimeMillis();
        		while(now + cooldown_time > System.currentTimeMillis() ){}
        		String[] message = {"Estimated time: " +  (files.size() / batch_size * 20) + " Seconds", "c"};
	            attatchment.notifyObservers(message);	
	            int file_no = 0;
	            String[] message3 = {"Determining root hierarchy of Files and adding them to batch requests: " + parent_id , "c"};
	            attatchment.notifyObservers(message3);	
	            for (File file : files) {
	            	if(parent_id != ""){
        				String[] file_count = {total_files + " " + file.getId(), "l"};
        		    	attatchment.notifyObservers(file_count);	
            			if(!checkParent_ids(file, parent_id)){
            				total_files--;
            				continue;
            			}
	            	}
	            	else{

	            	}
    	
	            	addToPermissionBatch(batch, callback, file, role_level, type_of_permission, email_of_permission);
            		String[] message5 = {++file_no + " / " + total_files, "l"};
            		attatchment.notifyObservers(message5);	
	            	if (counter++ == batch_size){
	            		now = System.currentTimeMillis();
	            		while(now + cooldown_time > System.currentTimeMillis() ){}
	            		batch.execute();
	            		now = System.currentTimeMillis();
	            		while(now + cooldown_time > System.currentTimeMillis() ){}
	            		System.out.println(batch_counter);
	            		String[] message4 = {"Batch " + ++batch_counter + " of " + (int)Math.ceil(((float)total_files) / batch_size) + "(Estimated) Processed", "c"};
		            	attatchment.notifyObservers(message4);	   
		            	            
	            		counter = 0;
	            	}
	            }
	            System.out.println(total_files);
	            if(total_files == 0){
		            String[] message7 = {"Nothing was found for " + parent_id + " <br/>", "r"};
		            attatchment.notifyObservers(message7);	
					return "Permissions set finished";	
	            }
	            else if(counter != batch_size - 1){
	            	batch.execute();
	            }
	            String[] message6 = {total_files + " / " + total_files, "l"};
	            attatchment.notifyObservers(message6);	
	            String[] message7 = {"", "r"};
	            attatchment.notifyObservers(message7);	
				return "Permissions set finished";	

	        }
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error setting permissions: " + e.getMessage();	
		}
	}
	
	private boolean checkParent_ids(File file, String parent_id) throws IOException{
		if(file.get("parents") != null){
			if(((String) ((ArrayList)file.get("parents")).get(0)).equals(parent_id)){
				System.out.println("TRUE");
				return true;
			}
			else{
		    	File results = service.files().get((String) ((ArrayList)file.get("parents")).get(0)).setFields("parents").execute();
				checkParent_ids(results, parent_id);
			}
		}
		return false;
	}
	
	private Object[] buildPermissionBatchRequest(){
		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
		@Override
		  public void onFailure(GoogleJsonError e,
		                        HttpHeaders responseHeaders)
		      throws IOException {
		    // Handle error
			  String[] message = {e.getMessage(), "c"};
			  attatchment.notifyObservers(message);
			  System.err.println(e.getMessage());
		  }

		  @Override
		  public void onSuccess(Permission permission,
		                        HttpHeaders responseHeaders)
		      throws IOException {
		    System.out.println("Permission ID: " + permission.getId());
		    String[] message = {"Permissions set, ID: " + permission.getId() , "c"};
		    attatchment.notifyObservers(message);
		  }
		};
		Object[] obj = {((Object)service.batch()), ((Object)callback)};
		return obj;
	}
	
	private String addToPermissionBatch(BatchRequest batch, JsonBatchCallback<Permission> callback, File file, String role_level, 
			String type_of_permission, String email_of_permission) throws IOException{				
			Permission userPermission;
			file.getTeamDriveId();
			System.out.println(type_of_permission + " " + role_level + " " + email_of_permission);
			if(email_of_permission == null){
				userPermission = new Permission()
						.set("supportsTeamDrives", "true")
					    .setType(type_of_permission)			    
					    .setRole(role_level);
				//https://content.googleapis.com/drive/v3/files/140rDrFRHMozR_vScecNn7kZepyhYhIvR/permissions?supportsTeamDrives=true&alt=json&key=AIzaSyD-a9IF8KKYgoC3cpgS-Al7hLQDbugrDcw
				//https://content.googleapis.com/drive/v3/files/1xdm8dubYeTO_WweyW57qsLun6aZTDYUi/permissions?supportsTeamDrives=true&alt=json&key=AIzaSyD-a9IF8KKYgoC3cpgS-Al7hLQDbugrDcw
			}
			else{
				userPermission = new Permission()
						.set("supportsTeamDrives", "true")
					    .setType(type_of_permission)
					    .setRole(role_level)
					    .setEmailAddress(email_of_permission);
			}
		
			service.permissions().create(file.getId(), userPermission)
				    .setFields("id")
				    .queue(batch, callback);
			return "Batch added";
	}
	
    public String authenticate(){
		// Build a new authorized API client service.
		try {
			request_transport = GoogleNetHttpTransport.newTrustedTransport();
	        service = new Drive.Builder(request_transport, JSON_FACTORY, getCredentials(request_transport))
	                .setApplicationName(APPLICATION_NAME)
	                .build();
	        System.out.println(service.toString());
	    	return "Successfully authenticated";
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block
			return "Error: " +  e.getMessage();
		}
    }
       
    private  final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private  final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private  final String CREDENTIALS_FOLDER = "credentials"; // Directory to store user credentials.

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private final String CLIENT_SECRET_DIR = "client_secret.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If there is no client_secret.
     */
    private  Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveAPI.class.getResourceAsStream(CLIENT_SECRET_DIR);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

}
