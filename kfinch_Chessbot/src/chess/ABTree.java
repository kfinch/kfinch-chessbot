package chess;

import java.util.*;

/*
 * ABTree.java
 * An Alpha-Beta tree used by the bot to calculate best move
 */

class ABTree implements SearchTree {
	
	
	private int fixedDepth;
	private Move bestMove;
	private Evaluator e;
	private int checkmate;
	private int stalemate;

	public ABTree(Evaluator e){
		this.e = e;
		checkmate = e.getCheckmate();
		stalemate = e.getStalemate();
	}

	public Move getBestMoveFixed(Board b, int depth) {
		fixedDepth = depth;
		getBestMoveFixedRecurse(b,depth,-Integer.MAX_VALUE,Integer.MAX_VALUE);
		return bestMove;
	}
	
	private int getBestMoveFixedRecurse(Board b, int depth, int alpha, int beta){
		if(depth == 0)
			return e.evaluate(b);
		
		int curr;
		ArrayList<Move> ml = b.generateMoves();
		
		if(ml.isEmpty()){ //i.e. board is in a game over position
			if(b.inCheck(b.turn)) //checkmate!
				return -checkmate;
			else 				  //stalemate!
				return -stalemate;
		}
		for(Move m : ml){
			curr = -getBestMoveFixedRecurse(b.afterMove(m),depth-1,-beta,-alpha);  
			if(curr > alpha){
				alpha = curr;
				if(fixedDepth == depth)
					bestMove = m;
			}
			if(alpha >= beta)
				break;
		}
		return alpha;
	}

	public Move getBestMoveTimed(Board b) {
		return null; //TODO: Implement with new time control signature, etc.
	}

	
}

