package chess_frontend;

import chess_backend.Board;
import chess_backend.Move;

/**
 * Interface representing a player in a chess game.
 * @author Kelton Finch
 *
 */
public interface Player {
	public Move getMove(Board b);
	public Move getMoveTimed(Board b); //TODO: change signature / implement when I figure out what I need for this
}
