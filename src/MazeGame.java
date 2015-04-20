import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import javalib.impworld.World;
import javalib.worldimages.*;
import tester.*;

// Assignment 10
// Dhesikan Anish
// anishd
// McDonough Kevin
// kmacdoug

// NOTE: for Kruskal's algorithm, since the union-find structure is operating
//       on a very specific subset of Posns (first quadrant rectangle cornered
//       at the origin), we decided to use a 2 dimensional ArrayList instead of
//       a HashMap as our underlying data structure for union-find

// to represent a square in the maze game
class Cell {
	int x; // in grid coords
	int y; // in grid coords
	Edge left;
	Edge top;
	Edge right;
	Edge bot;

	// Default constructor
	Cell(int x, int y, Edge left, Edge top, Edge right, Edge bot) {
		this.x = x;
		this.y = y;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bot = bot;
	}

	// Initializing constructor
	Cell(int x, int y) {
		this.x = x;
		this.y = y;
		this.left = null;
		this.top = null;
		this.right = null;
		this.bot = null;
	}

	// draws this cell, along with its bottom and right edges
	WorldImage draw(int cellSize) {
		Posn center = new Posn(this.x * cellSize + cellSize / 2,
				this.y * cellSize + cellSize / 2);


		WorldImage cellImage = 
				new RectangleImage(center, cellSize, cellSize, this.getColor());

		WorldImage leftEdgeImage = this.left.draw(cellSize);
		WorldImage topEdgeImage = this.top.draw(cellSize);
		WorldImage rightEdgeImage = this.right.draw(cellSize);
		WorldImage botEdgeImage = this.bot.draw(cellSize);

		return cellImage.overlayImages(
				leftEdgeImage, topEdgeImage, rightEdgeImage, botEdgeImage);

	}

	Color getColor() {
		// Hue 282
		return new Color(203, 188, 227);
	}

}

// to represent a wall between two cells
abstract class Edge {
	static final Color COLOR = new Color(73, 46, 116);
	
	Cell cell1;
	Cell cell2;
	boolean isBlocking;
	double weight;

	Edge(Cell cell1, Cell cell2, boolean isBlocking, double weight) {
		this.cell1 = cell1;
		this.cell2 = cell2;
		this.isBlocking = isBlocking;
		this.weight = weight;
	}

	WorldImage draw(int cellSize) {
		// nonexistant walls draw offscreen (no dummy image type)
		if(!isBlocking) {
			return new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK);
		}

		return this.getImage(cellSize);
	}

	// gets the image of this edge regardless of isBlocking
	abstract WorldImage getImage(int cellSize);

	Color getColor() {
		// Hue 282
		return Edge.COLOR;
	}
}

// to represent an edge that connects left and right cells
class LREdge extends Edge {
	LREdge(Cell left, Cell right, boolean isBlocking, double weight) {
		super(left, right, isBlocking, weight);
	}

	// returns the image of this edge
	WorldImage getImage(int cellSize) {
		int rightCellX = this.cell2.x;
		int rightCellY = this.cell2.y;
		Posn center = new Posn(rightCellX * cellSize,
				rightCellY * cellSize + cellSize / 2);

		return new RectangleImage(
				center, cellSize / 10 + 1, cellSize, this.getColor());
	}
}

// to represent an edge that connects top and bottom cells
class TBEdge extends Edge {
	TBEdge(Cell top, Cell bot, boolean isBlocking, double weight) {
		super(top, bot, isBlocking, weight);
	}

	// returns the image of this edge
	WorldImage getImage(int cellSize) {

		int botCellX = this.cell2.x;
		int botCellY = this.cell2.y ;
		Posn center = new Posn(botCellX * cellSize + cellSize / 2,
				botCellY * cellSize);

		return new RectangleImage(
				center, cellSize, cellSize / 10 + 1, this.getColor());
	}
}

//to represent an edge that is on the border of the maze
class BorderEdge extends Edge {
	BorderEdge(Cell cell) {
		super(cell, cell, true, 0);
	}

	// returns the image of this edge
	WorldImage getImage(int cellSize) {
		// an image off the canvas for lack of a dummy Image
		return new RectangleImage(new Posn (-1, -1), 0, 0, Color.GRAY);
	}
}

// to compare edges by weight
class EdgeWeightComp implements Comparator<Edge> {
	// compare the given edges
	public int compare(Edge edge1, Edge edge2) {
		return Double.compare(edge1.weight, edge2.weight);
	}
}

// to represent a maze board
class Maze {
	// width and height of Maze in cells;
	int width;
	int height;

	// all the cells of the board represented as a list of columns of cells
	ArrayList<ArrayList<Cell>> cells;

