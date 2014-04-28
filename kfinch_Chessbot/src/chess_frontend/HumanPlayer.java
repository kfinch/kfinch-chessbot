package chess_frontend;

import java.util.List;
import java.util.Scanner;

import chess_backend.Board;
import chess_backend.Coordinate;
import chess_backend.Move;

public class HumanPlayer implements Player {

	private Scanner input;
	private boolean displayedUsage;
	
	private static String usage = "To enter a move, type the position of the piece you'd like to move,\n"
								+ "followed by a space, followed by the position you'd like to move it to\n"
								+ "For example, \"e2 e4\" would be the opening where White double-pushes the king's pawn,\n"
								+ "And \"d7 d5\" would be a queen side double-push response from Black.\n"
								+ "Enter \"pb\" to reprint the board, or \"pm\" to print all legal moves from this position.\n";
	private static String prompt = "Enter your move: ";
	private static String invalid = "That is not a valid command. Please try again.";
	private static String illegal = "That is not a legal move. Please try again.";
	
	public HumanPlayer(){
		input = new Scanner(System.in);
		displayedUsage = false;
	}
	
	//TODO: Make not exception if given invalid format.
	public Move getMove(Board b){
		Move m = null;
		String moveOrder;
		while(true){
			if(!displayedUsage){
				System.out.println(usage);
				displayedUsage = true;
			}
			System.out.println(prompt);
			moveOrder = input.nextLine();
			if(moveOrder.startsWith("pm")){
				List<Move> ml = b.generateMoves();
				for(Move temp : ml){
					System.out.println(temp.toNotation());
				}
				continue;
			}
			if(moveOrder.startsWith("pb")){
				System.out.println(b);
				continue;
			}
			if(moveOrder.length() != 5){
				System.out.println(invalid);
				System.out.println(usage);
				continue;
			}
			Coordinate sc = Board.notationToCoord(moveOrder.substring(0,2));
			Coordinate ec = Board.notationToCoord(moveOrder.substring(3,5));
			if(sc == null || ec == null){//i.e. string was invalid
				System.out.println(invalid);
				System.out.println(usage);
				continue;
			}
			m = new Move(sc.x, sc.y, ec.x, ec.y, false); //isCapture state not needed for comparison
			if(!b.isLegalMove(m))
				System.out.println(illegal);
			else
				break;
		}
		return m;
	}

	public Move getMoveTimed(Board b) {
		//TODO: Implement with proper signature
		return null;
	}
	
}
