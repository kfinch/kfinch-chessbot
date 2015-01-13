package chess_backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Board.java
 * Stores a complete board position, including whose turn it is, state of
 * castling and state of enpassant.
 */

public class Board {
	
	// The board, and the pieces on it. Color and piece packed into byte.
	private byte[][] board = new byte[8][8];

	// Marks the column of last turn's 2 space pawn push, or -1 if there wasn't one.
	private int previousDoublePush;
	
	 // Which player's turn it is.
	private byte turn;

	// Remembers if a castle is still possible on the kingside or queenside.
	// ex: kingsideCastle[WHITE] == true iff white can still castle on the kingside.
	private boolean[] kingsideCastle = new boolean[2], queensideCastle = new boolean[2];
	
	// Remembers if either player has castled.
	// ex. hasCastled[WHITE] == true iff white has castled.
	private boolean[] hasCastled = new boolean[2];

	// Redundantly stores the king positions for faster check calculations.
	private int[] kingx = new int[2];
	private int[] kingy = new int[2];
	
	// This object's hash value. Methods that modify this object's data should also update the hash properly.
	private int hash;

	public static final byte EMPTY = 0; //empty squares will always be 0x00
	public static final byte PAWN = 1;
	public static final byte KNIGHT = 2;
	public static final byte BISHOP = 3;
	public static final byte ROOK = 4;
	public static final byte QUEEN = 5;
	public static final byte KING = 6;

	public static final byte WHITE = 0;
	public static final byte BLACK = 1;

	public static final int[] KNIGHT_MOVES = {2,1,1,2,2,-1,1,-2,-2,1,-1,2,-2,-1,-1,-2};
	public static final int[] DIAGONAL_MOVES = {1,1,1,-1,-1,1,-1,-1};
	public static final int[] LINE_MOVES = {1,0,0,1,-1,0,0,-1};

	/* Each piece in each position should have a different arbitrary int value in order
	 * to avoid hash collision. The value for each piece is xor'd together to form
	 * the hash code. In addition, when it is black's turn, the hash value is xor'd with
	 * 0xCCCCCCCC
	 */
	private static int[] zobrist = new int[16 * 64];
	
	static {
		Random r = new Random(1337); //what's the worst that could happen?
		for (int i = 0; i < zobrist.length; i++)
			zobrist[i] = r.nextInt();
	}

	public static byte colorOf(byte x) { //returns the 'color' portion of a piece's byte packing
		return (byte) (x >> 3);
	}

	public static byte pieceOf(byte x) { //returns the 'piece type' portion of a piece's byte packing
		return (byte) (x & 7);
	}

	public static boolean isEmpty(byte x) { //returns true iff the given byte represents an empty square.
		return x == EMPTY;
	}

	public static byte makeSquare(byte c, byte p) { //returns the byte packing of a given color and piece type
		return (byte) (p | (c << 3));
	}
	
	/**
	 * A little helper method for converting a pair of coordinates into chess notation.
	 * For example: coordToNotation(new Coordinate(4,1)) = "e2"
	 * Returns null if given invalid coordinates.
	 */
	public static String coordToNotation(Coordinate c){
		if(c.x > 7 || c.x < 0 || c.y > 7 || c.y < 0)
			return null;
		String result = "";
		switch(c.x){
		case 0 : result += "a"; break;
		case 1 : result += "b"; break;
		case 2 : result += "c"; break;
		case 3 : result += "d"; break;
		case 4 : result += "e"; break;
		case 5 : result += "f"; break;
		case 6 : result += "g"; break;
		case 7 : result += "h"; break;
		}
		result += (c.y+1);
		return result;
	}
	
	/**
	 * A little helper method for converting chess notation into coordinates on the board's representation.
	 * For example: notationToCoord("e2") = (4,1)
	 * Returns null if given invalid notation.
	 */
	public static Coordinate notationToCoord(String n){
		if(n.length() != 2)
			return null;
		int x,y;
		switch(n.charAt(0)){
		case 'a' : x = 0; break;
		case 'b' : x = 1; break;
		case 'c' : x = 2; break;
		case 'd' : x = 3; break;
		case 'e' : x = 4; break;
		case 'f' : x = 5; break;
		case 'g' : x = 6; break;
		case 'h' : x = 7; break;
		default : return null;
		}
		y = Integer.parseInt(n.substring(1)) - 1;
		if(y > 7 || y < 0)
			return null;
		return new Coordinate(x,y);
	}