	// a list of all non-border edges in the game
	// INV: sorted by weight
	ArrayList<Edge> edges;

	// random instance to be used throughout maze generation
	Random rand;

	// Constructor creating a maze with given width and height (in cells)
	Maze(int width, int height) {
		this.rand = new Random();
		initializeBoard(width, height);
	}

	///////////////////////////////////////////////////////////////////////////
	// Initialization functions

	// EFFECT: modify cells as a board of given width and height (in cells) 
	// with all walls present
	void initializeBoard(int width, int height) {
		this.width = width;
		this.height = height;
		this.constructCells(width, height);
		this.connectCells();
	}

	// EFFECT: modify cells as a matrix of cells that aren't connected
	void constructCells(int width, int height) {
		cells = new ArrayList<ArrayList<Cell>>(width);
		for (int x = 0; x < width; x += 1) {
			cells.add(new ArrayList<Cell>(height));
			for (int y = 0; y < height; y += 1) {
				cells.get(x).add(new Cell(x, y));
			}
		}
	}

	// EFFECT: modify cells' edges to connect cells
	void connectCells() {
		this.edges = new ArrayList<Edge>();
		this.connectCellsHorizontal();
		this.connectCellsVertical();
	}

	// EFFECT: modify cells' left and right edges
	void connectCellsHorizontal() {
		for (int x = 0; x < cells.size() - 1; x += 1) {
			for (int y = 0; y < cells.get(x).size(); y += 1) {
				Cell curCell = cells.get(x).get(y);
				Cell rightCell = cells.get(x + 1).get(y);
				Edge edge = new LREdge(curCell, rightCell, true, 1);
				curCell.right = edge;
				rightCell.left = edge;
				this.edges.add(edge);
			}
		}

		for (int y = 0; y < cells.get(0).size(); y += 1) {
			Cell leftmostCell = cells.get(0).get(y);
			leftmostCell.left = new BorderEdge(leftmostCell);

			Cell rightmostCell = cells.get(cells.size() - 1).get(y);
			rightmostCell.right = new BorderEdge(rightmostCell);
		}
	}

