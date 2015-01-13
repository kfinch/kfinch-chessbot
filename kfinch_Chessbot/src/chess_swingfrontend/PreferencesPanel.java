package chess_swingfrontend;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel to display and allow modification of the game's preferences
 * TODO: Implement
 * 
 * @author Kelton Finch
 */
public class PreferencesPanel extends JPanel implements ActionListener{
	
	public PreferencesPanel(){
		JLabel message = new JLabel("Preferences NYI  )=   Press the prefs button again to return to an active game.");
		add(message, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

