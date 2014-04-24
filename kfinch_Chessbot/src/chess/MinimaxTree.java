package chess;

import java.util.List;

/**
 * A minimax tree for searching the chess game tree for the best move. Allows for variable ply.
 * Does NOT implement Alpha Beta pruning, this class is primarily for testing purposes.
 * 
 * @author Kelton Finch
 */
public class MinimaxTree implements SearchTree {
	
	private Evaluator e;
	private Move bestMove;
	private int fixedDepth;
	private int checkmate;
	private int stalemate;
	
	private int captureDepth;
	
	/**
	 * A minimax tree for searching the game tree.
	 * @param e - The evaluator function to be used on leaf nodes.
	 */
	public MinimaxTree(Evaluator e){
		this.e = e;
		checkmate = e.getCheckmate();
		stalemate = e.getStalemate();
		captureDepth = 0;
	}
	
	/**
	 * A minimax tree for searching the game tree.
	 * @param e - The evaluator function to be used on leaf nodes.
	 * @param captureDepth - Additional depth to be searched beyond what is specified,
	 * 						 looking only at capture moves (and check moves).
	 */
	public MinimaxTree(Evaluator e, int captureDepth){
		this(e);
		this.captureDepth = captureDepth;
	}
	
	/**
	 * Given a board and a fixed depth to search to, searches the game tree to that depth
	 * and then returns the best move found.
	 */
	public Move getBestMoveFixed(Board b, int depth){
		fixedDepth = depth + captureDepth;
		getBestMoveRecurse(b,depth + captureDepth);
		return bestMove; // /rainbow side-effects!
	}
	
	/*
	 * This method is recursively called to perform a minimax tree search.
	 * Modifications to bestMove are a side effect of this method call.
	 */
	private int getBestMoveRecurse(Board b, int depth){
		if(depth == 0)
			return e.evaluate(b);
		
		int best = -Integer.MAX_VALUE;
		int curr;
		List<Move> ml = b.generateMoves();
		
		if(ml.isEmpty()){ //i.e. board is in a game over position
			if(b.inCheck(b.turn)) //checkmate!
				return -checkmate;
			else 				  //stalemate!
				return -stalemate;
		}
		for(Move m : ml){
			if(depth - captureDepth <= 0 && !m.isCapture)
				continue;
			curr = -getBestMoveRecurse(b.afterMove(m),depth-1);  
			if(curr > best){
				best = curr;
				if(fixedDepth == depth)
					bestMove = m;
			}
		}
		if(best == -Integer.MAX_VALUE)
			best = e.evaluate(b);
		return best;
	}
	
	public Move getBestMoveTimed(Board b){return null;} //TODO: Implement
	
}
