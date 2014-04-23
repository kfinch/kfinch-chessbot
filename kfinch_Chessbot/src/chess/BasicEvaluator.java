package chess;

/**
 * This class provides an evaluation method that returns an estimate of the
 * static strength of a given board position. Evaluation function should do no tree searching,
 * and should run as quickly as possible.
 * @author Kelton Finch
 */
public class BasicEvaluator implements Evaluator {
	
	//The material values of the different pieces.
	//Numbers used copied from Prof. Danny Sleator's suggested starting values.
	private static final int PAWN_VALUE = 100;
	private static final int KNIGHT_VALUE = 300;
	private static final int BISHOP_VALUE = 300;
	private static final int ROOK_VALUE = 500;
	private static final int QUEEN_VALUE = 900;
	private static final int KING_VALUE = 0;
	private static final int CASTLE_VALUE = 50; //keep using this?
	
	private static final int CHECKMATE = 1000000;
	private static final int STALEMATE = 0;

	//The positional bonuses for different pieces.
	//Numbers used copied from Prof. Danny Sleator's suggested starting values.
	private static final int[][] PAWN_POS_VALUE =
		{ {  0,  8,  4,  0,  0,  0,  0,  0 },
		  {  0, 10,  8,  6,  4,  2,  0,  0 },
		  {  0, 15, 12,  9,  6,  3,  0,  0 },
		  {  0, 20, 16, 10, 10,  4, -5,  0 },
		  {  0, 20, 16, 10, 10,  4, -5,  0 },
		  {  0, 15, 12,  9,  6,  3,  0,  0 },
		  {  0, 10,  8,  6,  4,  2,  0,  0 },
		  {  0,  8,  4,  0,  0,  0,  0,  0 } };

	private static final int[][] KNIGHT_POS_VALUE =
		{ {-10, -8, -8, -8, -8, -8, -8,-10 },
		  { -5,  0,  0,  0,  0,  0,  0, -5 },
		  { -5,  0, 10,  8,  8, 10,  0, -5 },
		  { -5,  3,  8, 10, 10,  8,  3, -5 },
		  { -5,  3,  8, 10, 10,  8,  3, -5 },
		  { -5,  0, 10,  8,  8, 10,  0, -5 },
		  { -5,  0,  0,  0,  0,  0,  0, -5 },
		  {-10, -8, -8, -8, -8, -8, -8,-10 } };

	private static final int[][] BISHOP_POS_VALUE =
		{ { -5, -5, -5, -5, -5, -5, -5, -5 },
		  { -5, 10,  5,  3,  3,  5, 10, -5 },
		  { -5,  5,  3, 10, 10,  3,  5, -5 },
		  { -5,  8,  8,  3,  3,  8,  8, -5 },
		  { -5,  8,  8,  3,  3,  8,  8, -5 },
		  { -5,  5,  3, 10, 10,  3,  5, -5 },
		  { -5, 10,  5,  3,  3,  5, 10, -5 },
		  { -5, -5, -5, -5, -5, -5, -5, -5 } };

	private int randomnessStrength;
	private boolean usingRandomness;
	
	public BasicEvaluator(){
		randomnessStrength = 0;
		usingRandomness = false;
	}
	
	/**
	 * Creates an evaluator with a degree of randomness to its calculations.
	 * Useful for creating a bot that won't do the same thing every time.
	 * @param randomnessStrength - A random amount between 0 and this value will be added to each evaluate()
	 */
	public BasicEvaluator(int randomnessStrength){
		this.randomnessStrength = randomnessStrength;
		usingRandomness = true;
	}
	
	
	
	/** Evaluates the material and rough positional strength of a position.
	 *  Positive means better for the active player, negative means better for other player.
	 */
	public int evaluate(Board board) {
		int result = 0;
		byte curr;
		int val = 0;

		// Favor castled positions
		if(board.hasCastled[board.turn])
			result += CASTLE_VALUE;
		if(board.hasCastled[board.turn == Board.BLACK ? Board.WHITE : Board.BLACK])
			result -= CASTLE_VALUE;

		for(int x=0; x<8; x++){
			for(int y=0; y<8; y++){
				curr = board.board[x][y];
				if(Board.isEmpty(curr))
					continue;
				switch(Board.pieceOf(curr)){
				case Board.PAWN:
					val = PAWN_VALUE + PAWN_POS_VALUE[x][y];
					break;
				case Board.KNIGHT:
					val = KNIGHT_VALUE + KNIGHT_POS_VALUE[x][y];
					break;
				case Board.BISHOP:
					val = BISHOP_VALUE + BISHOP_POS_VALUE[x][y];
					break;
				case Board.ROOK:
					val = ROOK_VALUE;
					break;
				case Board.QUEEN:
					val = QUEEN_VALUE;
					break;
				case Board.KING:
					val = KING_VALUE;
					break;
				}
				if(Board.colorOf(curr) != board.turn)
					val *= -1;
				result += val;
			}
		}
		
		if(usingRandomness)
			result += (int)(Math.random()*randomnessStrength);
		
		return result;
	}
	
	public int getCheckmate(){
		return CHECKMATE;
	}
	
	public int getStalemate(){
		return STALEMATE;
	}
}
