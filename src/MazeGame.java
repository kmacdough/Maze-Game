import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

// to represent a stack of Ts
class Stack<T> {
    // stores all items in the stack, with the last item being the top
    ArrayList<T> stack;

    Stack() {
        this.stack = new ArrayList<T>();
    }

    // EFFECT: add an element to the end of stack
    void push(T item) {
        stack.add(item);
    }

    // EFFECT: remove the last element of stack, and return it
    T pop() {
        return this.stack.remove(stack.size() - 1);
    }

    // the size of this stack
    int size() {
        return stack.size();
    }

    // is this stack empty?
    boolean isEmpty() {
        return this.size() == 0;
    }
}

// to represent a queue of Ts
class Queue<T> {
    // location of first item in queue
    int head; 
    // stores all items in the queue with given head and last item at the end
    ArrayList<T> queue;

    Queue() {
        this.head = 0;
        this.queue = new ArrayList<T>();
    }

    // EFFECT: adds given item to the end of the queue
    void enqueue(T item) {
        this.queue.add(item);
    }

    // EFFECT: gets item at head, increments head, then returns item
    T dequeue() {
        T item = this.queue.get(head);
        head += 1;

        // if the extra space at the front is too wasteful, reset
        if (head > 20 && this.head > this.queue.size() / 3) {
            this.reset();
        }

        return item;
    }

    // the size of this queue
    int size() {
        return this.queue.size() - this.head;
    }

    // is this queue empty?
    boolean isEmpty() {
        return this.size() == 0;
    }

    // EFFECT: creates a new ArrayList with all items in queue and head = 0
    void reset() {
        ArrayList<T> newQueue = new ArrayList<T>(this.size());

        // add all items to the new list
        while (!this.isEmpty()) {
            // dequeue code without if block
            newQueue.add(this.queue.get(head));
            head += 1;
        }

        this.queue = newQueue;
        this.head = 0;
    }
}

// to represent a square in the maze game
class Cell {
    // position in grid coordinates
    int x;
    int y;
    // edges of the cell
    Edge left;
    Edge top;
    Edge right;
    Edge bot;
    // has this cell been traversed?
    boolean traversed;
    // is this cell on the direct path to the exit?
    boolean onPath;

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

    // get the color of this cell
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
}

// to represent a wall between two cells
abstract class Edge {
    // default color of edges
    static final Color COLOR = new Color(73, 46, 116);

    // the two cells this edge connects to; order is important but depends
    // on orientation
    Cell cell1;
    Cell cell2;
    // is this edge blocking movement?
    boolean isBlocking;
    // traversal weight of this edge
    double weight;

    // Create an edge connecting given cells with given attributes
    Edge(Cell cell1, Cell cell2, boolean isBlocking, double weight) {
        this.cell1 = cell1;
        this.cell2 = cell2;
        this.isBlocking = isBlocking;
        this.weight = weight;
    }

    // create a picture of this edge if on screen
    WorldImage draw(int cellSize) {
        // non-existant walls draw off screen (for lack of dummy image type)
        if (!isBlocking) {
            return new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK);
        }

        return this.getImage(cellSize);
    }

    // gets the image of this edge regardless of isBlocking
    abstract WorldImage getImage(int cellSize);

    // get the color of this edge
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

        // coordinates of the lower cell in grid coordinates
        int botCellX = this.cell2.x;
        int botCellY = this.cell2.y ;
        // graphical center of edge
        Posn center = new Posn(botCellX * cellSize + cellSize / 2,
                botCellY * cellSize);

        return new RectangleImage(
                center, cellSize, cellSize / 10 + 1, this.getColor());
    }
}

// to represent an edge that is on the border of the maze
class BorderEdge extends Edge {
    // Default Constructor
    BorderEdge(Cell cell) {
        super(cell, cell, true, 0);
    }

