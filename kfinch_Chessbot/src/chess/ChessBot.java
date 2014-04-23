package chess;

/*
 * ChessBot.java
 * The chess bot itself. Given a board as input and its internal AB Tree, calculates the best move. 
 */

public class ChessBot implements Player{
	
	private int fixedDepth;
	
	private Evaluator evaluator;
	private SearchTree searchTree;
	
	//public ChessBot(int fixedDepth, int captureDepth, int randomness){
	//	evaluator = new BasicEvaluator(randomness);
	//	searchTree = new MinimaxTree(evaluator, captureDepth);
	//	this.fixedDepth = fixedDepth;
	//}
	
	public ChessBot(){
		evaluator = new BasicEvaluator();
		searchTree = new ABTree(evaluator);
		fixedDepth = 6;
	}
	
	public ChessBot(int randomness){
		evaluator = new BasicEvaluator(randomness);
		searchTree = new ABTree(evaluator);
		fixedDepth = 4;
	}

	public Move getMove(Board b) {
		return searchTree.getBestMoveFixed(b, fixedDepth);
	}

	public void opponentsMove(Move m){};


}
