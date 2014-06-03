package chess_swingfrontend;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.Timer;

import chess_backend.Board;
import chess_backend.Move;
import chessbot.BasicEvaluator;
import chessbot.ChessBotWorker;

/**
 * GamePanel.java
 * 
 * The main panel for displaying a game of chess and associated information.
 * TODO: Features to implement: 
 * - Controlling options via the preferences panel
 * - Bot with hard cap on turn time
 * - Support for a timed game
 * - Legal move highlighting when dragging a piece
 * - 'bot ponder' highlighting
 * - Improved sidebar layout
 * - 'Piece Graveyard'
 * - Square coordinate letters and numbers along edge of board
 * 
 * 
 * @author Kelton Finch
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, ActionListener {

	protected static final Color WHITE_SQUARE_COLOR = Color.decode("#999999"); //color of white squares on chess board
	protected static final Color BLACK_SQUARE_COLOR = Color.decode("#444444"); //color of black squares on chess board
	protected static final Color PREVMOVE_SQUARE_COLOR = Color.decode("#774444"); //color of highlighted previous move
	protected static final Color WHITE_PIECE_COLOR = Color.decode("#ffffff"); //color of white pieces
	protected static final Color BLACK_PIECE_COLOR = Color.decode("#000000"); //color of black pieces
	
	private static final int EDGE_MARGIN = 15;
	
	private static final String WHITE_VICTORY_MESSAGE = "Checkmate! White wins!";
	private static final String BLACK_VICTORY_MESSAGE = "Checkmate! Black wins!";
	private static final String STALEMATE_MESSAGE = "Stalemate! Everybody loses!";
	private static final String WHITES_TURN_MESSAGE = "White to play";
	private static final String BLACKS_TURN_MESSAGE = "Black to play";
	
	private ChessGameSwing parent;
	
	private ChessBotWorker chessBot; //the worker thread that will run bot computations
	private Move bestMove; //best move found by bot (so far)
	private int botSearchDepth; //maximum depth to which the bot will search (set in prefs)
	private int botSearchTime; //maximum time the bot will search for (in milliseconds) (set in prefs)
	
	private Timer turnTimer;
	
	private List<Board> gameHistory; //an ordered list of positions the game has been in. Last element is the current game state.
	private List<Move> moveHistory; //an ordered list of moves taken this game. Last element is the most recent move. 
	
	private boolean isStarted; //true if there is a game running
	private boolean isBotGame; //is the bot playing in the current game
	private byte botColor; //color the bot is playing as, if it's playing
	
	private int panelWidth; //width of panel in pixels
	private int panelHeight; //height of panel in pixels
	
	private int sidebarVertical; //pixels from the left to display side bar elements
	private int timerHorizontal; //pixels from the top to display the timers
	private int statusHorizontal; //pixels from the top to display the status
	private int buttonsHorizontal; //pixels from the top to display the buttons
	
	private String turnStatus; //string to be displayed on turn status bar
	private String additionalStatusInfo; //additional info to be displayed below the turn status bar
	
	private BoardPanel boardPanel;
	
	public GamePanel(ChessGameSwing parent){
		//JLabel message = new JLabel("Game goes here LOL");
		//add(message, BorderLayout.CENTER);
		addMouseListener(this);
		addMouseMotionListener(this);
		addComponentListener(this);
		
		this.parent = parent;
		
		botSearchDepth = 5; //TODO: make this prefs controlled
		botSearchTime = 5*1000; //TODO: make this prefs controlled
		botColor = Board.BLACK; //TODO: make this prefs controlled
		
		boardPanel = new BoardPanel(this);
		add(boardPanel);
		
		gameHistory = new ArrayList<Board>();
		moveHistory = new ArrayList<Move>();
		isStarted = false;
		
		turnStatus = "";
		additionalStatusInfo = "";
		
		updateDimensions();
		repaint();
	}
	
	/**
	 * Gets the current game state.
	 * Will return null if there is no game state.
	 */
	protected Board getGameState(){
		if(gameHistory.isEmpty())
			return null;
		return gameHistory.get(gameHistory.size()-1);
	}
	
	/**
	 * Gets the most recent move.
	 * Will return null if there is no previous move.
	 */
	protected Move getPrevMove(){
		if(moveHistory.isEmpty())
			return null;
		return moveHistory.get(moveHistory.size()-1);
	}
	
	/**
	 * Called when user input or bot input orders a move.
	 * Tests for move legality, and ignores bad coordinates on the move.
	 * @param m The move requested
	 */
	protected void requestMove(Move m){
		Board gameState = getGameState();
		
		if(gameState != null && gameState.isLegalMove(m)){
			makeMove(m);
			updatePanelState();
		}
	}
	
	/**
	 * Executes the specified move on the current game state. Does not check for move legality.
	 * 
	 * @param m The move to be executed
	 */
	protected void makeMove(Move m){
		Board currPos = getGameState();
		Board newPos = currPos.afterMove(m);
		
		gameHistory.add(newPos);
		moveHistory.add(m);
		
		repaint();
	}
	
	/**
	 * Checks game state and updates sidebar info accordingly.
	 * Also orders the bot to move, if needed.
	 */
	protected void updatePanelState(){
		Board gameState = getGameState();
		byte turn = gameState.getTurn();
		
		//update turn status
		if(gameState.generateMoves().size() == 0){
			if(gameState.inCheck(turn)){
				if(turn == Board.WHITE)
					turnStatus = BLACK_VICTORY_MESSAGE;
				else
					turnStatus = WHITE_VICTORY_MESSAGE;
			}
			else
				turnStatus = STALEMATE_MESSAGE;
			isStarted = false; //game over man, game over!
		}
		else{
			if(turn == Board.WHITE)
				turnStatus = WHITES_TURN_MESSAGE;
			else
				turnStatus = BLACKS_TURN_MESSAGE;
		}
		
		if(gameState.inCheck(turn))
			additionalStatusInfo = "Check!";
		else
			additionalStatusInfo = "";
		
		repaint();
		
		if(isStarted && isBotGame && turn == botColor)
			requestBotMove();
	}
	
	/**
	 * Requests the bot begin calculating its move (in a worker thread).
	 */
	protected void requestBotMove(){
		//TODO: Implement timed moves, clean this up in general.
		chessBot = new ChessBotWorker(getGameState(), botSearchDepth, new BasicEvaluator(), this, true);
		chessBot.execute();
		
		turnTimer = new Timer(botSearchTime, this);
		turnTimer.setRepeats(false);
		turnTimer.start();
		
		repaint();
	}
	
	public void updateBestMove(Move m){
		bestMove = m;
		if(bestMove != null && getGameState().getTurn() == botColor){
			additionalStatusInfo = "Bot is pondering " + bestMove.toNotation() + "...";
			repaint();
		}
	}
	
	/**
	 * Retrieves the move generated by the chessbot and then executes it (if it's legal)
	 */
	protected void finishBotMove(){
		try {
			requestMove(chessBot.get());
		}
		catch (InterruptedException e) {
			System.err.println("InterruptedException: " + e.getMessage());
		}
		catch (ExecutionException e) {
			System.err.println("ExecutionException: " + e.getMessage());
		}
	}
	
	/**
	 * Resets current game, if relevant, and starts a new hotseat game of chess
	 * using the current preferences.
	 */
	protected void newHotseatGame(){
		setStartingPosition();
		isStarted = true;
		isBotGame = false;
	}
	
	/**
	 * Resets current game, if relevant, and starts a new game of chess vs the bot
	 * using the current preferences.
	 */
	protected void newBotGame(){
		setStartingPosition();
		isStarted = true;
		isBotGame = true;
		updatePanelState();
	}

	private void setStartingPosition(){
		gameHistory = new ArrayList<Board>();
		moveHistory = new ArrayList<Move>();
		Board startingPos = new Board();
		gameHistory.add(startingPos);
		repaint();
	}
	
	/*
	 * Updates size and locations of displayed elements based on size of allotted window.
	 */
	protected void updateDimensions(){
		panelWidth = getSize().width;
		panelHeight = getSize().height;
		
		boardPanel.setBounds(EDGE_MARGIN, EDGE_MARGIN, panelHeight-2*EDGE_MARGIN, panelHeight-2*EDGE_MARGIN);
		
		sidebarVertical = panelHeight;
		//System.out.println(panelWidth + " " + panelHeight);
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		doDrawing(g);
	}
	
	private void doDrawing(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		
		updateDimensions();
		
		if(gameHistory.isEmpty()) //if there's no game to display, we're done here
			return;
		
		Board gameState = getGameState();
		
		//Draw turn status and additional info TODO: formalize location of this info
		g2d.setFont(new Font("Sans_Serif", Font.BOLD, 18));
		g2d.setColor(Color.black);
		g2d.drawString(turnStatus, sidebarVertical, panelHeight/2);
		g2d.drawString(additionalStatusInfo, sidebarVertical, panelHeight/2 + 30);
		
		
		//TODO: draw other elements
	}
	

	
	
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}

	public void componentResized(ComponentEvent e) {
		updateDimensions();
		repaint();
	}

	public void componentShown(ComponentEvent e) {}

	public void actionPerformed(ActionEvent e) {
		finishBotMove();
	}
	
}