	/** Creates a new board in the standard starting position */
	public Board() {
		turn = WHITE;
		for(int x=0; x<8; x++){
			for(int y=2; y<6; y++){
				board[x][y] = EMPTY;
			}
		}

		previousDoublePush = -1;
		for(int c=0; c<2; c++){
			kingsideCastle[c] = true;
			queensideCastle[c] = true;
			hasCastled[c] = false;
		}

		board[0][0] = makeSquare(WHITE, ROOK);
		board[1][0] = makeSquare(WHITE, KNIGHT);
		board[2][0] = makeSquare(WHITE, BISHOP);
		board[3][0] = makeSquare(WHITE, QUEEN);
		board[4][0] = makeSquare(WHITE, KING);
		board[5][0] = makeSquare(WHITE, BISHOP);
		board[6][0] = makeSquare(WHITE, KNIGHT);
		board[7][0] = makeSquare(WHITE, ROOK);
		for(int x=0; x<8; x++){
			board[x][7] = makeSquare(BLACK, pieceOf(board[x][0]));
			board[x][1] = makeSquare(WHITE, PAWN);
			board[x][6] = makeSquare(BLACK, PAWN);
		}

		kingx[WHITE] = 4;
		kingy[WHITE] = 0;
		kingx[BLACK] = 4;
		kingy[BLACK] = 7;

		//zobrist hashing to allow implementation of transposition tables
		hash = 0;
		for(int x=0; x<8; x++) {
			for(int y=0; y<8; y++) {
				hash ^= zobrist[x*128 + y*16 + board[x][y]];
			}
		}
	}

	/** Creates a new board identical to b */
	public Board(Board b) {
		setState(b);
	}

	/** Sets this board to be in an identical state to b */
	public void setState(Board b) {
		for(int x=0; x<8; x++)
			for(int y=0; y<8; y++)
				board[x][y] = b.board[x][y];

		previousDoublePush = b.previousDoublePush;
		turn = b.turn;
		hash = b.hash;
		for(int i=0; i<2; i++){
			kingsideCastle[i] = b.kingsideCastle[i];
			queensideCastle[i] = b.queensideCastle[i];
			hasCastled[i] = b.hasCastled[i];
			kingx[i] = b.kingx[i];
			kingy[i] = b.kingy[i];
		}
	}

	/** Creates a new board in the specified state. 
	 *  Automatically generates correct hash value. Does not check for state legality */
	public Board(byte[][] board, byte previousDoublePush, byte turn,
				 boolean[] kingsideCastle, boolean[] queensideCastle, boolean[] hasCastled,
				 int[] kingx, int[] kingy){
		this.board = board;
		this.previousDoublePush = previousDoublePush;
		this.turn = turn;
		this.kingsideCastle = kingsideCastle;
		this.queensideCastle = queensideCastle;
		this.hasCastled = hasCastled;
		this.kingx = kingx;
		this.kingy = kingy;
		hash = 0;
		for(int x=0; x<8; x++){
			for(int y=0; y<8; y++){
				hash ^= zobrist[x*128 + y*16 + board[x][y]];
			}
		}
	}
	
	/**
	 * Getter method for board.
	 * @return The 2D byte array that represents the piece positions.
	 */
	public byte[][] getBoard(){
		return board;
	}
	
	/**
	 * Getter method for individual squares on the board. Throws ArrayOutOfBounds if given bad coordinates.
	 * @param c The coordinates of the desired square.
	 * @return The byte representation of the contents of the specified square.
	 */
	public byte getSquare(Coordinate c){
		return board[c.x][c.y];
	}
	
	/**
	 * Getter method for individual squares on the board. Throws ArrayOutOfBounds if given bad coordinates.
	 * @param x The x value (column) of the desired square.
	 * @param y The y value (row) of the desired square.
	 * @return The byte representation of the contents of the specified square.
	 */
	public byte getSquare(int x, int y){
		return board[x][y];
	}
	
	/**
	 * Getter method for previousDoublePush.
	 * @return The column of last turn's double pawn push (indexed from 0), or -1 if there wasn't one.
	 */
	public int getPreviousDoublePush(){
		return previousDoublePush;
	}
	
