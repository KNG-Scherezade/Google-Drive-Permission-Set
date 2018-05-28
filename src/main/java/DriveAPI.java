import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

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
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

//template from Java quickstart code and https://developers.google.com/drive/api/v3/batch
public class DriveAPI {
	private NetHttpTransport request_transport = new NetHttpTransport();
	private Drive service;
	private ConsoleModel attatchment;
	public DriveAPI(ConsoleModel cm){this.attatchment = cm;}
	
	public String setPermissions(String folder_to_set, String role_level, String type_of_permission, String email_of_permission){
		type_of_permission = type_of_permission.toLowerCase();
		role_level = role_level.toLowerCase();
        // Print the names and IDs for up to 10 files.
        FileList result;
		try {
			System.out.println(folder_to_set);
			result = service.files().list()
					.setSupportsTeamDrives(true)
					.setIncludeTeamDriveItems(true)
			        .setPageSize(1000)
			        .setFields("files(id, name, mimeType)")
			        .setQ("name=\"" + folder_to_set + "\"")
			        .execute();
	        List<File> files = result.getFiles();
	        if (files == null || files.isEmpty()) {
	        	return "No files matched";	
	        } else {
	            System.out.println("Files:");
	            System.out.println(files.toString());
	            Object[] obj = buildPermissionBatchRequest();
	            BatchRequest batch = (BatchRequest)obj[0];
	            @SuppressWarnings("unchecked")
				JsonBatchCallback<Permission> callback = (JsonBatchCallback<Permission>)obj[1];
	            for (File file : files) {
	            	addToPermissionBatch(batch, callback, file, role_level, type_of_permission, email_of_permission);
	            }
		        batch.execute();
				return "Permissions set finished";	
	        }
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error setting permissions: " + e.getStackTrace().toString();	
		}
	}
	
	private Object[] buildPermissionBatchRequest(){
		JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
		@Override
		  public void onFailure(GoogleJsonError e,
		                        HttpHeaders responseHeaders)
		      throws IOException {
		    // Handle error
			  attatchment.notifyObservers(e.getMessage());
			  System.err.println(e.getMessage());
		  }

		  @Override
		  public void onSuccess(Permission permission,
		                        HttpHeaders responseHeaders)
		      throws IOException {
		    System.out.println("Permission ID: " + permission.getId());
		    attatchment.notifyObservers("Permissions set, ID: " + permission.getId() );
		  }
		};
		Object[] obj = {((Object)service.batch()), ((Object)callback)};
		return obj;
	}
	
	private String addToPermissionBatch(BatchRequest batch, JsonBatchCallback<Permission> callback, File file, String role_level, 
			String type_of_permission, String email_of_permission) throws IOException{				
			Permission userPermission;
			System.out.println(type_of_permission + " " + role_level + " " + email_of_permission);
			if(email_of_permission == null){
				userPermission = new Permission()
					    .setType(type_of_permission)
					    .setRole(role_level);
			}
			else{
				userPermission = new Permission()
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
			return "Error: " +  e.getStackTrace().toString();
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
