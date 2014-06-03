package chess_swingfrontend;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A simple splash screen to be displayed when the app is launched.
 * TODO: Make this fancier
 * 
 * @author Kelton Finch
 */
public class SplashPanel extends JPanel{

	private static final String SPLASH_MESSAGE = "Welcome to kfinch's Chess app!";
	
	private JLabel message;
	
	public SplashPanel(){
		message = new JLabel(SPLASH_MESSAGE);
		add(message, BorderLayout.CENTER);
	}
	
}
