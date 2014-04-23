package chess;

public interface Evaluator {
	public int getCheckmate();
	public int getStalemate();
	public int evaluate(Board b);
}