	// EFFECT: modify cells' top and bottom edges
	void connectCellsVertical() {
		for (int x = 0; x < cells.size(); x += 1) {
			for (int y = 0; y < cells.get(x).size() - 1; y += 1) {
				Cell curCell = cells.get(x).get(y);
				Cell downCell = cells.get(x).get(y + 1);
				Edge edge = new TBEdge(curCell, downCell, true, 1);
				curCell.bot = edge;
				downCell.top = edge;
				this.edges.add(edge);
			}
		}

		for (int x = 0; x < cells.size(); x += 1) {
			Cell topmostCell = cells.get(x).get(0);
			topmostCell.top = new BorderEdge(topmostCell);

			Cell bottommostCell = cells.get(x).get(cells.get(x).size() - 1);
			bottommostCell.bot = new BorderEdge(bottommostCell);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Drawing functions

	// draws this maze onto the given background image
	WorldImage drawOnto(int cellSize, WorldImage bg) {

		WorldImage mazeImage =
				this.drawSection(cellSize, 0, 0, 
						this.width, this.height);
		
		// width and height of maze in pixels
		int pWidth = this.width * cellSize;
		int pHeight = this.height * cellSize;
		
		///////////////////////////////////////////////////////////////////////
		// Draw Border
		
		// positions of the centers of the sides
		Posn topC = new Posn(pWidth / 2, 0);
		Posn botC = new Posn(pWidth / 2, pHeight);
		Posn leftC = new Posn(0, pHeight / 2);
		Posn rightC = new Posn(pWidth, pHeight / 2);
		
		int borderW = cellSize / 10 + 1;

		WorldImage top =
				new RectangleImage(topC, pWidth, borderW, Edge.COLOR);
		WorldImage bot =
				new RectangleImage(botC, pWidth, borderW, Edge.COLOR);
		WorldImage left =
				new RectangleImage(leftC, borderW, pWidth, Edge.COLOR);
		WorldImage right =
				new RectangleImage(rightC, borderW, pWidth, Edge.COLOR);
		
		WorldImage border = top.overlayImages(bot, left, right);
		
		return mazeImage.overlayImages(border);
	}

	// draws a section of cells within the given bounds
	// minX and minY are inclusive, maxX and maxY are exclusive
	WorldImage drawSection(int cellSize, 
			int minX, int minY, int maxX, int maxY) {
		int width = maxX - minX; // in cells
		int height = maxY - minY; // in cells

		if (width <= 0 || height <= 0) {
			throw new RuntimeException("Drawing Empty Section");
		}
		else if (width == 1 && height == 1) {
			return this.cells.get(minX).get(minY).draw(cellSize);
		} 
		else {
			if (width == 1) {
				// height is greater than 1
				int midY = (minY + maxY) / 2;
				WorldImage topHalf = 
						this.drawSection(cellSize, minX, minY, maxX, midY);
				WorldImage botHalf = 
						this.drawSection(cellSize, minX, midY, maxX, maxY);
				return topHalf.overlayImages(botHalf);
			} 
			else {
				int midX = (minX + maxX) / 2;
				WorldImage leftHalf = 
						this.drawSection(cellSize, minX, minY, midX, maxY);
				WorldImage rightHalf = 
						this.drawSection(cellSize, midX, minY, maxX, maxY);
				return leftHalf.overlayImages(rightHalf);
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Maze creation functions

	// put all walls in maze back up
	void wallsUp() {
		for (Edge edge: edges) {
			edge.isBlocking = true;
		}
	}
	
	// EFFECT: modifies the weights of the edges to give them random weights
	void assignRandomWeights() {
		for (Edge edge: edges) {
			edge.weight = rand.nextDouble();
		}
		edges.sort(new EdgeWeightComp());
	}

//	// EFFECT: modifies isBlocking fields of edges to generate a maze using
//	//   Kruskal's algorithm immediately
//	void generateMazeFast() {
//		UnionFindPosn uFind = new UnionFindPosn(width, height);
//		int edgesUsed = 0;
//		int edgesNeeded = this.width * this.height - 1;
//		int currEdge = 1;
//
//		while (edgesUsed < edgesNeeded) {
//			Edge nextEdge = this.edges.get(currEdge);
//			Posn cell1Posn = new Posn(nextEdge.cell1.x, nextEdge.cell1.y);
//			Posn cell2Posn = new Posn(nextEdge.cell2.x, nextEdge.cell2.y);
//			if (uFind.sameGroup(cell1Posn, cell2Posn)) {
//				// Do nothing
//			}
//			else {
//				nextEdge.isBlocking = false;
//				edgesUsed += 1;
//				uFind.connect(cell1Posn, cell2Posn);
//			}
//			currEdge += 1;
//		}
//
//	}
}

// a union-find structure for creating groups of Posns among a rectangular,
// strictly positive set of Posns cornered at the origin
class UnionFindPosn {
	ArrayList<ArrayList<Posn>> map;

	// Constructor initializing every posn to be grouped with itself
	UnionFindPosn(int width, int height) {
		this.map = new ArrayList<ArrayList<Posn>>();

		// connect each posn to itself
		for (int x = 0; x < width; x += 1) {
			map.add(new ArrayList<Posn>());
			for (int y = 0; y < height; y += 1) {
				map.get(x).add(new Posn(x, y));
			}
		}
	}

	// are the given posns in the same group?
	boolean sameGroup(Posn p1, Posn p2) {
		return samePosn(this.getGroup(p1), this.getGroup(p2));
	}

	// connect the two groups containing the given Posns
	void connect(Posn p1, Posn p2) {
		Posn group1 = this.getGroup(p1);
		Posn group2 = this.getGroup(p2);
		this.map.get(group1.x).set(group1.y, group2);
	}

	// are the 2 given posns the same? 
	boolean samePosn(Posn p1, Posn p2) {
		return p1.x == p2.x && p1.y == p2.y;
	}

	// find the posn labeling the group containing p
	Posn getGroup(Posn p) {
		Posn curr = p;
		Posn next = this.map.get(curr.x).get(curr.y);
		while(!this.samePosn(curr, next)) {
			curr = next;
			next = this.map.get(curr.x).get(curr.y);
		}
		// by the test condition returning curr or next is equivalent
		// using this.samePosn
		return next;
	}
}

// to animate algorithms on a maze
abstract class MazeAnimator {
	Maze maze;

	MazeAnimator(Maze maze) {
		this.maze = maze;
	}

	// EFFECT: update animation on tick
	abstract void onTick();

	// draw an the frame of the animation onto given background
	WorldImage drawOnto(int cellSize, WorldImage bg) {
		WorldImage mazeImg = this.maze.drawOnto(cellSize, bg);
		// bottom middle of screen
		Posn textLoc = new Posn(bg.getWidth() / 2, bg.getHeight() - 7);
		return mazeImg.overlayImages(
				new TextImage(textLoc, this.status(), 15, Color.BLACK));
	}
	
	// get the status text of this animation
	abstract String status();

	// is this animation complete?
	abstract boolean isComplete();
}

// a blank maze animator that just shows a maze
class IdleAnimator extends MazeAnimator {
	IdleAnimator(Maze maze) {
		super(maze);
	}

	// EFFECT: update this Animator's fields to progress one step
	void onTick() {
		
	}

	// is this animation complete?
	boolean isComplete() {
		return true;
	}

	// get the status text of this animation
	String status() {
		return "Idle";
	}
}

// animate Kruskal generation of a maze
class KruskalAnimator extends MazeAnimator {
	UnionFindPosn uFind;
	int edgesUsed;
	int edgesNeeded;
	int currEdge;

	KruskalAnimator(Maze maze, int width, int height) {
		super(maze);
		this.maze.wallsUp();
		
		uFind = new UnionFindPosn(width, height);
		edgesUsed = 0;
		edgesNeeded = width * height - 1;
		currEdge = 0;
	}

	// EFFECT: update this Animator's fields to progress one step
	void onTick() {
		if (!this.isComplete()) {
			Edge nextEdge = this.maze.edges.get(currEdge);
			Posn cell1Posn = new Posn(nextEdge.cell1.x, nextEdge.cell1.y);
			Posn cell2Posn = new Posn(nextEdge.cell2.x, nextEdge.cell2.y);
			currEdge += 1;
			
			// try to find a working edge each tick
			while (uFind.sameGroup(cell1Posn, cell2Posn)) {
				nextEdge = this.maze.edges.get(currEdge);
				cell1Posn = new Posn(nextEdge.cell1.x, nextEdge.cell1.y);
				cell2Posn = new Posn(nextEdge.cell2.x, nextEdge.cell2.y);
				currEdge += 1;
			}
			nextEdge.isBlocking = false;
			edgesUsed += 1;
			uFind.connect(cell1Posn, cell2Posn);
		}
	}

	// is this animation complete?
	boolean isComplete() {
		return edgesUsed >= edgesNeeded;
	}

	// get the status text of this animation
	String status() {
		return "Generating maze: " + this.edgesUsed + "/" + this.edgesNeeded;
	}
}

// to instantly complete any other animator
class InstantAnimator extends MazeAnimator {
	MazeAnimator anim;
	
	InstantAnimator(Maze maze, MazeAnimator anim) {
		super(maze);
		this.anim = anim;
	}

	// instantly complete animation
	void onTick() {
		while(!this.anim.isComplete()) {
			this.anim.onTick();
		}
	}

	// get status for this animator
	String status() {
		return "Completing Animation";
	}

	// is this animation complete?
	boolean isComplete() {
		return this.anim.isComplete();
	}
	
	
}

// to represent a world containing a maze
class MazeWorld extends World {

	int width; // in cells
	int height; // in cells
	int cellSize; // in pixels
	Maze maze;
	// animates certain algorithms on the maze
	MazeAnimator animator;

	MazeWorld(int width, int height, int cellSize) {
		this.width = width;
		this.height = height;
		this.cellSize = cellSize;
		this.maze = new Maze(width, height);
		this.animator = new IdleAnimator(maze);
	}

	public WorldImage makeImage() {
		int pWidth = this.getPixelWidth();
		int pHeight = this.getPixelHeight();
		WorldImage bg = new RectangleImage(
				new Posn(pWidth / 2, pHeight / 2),
				pWidth, pHeight, Color.GRAY);

		return this.animator.drawOnto(this.cellSize, bg);
	}
	
	// update the world on each tick
	public void onTick() {
		if (this.animator.isComplete()) {
			this.animator = new IdleAnimator(maze);
		}
		this.animator.onTick();
	}

	// handle user key presses
	public void onKeyEvent(String ke) {
		// assign random weights
		if (ke.equals("r")) {
			this.maze.assignRandomWeights();
		}
		// generate maze
		else if (ke.equals("g")) {
			this.animator =
					new KruskalAnimator(this.maze, this.width, this.height);
		}
		// reset maze
		else if (ke.equals("\b")) {
			this.maze = new Maze(this.width, this.height);
			this.animator = new IdleAnimator(this.maze);
		}
		// complete animation
		else if (ke.equals("\n")) {
			this.animator = new InstantAnimator(this.maze, this.animator);
		}
		else {
			System.out.println(ke);
		}
	}

	// return the width of the maze in pixels 
	int getPixelWidth() {
		return this.width * this.cellSize;
	}

	// return the height of the maze in pixels 
	int getPixelHeight() {
		return this.height * this.cellSize + 20;
	}
}

// tests and examples for mazes
class ExamplesMaze {

	// test to start off big bang
	void testBigBang(Tester t) {
		MazeWorld initWorld = new MazeWorld(20, 20, 20);

		initWorld.bigBang(initWorld.getPixelWidth(),
				initWorld.getPixelHeight(),
				0.1);
	}
}