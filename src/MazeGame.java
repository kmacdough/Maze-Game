import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

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
    boolean traversed;
    boolean onPath; // is this cell on the direct path to the exit?

    // Default constructor
    Cell(int x, int y, Edge left, Edge top, Edge right, Edge bot, 
            boolean traversed, boolean onPath) {
        this.x = x;
        this.y = y;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bot = bot;
        this.traversed = traversed;
        this.onPath = onPath;
    }

    // Initializing constructor
    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.left = null;
        this.top = null;
        this.right = null;
        this.bot = null;
        this.traversed = false;
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
        if (this.onPath) {
            return new Color(250, 70, 70);
        }
        else if (this.traversed) {
            return new Color(150, 150, 220);	        
        } 
        else {
            // Hue 282
            return new Color(203, 188, 227);
        }
    }
    
    // Returns the name of this cell as a string of its position
    String getName() {
        return Integer.toString(this.x).concat(Integer.toString(this.y));
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

    // Returns the bottom-right cell of this maze
    Cell getFinalCell() {
        ArrayList<Cell> lastCol = this.cells.get(this.cells.size() - 1);
        return lastCol.get(lastCol.size() - 1);
    }

    // Returns the top-left cell of this maze
    Cell getFirstCell() {
        return this.cells.get(0).get(0);
    }

    // Resets every cell in this maze to be not marked as traversed
    // or on path
    void resetTraversals() {
        for (ArrayList<Cell> col: cells) {
            for (Cell cell: col) {
                cell.traversed = false;
                cell.onPath = false;
            }
        }
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

    // react to keystrokes from user
    void onKeyEvent(String ke) {

    }

    // get the status text of this animation
    abstract String status();

    // is this animation complete?
    abstract boolean isComplete();

    // next animator to use when done
    abstract MazeAnimator nextAnimator();
}

// animate a depth-first search of a maze
class DFSAnimator extends MazeAnimator {
    // Note: cameFromCell connects each cell to its previous cell
    HashMap<Cell, Cell> cameFromCell;
    
    Stack<Cell> worklist;
    boolean completed;
    int moves;

    DFSAnimator(Maze maze) {
        super(maze);
        cameFromCell = new HashMap<Cell, Cell>();
        this.worklist = new Stack<Cell>();
        this.worklist.push(this.maze.getFirstCell());
        this.completed = false;
        this.moves = 0;
    }

    // EFFECT: update this Animator's fields to progress one step
    void onTick() {
        if (!this.isComplete() && !this.worklist.empty()) {
            Cell next = worklist.pop();

            if (next.traversed) {
                // Do Nothing
            }
            else if (next == this.maze.getFinalCell()) {
                System.out.println("Game is won");
                // TODO: Get reconstruct working
//                this.reconstruct(next);
                this.completed = true;
            }
            else {
                if (!next.left.isBlocking) {
                    Cell left = next.left.cell1;
                    this.worklist.push(left);
                    this.cameFromCell.put(left, next);
                }
                if (!next.top.isBlocking) {
                    Cell top = next.top.cell1;
                    this.worklist.push(top);
                    this.cameFromCell.put(top, next);
                }
                if (!next.right.isBlocking) {
                    Cell right = next.right.cell2;
                    this.worklist.push(right);
                    this.cameFromCell.put(right, next);
                }
                if (!next.bot.isBlocking) {
                    Cell bot = next.bot.cell2;
                    this.worklist.push(bot);
                    this.cameFromCell.put(bot, next);
                }
                this.moves += 1;
            }
            next.traversed = true;
        } 
        else {
            this.completed = true;
        }
    }
    
    // draw an the frame of the animation onto given background
    WorldImage drawOnto(int cellSize, WorldImage bg) {
        WorldImage mazeImg = this.maze.drawOnto(cellSize, bg);
        // bottom middle of screen
        Posn textLoc = new Posn(bg.getWidth() / 2, bg.getHeight() - 7);
        
        if (this.isComplete()) {
            Posn winTextLoc = new Posn(bg.getWidth() / 2, bg.getHeight() / 2);
            mazeImg = mazeImg.overlayImages(
                new TextImage(winTextLoc, "Search Complete", 30, Color.GREEN));
        }
        
        Posn winTextLoc = new Posn(bg.getWidth() / 2, 20);
        mazeImg = mazeImg.overlayImages(
            new TextImage(winTextLoc, "Moves:" + this.moves, 20, Color.BLACK));
        
        return mazeImg.overlayImages(
                new TextImage(textLoc, this.status(), 15, Color.BLACK));
    }
    
    // find direct path from end to start and mark all cells in path
    // as being on the path
    void reconstruct(Cell cell) {
        Cell curCell = cell;
        Cell prevCell = this.cameFromCell.get(curCell);
        while (curCell != this.maze.getFirstCell() && !curCell.onPath) {
            curCell.onPath = true;
            
            curCell = prevCell;
            
            prevCell = this.cameFromCell.get(prevCell);
            this.cameFromCell.remove(curCell);
            
//            if (curCell == prevEdge.cell1.getName()) {
//                curCell = prevEdge.cell2.getName();
//            }
//            else if (curCell == prevEdge.cell2.getName()) {
//                curCell = prevEdge.cell1.getName();
//            }
            
            
//            prevEdge = this.cameFromEdge.get(curCell);
            System.out.println(curCell);
        }
    }

    // is this animation complete?
    boolean isComplete() {
        return this.completed;
    }

    // get the status text of this animation
    String status() {
        if (this.isComplete()) {
            return "Completed Depth First Search";
        } else {
            return "Depth First Searching";
        }
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return this;
    }
}

//animate a breadth-first search of a maze
class BFSAnimator extends MazeAnimator {
    // TODO: Make our own implementation of queue
    LinkedList<Cell> worklist;
    boolean completed;
    int moves;

    BFSAnimator(Maze maze) {
        super(maze);
        this.worklist = new LinkedList<Cell>();
        this.worklist.add(this.maze.getFirstCell());
        this.completed = false;
        this.moves = 0;
    }

    // EFFECT: update this Animator's fields to progress one step
    void onTick() {
        if (!this.isComplete() && !this.worklist.isEmpty()) {
            Cell next = worklist.remove();

            if (next.traversed) {
                // Do Nothing
            }
            else if (next == this.maze.getFinalCell()) {
                System.out.println("Game is won");
                this.completed = true;
            }
            else {
                if (!next.left.isBlocking) {
                    this.worklist.add(next.left.cell1);
                }
                if (!next.top.isBlocking) {
                    this.worklist.add(next.top.cell1);
                }
                if (!next.right.isBlocking) {
                    this.worklist.add(next.right.cell2);
                }
                if (!next.bot.isBlocking) {
                    this.worklist.add(next.bot.cell2);
                }
                this.moves += 1;
            }

            next.traversed = true;
        }
    }

    // draw an the frame of the animation onto given background
    WorldImage drawOnto(int cellSize, WorldImage bg) {
        WorldImage mazeImg = this.maze.drawOnto(cellSize, bg);
        // bottom middle of screen
        Posn textLoc = new Posn(bg.getWidth() / 2, bg.getHeight() - 7);
        
        if (this.isComplete()) {
            Posn winTextLoc = new Posn(bg.getWidth() / 2, bg.getHeight() / 2);
            mazeImg = mazeImg.overlayImages(
                new TextImage(winTextLoc, "Search Complete", 30, Color.GREEN));
        }
        
        Posn winTextLoc = new Posn(bg.getWidth() / 2, 20);
        mazeImg = mazeImg.overlayImages(
            new TextImage(winTextLoc, "Moves:" + this.moves, 20, Color.BLACK));
        
        return mazeImg.overlayImages(
                new TextImage(textLoc, this.status(), 15, Color.BLACK));
    }
    
    // is this animation complete?
    boolean isComplete() {
        return this.completed;
    }

    // get the status text of this animation
    String status() {
        if (this.isComplete()) {
            return "Completed Breadth First Search";
        } else {
            return "Breadth First Searching";
        }
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return this;
    }
}

// User-controlled animator for maze traversal
class PlayAnimator extends MazeAnimator {
    Cell head;
    int moves;
    
    PlayAnimator(Maze maze) {
        super(maze);
        this.head = maze.getFirstCell();
        head.traversed = true;
        head.onPath = true;
        moves = 0;
    }

    // EFFECT: update this Animator's fields to progress one step
    void onTick() {
        
    }
    
    void onKeyEvent(String ke) {
        if (!this.isComplete()) {
            if (ke == "left" && !this.head.left.isBlocking) {
                this.head.onPath = false;
                this.head = this.head.left.cell1;
                this.head.traversed = true;
                this.head.onPath = true;
                moves += 1;
            }
            if (ke == "up" && !this.head.top.isBlocking) {
                this.head.onPath = false;
                this.head = this.head.top.cell1;
                this.head.traversed = true;
                this.head.onPath = true;
                moves += 1;
            }
            if (ke == "right" && !this.head.right.isBlocking) {
                this.head.onPath = false;
                this.head = this.head.right.cell2;
                this.head.traversed = true;
                this.head.onPath = true;
                moves += 1;
            }
            if (ke == "down" && !this.head.bot.isBlocking) {
                this.head.onPath = false;
                this.head = this.head.bot.cell2;
                this.head.traversed = true;
                this.head.onPath = true;
                moves += 1;
            }
        }
    }

    // draw an the frame of the animation onto given background
    WorldImage drawOnto(int cellSize, WorldImage bg) {
        WorldImage mazeImg = this.maze.drawOnto(cellSize, bg);
        // bottom middle of screen
        Posn textLoc = new Posn(bg.getWidth() / 2, bg.getHeight() - 7);
        
        if (this.isComplete()) {
            Posn winTextLoc = new Posn(bg.getWidth() / 2, bg.getHeight() / 2);
            mazeImg = mazeImg.overlayImages(
                    new TextImage(winTextLoc, "YOU WIN", 30, Color.GREEN));
        }
        
        Posn winTextLoc = new Posn(bg.getWidth() / 2, 20);
        mazeImg = mazeImg.overlayImages(
            new TextImage(winTextLoc, "Moves:" + this.moves, 20, Color.BLACK));
        
        return mazeImg.overlayImages(
            new TextImage(textLoc, this.status(), 15, Color.BLACK));
    }
    
    // is this animation complete?
    boolean isComplete() {
        return this.head == this.maze.getFinalCell();
    }

    // get the status text of this animation
    String status() {
        return "Idle";
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return this;
    }
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

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return this;
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
            this.edgesUsed += 1;
            this.uFind.connect(cell1Posn, cell2Posn);
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

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return new IdleAnimator(this.maze);
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

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return this.anim.nextAnimator();
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
            this.animator = animator.nextAnimator();
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
            this.maze.resetTraversals();
            this.animator =
                    new KruskalAnimator(this.maze, this.width, this.height);
        }     
        // begin depth-first search
        else if (ke.equals("d")) {
            this.maze.resetTraversals();
            this.animator = new DFSAnimator(this.maze);
        }
        // begin breadth-first search
        else if (ke.equals("b")) {
            this.maze.resetTraversals();
            this.animator = new BFSAnimator(this.maze);
        }        
        // play maze
        else if (ke.equals("p")) {
            this.maze.resetTraversals();
            this.animator = new PlayAnimator(this.maze);
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

        animator.onKeyEvent(ke);
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