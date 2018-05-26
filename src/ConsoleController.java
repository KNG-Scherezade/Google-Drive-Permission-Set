import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

public class ConsoleController implements ActionListener, ItemListener{
	
	private ConsoleView console_view;
	private ConsoleModel consoleModel;
	
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
			
			this.consoleModel = new ConsoleModel();
			
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
	public void itemStateChanged(ItemEvent arg0) {}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand() == "Exit"){
			console_view.closeDialog();
		}
	}

}