	/**
	 * Getter method for turn.
	 * @return Whose turn it is.
	 */
	public byte getTurn(){
		return turn;
	}
	
	/**
	 * Getter method for canKingsideCastle.
	 * @param turn Which player we're checking the castle status of.
	 * @return True iff it is still possible for the specified player to castle on the king side.
	 * 		   (But not necessarily this turn)
	 */
	public boolean canKingsideCastle(byte turn){
		return kingsideCastle[turn];
	}

	/**
	 * Getter method for canQueensideCastle.
	 * @param turn Which player we're checking the castle status of.
	 * @return True iff it is still possible for the specified player to castle on the queen side.
	 * 		   (But not necessarily this turn)
	 */
	public boolean canQueensideCastle(byte turn){
		return queensideCastle[turn];
	}
	
	/**
	 * Getter method for hasCastled.
	 * @param turn Which player we're checking the castle status of.
	 * @return True iff the specified player has castled.
	 */
	public boolean hasCastled(byte turn){
		return hasCastled[turn];
	}
	
	/**
	 * Getter method for kingx/kingy
	 * @param turn Which player's king we're getting.
	 * @return A coordinate representing the location of the specified player's king.
	 */
	public Coordinate getKingPosition(byte turn){
		return new Coordinate(kingx[turn], kingy[turn]);
	}
	
	/**
	 * Returns true iff m is a legal move for this board.
	 * 
	 * This method is implemented naively for reduced complexity, but that's making it quite slow.
	 * It's intended to be used only to verify the legality of a human player's requested move
	 * (or the legality of a bot's final choice of move).
	 * To find all legal moves for a bot while tree-searching, use generateMoves() instead.
	 */
	public boolean isLegalMove(Move m){
		List<Move> ml = generateMoves();
		if(ml.contains(m))
			return true;
		return false;
	}

	/* Helper method that handles keeping the hash value correct when the board state is modified.
	 * Replaces whatever is at board[x][y] with piece.
	 */
	private void modifySquare(int x, int y, byte piece){
		hash ^= zobrist[x*128 + y*16 + board[x][y]];
		board[x][y] = piece;
		hash ^= zobrist[x*128 + y*16 + board[x][y]];
	}
	
	/** Applies Move m to this Board while modifying the hash appropriately.
	 * Does NOT check for move legality. */
	/* Weirdness involving handling pawns moving off the edges of the board are to handle knight promotes.
	 * In short, ordering a pawn off the back edge of a board is equivalent to ordering it to the back row,
	 * but asking for a Knight promote rather than a Queen promote.
	 */
	public void makeMove(Move m) {
		previousDoublePush = -1; //will be set again if this move actually is a double push
		byte piece = board[m.sx][m.sy];
		modifySquare(m.sx, m.sy, EMPTY);
		
		/* Handles marking castling as impossible when rooks move or are captured.
		 * If a move either starts or ends at a rook starting position, it means the rook that started
		 * there either moved or was captured, both of which invalidate castling with that rook.
		 * The code checks if the ending y is <= 0 or >= 7 to handle the (rare) case of capturing a rook
		 * with a knight promoting pawn. 
		 */
		if     ((m.sx == 0 && m.sy == 0) || (m.ex == 0 && m.ey <= 0))
			queensideCastle[WHITE] = false;
		else if((m.sx == 7 && m.sy == 0) || (m.ex == 7 && m.ey <= 0))
			kingsideCastle[WHITE] = false;
		else if((m.sx == 0 && m.sy == 7) || (m.ex == 0 && m.ey >= 7))
			queensideCastle[BLACK] = false;
		else if((m.sx == 7 && m.sy == 7) || (m.ex == 7 && m.ey >= 7))
			kingsideCastle[BLACK] = false;
		
		/* Handles the special pawn moves: promotion, enpassant, and double push */
		if(pieceOf(piece) == PAWN){
			if(m.ey >= 7 || m.ey <= 0){ //it's a promotion move
				if(m.ey == 8){ //white is doing a knight promote
					modifySquare(m.ex, 7, makeSquare(turn, KNIGHT));
				}
				else if(m.ey == -1){ //black is doing a knight promote
					modifySquare(m.ex, 0, makeSquare(turn, KNIGHT));
				}
				else{ //it's a queen promote
					modifySquare(m.ex, m.ey, makeSquare(turn, QUEEN));
				}
			}
			else if(m.sx != m.ex && isEmpty(board[m.ex][m.ey])){ //it's an enpassant
				modifySquare(m.ex, m.ey, piece);
				modifySquare(m.ex, m.sy, EMPTY); //this is where the piece is captured
			}
			else if(Math.abs(m.sy - m.ey) == 2){ //it's a double push
				previousDoublePush = m.sx;
				modifySquare(m.ex, m.ey, piece);
			}
			else{ //it's a single push
				modifySquare(m.ex, m.ey, piece);
			}
		}

		/* Handles castling and manipulation of kingx and kingy */
		else if(pieceOf(piece) == KING){
			kingx[turn] = m.ex;
			kingy[turn] = m.ey;
			kingsideCastle[turn] = false; //you can no longer castle on either side after moving the king
			queensideCastle[turn] = false;

			if((m.ex - m.sx) == 2){ //it's a kingside castle
				modifySquare(m.ex, m.ey, piece); //moves the king
				modifySquare(5, m.ey, board[7][m.ey]); //moves the rook
				modifySquare(7, m.ey, EMPTY);
				hasCastled[turn] = true;
			}
			else if((m.sx - m.ex) == 2){ //it's a queenside castle
				modifySquare(m.ex, m.ey, piece); //moves the king
				modifySquare(3, m.ey, board[0][m.ey]); //moves the rook
				modifySquare(0, m.ey, EMPTY);
				hasCastled[turn] = true;
			}
			else{ //it's a regular king move
				modifySquare(m.ex, m.ey, piece);
			}
		}

		/* It's a regular 'ole move! */
		else{
			modifySquare(m.ex, m.ey, piece);
		}
		
		turn = (byte) (1 - turn); //move is complete, it's now the other player's turn
		hash ^= 0xCCCCCCCC;
	}
	
