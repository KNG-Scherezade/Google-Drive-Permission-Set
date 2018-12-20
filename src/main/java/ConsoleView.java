

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

public class ConsoleView  extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextPane console_text = new JTextPane();
	private JTextField console_input = new JTextField();
	private JLabel console_label = new JLabel("Google Drive Permission Automator");
	private JMenuBar menu_bar = new JMenuBar();
	private JProgressBar progress_bar;
	private JFrame frame;
	private ArrayList<String> console_lines = new ArrayList<String>();
	
	private JMenuItem exit_item;
	
	public ConsoleView() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		this.frame = new JFrame("Drive Permission Setter");
		
		frame.setJMenuBar(menu_bar);
		JMenu menu = new JMenu("File");
		this.exit_item = new JMenuItem("Exit");
		menu_bar.add(menu);
		menu.add(exit_item);

		new JPanel(new BorderLayout());
		frame.getContentPane().setLayout(new BorderLayout());
		
		frame.getContentPane().add(console_input, BorderLayout.PAGE_START);
		console_input.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		console_input.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		console_input.setText("");
		console_input.setCaretPosition(0);
		
		JScrollPane scrollPane = new JScrollPane(console_text);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		console_text.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		console_text.setPreferredSize(new Dimension(500,300));
		console_text.setEnabled(false);
		console_text.setBackground(Color.BLACK);
		console_text.setCaretColor(Color.WHITE);
		console_text.setSelectedTextColor(Color.WHITE);
		console_text.setDisabledTextColor(Color.WHITE);
		console_text.setContentType("text/html");
		console_text.setText("Initializing...");
		DefaultCaret caret = (DefaultCaret)console_text.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		frame.getContentPane().add(console_label, BorderLayout.PAGE_END);
		console_label.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		console_label.setHorizontalAlignment(JLabel.CENTER);
		console_label.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.black));
		
		frame.pack();
		//https://stackoverflow.com/questions/2442599/how-to-set-jframe-to-appear-centered-regardless-of-monitor-resolution
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	public int createDialog(String dialog_message, String dialog_title, int dialog_icon, int dialog_option){
		if(dialog_title == null) dialog_title="";
		if(dialog_icon <= 0) dialog_icon=JOptionPane.PLAIN_MESSAGE;
		return JOptionPane.showOptionDialog(this.frame, dialog_message, dialog_title, dialog_option, dialog_icon, null, null, null);
	}
	
	public void closeDialog(){
		if(0 == createDialog("Exit Program?", "Quit Google Drive Permissions", JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION))				
			System.exit(0);
	}
	
	public void createLoadDialog(){
		JOptionPane load_dialog = new JOptionPane(JOptionPane.INFORMATION_MESSAGE);
		load_dialog.setOptions(null);
		load_dialog.setLayout(new BorderLayout());
		load_dialog.add(new JLabel("Your actions are being processed.."), BorderLayout.PAGE_START);
		progress_bar = new JProgressBar(0,100);
		progress_bar.setStringPainted(true);
		progress_bar.setValue(40);
		progress_bar.setPreferredSize(new Dimension(100,20));
		load_dialog.add(progress_bar, BorderLayout.CENTER);
		load_dialog.add(new JLabel("Thing being processed.."), BorderLayout.PAGE_END);
		JOptionPane.showMessageDialog(this.frame,load_dialog);
	}
	public JMenuItem getExitMenu(){
		return exit_item;
	}
	public JFrame getFrame(){
		return frame;
	}
	public JTextField getConsoleInput(){
		return console_input;
	}
	public String getConsoleText(){
		return console_text.getText();
	}
	public void updateConsoleText(String html_line){
		String text_lines = "<br/><strong style='font-family:Courier New;line-height: 10px;'>> " + html_line + "</strong><br/><br/>";
		//System.out.println(console_lines.toString());
		for(String text:console_lines){
			//System.out.println(text);
			text_lines += "<strong style='font-family:Courier New;line-height: 10px;'>> " + text + "</strong><br/>";
		}
		console_text.setText(text_lines);
		console_lines.add(0, html_line);
	}
}
