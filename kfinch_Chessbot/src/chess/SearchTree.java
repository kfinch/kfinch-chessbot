package chess;

public interface SearchTree {
	public Move getBestMoveFixed(Board b, int depth);
	public Move getBestMoveTimed(Board b); //TODO: Change signature once I know best way to implement
}
