package chess_swingfrontend;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ChessGameSwing extends JFrame implements ActionListener {

	private static final String GAME_CARDNAME = "game";
	private static final String SPLASH_CARDNAME = "splash";
	private static final String PREFS_CARDNAME = "prefs";
	
	private JPanel mainPanel; //a container for the game, splash, and preferences panels
	private GamePanel gamePanel; //displays a game of chess and associated elements
	private SplashPanel splashPanel; //displays a splash welcome screen
	private PreferencesPanel preferencesPanel; //displays game preferences
	
	private JPanel buttonPanel; //panel for displaying relevant buttons
	private JButton newHotseatGameButton;
	private JButton newBotGameButton;
	private JButton preferencesButton;
	private JButton quitButton;
	
	public ChessGameSwing(){
		initUI();
	}
	
	private void initUI(){
		//initialize the frame
		setTitle("Chess");
		setSize(850, 600); //TODO: tweak size
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        //initialize game panel
        gamePanel = new GamePanel(this);
        gamePanel.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.gray));

        //initialize splash panel
        splashPanel = new SplashPanel();
        splashPanel.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.gray));
        
        //initialize prefs panel
        preferencesPanel = new PreferencesPanel();
        preferencesPanel.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.gray));
        
        //initialize and add the main panel and its cards
        mainPanel = new JPanel(new CardLayout());
        mainPanel.add(gamePanel, GAME_CARDNAME);
        mainPanel.add(splashPanel, SPLASH_CARDNAME);
        mainPanel.add(preferencesPanel, PREFS_CARDNAME);
        add(mainPanel, BorderLayout.CENTER);
        
        //initialize and add button panel
        buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEtchedBorder(Color.gray, Color.gray));
        add(buttonPanel, BorderLayout.SOUTH);
        ((CardLayout)mainPanel.getLayout()).show(mainPanel, SPLASH_CARDNAME); //sets splash as front card
        
        //add buttons
        newHotseatGameButton = new JButton("New Hotseat Game");
        newBotGameButton = new JButton("New Bot Game");
        preferencesButton = new JButton("Preferences");
        quitButton = new JButton("Quit");
        
        newHotseatGameButton.addActionListener(this);
        newBotGameButton.addActionListener(this);
        preferencesButton.addActionListener(this);
        quitButton.addActionListener(this);
        
        buttonPanel.add(newHotseatGameButton);
        buttonPanel.add(newBotGameButton);
        buttonPanel.add(preferencesButton);
        buttonPanel.add(quitButton);
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals("New Hotseat Game")){
			((CardLayout)mainPanel.getLayout()).show(mainPanel, GAME_CARDNAME);
			gamePanel.newHotseatGame();
		}
		else if(command.equals("New Bot Game")){
			((CardLayout)mainPanel.getLayout()).show(mainPanel, GAME_CARDNAME);
			gamePanel.newBotGame();
		}
		else if(command.equals("Preferences")){
			if(preferencesPanel.isVisible())
				((CardLayout)mainPanel.getLayout()).show(mainPanel, GAME_CARDNAME);
			else
				((CardLayout)mainPanel.getLayout()).show(mainPanel, PREFS_CARDNAME);
		}
		if(command.equals("Quit")){
			System.exit(0);
		}
	}

	
	public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	ChessGameSwing game = new ChessGameSwing();
            	game.setVisible(true);
            }
        });
    }
}
