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
		int sx, sy, ex, ey;
		System.out.println(b);
		while(true){
			System.out.println(prompt);
			moveOrder = input.nextLine();
			if(moveOrder.startsWith("pm")){
				ArrayList<Move> ml = b.generateMoves();
				for(Move temp : ml){
					System.out.println(temp);
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
			sx = charToCoord(moveOrder.charAt(0));
			sy = charToCoord(moveOrder.charAt(1));
			ex = charToCoord(moveOrder.charAt(3));
			ey = charToCoord(moveOrder.charAt(4));
			m = new Move(sx, sy, ex, ey, false); //isCapture state not needed for comparison
			if(!b.isLegalMove(m))
				System.out.println(invalid);
			else
				break;
		}
		System.out.println("\n" + b.afterMove(m));
		return m;
	}
	
	public void opponentsMove(Move m){
		System.out.println("Opponent's move: " + moveToNotation(m) + "\n");
	}
	
	private int charToCoord(char c){
		switch(c){
		case '1' : return 0;
		case '2' : return 1;
		case '3' : return 2;
		case '4' : return 3;
		case '5' : return 4;
		case '6' : return 5;
		case '7' : return 6;
		case '8' : return 7;
		case 'a' : return 0;
		case 'b' : return 1;
		case 'c' : return 2;
		case 'd' : return 3;
		case 'e' : return 4;
		case 'f' : return 5;
		case 'g' : return 6;
		case 'h' : return 7;
		default  : return -1;
		}
	}
	
	private String moveToNotation(Move m){
		String result = "";
		result += coordToChar(m.sx)
		        + Integer.toString(m.sy+1)
		        + " -> "
		        + coordToChar(m.ex)
		        + Integer.toString(m.ey+1);
		if(m.isCapture)
			result += " *";
		return result;
	}
	
	private char coordToChar(int i){
		switch(i){
		case 0 : return 'a';
		case 1 : return 'b';
		case 2 : return 'c';
		case 3 : return 'd';
		case 4 : return 'e';
		case 5 : return 'f';
		case 6 : return 'g';
		case 7 : return 'h';
		default : return '!';
		}
	}
	
}
