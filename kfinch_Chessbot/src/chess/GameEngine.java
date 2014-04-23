package chess;


/*
 * GameEngine.java
 * This class manages and runs a game of chess.
 * The players are the two Players specified in the constructor,
 * this class is effectively the 'server' to the two Player's clients
 */
//TODO: Make this entire class not shit
public class GameEngine {
	
	private Board gameState;
	private Player white, black;
	
	public GameEngine(Player white, Player black){
		this.white = white;
		this.black = black;
		gameState = new Board();
	}
	
	/* Begin a game from a non default position */
	public GameEngine(Player white, Player black, Board gameState){
		this.white = white;
		this.black = black;
		this.gameState = gameState;
	}
	
	/**
	 * Runs the chess game, starting from gameState.
	 * If either player submits an invalid move, this method will simply ask it again.
	 * @return - 0 for white victory, 1 for black victory, 2 for stalemate
	 */
	public int runGame(){
		Move m;
		while(true){
			if(gameState.generateMoves().isEmpty()){ //The game is over.
				if(gameState.inCheck(gameState.turn)){
					System.out.println(gameState + "\n");
					System.out.println("Checkmate!");
					if(gameState.turn == Board.WHITE)
						return 1;
					else
						return 0;
				}
				else{
					System.out.println(gameState + "\n");
					System.out.println("Stalemate!");
					return 2;
				}
			}
			else if(gameState.turn == Board.WHITE){ //It's white's turn.
				do{
					m = white.getMove(gameState);
				} while(m == null || !gameState.isLegalMove(m));
				black.opponentsMove(m);
				gameState.makeMove(m);
			}
			else{ //It's black's turn.
				do{
					m = black.getMove(gameState);
				} while(m == null || !gameState.isLegalMove(m));
				white.opponentsMove(m);
				gameState.makeMove(m);
			}
		}
	}
	
}
