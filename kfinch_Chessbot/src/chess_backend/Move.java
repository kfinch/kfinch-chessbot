package chess_backend;

public class Move {
	public int sx;
	public int sy;
	public int ex;
	public int ey;
	public boolean isCapture;
	
	public Move(int sx, int sy, int ex, int ey, boolean isCapture){
		this.sx = sx;
		this.sy = sy;
		this.ex = ex;
		this.ey = ey;
		this.isCapture = isCapture;
	}
	
	public String toNotation(){
		Coordinate sc = new Coordinate(sx,sy);
		Coordinate ec = new Coordinate(ex,ey);
		String result = Board.coordToNotation(sc) + " -> " + Board.coordToNotation(ec);
		if(isCapture)
			result += " *";
		return result;
	}
	
	public String toString(){
		Coordinate sc = new Coordinate(sx,sy);
		Coordinate ec = new Coordinate(ex,ey);
		String result = sc + " -> " + ec;
		if(isCapture)
			result += "*";
		return result;
	}
	
	/**
	 * Checks for equality of this and o, but does not care if isCapture is different.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		Move m = (Move)o;
		return ((sx == m.sx) && (sy == m.sy) && (ex == m.ex) && (ey == m.ey));
	}
	
}
