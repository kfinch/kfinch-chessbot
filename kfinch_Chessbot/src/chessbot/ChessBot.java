package chessbot;

import chess_backend.Board;
import chess_backend.Move;
import chess_frontend.Player;

/*
 * ChessBot.java
 * The chess bot itself. Given a board as input and its internal AB Tree, calculates the best move. 
 */
// this isn't actually used by the swing front end, I may end up discarding it entirely
public class ChessBot implements Player{
	
	private int fixedDepth;
	
	private Evaluator evaluator;
	private SearchTree searchTree;
	
	public ChessBot(){
		evaluator = new BasicEvaluator();
		searchTree = new ABTree(evaluator);
		fixedDepth = 6;
	}
	
	public ChessBot(int fixedDepth, boolean verbose){
		evaluator = new BasicEvaluator();
		searchTree = new ABTree(evaluator, verbose);
		this.fixedDepth = fixedDepth;
	}

	public Move getMove(Board b) {
		return searchTree.getBestMoveFixed(b, fixedDepth);
	}

	public Move getMoveTimed(Board b){
		return null; //TODO: Implement
	}


}