	/**
	 * Returns a Board representing the result of Move m being applied to this board.
	 */
	public Board afterMove(Move m){
		Board nb = new Board(this);
		nb.makeMove(m);
		return nb;
	}
	
	/** Returns true iff player is in check. */
	public boolean inCheck(byte player) {
		int mkx = kingx[player];
		int mky = kingy[player];
		int x, y;

		//first check all ranks, files, and diagonals for threats
		for(int dx=-1; dx<2; dx++){
			for(int dy=-1; dy<2; dy++){
				if(dx == 0 && dy == 0)
					continue;
				boolean first = true;
				// "((x | y) & 8) == 0" is a silly but very fast and compact bounds check
				for(x = mkx+dx, y = mky+dy; ((x | y) & 8) == 0; x += dx, y += dy){
					if(board[x][y] == EMPTY){
						first = false; //we're no longer one away
						continue;
					}
					if (colorOf(board[x][y]) == player)
						break; //can't be put in check by your own pieces
					switch(pieceOf(board[x][y])){
					case QUEEN:
						return true;
					case BISHOP:
						if((dx != 0) && (dy != 0)) //bishop can't threaten down ranks and files
							return true;
						break;
					case ROOK:
						if((dx == 0) || (dy == 0)) //rooks can't threaten down diagonals
							return true;
						break;
					case KING:
						if(first) //kings only threaten from distance one
							return true;
						break;
					case PAWN:
						if(first && (dx != 0)){ //pawns only threaten from the correct distance one diagonal
							if(player == WHITE && (dy == 1))
								return true;
							else if(player == BLACK && (dy == -1))
								return true;
						}
						break;
					case KNIGHT: //can block checks. Checks by a knight must be looked at separately
						break;
					}
					break; //if this line is hit, something has gone wrong TODO: add more useful error handling?
				}
			}
		}
		
		//checks for knight checks in the eight possible directions
		for(int i=0; i<KNIGHT_MOVES.length; i+=2){
			x = mkx + KNIGHT_MOVES[i];
			y = mky + KNIGHT_MOVES[i+1];
			if(((x | y) & 8) == 0 && pieceOf(board[x][y]) == KNIGHT && colorOf(board[x][y]) != player)
				return true;
		}
		
		return false;
	}

