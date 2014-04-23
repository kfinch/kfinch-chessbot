package chess;

import java.util.ArrayList;
import java.util.Scanner;

public class HumanPlayer implements Player {

	private Scanner input;
	
	private static String prompt = "Enter your move: ";
	private static String invalid = "That is not a legal move. Please try again.";
	
	public HumanPlayer(){
		input = new Scanner(System.in);
	}
	
	//TODO: Make not exception if given invalid format.
	public Move getMove(Board b){
		Move m = null;
		String moveOrder;
		while(true){
			System.out.println(prompt);
			moveOrder = input.nextLine();
			if(moveOrder.startsWith("pm")){
				ArrayList<Move> ml = b.generateMoves();
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
				continue;
			}
			Coordinate sc = Board.notationToCoord(moveOrder.substring(0,2));
			Coordinate ec = Board.notationToCoord(moveOrder.substring(3,5));
			if(sc == null || ec == null){//i.e. string was invalid
				System.out.println(invalid);
				continue;
			}
			m = new Move(sc.x, sc.y, ec.x, ec.y, false); //isCapture state not needed for comparison
			if(!b.isLegalMove(m))
				System.out.println(invalid);
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
