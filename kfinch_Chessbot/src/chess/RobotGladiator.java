package chess;

public class RobotGladiator extends ChessBot {
	
	public RobotGladiator(){
		super();
	}
	
	public Move getMove(Board b){
		System.out.println(b);
		Move result = super.getMove(b);
		System.out.println(result);
		return result;
	}
}