	/**
	 * Generates and returns a list of every legal move for the piece at the given coordinates.
	 */
	public List<Move> generateSquareMoves(int x, int y){
		List<Move> moveList = new ArrayList<Move>(27); //27 is the maximum number of legal moves for a single piece
		if(isEmpty(board[x][y])) //can't move a piece that isn't there
			return moveList;
		if(colorOf(board[x][y]) != turn) //can't move pieces that aren't the active player's
			return moveList;
		switch(pieceOf(board[x][y])){
		case PAWN:
			generatePawnMoves(moveList, x, y);
			break;
		case KNIGHT:
			generatePieceMoves(moveList, x, y, KNIGHT_MOVES, false);
			break;
		case BISHOP:
			generatePieceMoves(moveList, x, y, DIAGONAL_MOVES, true);
			break;
		case ROOK:
			generatePieceMoves(moveList, x, y, LINE_MOVES, true);
			break;
		case QUEEN:
			generatePieceMoves(moveList, x, y, DIAGONAL_MOVES, true);
			generatePieceMoves(moveList, x, y, LINE_MOVES, true);
			break;
		case KING:
			generateKingMoves(moveList);
			generateCastlingMoves(moveList);
			break;
		}
		return moveList;
	}
	
	/**
	 * Returns a list of every legal move from the current game state
	 */
	public List<Move> generateMoves() {
		//TODO: Empirically test to see if 50 is a reasonable starting size for this list
		List<Move> moveList = new ArrayList<Move>(50);

		for(int x=0; x<8; x++){
			for(int y=0; y<8; y++){
				if(isEmpty(board[x][y])) //can't move a piece that isn't there
					continue;
				if(colorOf(board[x][y]) != turn) //can't move pieces that aren't yours
					continue;
				switch(pieceOf(board[x][y])){
				case PAWN:
					generatePawnMoves(moveList, x, y);
					break;
				case KNIGHT:
					generatePieceMoves(moveList, x, y, KNIGHT_MOVES, false);
					break;
				case BISHOP:
					generatePieceMoves(moveList, x, y, DIAGONAL_MOVES, true);
					break;
				case ROOK:
					generatePieceMoves(moveList, x, y, LINE_MOVES, true);
					break;
				case QUEEN:
					generatePieceMoves(moveList, x, y, DIAGONAL_MOVES, true);
					generatePieceMoves(moveList, x, y, LINE_MOVES, true);
					break;
				}
			}
		}
		generateKingMoves(moveList);
		generateCastlingMoves(moveList);
		return moveList;
	}
	
	/** Generates all legal moves for the pawn at (x,y), and adds them to moveList.
	 *  Note that moving a pawn to the opponents back row is assumed to be a queen promotion,
	 *  and moving the pawn one past the back row is how a knight promote is represented. (see Move.java)
	 */
	public void generatePawnMoves(List<Move> moveList, int x, int y) {
		int ex, ey;
		int dir = 1; //direction this pawn moves
		int homeRow = 1; //this pawn's home row
		if(turn == BLACK){
			dir = -1;
			homeRow = 6;
		}
		int enpassant = homeRow + (dir*3); //y value at which you can enpassant
		
		//must temporarily move the piece to ensure an otherwise legal move doesn't put active player in check
		byte orig = board[x][y];
		board[x][y] = EMPTY;
		byte captured; //used to remember a 'temporarily captured' piece
		
		ey = y + dir; //don't have to bounds check because a pawn can never be on opponent's back row
		if(board[x][ey] == EMPTY){ //can we single push?
			board[x][ey] = orig;
			if(!inCheck(turn))
				moveList.add(new Move(x, y, x, ey, false));
			board[x][ey] = EMPTY;
			ey += dir;
			if(y == homeRow && board[x][ey] == EMPTY){ //can we double push?
				board[x][ey] = orig;
				if(!inCheck(turn))
					moveList.add(new Move(x, y, x, ey, false));
				board[x][ey] = EMPTY;
			}
		}
		
		ey = y + dir;
		ex = x + 1;
		if(ex <= 7 && board[ex][ey] != EMPTY && colorOf(board[ex][ey]) != turn){ //can we capture in +x diagonal?
			captured = board[ex][ey];
			board[ex][ey] = orig;
			if(!inCheck(turn))
				moveList.add(new Move(x, y, ex, ey, true));
			board[ex][ey] = captured;
		}
		
		ex = x - 1;
		if(ex >= 0 && board[ex][ey] != EMPTY && colorOf(board[ex][ey]) != turn){ //can we capture in -x diagonal?
			captured = board[ex][ey];
			board[ex][ey] = orig;
			if(!inCheck(turn))
				moveList.add(new Move(x, y, ex, ey, true));
			board[ex][ey] = captured;
		}
		
		if(y == enpassant && previousDoublePush != -1){ 
			ex = x + 1;
			if(previousDoublePush == ex){ //can we enpassant in +x direction?
				board[ex][ey] = orig;
				captured = board[ex][y];
				board[ex][y] = EMPTY;
				if(!inCheck(turn))
					moveList.add(new Move(x, y, ex, ey, true));
				board[ex][ey] = EMPTY;
				board[ex][y] = captured;
			}
			ex = x - 1;
			if(previousDoublePush == ex){ //can we enpassant in -x direction?
				board[ex][ey] = orig;
				captured = board[ex][y];
				board[ex][y] = EMPTY;
				if(!inCheck(turn))
					moveList.add(new Move(x, y, ex, ey, true));
				board[ex][ey] = EMPTY;
				board[ex][y] = captured;
			}
		}
		
		board[x][y] = orig; //restores board state
	}
	
