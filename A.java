import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * This program can play chess.
 * 
 * TODO:
 *   - Draw on position repeats 3 times.
 *   - Draw when mate is not possiple.
 *   - Draw when in last 50 moves there was no stroke or pawn move.
 * 
 * @author Mykolas Juraitis
 */
public final class A extends JComponent {

	private final static int BW = 8;

	private final static int BH = 8;

	/**
	 * Move: int[] {from, to, promotedFigure}
	 */
	private final static int[] s = new int[3];

	/**
	 * Destination square of last move.
	 */
	private int lm;

	private static int cx, cy;

	public A() {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		lm = s[0] = -1;
		//
		f[0] = f[7] = f[56] = f[63] = '\u265C';
		f[1] = f[6] = f[57] = f[62] = '\u265E';
		f[2] = f[5] = f[58] = f[61] = '\u265D';
		f[3] = f[59] = '\u265B';
		f[4] = f[60] = '\u265A';
		for (int x = 0; x < BW; x++) {
			f[BW + x] = f[BW * (BH - 2) + x] = '\u265F';
			for (int y = 2; y < BH - 2; y++)
				f[y * BW + x] = ' ';
			c[BW * (BH - 2) + x] = c[BW * (BH - 1) + x] = true;
		}
	}

	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			if (s[0] == -1)
				s[0] = e.getY() / cy * BW + e.getX() / cx;
			else {
				s[1] = e.getY() / cy * BW + e.getX() / cx;
				int[][] moves = new int[96][3];
				int moveCount = l(moves, true);
				for (int i = 0; i < moveCount; i++) {
					if (moves[i][0] == s[0] && moves[i][1] == s[1]) {
						s[2] = p[0].isSelected() ? '\u265B' : p[1].isSelected() ? '\u265C' : p[2].isSelected() ? '\u265D' : '\u265E';
						mv(s);
						s[0] = -1;
						lm = s[1];
						paintImmediately(0, 0, getWidth(), getHeight());
						// Bot move
						moveCount = l(moves, false);
						if (moveCount == 0) {
							JOptionPane.showMessageDialog(null, ic(this, false) ? "You won" : "Draw");
							System.exit(0);
						}
						int bestIndex = 0;
						int best = -100000;
						int alpha = -100000;
						//nodeCount = 0;
						//System.out.println("Possible moves: " + moveCount);
						for (int j = 0; j < moveCount; j++ ) {
							A newPos = new A(this);
							newPos.mv(moves[j]);
							if (best > alpha)
								alpha = best;
							int value = -ab(newPos, 2, -100000, -alpha);
							//System.out.print(moves[j][0] + "," + moves[j][1] + " -> " + moves[j][2] + "," + moves[j][3] + " value = " + value);
							if (value > best || (value == best && Math.random() > 0.85)) {
								best = value;
								bestIndex = j;
								//System.out.println(" (currently the best)");
							}
							//else
							//	System.out.println();
						}
						//System.out.println("Positions searched: " + nodeCount);
						mv(moves[bestIndex]);
						lm = moves[bestIndex][1];
						moveCount = l(moves, true);
						if (moveCount == 0) {
							JOptionPane.showMessageDialog(null, ic(this, true) ? "You lost" : "Draw");
							System.exit(0);
						}
						break;
					}
				}
				s[0] = -1;
			}
			repaint();
		}
	}

	public void paint(Graphics g) {
		cx = getWidth() / BW;
		cy = getHeight() / BH;
		for (int y = 0; y < BH; y++)
			for (int x = 0; x < BW; x++) {
				int from = y * BW + x;
				Color color = from == s[0] ? Color.RED : from == lm ? Color.GREEN : (y + x) % 2 == 0 ? Color.GRAY : Color.DARK_GRAY;
				g.setColor(color);
				g.fillRect(x * cx, y * cy, 10000, 10000);
				g.setColor(c[from] ? Color.WHITE : Color.BLACK);
				String figure = String.valueOf((char)f[from]);
				Rectangle2D bounds = g.getFontMetrics().getStringBounds(figure, g);
				g.drawString(figure,
					x * cx + (cx - (int)bounds.getWidth()) / 2,
					y * cy + (cy - (int)bounds.getHeight()) / 2 + g.getFontMetrics().getAscent());
			}
	}

	private final static JRadioButton p[] = new JRadioButton[4];

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel menuBar = new JPanel();
		menuBar.add(new JLabel("Promote to:"));
		ButtonGroup group = new ButtonGroup();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(menuBar, BorderLayout.NORTH);
        A a = new A();
        frame.getContentPane().add(a, BorderLayout.CENTER);
        //
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (int i = 0; i < fonts.length; i++)
			if (fonts[i].canDisplay('\u265E')) {
				Font font = new Font(fonts[i].getName(), Font.PLAIN, 48);
				for (int j = 0; j < 4; j++) {
					p[j] = new JRadioButton(String.valueOf((char)('\u265B' + j)), j == 0);
					p[j].setFont(font);
					menuBar.add(p[j]);
					group.add(p[j]);
				}
				a.setFont(font);
				break;
			}
		// Center window
		Dimension d = frame.getToolkit().getScreenSize();
		frame.setBounds((d.width - 500) / 2, (d.height - 600) / 2, 500, 600);
		// Show window
		frame.setVisible(true);
	}

	//private static int nodeCount;

	/**
	 * AlphaBeta search for the best move.
	 */
	private int ab(A pos, int depth, int alpha, int beta) {
		//nodeCount++;
		int[][] moves = new int[96][3];
		int moveCount = pos.l(moves, pos.w);
		int best = -100000;
		int value = 0;
		if (depth == 0) {
			for (int y = 0; y < BH; y++)
				for (int x = 0; x < BW; x++)
					value += pos.c[y * BW + x] == pos.w ? fv[pos.f[y * BW + x]] : -fv[pos.f[y * BW + x]];
			// Reward for attacking pieces
			for (int k = 0; k < moveCount; k++)
				value += fv[pos.f[moves[k][1]]] / 100;
			// Penalty for attacked pieces
			moveCount = pos.l(moves, !pos.w);
			for (int k = 0; k < moveCount; k++)
				value -= fv[pos.f[moves[k][1]]] / 100;
			return moveCount == 0 ? best : value;
		}
		for (int i = 0; i < moveCount && best < beta; i++) {
			A newPos = new A(pos);
			newPos.mv(moves[i]);
			if (best > alpha)
				alpha = best;
			value = -ab(newPos, depth - 1, -beta, -alpha);
			if (value > best)
				best = value;
		}
		return best;
	}

	/**
	 * Figures
	 */
	private int[] f = new int[BW * BH];

	/**
	 * Figure colors (true = white)
	 */
	private boolean[] c = new boolean[BW * BH];

	/**
	 * true = figure has already moved
	 */
	private boolean[] m = new boolean[BW * BH];

	/**
	 * Coordinate of passing pawn
	 */
	private int pp;

	/**
	 * true = white turn
	 */
	private boolean w = true;

	public A(A pos) {
		for (int i = 0; i < BW * BH; i++) {
			f[i] = pos.f[i];
			c[i] = pos.c[i];
			m[i] = pos.m[i];
		}
		pp = pos.pp;
		w = pos.w;
	}

	/**
	 * Make legal move
	 */
	public void mv(int[] move) {
		pp = 0; // clear passing pawn coordinates
		int figure = f[move[0]];
		// Castling of the king
		if (figure == '\u265A')
			// TODO: Potential place for optimization
			// Queen side
			if (move[0] - move[1] == 2) {
				f[move[1] + 1] = '\u265C';
				int rookCoord = move[0] < BW ? 0 : BW * (BH - 1);
				f[rookCoord] = ' ';
				c[move[1] + 1] = c[rookCoord];
			}
			// King side
			else if (move[1] - move[0] == 2) {
				f[move[1] - 1] = '\u265C';
				int rookCoord = (move[0] < BW ? BW : BW * BH) - 1;
				f[rookCoord] = ' ';
				c[move[1] - 1] = c[rookCoord];
			}
		// Pawn
		if (figure == '\u265F') {
			// Set passing pawn coordinates
			if (Math.abs(move[0] - move[1]) == BW * 2)
				pp = move[1];
			// Passing pawn stroke
			else if (Math.abs(move[0] - move[1]) != BW && f[move[1]] == ' ')
				f[move[1] + (move[0] - move[1] > 0 ? BW : -BW)] = ' ';
			// Promotion
			else if (move[1] < BW || move[1] >= BW * (BH - 1))
				figure = move[2];
		}
		// Make move
		f[move[1]] = figure;
		f[move[0]] = ' ';
		c[move[1]] = c[move[0]];
		m[move[0]] = m[move[1]] = true;
		// Switch turn
		w = !w;
	}

	/**
	 * Possible moves for figures
	 */
	private static int[][][] fm = new int[0x265F + 1][][];

	private static int[] fv = new int[0x265F + 1];

	static {
		fm[0x265C] = new int[][] { // Rook
			{ 1,  0, 8}, { 0,  1, 8}, { 0, -1, 8}, {-1,  0, 8}};
		fm[0x265E] = new int[][] { // Knight
			{ 1,  2, 1}, { 1, -2, 1}, { 2,  1, 1}, { 2, -1, 1},
			{-1,  2, 1}, {-1, -2, 1}, {-2,  1, 1}, {-2, -1, 1}};
		fm[0x265D] = new int[][] { // Bishop
			{ 1,  1, 8}, { 1, -1, 8}, {-1, -1, 8}, {-1,  1, 8}};
		fm[0x265B] = new int[][] { // Queen
			{-1, -1, 8}, {-1,  0, 8}, {-1,  1, 8}, { 0,  1, 8},
			{ 1,  1, 8}, { 1,  0, 8}, { 1, -1, 8}, { 0, -1, 8}};
		fm[0x265A] = new int[][] { // King
			{-1, -1, 1}, {-1,  0, 1}, {-1,  1, 1}, { 0,  1, 1},
			{ 1,  1, 1}, { 1,  0, 1}, { 1, -1, 1}, { 0, -1, 1}};
		//
		fv[0x265B] = 900;
		fv[0x265C] = 500;
		fv[0x265D] = fv[0x265E] = 300;
		fv[0x265F] = 100;
	}

	/**
	 * Test if king is under check
	 */
	public boolean ic(A pos, boolean whiteTurn) {
		int[][] moves = new int[96][3];
		int moveCount = pos.p(moves, !whiteTurn);
		for (int j = 0; j < moveCount; j++)
			if (pos.f[moves[j][1]] == 0x265A && pos.c[moves[j][1]] == whiteTurn)
				return true;
		return false;
	}

	/**
	 * Construct legal move list
	 */
	public int l(int[][] moves, boolean whiteTurn) {
		int moveCount = p(moves, whiteTurn);
		for (int i = 0; i < moveCount; i++) {
			A pos = new A(this);
			pos.mv(moves[i]);
			boolean isAllowed = !ic(pos, whiteTurn);
			// Disallow castling when king is under check or goes through checked cell
			if (f[moves[i][1]] == 0x265A && Math.abs(moves[i][0] - moves[i][1]) == 2) {
				if (ic(this, whiteTurn))
					isAllowed = false;
				else {
					pos = new A(this);
					pos.mv(new int[] {moves[i][0], (moves[i][0] + moves[i][1]) / 2, ' '});
					if (ic(pos, whiteTurn))
						isAllowed = false;
				}
			}
			if (!isAllowed)
				moves[i--] = (int[])moves[--moveCount].clone();
			// Generate all possible promotions
			else if (f[moves[i][0]] == 0x265F && (moves[i][1] < BW || moves[i][1] >= BW * (BH - 1)) && moves[i][2] == ' ') {
				moves[i][2] = '\u265B';
				moves[moveCount] = (int[])moves[i].clone();
				moves[moveCount++][2] = '\u265C';
				moves[moveCount] = (int[])moves[i].clone();
				moves[moveCount++][2] = '\u265D';
				moves[moveCount] = (int[])moves[i].clone();
				moves[moveCount++][2] = '\u265E';
			}
		}
		return moveCount;
	}

	/**
	 * Construct possible move list
	 */
	private int p(int[][] moves, boolean whiteTurn) {
		int moveCount = 0;
		for (int ty = 0; ty < BH; ty++)
			for (int tx = 0; tx < BW; tx++)
				if (c[ty * BW + tx] == whiteTurn) {
					int from = ty * BW + tx;
					int figure = f[from];
					// Castling of the king
	 				if (figure == 0x265A && !m[from]) {
						if (f[from + 1] == ' ' && f[from + 2] == ' ' && !m[from + 3]) {
							moves[moveCount][0] = from;
							moves[moveCount][1] = from + 2;
							moves[moveCount++][2] = ' ';
						}
						if (f[from - 1] == ' ' && f[from - 2] == ' ' && f[from - 3] == ' ' && !m[from - 4]) {
							moves[moveCount][0] = from;
							moves[moveCount][1] = from - 2;
							moves[moveCount++][2] = ' ';
						}
					}
					// Pawn moves
	 				if (figure == 0x265F) {
	 					int dir = c[from] ? -1 : 1; // Direction of pawn move depending on color
	 					int to = from + dir * BW; // One square ahead
						// Stroke to the right
						if (tx < 7 && (f[to + 1] != ' ' && c[to + 1] != whiteTurn || from + 1 == pp)) {
							moves[moveCount][0] = from;
							moves[moveCount][1] = to + 1;
							moves[moveCount++][2] = ' ';
						}
						// Stroke to the right
						if (tx > 0 && (f[to - 1] != ' ' && c[to - 1] != whiteTurn || from - 1 == pp)) {
							moves[moveCount][0] = from;
							moves[moveCount][1] = to - 1;
							moves[moveCount++][2] = ' ';
						}
						// Move one square forward
						if (f[to] == ' ') {
							moves[moveCount][0] = from;
							moves[moveCount][1] = to;
							moves[moveCount++][2] = ' ';
						}
						// Move two squares forward
						if (!m[from] && f[to] == ' ' && f[to + dir * BW] == ' ') {
							moves[moveCount][0] = from;
							moves[moveCount][1] = to + dir * BW;
							moves[moveCount++][2] = ' ';
						}
					}
	 				// All other figures left, using lookup table
					int[][] figureMoves = fm[f[from]];
					if (figureMoves != null) {
						for (int i = 0; i < figureMoves.length; i++) {
							int j = 0;
							int xi = figureMoves[i][0];		// +x
							int yi = figureMoves[i][1];		// +y
							int depth = figureMoves[i][2];	// search depth
							int nx = tx;
							int ny = ty;
							do {
								nx = nx + xi;
								ny = ny + yi;
								// If not out of bounds
								if (((nx & 7) == nx) && ((ny & 7) == ny)) {
									int to = ny * BW + nx;
									int field = f[to]; // piece of target field
									if (field == ' ') {
										moves[moveCount][0] = from;
										moves[moveCount][1] = to;
										moves[moveCount++][2] = ' ';
									}
									else {
										if (c[ny * BW + nx] != whiteTurn) {
											moves[moveCount][0] = from;
											moves[moveCount][1] = to;
											moves[moveCount++][2] = ' ';
										}
										break;
									}
								}
								else
									break; // Out of bounds
								j++;
							} while (j < depth);
						} // end of for movesForFigure
					}
				}
		return moveCount;
	}
}