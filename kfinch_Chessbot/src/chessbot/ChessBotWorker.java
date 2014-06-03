package chessbot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import chess_backend.Board;
import chess_backend.Move;
import chess_swingfrontend.GamePanel;

/**
 * ChessBotWorker.java
 * 
 * This class is basically a copy pasta of ABTree.java that extends SwingWorker.
 * Unfortunately, the way I intended to set up the 'SearchTree' interface just doesn't work out when trying
 * to use SwingWorker to run a threaded version of the tree search that can be stopped at any time.
 * 
 * This class's main function is to use a minimax tree with alpha-beta pruning to searching the game tree.
 * This search tree is further optimized with a transposition table and iterative deepening.
 */
public class ChessBotWorker extends SwingWorker<Move,Move>{
	
	private GamePanel client;
	private Evaluator e; //static position evaluator used by the search
	private Board b; //game state to start search from
	private int maxDepth; //maximum depth to search to
	
	private int searchDepth; //current search depth used by iterative deepened recurse search
	private Move bestMove; //the best move found so far
	
	private int checkmate; //value the evaluator gives to checkmate
	private int stalemate; //value the evaluator gives to stalemate
	
	//The switch for verbose mode, and various stats verbose mode displays
	private boolean verbose;
	private int posEvalCount, evalsSkipped;

	//Keeps a store of previously evaluated board positions so as to not redundantly search the same board state repeatedly.
	private Map<Board,PositionInfo> transpositionTable;
	
	public ChessBotWorker(Board b, int maxDepth, Evaluator e, GamePanel client){
		this.b = b;
		this.maxDepth = maxDepth;
		this.e = e;
		this.client = client;
		checkmate = e.getCheckmate();
		stalemate = e.getStalemate();
		transpositionTable = new HashMap<Board,PositionInfo>();
		verbose = false;
	}
	
	public ChessBotWorker(Board b, int maxDepth, Evaluator e, GamePanel client, boolean verbose){
		this(b,maxDepth,e,client);
		this.verbose = verbose;
	}

	/**
	 * Searches to the specified depth starting from the specified board position, and returns the best move found.
	 */
	@Override
	public Move doInBackground() {
		posEvalCount = 0;
		evalsSkipped = 0;
		bestMove = null;
		
		long beginTime, endTime;
		beginTime = System.nanoTime();
		//TODO: Search clearly slows WAY down when nearing OOM without doing much useful work. Investigate possible fixes.
		try{
			//search is progressively deepened, with the best move from previous iterations searched first.
			for(int i=2; i<=maxDepth; i++){
				if(verbose)
					System.out.println("Searching at depth " + i + "...");
				searchDepth = i;
				treeSearchRecurse(b, i, -Integer.MAX_VALUE, Integer.MAX_VALUE);
				if(verbose)
					System.out.println("Current best move found: " + bestMove.toNotation());
			}
		//if we OOM during a tree search, just return our best working solution.
		} catch (OutOfMemoryError e){
			if(verbose)
				System.out.println("Ran out of memory! Returning best working solution.");
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
	 * Recursive helper for doInBackground. Performs an alpha-beta pruned minimax tree search.
	 * Optimizes search time via a transposition table.
	 * Modifies bestMove as a side effect.
	 */
	private int treeSearchRecurse(Board b, int depth, int alpha, int beta){
		posEvalCount++;
		publish(bestMove); //constantly updates publish with current best move TODO: too frequently?
		
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
		if(searchDepth == depth && bestMove != null){
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
				if(searchDepth == depth)
					bestMove = m;
			}
			if(alpha >= beta)
				break;
		}
		
		//Add the new (or deeper) evaluation to the transposition table.
		transpositionTable.put(b, new PositionInfo(depth, alpha));
		
		return alpha;
	}
	
	@Override
	public void process(List<Move> moves){
		client.updateBestMove(moves.get(moves.size()-1));
	}
	

	/*
	 * This class is used as a glorified struct for the transposition table.
	 * The transposition table is a Map with Key -> Board and Value -> PositionInfo.
	 * 'depth' is the depth at which the board-key was evaluated,
	 * and 'evaluation' is the resulting strength valuation of the board-key at that depth.
	 */
	private class PositionInfo{
		private int depth;
		private int evaluation;
		
		private PositionInfo(int depth, int evaluation){
			this.depth = depth;
			this.evaluation = evaluation;
		}
	}
}