	/** Given a moveList to add to, a pair of coordinates, a list of move directions, 
	 *  and whether the piece can multi-move, adds to moveList all legal moves for the piece.
	 *  This generic method serves to generate moves for the bishop, knight, rook, and queen.
	 */
	public void generatePieceMoves(List<Move> moveList, int x, int y, int[] moves, boolean multi) {
		int ex, ey, dx, dy;
		
		//must temporarily move the piece to ensure an otherwise legal move doesn't put active player in check
		byte orig = board[x][y];
		board[x][y] = EMPTY;
		byte captured; //used to remember a 'temporarily captured' piece
		
		for(int i=0; i<moves.length; i+=2){
			dx = moves[i];
			dy = moves[i+1];
			ex = x + dx;
			ey = y + dy;
			while(((ex | ey) & 8) == 0){ //checks bounds
				if(isEmpty(board[ex][ey])){
					board[ex][ey] = orig;
					if(!inCheck(turn))
						moveList.add(new Move(x, y, ex, ey, false));
					board[ex][ey] = EMPTY;
					if(!multi)
						break;
				}
				else if(colorOf(board[ex][ey]) != turn){ //did we hit an opponent's piece?
					captured = board[ex][ey];
					board[ex][ey] = orig;
					if(!inCheck(turn))
						moveList.add(new Move(x, y, ex, ey, true));
					board[ex][ey] = captured;
					break;
				}
				else //we hit one of our own pieces
					break;
				ex += dx;
				ey += dy;
			}
		}
		board[x][y] = orig; //restores board state
	}

	/** Separate function handles king moves because kingx and kingy must also be manipulated.
	 *  Adds to moveList all legal moves for the current player's king. 
	 */
	public void generateKingMoves(List<Move> moveList) {
		int x = kingx[turn];
		int y = kingy[turn];
		int ex, ey, dx, dy;
		
		//must temporarily move the piece to ensure an otherwise legal move doesn't put active player in check
		byte orig = board[x][y];
		board[x][y] = EMPTY;
		byte captured; //used to remember a 'temporarily captured' piece

		for(dx = -1; dx < 2; dx++){
			for(dy = -1; dy < 2; dy++){
				ex = x + dx;
				ey = y + dy;
				if(dx == 0 && dy == 0) //can't move nowhere!
					continue;
				if(((ex | ey) & 8) != 0) //bounds check
					continue;
				if(isEmpty(board[ex][ey])){
					board[ex][ey] = orig;
					kingx[turn] = ex;
					kingy[turn] = ey;
					if(!inCheck(turn))
						moveList.add(new Move(x, y, ex, ey, false));
					board[ex][ey] = EMPTY;
				}
				else if(colorOf(board[ex][ey]) != turn){ //something here to capture?
					captured = board[ex][ey];
					board[ex][ey] = orig;
					kingx[turn] = ex;
					kingy[turn] = ey;
					if(!inCheck(turn))
						moveList.add(new Move(x, y, ex, ey, true));
					board[ex][ey] = captured;
				}
			}
		}
		
		board[x][y] = orig; //restores board state
		kingx[turn] = x;
		kingy[turn] = y;
	}

