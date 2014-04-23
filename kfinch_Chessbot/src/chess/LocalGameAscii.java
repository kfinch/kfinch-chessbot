package chess;


/*
 * LocalGameAscii.java
 * This class manages and runs a local game of chess, printing game state info to System.out
 * Can be a hot-seat game between two human players,
 * a game vs a chessbot, or even two bots playing against one another.
 * The players are the two Players specified in runGame.
 * This class is effectively the 'server' to the two Player's clients
 */
public class LocalGameAscii {
	
	public LocalGameAscii(){}
	
	/**
	 * Runs a game of hot-seat chess.
	 * This method will check the player's requested moves to ensure they're legal.
	 * @param gameState - The game state from which to start.
	 * @param white - The white player, can be a human or bot.
	 * @param black - The black player, can be a human or bot.
	 * @return - 0 for white victory, 1 for black victory, 2 for stalemate.
	 */
	public static int runGame(Board gameState, Player white, Player black){ //TODO: Should this be static?
		Move m;
		while(true){
			System.out.println(gameState + "\n");
			if(gameState.generateMoves().isEmpty()){ //The game is over.
				if(gameState.inCheck(gameState.turn)){
					System.out.println("Checkmate!");
					if(gameState.turn == Board.WHITE){
						System.out.println("White wins!");
						return 1;
					}
					else{
						System.out.println("Black wins!");
						return 0;
					}
				}
				else{
					System.out.println("Stalemate!");
					//System.out.println("Everybody loses!");
					return 2;
				}
			}
			else if(gameState.turn == Board.WHITE){ //It's white's turn.
				do{
					m = white.getMove(gameState);
				} while(m == null || !gameState.isLegalMove(m)); //check move legality
			}
			else{ 									//It's black's turn.
				do{
					m = black.getMove(gameState);
				} while(m == null || !gameState.isLegalMove(m)); //check move legality
			}
			gameState.makeMove(m);
			System.out.println(m.toNotation());
		}
	}
	
	public int runGameTimed(Board gameState, Player white, Player black){
		return -1; //TODO: Implement, changing signature as needed to accommodate timing.
	}
	
}
