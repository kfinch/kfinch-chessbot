package chessbot;

import chess_backend.Board;

public interface Evaluator {
	public int getCheckmate();
	public int getStalemate();
	public int evaluate(Board b);
}
