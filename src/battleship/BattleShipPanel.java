package battleship;

import java.awt.Graphics;

import javax.swing.JPanel;

public class BattleShipPanel extends JPanel{
	//constants
	final char[] BOARD_LABELS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
	
	
	//board variables
	int boardSize = 400;
	
	//left
	int leftBoardX = 100;
	int leftBoardY = 100;
	
	//right
	int rightBoardX = 650;
	int rightBoardY = 100;
	
	
	public BattleShipPanel()
	{
		super();
	}
	
	public void paintComponent(Graphics g){
		//draw boards
		drawBoard(leftBoardX, leftBoardY, g);
		drawBoard(rightBoardX, rightBoardY, g);
		
		Carrier testCarrier = new Carrier(1, 1);
		testCarrier.drawMe(rightBoardX, rightBoardY, boardSize, g);
		
	}
	
	//draws a playing board
	public void drawBoard(int x, int y, Graphics g)
	{
		
		for(int i = 0; i < 11; i++)
		{
			int spacing = (boardSize * i)/10;
			g.drawLine(x + spacing, y, x + spacing, y+boardSize);
			g.drawLine(x, y + spacing, x + 400, y + spacing);
			if(i != 0)
			{
				g.drawString(i+"", x + spacing - (boardSize/20), y -2);
				g.drawString(BOARD_LABELS[i -1] + "", x -10, y + spacing - (boardSize/20));
			}
		}
	}
}