	/** Adds to moveList all legal castling moves for the current player. */
	public void generateCastlingMoves(List<Move> moveList) {
		int x = kingx[turn];
		int y = kingy[turn];
		
		if(hasCastled[turn]) //can't castle twice
			return;

		//must temporarily move the piece to ensure an otherwise legal move doesn't put active player in check
		byte orig = board[x][y];
		board[x][y] = EMPTY;

		/* Check if kingside castle is possible. */
		boolean legalCastle = true;
		if(kingsideCastle[turn]){ //the king + rook must still be in their starting positions if this is true
			for(int i=4; i<7; i++){ //can't castle out of, through, or into check
				kingx[turn] = i;
				if(!isEmpty(board[i][y]) || inCheck(turn))
					legalCastle = false;
			}
			if(legalCastle)
				moveList.add(new Move(x, y, 6, y, false));
		}

		/* Check if queenside castle is possible */
		legalCastle = true;
		if(queensideCastle[turn]){ //the king + rook must still be in their starting positions if this is true
			for(int i=4; i>1; i--){ //can't castle out of, through, or into check
				kingx[turn] = i;
				if(!isEmpty(board[i][y]) || inCheck(turn))
					legalCastle = false;
			}
			if(!isEmpty(board[1][y])) //rook can't be blocked
				legalCastle = false;
			if(legalCastle)
				moveList.add(new Move(x, y, 2, y, false));
		}
		
		board[x][y] = orig;
		kingx[turn] = x;
		kingy[turn] = y;
	}

	public boolean equals(Object o) {
		if(!(o instanceof Board))
			return false; //u wot m8

		Board b = (Board) o;
		//check hash first because it is almost certainly the only thing needed if not equals.
		if(hash != b.hash)
			return false;

		if(kingsideCastle[0] != b.kingsideCastle[0])
			return false;
		if(kingsideCastle[1] != b.kingsideCastle[1])
			return false;
		if(queensideCastle[0] != b.queensideCastle[0])
			return false;
		if(queensideCastle[1] != b.queensideCastle[1])
			return false;
		if(previousDoublePush != b.previousDoublePush)
			return false;
		if(turn != b.turn)
			return false;
		for(int x=0; x<8; x++)
			for(int y=0; y<8; y++)
				if(board[x][y] != b.board[x][y])
					return false;

		return true;
	}

	public int hashCode(){
		return hash;
	}

	/* 
	 * Helper function for toString(), converts the byte representation of a piece to a string.
	 * King = K, Queen = Q, Rook = R, Knight = N, Bishop = B, Pawn = P.
	 * Uppercase = White, Lowercase = Black. Empty square represented by a period.
	 */ 
	private String pieceToString(byte piece){
		switch(piece){
			case 0 :  return "."; 
			case 1 :  return "P"; 
			case 2 :  return "N";
			case 3 :  return "B";
			case 4 :  return "R";
			case 5 :  return "Q";
			case 6 :  return "K";
			case 9 :  return "p";
			case 10 : return "n";
			case 11 : return "b";
			case 12 : return "r";
			case 13 : return "q";
			case 14 : return "k";
		}
		return "INVALID"; //Should never hit this. TODO: Add error handling here?
	}

	/**
	 * Returns an ASCII representation of the game state (with trailing newline).
	 */
	public String toString(){
		String result = "";
		if(turn == WHITE)
			result += "  -- White to play --  \n";
		else
			result += "  -- Black to play --  \n";
		result     += "* - a b c d e f g h - *\n"
			       +  "|                     |\n";
		for(int y=7; y>=0; y--){
			result += String.valueOf(y+1) + "   ";
			for(int x=0; x<8; x++){
				result += pieceToString(board[x][y]) + " ";
			}
			result += "  " + String.valueOf(y+1) + "\n";
		}
		result += "|                     |\n"
				+ "* - a b c d e f g h - *\n";
		//result += "White King @ (" + kingx[WHITE] + "," + kingy[WHITE] + ")\n";
		//result += "Black King @ (" + kingx[BLACK] + "," + kingy[BLACK] + ")\n";
		return result;
	}


}
