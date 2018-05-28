
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

public class ConsoleController implements ActionListener, Observer{
	
	private ConsoleView console_view;
	private ConsoleModel console_model;
	public String startup_message = "Type in file/folder you wish to change permissions of. Case insensitive";
	public ConsoleController(){}
	
	public void initialize(){
		try {
			this.console_view = new ConsoleView();
			console_view.getExitMenu().addActionListener(this);
			//https://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
			console_view.getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			console_view.getFrame().addWindowListener(new java.awt.event.WindowAdapter() {
			    @Override
			    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			    	console_view.closeDialog();
			    }
			});
			console_view.getConsoleInput().addActionListener(this);
			
			this.console_model = new ConsoleModel();
			this.console_model.attatch(this);
			String old_messages = this.console_view.getConsoleText();
			System.out.println(old_messages);
			this.console_view.updateConsoleText("Logging into service...");
			this.console_view.updateConsoleText(console_model.loggin());
			this.console_view.updateConsoleText(startup_message);
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand() == "Exit"){
			console_view.closeDialog();
		}
		else{
			if(console_model.folder_to_set == null){
				console_model.folder_to_set = console_view.getConsoleInput().getText();
				console_view.updateConsoleText("Location: " + console_model.folder_to_set + "<br>");
				
				console_view.updateConsoleText("Set the permission level:"
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> Organizer,"
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> Owner, "
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> Writer, "
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> Commenter, "
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> Reader, <br>");
			}
			else if(console_model.role_level == null){
				console_model.role_level = console_view.getConsoleInput().getText();
				console_view.updateConsoleText("Level: " +console_model.role_level + "<br>");
				
				console_view.updateConsoleText("Set who it aplies to:"
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> User,"
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;> Anyone, <br>");
			}
			else if(console_model.type_of_permission == null){
				console_model.type_of_permission = console_view.getConsoleInput().getText();
				console_view.updateConsoleText("Applies: " + console_model.type_of_permission + "<br>");
				
				if(console_model.type_of_permission.equalsIgnoreCase("user")){
					console_view.updateConsoleText("Enter the email-address of the user to which the permission effects:" + "<br>");			
				}
				else{
					console_view.updateConsoleText("Permissions are being set. <strike>Notification when finished</strike>" + "<br>");
					//console_view.createLoadDialog();
					console_view.updateConsoleText(console_model.setPermissions());
					console_view.updateConsoleText(startup_message);
					console_model.email_of_permission = null;
					console_model.role_level = null;
					console_model.type_of_permission = null;
					console_model.folder_to_set = null;
				}
			}
			else{
				console_model.email_of_permission = console_view.getConsoleInput().getText();
				console_view.updateConsoleText("EMail: " +console_model.email_of_permission + "<br>");
				
				console_view.updateConsoleText("Permissions are being set. <strike>Notification when finished</strike>");
				//console_view.createLoadDialog();
				console_view.updateConsoleText(console_model.setPermissions());
				console_view.updateConsoleText(startup_message);
				console_model.email_of_permission = null;
				console_model.role_level = null;
				console_model.type_of_permission = null;
				console_model.folder_to_set = null;
			}
			console_view.getConsoleInput().setText("");
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		console_view.updateConsoleText((String)arg);
	}

}