    // returns the image of this edge
    WorldImage getImage(int cellSize) {
        // an image off the canvas for lack of a dummy Image
        return new RectangleImage(new Posn(-1, -1), 0, 0, Color.GRAY);
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

        // should never be tasked with drawing empty sections
        if (width <= 0 || height <= 0) {
            throw new RuntimeException("Drawing Empty Section");
        }
        // base case: draw 1 cell
        else if (width == 1 && height == 1) {
            return this.cells.get(minX).get(minY).draw(cellSize);
        } 
        // halve the drawing vertically
        else if (width == 1) {
            // CONTEXT: height is greater than 1
            int midY = (minY + maxY) / 2;
            WorldImage topHalf = 
                    this.drawSection(cellSize, minX, minY, maxX, midY);
            WorldImage botHalf = 
                    this.drawSection(cellSize, minX, midY, maxX, maxY);
            return topHalf.overlayImages(botHalf);
        } 
        // halve the drawing horizontally
        else {
            int midX = (minX + maxX) / 2;
            WorldImage leftHalf = 
                    this.drawSection(cellSize, minX, minY, midX, maxY);
            WorldImage rightHalf = 
                    this.drawSection(cellSize, midX, minY, maxX, maxY);
            return leftHalf.overlayImages(rightHalf);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Maze creation functions

    // EFFECT: modifies edges to put all walls in maze back up
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
        Collections.sort(edges, new EdgeWeightComp());
    }

    // the bottom-right cell of this maze
    Cell getFinalCell() {
        ArrayList<Cell> lastCol = this.cells.get(this.cells.size() - 1);
        return lastCol.get(lastCol.size() - 1);
    }

    // the top-left cell of this maze
    Cell getFirstCell() {
        return this.cells.get(0).get(0);
    }

    // EFFECT: modifies cells to resets every cell in this maze to be marked
    // as not traversed and not on path
    void resetTraversals() {
        for (ArrayList<Cell> col: cells) {
            for (Cell cell: col) {
                cell.traversed = false;
                cell.onPath = false;
            }
        }
    }
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

    // EFFECT: modifies map to connect the two groups
    //   containing the given Posns
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
        while (!this.samePosn(curr, next)) {
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

    // EFFECT: unknown to react to keystrokes from user
    void onKeyEvent(String ke) {
        // DO NOTHING: most animators don't need to react to keystrokes; those
        //   that do can override this method
    }

    // does this animator guarantee termination?
    boolean alwaysTerminates() {
        return true;
    }

    // get the status text of this animation
    abstract String status();

    // is this animation complete?
    abstract boolean isComplete();

    // next animator to use when done
    abstract MazeAnimator nextAnimator();
}

// to represent animators for solving the maze
abstract class SolveAnimator extends MazeAnimator {
    // Note: cameFromCell connects each cell to its previous cell
    HashMap<Cell, Cell> cameFromCell;
    boolean completed;
    int moves;

    SolveAnimator(Maze maze) {
        super(maze);
        this.cameFromCell = new HashMap<Cell, Cell>();
        completed = false;
        moves = 0;
    }

    // EFFECT: modify work list and cameFromCell to try to make the move
    //   between given cells
    void tryAddMove(Cell to, Cell from) {
        if (!to.traversed) {
            this.addWork(to);
            this.cameFromCell.put(to, from);
        }
    }

    // EFFECT: add given cell to the worklist
    abstract void addWork(Cell cell);

    // EFFECT: modify cells onPath field to find direct path from end to start
    void reconstruct(Cell cell) {
        Cell curCell = cell;
        Cell prevCell = this.cameFromCell.get(curCell);
        while (curCell != this.maze.getFirstCell()/* && !curCell.onPath*/) {
            curCell.onPath = true;

            curCell = prevCell;

            prevCell = this.cameFromCell.get(prevCell);
        }
        curCell.onPath = true;
    }

    // is this animation completed?
    boolean isComplete() {
        return this.completed;
    }
}

// to represent an automatic solver animation
abstract class AutoSolveAnimator extends SolveAnimator {

    AutoSolveAnimator(Maze maze) {
        super(maze);
    }

    // EFFECT: changes the maze and fields on this animator to progress
    //   algorithm
    void onTick() {
        if (!this.hasWork()) {
            this.completed = true;
        }
        else if (!this.isComplete()) {
            Cell next = this.getWork();
            // indicate we've now visited this cell
            next.traversed = true;

            if (next == this.maze.getFinalCell()) {
                this.reconstruct(next);
                this.completed = true;
            }
            // try adding all unblocked neighbor cells
            else {
                if (!next.left.isBlocking) {
                    this.tryAddMove(next.left.cell1, next);
                }
                if (!next.top.isBlocking) {
                    this.tryAddMove(next.top.cell1, next);
                }
                if (!next.right.isBlocking) {
                    this.tryAddMove(next.right.cell2, next);
                }
                if (!next.bot.isBlocking) {
                    this.tryAddMove(next.bot.cell2, next);
                }
                this.moves += 1;
            }
        }

    }

    // get next cell to work on
    abstract Cell getWork();

    // are there more cells to work on?
    abstract boolean hasWork();
}

// animate a depth-first search of a maze
class DFSAnimator extends AutoSolveAnimator {

    Stack<Cell> worklist;

    DFSAnimator(Maze maze) {
        super(maze);
        this.worklist = new Stack<Cell>();
        this.worklist.push(this.maze.getFirstCell());
    }

    // get the status text of this animation
    String status() {
        return "Depth First Searching.   Moves: " + this.moves;
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return new MsgAnimator(this.maze,
                "Completed Depth First Search.   Moves: " + this.moves);
    }

    // EFFECT: add given cell to the worklist
    void addWork(Cell cell) {
        this.worklist.push(cell);
    }

    // EFFECT: remove and return next item from worklist to work on
    Cell getWork() {
        return this.worklist.pop();
    }

    // are there more cells to work on?
    boolean hasWork() {
        return !this.worklist.isEmpty();
    }
}

//animate a breadth-first search of a maze
class BFSAnimator extends AutoSolveAnimator {
    Queue<Cell> worklist;

    BFSAnimator(Maze maze) {
        super(maze);
        this.worklist = new Queue<Cell>();
        this.worklist.enqueue(this.maze.getFirstCell());
    }

    // is this animation complete?
    boolean isComplete() {
        return this.completed;
    }

    // get the status text of this animation
    String status() {
        return "Breadth First Searching.   Moves: " + this.moves;
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return new MsgAnimator(this.maze,
                "Completed Breadth First Search.   Moves: " + this.moves);
    }

    // EFFECT: modify worklist to add given cell
    void addWork(Cell cell) {
        this.worklist.enqueue(cell);
    }

    // EFFECT: remove and return next item from worklist to work on
    Cell getWork() {
        return this.worklist.dequeue();
    }

    // are there more cells to work on?
    boolean hasWork() {
        return !this.worklist.isEmpty();
    }
}

// User-controlled animator for maze traversal
class PlayAnimator extends SolveAnimator {
    Cell head;

    PlayAnimator(Maze maze) {
        super(maze);
        this.head = maze.getFirstCell();
        head.traversed = true;
        head.onPath = true;
    }

    // EFFECT: update this Animator's fields to progress one step
    void onTick() {
        // do nothing since only operates on keypresses
    }

    // EFFECT: modify this Animator's fields to react to keystrokes
    void onKeyEvent(String ke) {
        if (!this.isComplete()) {
            // move left
            if (ke.equals("left") && !this.head.left.isBlocking) {
                tryAddMove(this.head.left.cell1, this.head);
                moves += 1;
            }
            // move left
            if (ke.equals("up") && !this.head.top.isBlocking) {
                tryAddMove(this.head.top.cell1, this.head);
                moves += 1;
            }
            // move left
            if (ke.equals("right") && !this.head.right.isBlocking) {
                tryAddMove(this.head.right.cell2, this.head);
                moves += 1;
            }
            // move left
            if (ke.equals("down") && !this.head.bot.isBlocking) {
                tryAddMove(this.head.bot.cell2, this.head);
                moves += 1;
            }

        }
    }

    // is this animation complete?
    boolean isComplete() {
        return this.completed;
    }

    // get the status text of this animation
    String status() {
        return "Player Solving Puzzle.   Moves: " + this.moves;
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        return new MsgAnimator(this.maze,
                "Puzzle Complete!   Moves: " + this.moves);
    }

    // EFFECT: modify this animator and from and to Cells to move player
    //   between given cells
    void tryAddMove(Cell to, Cell from) {
        super.tryAddMove(to, from);


        to.onPath = true;
        to.traversed = true;
        from.onPath = false;
        this.head = to;

        // check to see if this keystroke completed maze
        if (this.head == this.maze.getFinalCell()) {
            this.completed = true;
            this.reconstruct(head);
        }
    }

    // EFFECT: modify worklist to add given cell
    void addWork(Cell cell) {
        // DO NOTHING since no worklist
    }

    // does this animator always terminate?
    boolean alwaysTerminates() {
        return false;
    }
}


// a blank maze animator that just shows a maze
class IdleAnimator extends MazeAnimator {
    IdleAnimator(Maze maze) {
        super(maze);
    }

    // EFFECT: update this Animator's fields to progress one step
    void onTick() {
        // DO NOTHING: animator is idle
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

// a blank maze animator that allows for a specific message to be played
class MsgAnimator extends IdleAnimator {
    String msg;

    // Construct a message animator displaying given message
    MsgAnimator(Maze maze, String msg) {
        super(maze);
        this.msg = msg;
    }

    // get the status text of this animation
    String status() {
        return msg;
    }
}

// animate Kruskal generation of a maze
class KruskalAnimator extends MazeAnimator {
    // union find structure, representing Cells as Posns of their location
    UnionFindPosn uFind;
    // number of edges used in current tree
    int edgesUsed;
    // number of edges for a spanning tree
    int edgesNeeded;
    // index of edge about to be checked
    int currEdge;

    KruskalAnimator(Maze maze) {
        super(maze);
        this.maze.wallsUp();
        
        int width = maze.width;
        int height = maze.height;

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
        if (this.anim.alwaysTerminates()) {
            while (!this.anim.isComplete()) {
                this.anim.onTick();
            }
        }
    }

    // get status for this animator
    String status() {
        return "Completing Animation";
    }

    // is this animation complete?
    boolean isComplete() {
        if (this.anim.alwaysTerminates()) {
            return this.anim.isComplete();
        }
        else {
            return true;
        }
    }

    // next animator to use when done
    MazeAnimator nextAnimator() {
        if (this.anim.alwaysTerminates()) {
            return this.anim.nextAnimator();
        }
        else {
            return this.anim;
        }
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

    // Constructor automatically initializing with a small maze
    MazeWorld() {
        initialize(50, 30, 20, "Large Maze");
    }

    // Constructor to make a maze with specific dimensions and label
    MazeWorld(int width, int height, int cellSize, String label) {
        initialize(width, height, cellSize, label);
    }

    // EFFECT: assign all fields to generate a fresh maze
    void initialize(int width, int height, int cellSize, String label) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.maze = new Maze(width, height);
        this.animator = new MsgAnimator(maze, label);
    }

    // creates an image of this maze world
    public WorldImage makeImage() {
        int pWidth = this.getPixelWidth();
        int pHeight = this.getPixelHeight();
        WorldImage bg = new RectangleImage(
                new Posn(pWidth / 2, pHeight / 2),
                pWidth, pHeight, Color.GRAY);

        return this.animator.drawOnto(this.cellSize, bg);
    }

    // EFFECT: update the world on each tick
    public void onTick() {
        // delegates onTick to animators
        if (this.animator.isComplete()) {
            this.animator = animator.nextAnimator();
        }
        this.animator.onTick();
    }

    // EFFECT: handle user key presses
    public void onKeyEvent(String ke) {
        // assign random weights
        if (ke.equals("r")) {
            // only adjust weights if maze is idle
            if (this.animator.isComplete()) {
                this.maze.assignRandomWeights();
                this.animator = new MsgAnimator(this.maze, "Weights Randomized");
            }
        }
        // generate maze
        else if (ke.equals("g")) {
            this.maze.resetTraversals();
            this.animator =
                    new KruskalAnimator(this.maze);
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
        // Set different size mazes
        else if (ke.equals("1")) {
            initialize(10, 6, 100, "Tiny Maze");
        }
        else if (ke.equals("2")) {
            initialize(25, 15, 40, "Small Maze");
        }
        else if (ke.equals("3")) {
            initialize(50, 30, 20, "Large Maze");
        }
        else if (ke.equals("4")) {
            initialize(100, 60, 10, "Huge Maze");
        }
        else if (ke.equals("5")) {
            initialize(200, 120, 5, "Gigantic Maze");
        }

        // allow animators to react to keys
        animator.onKeyEvent(ke);
    }

    // the width of the maze in pixels 
    int getPixelWidth() {
        return this.width * this.cellSize;
    }

    // the height of the maze in pixels 
    int getPixelHeight() {
        return this.height * this.cellSize + 25;
    }
}

// tests and examples for mazes
class ExamplesMaze {

    // test to start off big bang
    void donttestBigBang(Tester t) {
        MazeWorld initWorld = new MazeWorld();

        initWorld.bigBang(initWorld.getPixelWidth(),
                initWorld.getPixelHeight(),
                0.04);
    }

    // stack push pop size isEmpty
    /***************************************
     * Tests for Stack
     ***************************************/

    Stack<Integer> stack;

    // initialize stack
    void initializeStack() {
        stack = new Stack<Integer>();
    }

    // test push method on Stack
    void testPush(Tester t) {
        initializeStack();

        stack.push(3);
        stack.push(12);
        stack.push(9);

        t.checkExpect(stack.stack.get(0), 3);
        t.checkExpect(stack.stack.get(1), 12);
        t.checkExpect(stack.stack.get(2), 9);
    }

    // test pop method on Stack
    void testPopStack(Tester t) {
        initializeStack();

        stack.stack = new ArrayList<Integer>(Arrays.asList(3, 7, 8, 2));

        t.checkExpect(stack.pop(), 2);
        t.checkExpect(stack.pop(), 8);
        t.checkExpect(stack.pop(), 7);
        t.checkExpect(stack.pop(), 3);
    }

    // test push and pop methods on Stack together to be sure they work
    void testPushPopStack(Tester t) {
        initializeStack();

        stack.push(1);
        stack.push(2);
        stack.push(3);

        t.checkExpect(stack.pop(), 3);
        t.checkExpect(stack.pop(), 2);

        stack.push(4);

        t.checkExpect(stack.pop(), 4);
        t.checkExpect(stack.pop(), 1);
    }

    // test size method on Stack
    void testSizeStack(Tester t) {
        initializeStack();

        t.checkExpect(stack.size(), 0);
        stack.push(1);
        t.checkExpect(stack.size(), 1);
        stack.push(1);
        t.checkExpect(stack.size(), 2);
        stack.push(1);
        t.checkExpect(stack.size(), 3);

        stack.pop();
        t.checkExpect(stack.size(), 2);

        stack.push(1);
        t.checkExpect(stack.size(), 3);

        stack.pop();
        t.checkExpect(stack.size(), 2);
        stack.pop();
        t.checkExpect(stack.size(), 1);
        stack.pop();
        t.checkExpect(stack.size(), 0);

        for (int i = 0; i < 1000; i++) {
            stack.push(1);
        }
        t.checkExpect(stack.size(), 1000);
    }

    // test isEmpty method on Stack
    void testIsEmptyStack(Tester t) {
        initializeStack();

        t.checkExpect(stack.isEmpty(), true);
        stack.push(1);
        stack.push(1);
        stack.push(1);
        stack.push(1);
        t.checkExpect(stack.isEmpty(), false);
        stack.pop();
        stack.pop();
        t.checkExpect(stack.isEmpty(), false);
        stack.pop();
        stack.pop();
        t.checkExpect(stack.isEmpty(), true);
    }

    /***************************************
     * Tests for Queue
     ***************************************/

    Queue<Integer> queue;

    // initialize queue
    void initializeQueue() {
        queue = new Queue<Integer>();
    }

    // test enqueue method on Queue
    void testEnqueueQueue(Tester t) {
        initializeQueue();

        queue.enqueue(3);
        t.checkExpect(queue.queue.get(0), 3);

        queue.enqueue(5);
        queue.enqueue(7);
        queue.enqueue(12);
        t.checkExpect(queue.queue.get(0), 3);
        t.checkExpect(queue.queue.get(1), 5);
        t.checkExpect(queue.queue.get(2), 7);
        t.checkExpect(queue.queue.get(3), 12);
    }

    // test enqueue and dequeue together on Queue
    void testEnqueueDequeueQueue(Tester t) {
        initializeQueue();

        queue.enqueue(1);
        t.checkExpect(queue.dequeue(), 1);

        queue.enqueue(2);
        queue.enqueue(3);
        queue.enqueue(4);
        t.checkExpect(queue.dequeue(), 2);
        t.checkExpect(queue.dequeue(), 3);

        queue.enqueue(5);
        t.checkExpect(queue.dequeue(), 4);
        t.checkExpect(queue.dequeue(), 5);

        initializeQueue();

        // test large number of items and if/where it fails
        int testSize = 1000;
        for (int i = 0; i < testSize; i += 1) {
            queue.enqueue(i);
        }

        boolean pass = true;
        int firstFail = -1;
        for (int i = 0; i < testSize; i += 1) {
            if (pass && queue.dequeue() != i) {
                pass = false;
                firstFail = i;
            }
        }

        t.checkExpect(firstFail, -1);
    }

    // test size method on Queue
    void testSizeQueue(Tester t) {
        initializeQueue();

        t.checkExpect(queue.size(), 0);
        queue.enqueue(1);
        t.checkExpect(queue.size(), 1);
        queue.enqueue(1);
        t.checkExpect(queue.size(), 2);
        queue.enqueue(1);
        t.checkExpect(queue.size(), 3);

        queue.dequeue();
        t.checkExpect(queue.size(), 2);

        queue.enqueue(1);
        t.checkExpect(queue.size(), 3);

        queue.dequeue();
        t.checkExpect(queue.size(), 2);
        queue.dequeue();
        t.checkExpect(queue.size(), 1);
        queue.dequeue();
        t.checkExpect(queue.size(), 0);

        for (int i = 0; i < 1000; i++) {
            queue.enqueue(1);
        }
        t.checkExpect(queue.size(), 1000);
    }

    // test isEmpty method on Stack
    void testIsEmptyQueue(Tester t) {
        initializeQueue();

        t.checkExpect(queue.isEmpty(), true);
        queue.enqueue(1);
        queue.enqueue(1);
        queue.enqueue(1);
        queue.enqueue(1);
        t.checkExpect(queue.isEmpty(), false);
        queue.dequeue();
        queue.dequeue();
        t.checkExpect(queue.isEmpty(), false);
        queue.dequeue();
        queue.dequeue();
        t.checkExpect(queue.isEmpty(), true);
    }

    // test reset method on Stack
    void testResetQueue(Tester t) {
        initializeQueue();

        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        queue.enqueue(4);
        queue.enqueue(5);
        queue.dequeue();
        queue.dequeue();

        queue.reset();
        t.checkExpect(queue.head, 0);
        t.checkExpect(queue.size(), queue.queue.size());
        t.checkExpect(queue.dequeue(), 3);
        t.checkExpect(queue.dequeue(), 4);
        t.checkExpect(queue.dequeue(), 5);
    }


    /***************************************
     * Tests for Cell and Edge
     ***************************************/
    Cell cell00;
    Cell cell01;
    Cell cell02;
    Cell cell10;
    Cell cell11;
    Cell cell12;
    Cell cell20;
    Cell cell21;
    Cell cell22;

    Edge lrEdge00;
    Edge lrEdge01;
    Edge lrEdge02;
    Edge lrEdge10;
    Edge lrEdge11;
    Edge lrEdge12;
    Edge lrEdge20;
    Edge lrEdge21;
    Edge lrEdge22;
    Edge lrEdge30;
    Edge lrEdge31;
    Edge lrEdge32;

    Edge tbEdge00;
    Edge tbEdge01;
    Edge tbEdge02;
    Edge tbEdge03;
    Edge tbEdge10;
    Edge tbEdge11;
    Edge tbEdge12;
    Edge tbEdge13;
    Edge tbEdge20;
    Edge tbEdge21;
    Edge tbEdge22;
    Edge tbEdge23;

    // Initialize cells
    void initCells() {
        cell00 = new Cell(0, 0);
        cell01 = new Cell(0, 1);
        cell02 = new Cell(0, 2);
        cell10 = new Cell(1, 0);
        cell11 = new Cell(1, 1);
        cell12 = new Cell(1, 2);
        cell20 = new Cell(2, 0);
        cell21 = new Cell(2, 1);
        cell22 = new Cell(2, 2);
    }

    // Initialize edges, connected to cells
    void initEdges() {
        lrEdge00 = new LREdge(cell00, cell00, true, 0);
        lrEdge01 = new LREdge(cell01, cell01, true, 0);
        lrEdge02 = new LREdge(cell02, cell02, true, 0);
        lrEdge10 = new LREdge(cell00, cell10, true, 0);
        lrEdge11 = new LREdge(cell01, cell11, true, 0);
        lrEdge12 = new LREdge(cell02, cell12, true, 0);
        lrEdge20 = new LREdge(cell10, cell20, true, 0);
        lrEdge21 = new LREdge(cell11, cell21, true, 0);
        lrEdge22 = new LREdge(cell12, cell22, true, 0);
        lrEdge30 = new LREdge(cell20, cell20, true, 0);
        lrEdge31 = new LREdge(cell21, cell21, true, 0);
        lrEdge32 = new LREdge(cell22, cell22, true, 0);

        tbEdge00 = new TBEdge(cell00, cell00, true, 0);
        tbEdge01 = new TBEdge(cell00, cell01, true, 0);
        tbEdge02 = new TBEdge(cell01, cell02, true, 0);
        tbEdge03 = new TBEdge(cell02, cell02, true, 0);
        tbEdge10 = new TBEdge(cell10, cell10, true, 0);
        tbEdge11 = new TBEdge(cell10, cell11, true, 0);
        tbEdge12 = new TBEdge(cell11, cell12, true, 0);
        tbEdge13 = new TBEdge(cell12, cell12, true, 0);
        tbEdge20 = new TBEdge(cell20, cell20, true, 0);
        tbEdge21 = new TBEdge(cell20, cell21, true, 0);
        tbEdge22 = new TBEdge(cell21, cell22, true, 0);
        tbEdge23 = new TBEdge(cell22, cell22, true, 0);
    }

    // Connect cells to edges
    void connectCells() {
        cell00.left = lrEdge00;
        cell01.left = lrEdge01;
        cell02.left = lrEdge02;
        cell10.left = lrEdge10;
        cell11.left = lrEdge11;
        cell12.left = lrEdge12;
        cell20.left = lrEdge20;
        cell21.left = lrEdge21;
        cell22.left = lrEdge22;

        cell00.right = lrEdge10;
        cell01.right = lrEdge11;
        cell02.right = lrEdge12;
        cell10.right = lrEdge20;
        cell11.right = lrEdge21;
        cell12.right = lrEdge22;
        cell20.right = lrEdge30;
        cell21.right = lrEdge31;
        cell22.right = lrEdge32;

        cell00.top = tbEdge00;
        cell01.top = tbEdge01;
        cell02.top = tbEdge02;
        cell10.top = tbEdge10;
        cell11.top = tbEdge11;
        cell12.top = tbEdge12;
        cell20.top = tbEdge20;
        cell21.top = tbEdge21;
        cell22.top = tbEdge22;

        cell00.bot = tbEdge01;
        cell01.bot = tbEdge02;
        cell02.bot = tbEdge03;
        cell10.bot = tbEdge11;
        cell11.bot = tbEdge12;
        cell12.bot = tbEdge13;
        cell20.bot = tbEdge21;
        cell21.bot = tbEdge22;
        cell22.bot = tbEdge23;
    }

    // Change whether certain cells are traversed or onPath
    void initCellStatus() {
        cell00.traversed = true;
        cell01.traversed = true;
        cell02.onPath = true;
        cell20.onPath = true;
        cell21.traversed = true;
        cell21.onPath = true;
    }

    // Tests for the getColor method on Cell
    void testGetColorCell(Tester t) {
        initCells();

        Color untraversed = new Color(203, 188, 227);
        Color traversed = new Color(150, 150, 220);
        Color onPath = new Color(250, 70, 70);

        t.checkExpect(cell00.getColor(), untraversed);
        t.checkExpect(cell01.getColor(), untraversed);
        t.checkExpect(cell02.getColor(), untraversed);
        t.checkExpect(cell10.getColor(), untraversed);
        t.checkExpect(cell11.getColor(), untraversed);
        t.checkExpect(cell12.getColor(), untraversed);
        t.checkExpect(cell20.getColor(), untraversed);
        t.checkExpect(cell21.getColor(), untraversed);
        t.checkExpect(cell22.getColor(), untraversed);

        initCellStatus();

        t.checkExpect(cell00.getColor(), traversed);
        t.checkExpect(cell01.getColor(), traversed);
        t.checkExpect(cell02.getColor(), onPath);
        t.checkExpect(cell10.getColor(), untraversed);
        t.checkExpect(cell11.getColor(), untraversed);
        t.checkExpect(cell12.getColor(), untraversed);
        t.checkExpect(cell20.getColor(), onPath);
        t.checkExpect(cell21.getColor(), onPath);
        t.checkExpect(cell22.getColor(), untraversed);

    }

    // Tests for the draw method on Cell
    void testDrawCell(Tester t) {
        initCells();
        initEdges();
        connectCells();

        ArrayList<Cell> cells = new ArrayList<Cell>();
        cells.add(cell00);
        cells.add(cell01);
        cells.add(cell02);
        cells.add(cell10);
        cells.add(cell11);
        cells.add(cell12);
        cells.add(cell20);
        cells.add(cell21);
        cells.add(cell22);

        for (Cell curCell: cells) {
            Posn center = new Posn(curCell.x * 10 + 5, curCell.y * 10 + 5);

            WorldImage cellImage = 
                    new RectangleImage(center, 10, 10, curCell.getColor());

            WorldImage leftEdgeImage = curCell.left.draw(10);
            WorldImage topEdgeImage = curCell.top.draw(10);
            WorldImage rightEdgeImage = curCell.right.draw(10);
            WorldImage botEdgeImage = curCell.bot.draw(10);

            WorldImage finalImage = cellImage.overlayImages(
                    leftEdgeImage, topEdgeImage, rightEdgeImage, botEdgeImage);

            t.checkExpect(curCell.draw(10), finalImage);
        }
    }

    // Tests for the draw method on Edge
    // Note: also tests getImage in testing draw
    void testDrawEdge(Tester t) {
        initCells();
        initEdges();
        t.checkExpect(lrEdge00.draw(10),
                new RectangleImage(new Posn(0, 5), 2, 10, Edge.COLOR));
        t.checkExpect(lrEdge00.draw(0),
                new RectangleImage(new Posn(0, 0), 1, 0, Edge.COLOR));
        t.checkExpect(lrEdge10.draw(20),
                new RectangleImage(new Posn(20, 10), 3, 20, Edge.COLOR));
        t.checkExpect(lrEdge21.draw(20),
                new RectangleImage(new Posn(40, 30), 3, 20, Edge.COLOR));
        t.checkExpect(tbEdge00.draw(10),
                new RectangleImage(new Posn(5, 0), 10, 2, Edge.COLOR));
        t.checkExpect(tbEdge00.draw(0),
                new RectangleImage(new Posn(0, 0), 0, 1, Edge.COLOR));
        t.checkExpect(tbEdge10.draw(20),
                new RectangleImage(new Posn(30, 0), 20, 3, Edge.COLOR));
        t.checkExpect(tbEdge21.draw(20),
                new RectangleImage(new Posn(50, 20), 20, 3, Edge.COLOR));

        lrEdge00.isBlocking = false;
        lrEdge00.isBlocking = false;
        lrEdge10.isBlocking = false;
        lrEdge21.isBlocking = false;
        tbEdge00.isBlocking = false;
        tbEdge00.isBlocking = false;
        tbEdge10.isBlocking = false;
        tbEdge21.isBlocking = false;

        t.checkExpect(lrEdge00.draw(10),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(lrEdge00.draw(0),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(lrEdge10.draw(20),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(lrEdge21.draw(20),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(tbEdge00.draw(10),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(tbEdge00.draw(0),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(tbEdge10.draw(20),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
        t.checkExpect(tbEdge21.draw(20),
                new RectangleImage(new Posn(-1, -1), 0, 0, Color.BLACK));
    }

    /***************************************
     * Tests for Maze
     ***************************************/
    
    // NOTE: Drawing methods not tested for impracticality reasons
    
    Maze maze1;
    Maze maze2;
    
    // initialize both mazes with given width and height
    void initMaze(int width, int height) {
        maze1 = new Maze(width, height);
        maze2 = new Maze(width, height);
    }
    
    // test the initializeBoard method on Maze
    void testInitializeBoardMaze(Tester t) {
        initMaze(1, 1);
        
        maze1.initializeBoard(3, 3);

        maze2.width = 3;
        maze2.height = 3;
        maze2.constructCells(3, 3);
        maze2.connectCells();
        
        t.checkExpect(maze1, maze2);
        
        initMaze(1, 1);
        
        maze1.initializeBoard(5, 6);

        maze2.width = 5;
        maze2.height = 6;
        maze2.constructCells(5, 6);
        maze2.connectCells();
        
        t.checkExpect(maze1, maze2);
    }
    
    // test the constructCells method on Maze
    void testConstructCellsMaze(Tester t) {
        initMaze(1, 1);
        
        maze1.width = 2;
        maze1.height = 1;
        maze1.constructCells(2, 1);
        
        maze2.width = 2;
        maze2.height = 1;
        ArrayList<ArrayList<Cell>> cells2 = new ArrayList<ArrayList<Cell>>();
        cells2.add(new ArrayList<Cell>());
        cells2.add(new ArrayList<Cell>());
        cells2.get(0).add(new Cell(0, 0));
        cells2.get(1).add(new Cell(1, 0));
        maze2.cells = cells2;
        
        t.checkExpect(maze1, maze2);

        initMaze(1, 1);
        
        maze1.width = 3;
        maze1.height = 3;
        maze1.constructCells(3, 3);
        
        maze2.width = 3;
        maze2.height = 3;
        cells2 = new ArrayList<ArrayList<Cell>>();
        cells2.add(new ArrayList<Cell>());
        cells2.add(new ArrayList<Cell>());
        cells2.add(new ArrayList<Cell>());
        cells2.get(0).add(new Cell(0, 0));
        cells2.get(0).add(new Cell(0, 1));
        cells2.get(0).add(new Cell(0, 2));
        cells2.get(1).add(new Cell(1, 0));
        cells2.get(1).add(new Cell(1, 1));
        cells2.get(1).add(new Cell(1, 2));
        cells2.get(2).add(new Cell(2, 0));
        cells2.get(2).add(new Cell(2, 1));
        cells2.get(2).add(new Cell(2, 2));
        maze2.cells = cells2;
        
        t.checkExpect(maze1, maze2);
    }
    
    // test connectCells method on Maze
    void testConnectCells(Tester t) {
        initMaze(1, 1);

        maze1.width = 3;
        maze1.height = 3;
        maze1.constructCells(3, 3);
        maze1.connectCells();
        
        maze2.width = 3;
        maze2.height = 3;
        maze2.constructCells(3, 3);
        maze2.edges = new ArrayList<Edge>();
        maze2.connectCellsHorizontal();
        maze2.connectCellsVertical();
        
        t.checkExpect(maze1, maze2);

        initMaze(1, 1);

        maze1.width = 5;
        maze1.height = 6;
        maze1.constructCells(5, 6);
        maze1.connectCells();
        
        maze2.width = 5;
        maze2.height = 6;
        maze2.constructCells(5, 6);
        maze2.edges = new ArrayList<Edge>();
        maze2.connectCellsHorizontal();
        maze2.connectCellsVertical();
        
        t.checkExpect(maze1, maze2);
    }
    
    // test connectCellsVertical method on Maze
    void testConnectCellsVertical(Tester t) {
        initMaze(1, 1);
        
        maze1.width = 1;
        maze1.height = 3;
        maze1.constructCells(1, 3);
        maze1.edges = new ArrayList<Edge>();
        maze1.connectCellsVertical();
        
        Cell c00 = maze1.cells.get(0).get(0);
        Cell c01 = maze1.cells.get(0).get(1);
        Cell c02 = maze1.cells.get(0).get(2);
        t.checkExpect(c00.top.cell1, c00);
        t.checkExpect(c00.top.cell2, c00);
        t.checkExpect(c00.bot.cell1, c00);
        t.checkExpect(c00.bot.cell2, c01);

        t.checkExpect(c01.top.cell1, c00);
        t.checkExpect(c01.top.cell2, c01);
        t.checkExpect(c01.bot.cell1, c01);
        t.checkExpect(c01.bot.cell2, c02);

        t.checkExpect(c02.top.cell1, c01);
        t.checkExpect(c02.top.cell2, c02);
        t.checkExpect(c02.bot.cell1, c02);
        t.checkExpect(c02.bot.cell2, c02);
        
        // only connecting edges should have been added
        t.checkExpect(maze1.edges.size(), 2);
    }
    
    // test connectCellsHorizontal method on Maze
    void testConnectCellsHorizontal(Tester t) {
        initMaze(1, 1);
        
        maze1.width = 3;
        maze1.height = 1;
        maze1.constructCells(3, 1);
        maze1.edges = new ArrayList<Edge>();
        maze1.connectCellsHorizontal();
        
        Cell c00 = maze1.cells.get(0).get(0);
        Cell c10 = maze1.cells.get(1).get(0);
        Cell c20 = maze1.cells.get(2).get(0);
        t.checkExpect(c00.left.cell1, c00);
        t.checkExpect(c00.left.cell2, c00);
        t.checkExpect(c00.right.cell1, c00);
        t.checkExpect(c00.right.cell2, c10);

        t.checkExpect(c10.left.cell1, c00);
        t.checkExpect(c10.left.cell2, c10);
        t.checkExpect(c10.right.cell1, c10);
        t.checkExpect(c10.right.cell2, c20);

        t.checkExpect(c20.left.cell1, c10);
        t.checkExpect(c20.left.cell2, c20);
        t.checkExpect(c20.right.cell1, c20);
        t.checkExpect(c20.right.cell2, c20);
        
        // only connecting edges should have been added
        t.checkExpect(maze1.edges.size(), 2);
    }
    
    // test wallsUp method on Maze
    void testWallsUp(Tester t) {
        initMaze(3, 3);
        
        // shouldn't do anything on new maze
        maze1.wallsUp();
        t.checkExpect(maze1, maze2);
        
        initMaze(3, 3);
        
        Edge e1 = maze1.cells.get(0).get(0).right;
        Edge e2 = maze1.cells.get(1).get(0).bot;
        Edge e3 = maze1.cells.get(1).get(2).left;
        Edge e4 = maze1.cells.get(2).get(2).top;
        
        e1.isBlocking = false;
        e2.isBlocking = false;
        e3.isBlocking = false;
        e4.isBlocking = false;
        
        maze1.wallsUp();

        t.checkExpect(e1.isBlocking, true);
        t.checkExpect(e2.isBlocking, true);
        t.checkExpect(e3.isBlocking, true);
        t.checkExpect(e4.isBlocking, true);
    }
    
    // test that the assignRandomWeights method on Maze is reasonable
    void testAssignRandomWeights(Tester t) {
        initMaze(3, 3);
        maze1.assignRandomWeights();
        for (ArrayList<Cell> col : maze1.cells) {
            for (Cell cell : col) {
                checkRandomWeight(cell.top, t);
                checkRandomWeight(cell.bot, t);
                checkRandomWeight(cell.left, t);
                checkRandomWeight(cell.right, t);
            }
        }
    }
    
    // helper method to check if a non-border edge has reasonable ranom weight
    void checkRandomWeight(Edge edge, Tester t) {
        if (edge instanceof BorderEdge) {
            // PASS
        }
        else {
            t.checkExpect(edge.weight >= 0, true);
            t.checkExpect(edge.weight < 1, true);
        }
    }
    
    // test getFinalCell method on Maze
    void testGetFinalCell(Tester t) {
        initMaze(1, 1);
        t.checkExpect(maze1.getFinalCell().x, 0);
        t.checkExpect(maze1.getFinalCell().y, 0);

        initMaze(5, 7);
        t.checkExpect(maze1.getFinalCell().x, 4);
        t.checkExpect(maze1.getFinalCell().y, 6);

        initMaze(12, 13);
        t.checkExpect(maze1.getFinalCell().x, 11);
        t.checkExpect(maze1.getFinalCell().y, 12);
    }
    
    // test getFirstCell method on Maze
    void testGetFirstCell(Tester t) {
        initMaze(1, 1);
        t.checkExpect(maze1.getFirstCell().x, 0);
        t.checkExpect(maze1.getFirstCell().y, 0);

        initMaze(5, 7);
        t.checkExpect(maze1.getFirstCell().x, 0);
        t.checkExpect(maze1.getFirstCell().y, 0);

        initMaze(12, 13);
        t.checkExpect(maze1.getFirstCell().x, 0);
        t.checkExpect(maze1.getFirstCell().y, 0);
    }
    
    // test resetTraversals method on Maze
    void testResetTraversals(Tester t) {
        initMaze(3, 3);

        Cell c1 = maze1.cells.get(0).get(0);
        Cell c2 = maze1.cells.get(1).get(2);
        Cell c3 = maze1.cells.get(1).get(1);
        Cell c4 = maze1.cells.get(2).get(0);
        
        c1.onPath = true;
        c1.traversed = true;
        c2.onPath = true;
        c3.traversed = true;
        c4.traversed = true;
        
        maze1.resetTraversals();
        
        for (ArrayList<Cell> col : maze1.cells) {
            for (Cell cell : col) {
                t.checkExpect(cell.onPath, false);
                t.checkExpect(cell.traversed, false);
            }
        }
    }

    /***************************************
     * Tests for UnionFindPosn
     ***************************************/
    
    UnionFindPosn uFind;
    
    // initialize UnionFindPosn
    void initUFind() {
        uFind = new UnionFindPosn(3, 3);
    }
    
    // test samePosn method on UnionFindPosn
    void testSamePosn(Tester t) {
        initUFind();

        t.checkExpect(uFind.samePosn(new Posn(0, 0), new Posn(0, 0)), true);
        t.checkExpect(uFind.samePosn(new Posn(12, 0), new Posn(0, 1)), false);
        t.checkExpect(uFind.samePosn(new Posn(3, 4), new Posn(3, 4)), true);
    }
    
    // test the sameGroup, getGroup and connect methods on UnionFindPosn since
    //   all interconnected
    void testUFind(Tester t) {
        initUFind();
        
        for (int x = 0; x < 3; x += 1) {
            for (int y = 0; y < 3; y += 1) {
                t.checkExpect(uFind.getGroup(new Posn(x, y)), new Posn(x, y));
            }
        }

        t.checkExpect(uFind.sameGroup(new Posn(0, 0), new Posn(1, 0)), false);
        t.checkExpect(uFind.sameGroup(new Posn(0, 1), new Posn(1, 1)), false);
        
        uFind.connect(new Posn(0, 0), new Posn(1, 0));
        
        t.checkExpect(uFind.getGroup(new Posn(0, 0)),
                uFind.getGroup(new Posn(1, 0)));
        t.checkExpect(uFind.sameGroup(new Posn(0, 0), new Posn(1, 0)), true);

        uFind.connect(new Posn(0, 1), new Posn(0, 0));
        uFind.connect(new Posn(1, 1), new Posn(1, 0));
        
        t.checkExpect(uFind.getGroup(new Posn(0, 1)),
                uFind.getGroup(new Posn(1, 1)));
        t.checkExpect(uFind.sameGroup(new Posn(0, 1), new Posn(1, 1)), true);
    }

    /***************************************
     * Tests for MazeAnimator
     ***************************************/
    
    /*
     * Since the only methods actually defined on this abstract class are
     * drawOnto (hard to test), onKeYyEvent (does nothing) and alwaysTerminates
     * (default returns true), there is nothing reasonable to test here
     */

    /***************************************
     * Tests for AutoSolveAnimator (DFS and BFS)
     ***************************************/
    
    // NOTE: The actual solving algorithm is really hard to test other than
    // checking that it works in practice
    
    DFSAnimator dfs;
    BFSAnimator bfs;
    
    // initialize auto-solving animators
    void initAutoSolve() {
        // set up and create maze (see Kruskal tests below)
        // prepare maze1
        initKruskal();
        // create maze1
        new InstantAnimator(maze1, kruskal).onTick();
        // CONTEXT: maze1 and maze2 should now be set up and solveable

        dfs = new DFSAnimator(maze1);
        bfs = new BFSAnimator(maze1);
    }
    
    // test isComplete method on DFSAnimator
    void testIsCompleteDFS(Tester t) {
        initAutoSolve();

        t.checkExpect(dfs.isComplete(), false);
        dfs.onTick();
        t.checkExpect(dfs.isComplete(), false);
        dfs.onTick();
        t.checkExpect(dfs.isComplete(), false);
        dfs.onTick();
        t.checkExpect(dfs.isComplete(), false);
        dfs.onTick();
        t.checkExpect(dfs.isComplete(), true);
    }
    
    // test hasWork method on DFSAnimator
    void testHasWorkDFS(Tester t) {
        initAutoSolve();

        t.checkExpect(dfs.hasWork(), true);
        dfs.onTick();
        t.checkExpect(dfs.hasWork(), true);
        dfs.onTick();
        t.checkExpect(dfs.hasWork(), true);
        dfs.onTick();
        t.checkExpect(dfs.hasWork(), true);
        dfs.onTick();
        t.checkExpect(dfs.hasWork(), true);
        dfs.onTick();
        t.checkExpect(dfs.hasWork(), true);
        // almost never ends up actually hitting false here
    }
    
    // test status method on DFSAnimator
    void testStatusDFS(Tester t) {
        initAutoSolve();

        t.checkExpect(dfs.status(), "Depth First Searching.   Moves: 0");
        dfs.onTick();
        t.checkExpect(dfs.status(), "Depth First Searching.   Moves: 1");
        dfs.onTick();
        t.checkExpect(dfs.status(), "Depth First Searching.   Moves: 2");
        dfs.onTick();
        t.checkExpect(dfs.status(), "Depth First Searching.   Moves: 3");
        dfs.onTick();
        t.checkExpect(dfs.status(), "Depth First Searching.   Moves: 3");
    }
    
    // test nextAnimator method on BFSAnimator
    void testNextAnimatorDFS(Tester t) {
        initAutoSolve();

        dfs.onTick();
        dfs.onTick();
        dfs.onTick();
        dfs.onTick();
        dfs.onTick();
        dfs.onTick();
        t.checkExpect(dfs.nextAnimator(),
                new MsgAnimator(maze1,
                        "Completed Depth First Search.   Moves: 3"));
    }
    
    // test addWork method on DFSAnimator
    void testAddWorkDFS(Tester t) {
        Cell c1 = new Cell(0, 1);
        
        initAutoSolve();
        dfs.addWork(c1);
        t.checkExpect(dfs.worklist.pop(), c1);
        
        initAutoSolve();
        dfs.onTick();
        dfs.onTick();
        dfs.onTick();
        dfs.addWork(c1);
        t.checkExpect(dfs.worklist.pop(), c1);
    }
    
    // test getWork method on DFSAnimator
    void testGetWorkDFS(Tester t) {
        initAutoSolve();
        t.checkExpect(dfs.getWork(), maze1.cells.get(0).get(0));

        initAutoSolve();
        dfs.onTick();
        t.checkExpect(dfs.getWork(), maze1.cells.get(1).get(0));

        initAutoSolve();
        dfs.onTick();
        dfs.onTick();
        dfs.onTick();
        t.checkExpect(dfs.getWork(), maze1.cells.get(2).get(1));
    }
    
    
    
    
    // test isComplete method on BFSAnimator
    void testIsCompleteBFS(Tester t) {
        initAutoSolve();

        t.checkExpect(bfs.isComplete(), false);
        bfs.onTick();
        t.checkExpect(bfs.isComplete(), false);
        bfs.onTick();
        t.checkExpect(bfs.isComplete(), false);
        bfs.onTick();
        t.checkExpect(bfs.isComplete(), false);
        bfs.onTick();
        t.checkExpect(bfs.isComplete(), false);
        bfs.onTick();
        t.checkExpect(bfs.isComplete(), false);
        bfs.onTick();
        t.checkExpect(bfs.isComplete(), true);
    }
    
    // test hasWork method on BFSAnimator
    void testHasWorkBFS(Tester t) {
        initAutoSolve();

        t.checkExpect(bfs.hasWork(), true);
        bfs.onTick();
        t.checkExpect(bfs.hasWork(), true);
        bfs.onTick();
        t.checkExpect(bfs.hasWork(), true);
        bfs.onTick();
        t.checkExpect(bfs.hasWork(), true);
        bfs.onTick();
        t.checkExpect(bfs.hasWork(), true);
        bfs.onTick();
        t.checkExpect(bfs.hasWork(), true);
        bfs.onTick();
        t.checkExpect(bfs.hasWork(), false);
    }
    
    // test status method on BFSAnimator
    void testStatusBFS(Tester t) {
        initAutoSolve();

        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 0");
        bfs.onTick();
        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 1");
        bfs.onTick();
        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 2");
        bfs.onTick();
        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 3");
        bfs.onTick();
        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 4");
        bfs.onTick();
        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 5");
        bfs.onTick();
        t.checkExpect(bfs.status(), "Breadth First Searching.   Moves: 5");
    }
    
    // test nextAnimator method on BFSAnimator
    void testNextAnimatorBFS(Tester t) {
        initAutoSolve();

        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        t.checkExpect(bfs.nextAnimator(),
                new MsgAnimator(maze1,
                        "Completed Breadth First Search.   Moves: 5"));
    }
    
    // test addWork method on BFSAnimator
    void testAddWorkBFS(Tester t) {
        Cell c1 = new Cell(0, 1);
        
        initAutoSolve();
        bfs.addWork(c1);
        // remove the 1 element in front of queue
        bfs.worklist.dequeue();
        t.checkExpect(bfs.worklist.dequeue(), c1);
        
        initAutoSolve();
        bfs.onTick();
        bfs.onTick();
        bfs.addWork(c1);
        // remove the 2 elements in front of queue
        bfs.worklist.dequeue();
        bfs.worklist.dequeue();
        t.checkExpect(bfs.worklist.dequeue(), c1);
    }
    
    // test getWork method on BFSAnimator
    void testGetWorkBFS(Tester t) {
        initAutoSolve();
        t.checkExpect(bfs.getWork(), maze1.cells.get(0).get(0));

        initAutoSolve();
        bfs.onTick();
        t.checkExpect(bfs.getWork(), maze1.cells.get(1).get(0));

        initAutoSolve();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        t.checkExpect(bfs.getWork(), maze1.cells.get(1).get(1));

        initAutoSolve();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        bfs.onTick();
        t.checkExpect(bfs.getWork(), maze1.cells.get(2).get(1));
    }
    

    /***************************************
     * Tests for PlayAnimator
     ***************************************/
    
    PlayAnimator play;
    // This animator is very difficult to test automatically, and best tested
    // manually by playing

    /***************************************
     * Tests for IdleAnimator
     ***************************************/
    
    IdleAnimator idle;
    
    // initialize Idle Animator
    void initIdle() {
        initMaze(3, 3);
        idle = new IdleAnimator(maze1);
    }
    
    // test Idle Animator
    void testIdleAnimator(Tester t) {
        initIdle();
        
        t.checkExpect(idle.status(), "Idle");
        t.checkExpect(idle.isComplete(), true);
        t.checkExpect(idle.nextAnimator(), idle);
        
        idle.onTick();
        // check that the maze hasn't deviated from maze2 which is initially
        // constructed identically
        t.checkExpect(idle.maze, maze2);
    }

    /***************************************
     * Tests for MsgAnimator
     ***************************************/
    
    MsgAnimator msg;
    
    // NOTE: only status method needs to be tested since rest inherited from
    // IdleAnimator
    
    // test status method on  MsgAnimator
    void testStatusMsgAnimator(Tester t) {
        initMaze(3, 3);
        
        msg = new MsgAnimator(maze1, "hello");
        t.checkExpect(msg.status(), "hello");
        
        msg = new MsgAnimator(maze1, "goodbye");
        t.checkExpect(msg.status(), "goodbye");
        
        msg = new MsgAnimator(maze1, "Help me I'm trapped in here!");
        t.checkExpect(msg.status(), "Help me I'm trapped in here!");
    }

    /***************************************
     * Tests for KruskalAnimator
     ***************************************/
    
    KruskalAnimator kruskal;
    
    // initialize Kruskal Animator
    void initKruskal() {
        initMaze(3, 2);
        /*
         * Weight assignments
         * +---+---+---+
         * |   1   6   |
         * +-4-+-2-+-7-+
         * |   3   5   |
         * +---+---+---+
         */
        
        maze1.cells.get(0).get(0).right.weight = 1;
        maze1.cells.get(1).get(0).right.weight = 6;
        maze1.cells.get(0).get(1).right.weight = 3;
        maze1.cells.get(1).get(1).right.weight = 5;
        maze1.cells.get(0).get(0).bot.weight = 4;
        maze1.cells.get(1).get(0).bot.weight = 2;
        maze1.cells.get(2).get(0).bot.weight = 7;
        Collections.sort(maze1.edges, new EdgeWeightComp());
        kruskal = new KruskalAnimator(maze1);
    }
    
    // test Kruskal algorithm on the maze (onTick, status, isComplete and
    // nextAnimator tested)
    void testKruskalAnimator(Tester t) {
        initKruskal();
        
        kruskal.onTick();
        t.checkExpect(maze1.cells.get(0).get(0).right.isBlocking, false);
        t.checkExpect(kruskal.status(), "Generating maze: 1/5");
        kruskal.onTick();
        t.checkExpect(maze1.cells.get(1).get(0).bot.isBlocking, false);
        t.checkExpect(kruskal.status(), "Generating maze: 2/5");
        kruskal.onTick();
        t.checkExpect(maze1.cells.get(0).get(1).right.isBlocking, false);
        t.checkExpect(kruskal.status(), "Generating maze: 3/5");
        kruskal.onTick();
        t.checkExpect(maze1.cells.get(1).get(1).right.isBlocking, false);
        t.checkExpect(kruskal.status(), "Generating maze: 4/5");
        kruskal.onTick();
        t.checkExpect(maze1.cells.get(1).get(0).right.isBlocking, false);
        t.checkExpect(kruskal.status(), "Generating maze: 5/5");
        // done now
        t.checkExpect(kruskal.isComplete(), true);
        // check that proper walls still up
        t.checkExpect(maze1.cells.get(0).get(0).bot.isBlocking, true);
        t.checkExpect(maze1.cells.get(2).get(0).bot.isBlocking, true);
        // check for correct next animator
        t.checkExpect(kruskal.nextAnimator(), new IdleAnimator(maze1));
        
        // make sure they're all seen as in the same group
        Posn p1 = new Posn(0, 0);
        for (ArrayList<Cell> col : maze1.cells) {
            for (Cell cell : col) {
                t.checkExpect(kruskal.uFind.sameGroup(p1,
                        new Posn(cell.x, cell.y)), true);
            }
        }
        
    }

    /***************************************
     * Tests for InstantAnimator
     ***************************************/
    
    InstantAnimator inst;
    
    // test InstantAnimator class
    void testInstantAnimator(Tester t) {
        initMaze(3, 3);
        
        idle = new IdleAnimator(maze1);
        inst = new InstantAnimator(maze1, idle);
        
        t.checkExpect(inst.status(), "Completing Animation");
        
        inst.onTick();
        t.checkExpect(inst.isComplete(), true);
        t.checkExpect(inst.nextAnimator(), idle.nextAnimator());
        t.checkExpect(inst.status(), "Completing Animation");
        
        msg = new MsgAnimator(maze1, "hello");
        inst = new InstantAnimator(maze1, msg);
        
        t.checkExpect(inst.status(), "Completing Animation");
        
        inst.onTick();
        t.checkExpect(inst.isComplete(), true);
        t.checkExpect(inst.nextAnimator(), msg.nextAnimator());
        t.checkExpect(inst.status(), "Completing Animation");
        
        kruskal = new KruskalAnimator(maze1);
        inst = new InstantAnimator(maze1, kruskal);
        
        t.checkExpect(inst.status(), "Completing Animation");
        
        inst.onTick();
        t.checkExpect(inst.isComplete(), true);
        t.checkExpect(inst.nextAnimator(), kruskal.nextAnimator());
        t.checkExpect(inst.status(), "Completing Animation");
        
        // Player animator has more specific reactions
        
        play = new PlayAnimator(maze1);
        inst = new InstantAnimator(maze1, play);
        
        t.checkExpect(inst.status(), "Completing Animation");

        // this should all be true even before onTick is called
        t.checkExpect(inst.isComplete(), true);
        t.checkExpect(inst.nextAnimator(), play);
        
        // and onTick shouldn't break
        inst.onTick();
        
        // just to be sure
        t.checkExpect(inst.isComplete(), true);
        t.checkExpect(inst.nextAnimator(), play);
        t.checkExpect(inst.status(), "Completing Animation");
    }

    /***************************************
     * Tests for MazeWorld
     ***************************************/
    
    // the maze world class is too high level and dependent on both random and
    // input factors to be practically tested

}