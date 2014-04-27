package chessbot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chess_backend.Board;
import chess_backend.Move;

/**
 * ABTree.java
 * A Minimax tree with alpha-beta pruning for searching the game tree.
 * This search tree is further optimized with a tranposition table and iterative deepening.
 */
class ABTree implements SearchTree {
	
	
	private int fixedDepth;
	private Move bestMove;
	private Evaluator e;
	private int checkmate;
	private int stalemate;
	
	//The switch for verbose mode, and some vars for keeping track of stats to be printed.
	private boolean verbose;
	private int posEvalCount;
	private int evalsSkipped;

	//Keeps a store of previously evaluated board positions so as to not redundantly search the same board state repeatedly.
	private Map<Board,PositionInfo> transpositionTable;
	
	public ABTree(Evaluator e){
		this.e = e;
		checkmate = e.getCheckmate();
		stalemate = e.getStalemate();
		transpositionTable = new HashMap<Board,PositionInfo>();
		verbose = false;
	}
	
	public ABTree(Evaluator e, boolean verbose){
		this(e);
		this.verbose = verbose;
	}

	/**
	 * Searches to the specified depth starting from the specified board position, and returns the best move found.
	 */
	public Move getBestMoveFixed(Board b, int depth) {
		posEvalCount = 0;
		evalsSkipped = 0;
		transpositionTable.clear(); //Table gets WAY too big if not cleared between calls.
		bestMove = null;
		
		long beginTime, endTime;
		beginTime = System.nanoTime();
		try{
			//fixedDepth = 2;
			//treeSearchRecurse(b,2,-Integer.MAX_VALUE,Integer.MAX_VALUE);
			//search is progressively deepened, with the best move from previous iterations searched first.
			for(int i=2; i<=depth; i++){
				if(verbose)
					System.out.println("Searching at depth " + i + "...");
				fixedDepth = i;
				treeSearchRecurse(b,i,-Integer.MAX_VALUE,Integer.MAX_VALUE);
				if(verbose)
					System.out.println("Current best move found: " + bestMove.toNotation());
			}
		//TODO: See if this works as intended. Furthermore, is this the best way to handle the possibility of OOM if depth too high?
		} catch (OutOfMemoryError e){
			if(verbose)
				System.out.println("Ran out of memory! Returning best working solution.");
			return bestMove;
		}
		endTime = System.nanoTime();
		
		if(verbose){
			System.out.println("Search took " + ((endTime - beginTime)/1000000000) + " seconds");
			System.out.println("Positions evaluated: " + posEvalCount);
			System.out.println("Evaluations skipped: " + evalsSkipped);
			System.out.println("TT Size: " + transpositionTable.size());
		}
		return bestMove;
	}
	
	/*
	 * Recursive helper for getBestMoveFixed. Performs an alpha-beta pruned minimax tree search.
	 * Optimizes search time via a transposition table.
	 * Modifies bestMove as a side effect.
	 */
	private int treeSearchRecurse(Board b, int depth, int alpha, int beta){
		posEvalCount++;
		
		//This comes before transposition table stuff because attempting to use 
		//transposition tables at depth 0 results in pretty immediate OOM.
		if(depth == 0)
			return e.evaluate(b);
		
		PositionInfo prevEval = transpositionTable.get(b);
		
		//if this position has been previously evaluated at at least as much depth, just use that evaluation
		if(prevEval != null && prevEval.depth >= depth){
			evalsSkipped++;
			return prevEval.evaluation;
		}
		
		List<Move> ml = b.generateMoves();
		
		if(ml.isEmpty()){ //i.e. board is in a game over position
			if(b.inCheck(b.getTurn())) //checkmate!
				return -checkmate;
			else 				  //stalemate!
				return -stalemate;
		}
		
		int curr;
		
		//If this is top call, search current best move first.
		//This produces more sensible behavior when the search must be stopped early due to running out of memory or time.
		//In addition, searching the probable best position first usually results in much more AB pruning.
		//Note that this won't cause a redundant search of bestMove in the following for loop
		//because this search's entry will have been added to the tranposition table.
		if(fixedDepth == depth && bestMove != null){
			curr = -treeSearchRecurse(b.afterMove(bestMove),depth-1,-beta,-alpha);
			if(curr > alpha)
				alpha = curr;
			//no need to set bestMove here: it already is bestMove.
		}
		
		//Recursively searches all possible moves from this position, looking for the best one.
		for(Move m : ml){
			curr = -treeSearchRecurse(b.afterMove(m),depth-1,-beta,-alpha);  
			if(curr > alpha){
				alpha = curr;
				if(fixedDepth == depth)
					bestMove = m;
			}
			if(alpha >= beta)
				break;
		}
		
		//Add the new (or better) evaluation to the transposition table.
		transpositionTable.put(b, new PositionInfo(depth, alpha));
		
		return alpha;
	}

	private class PositionInfo{
		private int depth;
		private int evaluation;
		
		private PositionInfo(int depth, int evaluation){
			this.depth = depth;
			this.evaluation = evaluation;
		}
	}
	
	public Move getBestMoveTimed(Board b) {
		return null; //TODO: Implement with new time control signature, etc.
	}

	
}

