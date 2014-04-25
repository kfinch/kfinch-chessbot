package chess_backend;

public class Coordinate{
	
	public int x, y;
	
	public Coordinate(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public String toString(){
		return "(" + x + "," + y + ")";
	}
	
	public boolean equals(Object o){
		Coordinate c = (Coordinate) o;
		return ((x == c.x) && (y == c.y));
	}
	
}