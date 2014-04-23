package chess;

import static org.junit.Assert.*;

import org.junit.*;

public class JTests {

	@Test
	public void coordToNotationTest() {
		assertEquals("e2","e2",Board.coordToNotation(new Coordinate(4,1)));
		assertEquals("f5","f5",Board.coordToNotation(new Coordinate(5,4)));
		assertEquals("null bad coords1",null,Board.coordToNotation(new Coordinate(10,1)));
		assertEquals("null bad coords2",null,Board.coordToNotation(new Coordinate(1,-2)));
	}
	
	@Test
	public void notationToCoordTest(){
		assertEquals("e2",new Coordinate(4,1),Board.notationToCoord("e2"));
		assertEquals("a7",new Coordinate(0,6),Board.notationToCoord("a7"));
		assertEquals("null too long",null,Board.notationToCoord("a7fdh"));
		assertEquals("null too short",null,Board.notationToCoord("a"));
		assertEquals("null bad char",null,Board.notationToCoord("y7"));
		assertEquals("null bad number",null,Board.notationToCoord("a9"));
	}

}
