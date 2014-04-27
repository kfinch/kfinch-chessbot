package chess_frontend;

import java.util.Scanner;

import chess_backend.Board;
import chessbot.ChessBot;


/**
 * A menu / frontend for a game of chess.
 * 
 * @author Kelton Finch
 */
//TODO: Make the code for this UI less hideous / more extensible
//Is doing that even worthwhile for an ascii UI? Perhaps this is good enough for until I build a real GUI.
public class FrontendAscii {

	public static String prompt = "Choose an option:\n";
	
	public static String menu = "1 : Play an untimed 'hotseat' game between two human players\n"
							  + "2 : Play a timed 'hotseat' game between two human players\n"
							  + "3 : Play an untimed game against the chessbot\n"
							  + "4 : Play a timed game against the chessbot\n"
							  + "5 : Exit\n";
	
	public static String invalid = "That is not a valid option\n";
	
	public static void main(String args[]){
		int option = 0;
		Scanner input = new Scanner(System.in);
		while(option != 5){ //main menu loop
			System.out.println(menu + prompt);
			option = input.nextInt();
			switch(option){
			case 1 : launchUntimedHotseatGame(); break;
			case 2 : launchTimedHotseatGame(); break;
			case 3 : launchUntimedBotGame(); break;
			case 4 : launchTimedBotGame(); break;
			case 5 : System.out.println("Thanks for playing!\n\n");
			default : System.out.println(invalid); break;
			}
		}
	}
	
	public static void launchUntimedHotseatGame(){
		LocalGameAscii.runGame(new Board(), new HumanPlayer(), new HumanPlayer());
	}
	
	public static void launchTimedHotseatGame(){
		//TODO: Implement timing apparatus
		System.out.println("Not yet implemented! Sorry )=\n\n");
	}
	
	public static void launchUntimedBotGame(){
		Scanner input = new Scanner(System.in);
		
		int botStrength;
		System.out.println("How strong would you like the bot to be?\n"
						 + "Choose between 2 and 6 ply. Odd number plys sometimes act a bit weird.\n"
						 + "Machines with less memory may take some time to calculate at 6 ply.\n");
		botStrength = input.nextInt();
		while(botStrength < 2 || botStrength > 6){
			System.out.println(invalid);
			System.out.println("Enter a strength between 2 and 6 (inclusive)");
			botStrength = input.nextInt();
		}
		
		System.out.println("Would you like the bot to be verbose? (y) or (n)");
		char verboseOption = input.next().charAt(0);
		while(verboseOption != 'y' && verboseOption != 'n'){
			System.out.println(invalid);
			System.out.println("Enter 'y' or 'n'");
			verboseOption = input.next().charAt(0);
		}
		
		System.out.println("Would you like to play as White (w) or Black (b)?");
		char colorOption = input.next().charAt(0);
		while(colorOption != 'w' && colorOption != 'b'){
			System.out.println(invalid);
			System.out.println("Enter 'w' or 'b'");
			colorOption = input.next().charAt(0);
		}
		
		boolean botVerbose = true;
		if(verboseOption == 'n')
			botVerbose = false;
		
		if(colorOption == 'w'){
			LocalGameAscii.runGame(new Board(), new HumanPlayer(), new ChessBot(botStrength,botVerbose));
		}
		else{
			LocalGameAscii.runGame(new Board(), new ChessBot(botStrength,botVerbose), new HumanPlayer());
		}
		
	}
	
	public static void launchTimedBotGame(){
		//TODO: Implement timing apparatus
		System.out.println("Not yet implemented! Sorry )=\n\n");
	}
	
	
}
