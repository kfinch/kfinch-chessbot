package chess;

import java.util.List;

class Tester {

	private static byte wp = Board.makeSquare(Board.WHITE, Board.PAWN);
	private static byte wn = Board.makeSquare(Board.WHITE, Board.KNIGHT);
	private static byte wb = Board.makeSquare(Board.WHITE, Board.BISHOP);
	private static byte wr = Board.makeSquare(Board.WHITE, Board.ROOK);
	private static byte wq = Board.makeSquare(Board.WHITE, Board.QUEEN);
	private static byte wk = Board.makeSquare(Board.WHITE, Board.KING);
	private static byte bp = Board.makeSquare(Board.BLACK, Board.PAWN);
	private static byte bn = Board.makeSquare(Board.BLACK, Board.KNIGHT);
	private static byte bb = Board.makeSquare(Board.BLACK, Board.BISHOP);
	private static byte br = Board.makeSquare(Board.BLACK, Board.ROOK);
	private static byte bq = Board.makeSquare(Board.BLACK, Board.QUEEN);
	private static byte bk = Board.makeSquare(Board.BLACK, Board.KING);
	private static byte e =  Board.EMPTY;

	private static Board board0 = new Board();
	
	private static byte board1Arr[][] = 
		{ {e , e , e , e , e , e , e , bk},
		  {e , e , e , wk, e , e , e , e },
		  {e , e , e , e , e , e , e , e },
		  {e , e , bn, e , bp, e , br, e },
		  {e , e , e , e , e , e , e , e },
		  {e , bp, e , e , wq, e , bq, e },
		  {e , e , e , e , e , e , e , e },
		  {e , e , wn, e , e , e , e , e } };
	private static boolean board1cancastle[] = {false, false};
	private static boolean board1hascastled[] = {false, false};
	private static int board1kingx[] = {1,0};
	private static int board1kingy[] = {3,7};
	
	private static Board board1 = new Board(board1Arr, (byte)-1, Board.WHITE, board1cancastle,
											board1cancastle, board1hascastled, board1kingx, board1kingy);

	public static void main(String args[]){
		test4();
	}

	private static void test0(){
		byte boop = Board.makeSquare(Board.BLACK, Board.BISHOP);
		System.out.println(Board.colorOf(boop));
		System.out.println(Board.pieceOf(boop));
	}
	
	private static void test1(){
		System.out.println(board0.toString());
		board0.makeMove(new Move(4,1,4,3,false));
		System.out.println(board0.toString());
	}
	
	private static void test2(){
		Board b = new Board(board0);
		b.makeMove(new Move(4,1,4,3,false));
		b.makeMove(new Move(4,6,4,4,false));
		List<Move> ml = b.generateMoves();
		Board temp;
		for(Move m : ml){
			temp = new Board(b);
			temp.makeMove(m);
			System.out.println(m.toString());
			System.out.println(temp.toString());
		}
	}
	
	private static void test3(){
		System.out.println("Generating moves...\n");
		List<Move> ml = board1.generateMoves();
		Move[] ma = new Move[ml.size()];
		ml.toArray(ma);
		Board temp;
		for(int i=0; i<ma.length; i++){
			temp = new Board(board1);
			temp.makeMove(ma[i]);
			System.out.println(ma[i].toString());
			System.out.println(temp.toString());
		}
	}
	
	private static void test4(){
		LocalGameAscii.runGame(new Board(), new HumanPlayer(), new ChessBot());
	}
}
