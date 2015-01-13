package chess_swingfrontend;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import chess_backend.Board;
import chess_backend.Coordinate;
import chess_backend.Move;

/**
 * A panel for displaying the chess board
 * 
 * @author Kelton Finch
 */
public class BoardPanel extends JPanel implements MouseListener, MouseMotionListener {

	//these are coordinate group with which to draw the various pieces on the board using drawPolygon()
	//the bounds are assume to be squares with sides of size 100, they are scaled properly in the painting method
	//all start from the bottom left of the piece polygon
	protected static final int[] PAWN_X = {30,70,60,65,60,56,50,44,40,35,40};
	protected static final int[] PAWN_Y = {85,85,60,50,45,37,33,37,45,50,60};
	protected static final int PAWN_N = 11;
	
	protected static final int[] BISHOP_X = {30,70,60,70,60,60,50,40,40,30,40};
	protected static final int[] BISHOP_Y = {85,85,60,50,40,30,10,30,40,50,60};
	protected static final int BISHOP_N = 11;
	
	protected static final int[] KNIGHT_X = {20,80,80,70,60,40,20,20,50,30};
	protected static final int[] KNIGHT_Y = {85,85,60,20,15,20,40,50,40,60};
	protected static final int KNIGHT_N = 10;
	
	protected static final int[] ROOK_X = {20,80,80,70,70,80,80,70,70,55,55,45,45,30,30,20,20,30,30,20};
	protected static final int[] ROOK_Y = {85,85,75,65,40,30,20,20,25,25,20,20,25,25,20,20,30,40,65,75};
	protected static final int ROOK_N = 20;
	
	protected static final int[] QUEEN_X = {20,80,80,75,90,65,70,55,50,45,30,35,10,25,20};
	protected static final int[] QUEEN_Y = {85,85,75,65,20,50,15,45,10,45,15,50,20,65,75};
	protected static final int QUEEN_N = 15;
	
	protected static final int[] KING_X = {20,80,80,90,90,80,65,55,55,65,65,55,55,45,45,35,35,45,45,35,20,10,10,20};
	protected static final int[] KING_Y = {85,85,70,60,40,30,35,35,30,30,20,20,10,10,20,20,30,30,35,35,30,40,60,70};
	protected static final int KING_N = 24;
	
	private GamePanel parent; //a pointer to the parent GamePanel
	
	private int squareDim; //pixels to a side of a square on the board
	
	boolean isPieceDragging; //tracks if a piece is being dragged by the mouse
	byte draggedPiece; //the piece being dragged by the mouse
	int mx, my; //the mouses location
	List<Move> humanPonder;
	
	//the board coordinates of a player specified move
	private int sx, sy, ex, ey;
	
	public BoardPanel(GamePanel parent){
		addMouseListener(this);
		addMouseMotionListener(this);
		
		this.parent = parent;
		isPieceDragging = false;
		humanPonder = new ArrayList<Move>();
		updateDimensions();
	}
	
	protected void updateDimensions(){
		int minSide = Math.min(getSize().width, getSize().height);
		squareDim = minSide/8;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		doDrawing(g);
	}
	
	private void doDrawing(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		
		updateDimensions();
		
		Board gameState = parent.getGameState();
		Move prevMove = parent.getPrevMove();
		Move bestMove = parent.getBestMove();
		
		//draw board
		g2d.setColor(GamePanel.WHITE_SQUARE_COLOR);
		g2d.fillRect(0, 0, squareDim*8, squareDim*8);
		g2d.setColor(GamePanel.BLACK_SQUARE_COLOR);
		for(int x=0; x<8; x++){
			for(int y=0; y<8; y++){
				if((x+y)%2 == 1)
					g2d.fillRect(x*squareDim, y*squareDim, squareDim, squareDim);
			}
		}
		
		//draw previous move highlighting
		if(prevMove != null){
			g2d.setColor(GamePanel.PREVMOVE_SQUARE_COLOR);
			for(int x=0; x<8; x++){
				for(int y=0; y<8; y++){
					if((x == prevMove.sx && y == prevMove.sy) || (x == prevMove.ex && y == prevMove.ey))
						g2d.fillRect(x*squareDim, (7-y)*squareDim, squareDim, squareDim);
				}
			}
		}
		
		//draw pieces
		if(gameState != null){
			byte piece;
			for(int x=0; x<8; x++){
				for(int y=0; y<8; y++){
					piece = gameState.getSquare(x, 7-y); //"7-y" to preserve white starting at botton of screen
					if(isPieceDragging && sx == x && sy == 7-y)
						drawPiece(g2d, piece, squareDim*x, squareDim*y, 0.5f);
					else	
						drawPiece(g2d, piece, squareDim*x, squareDim*y, 1.0f);
				}
			}
		}
		
		g2d.setColor(GamePanel.PONDER_COLOR);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
		
		//draw bot ponder
		if(bestMove != null){
			g2d.fillRect(bestMove.sx*squareDim, (7-bestMove.sy)*squareDim, squareDim, squareDim);
			g2d.fillRect(bestMove.ex*squareDim, (7-bestMove.ey)*squareDim, squareDim, squareDim);
		}
		
		//draw human ponder
		//if(!humanPonder.isEmpty())
			//g2d.fillRect(sx*squareDim, (7-sy)*squareDim, squareDim, squareDim);
		for(Move m : humanPonder){
			g2d.fillRect(m.ex*squareDim, (7-m.ey)*squareDim, squareDim, squareDim);
		}
		
		//draw dragged piece (if needed)
		if(isPieceDragging)
			drawPiece(g2d, draggedPiece, mx-squareDim/2, my-squareDim/2, 0.5f);
		
	}
	
