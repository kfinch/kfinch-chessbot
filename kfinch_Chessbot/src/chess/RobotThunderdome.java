package chess;

/**
 * MAKES TWO CHESSBOTS FIGHT, AND SHOWS YOU THE ACTION AS IT HAPPENS!
 * 
 * TWO ROBOTS ENTER! ONE ROBOT LEAVES!
 * TWO ROBOTS ENTER! ONE ROBOT LEAVES!
 * TWO ROBOTS ENTER! ONE ROBOT LEAVES!
 * TWO ROBOTS ENTER! ONE ROBOT LEAVES!
 * TWO ROBOTS ENTER! ONE ROBOT LEAVES!
 * TWO ROBOTS ENTER! ONE ROBOT LEAVES!
 * 
 * @author Kelton Finch
 */
public class RobotThunderdome {
	
	public static void FIGHT(RobotGladiator white, RobotGladiator black){
		GameEngine ge = new GameEngine(white, black);
		System.out.println("WELCOME TO THE ROBOT THUNDERDOME\n");
		ge.runGame();
	}
	
	public static void trialRuns(ChessBot white, ChessBot black, int runs){
		GameEngine ge = new GameEngine(white,black);
		int whiteWins = 0;
		int blackWins = 0;
		int stalemates = 0;
		int result;
		String winMessage = "";
		for(int i=0; i<runs; i++){
			System.out.println("Running game #" + i + "...");
			result = ge.runGame();
			switch(result){
			case 0 : whiteWins++; winMessage = "White wins!"; break;
			case 1 : blackWins++; winMessage = "Black wins!"; break;
			case 2 : stalemates++; winMessage = "Stalemate!"; break;
			}
			System.out.println(winMessage);
		}
		System.out.println("\nTrial run complete!");
		System.out.println("White wins: " + whiteWins);
		System.out.println("Black wins: " + blackWins);
		System.out.println("Stalemates: " + stalemates);
	}
	
}