	/*
	 * Helper method sizes and draws a piece at the specified location (top left of the square)
	 */
	private void drawPiece(Graphics2D g2d, byte piece, int px, int py, float alpha){
		if(piece == Board.EMPTY)
			return; //nothing to draw if the "piece" is an empty square
		
		int polyX[] = null;
		int polyY[] = null;
		int polyN = 0;
		
		int locatedPolyX[];
		int locatedPolyY[];
		
		//get the appearance of the piece here, then translate and resize the points
		switch(Board.pieceOf(piece)){
		case Board.PAWN:   polyX = PAWN_X;   polyY = PAWN_Y;   polyN = PAWN_N;   break;
		case Board.BISHOP: polyX = BISHOP_X; polyY = BISHOP_Y; polyN = BISHOP_N; break;
		case Board.KNIGHT: polyX = KNIGHT_X; polyY = KNIGHT_Y; polyN = KNIGHT_N; break;
		case Board.ROOK:   polyX = ROOK_X;   polyY = ROOK_Y;   polyN = ROOK_N;   break;
		case Board.QUEEN:  polyX = QUEEN_X;  polyY = QUEEN_Y;  polyN = QUEEN_N;  break;
		case Board.KING:   polyX = KING_X;   polyY = KING_Y;   polyN = KING_N;   break;
		default: System.err.println("invalid piece"); System.exit(1); //this should never happen
		}
		locatedPolyX = new int[polyN];
		locatedPolyY = new int[polyN];
		for(int i=0; i<polyN; i++){
			locatedPolyX[i] = polyX[i]*squareDim/100 + px;
			locatedPolyY[i] = polyY[i]*squareDim/100 + py;
		}
		
		//add alpha
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		
		//draw the piece from the generated coords
		if(Board.colorOf(piece) == Board.WHITE)
			g2d.setColor(GamePanel.WHITE_PIECE_COLOR);
		else
			g2d.setColor(GamePanel.BLACK_PIECE_COLOR);
		g2d.fillPolygon(locatedPolyX, locatedPolyY, polyN);
	}

	public void mouseDragged(MouseEvent e) {
		if(isPieceDragging){
			mx = e.getX();
			my = e.getY();
			repaint();
		}
	}
	
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		sx = mx/squareDim;
		sy = 7-(my/squareDim);
		
		if(sx >= 0 && sx < 8 && sy >= 0 && sy < 8){
			Board gameState = parent.getGameState();
			if(Board.colorOf(gameState.getSquare(sx, sy)) != gameState.getTurn())
				return; //prevents you from appearing to be able to drag your opponent's pieces
			
			isPieceDragging = true;
			draggedPiece = parent.getGameState().getSquare(sx,sy);
			humanPonder = gameState.generateSquareMoves(sx, sy);
		}
	}

	public void mouseReleased(MouseEvent e) {
		isPieceDragging = false;
		humanPonder.clear();
		
		mx = e.getX();
		my = e.getY();
		ex = mx/squareDim;
		ey = 7-(my/squareDim);
		
		Move m = new Move(sx,sy,ex,ey,false);
		parent.requestMove(m);
		repaint();
	}

}
