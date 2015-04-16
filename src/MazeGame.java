import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import javalib.impworld.World;
import javalib.worldimages.*;
import tester.*;

// Assignment 10
// Dhesikan Anish
// anishd
// McDonough Kevin
// kmacdoug

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
        return new Color(241, 208, 255);
    }
    
}

// to represent a wall between two cells
abstract class Edge {
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
    
    abstract WorldImage draw(int cellSize);
    
    Color getColor() {
        // Hue 282
        return new Color(127, 102, 137);
    }
}

// to represent an edge that connects left and right cells
class LREdge extends Edge {
    LREdge(Cell left, Cell right, boolean isBlocking, double weight) {
        super(left, right, isBlocking, weight);
    }
    
    WorldImage draw(int cellSize) {
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
    
    WorldImage draw(int cellSize) {
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
    
    // drawing off screen because border edges are not drawn
    WorldImage draw(int cellSize) {
        return new RectangleImage(new Posn (-1, -1), 0, 0, Color.GRAY);
    }
}

// to represent a maze board
class Maze {
    ArrayList<ArrayList<Cell>> cells;
    ArrayList<Edge> edges;

    // Constructor creating a maze with given width and height (in cells)
    Maze(int width, int height) {
        initializeBoard(width, height);
    }

    // EFFECT: modify cells as a board of given width and height (in cells) 
    // with all walls present
    void initializeBoard(int width, int height) {
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
    
    // draws this maze onto the given background image
    WorldImage drawOnto(int cellSize, WorldImage bg) {
        return this.drawSection(cellSize, 0, 0, 
                this.cells.size(), this.cells.get(0).size());
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
    
}

class MazeWorld extends World {
    
    int width; // in cells
    int height; // in cells
    int cellSize; // in pixels
    Maze maze; 
    
    MazeWorld(int width, int height, int cellSize) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;
        this.maze = new Maze(width, height);
    }

    public WorldImage makeImage() {
        int pWidth = this.getPixelWidth();
        int pHeight = this.getPixelWidth();
        WorldImage bg = new RectangleImage(
                new Posn(pWidth / 2, pHeight / 2),
                pWidth, pHeight, Color.GRAY);
        
        return this.maze.drawOnto(this.cellSize, bg);
    }
    
    // return the width of the maze in pixels 
    int getPixelWidth() {
        return this.width * this.cellSize;
    }
    
    // return the height of the maze in pixels 
    int getPixelHeight() {
        return this.height * this.cellSize;
    }
    
}

class ExamplesMaze {
    void testBigBang(Tester t) {
        MazeWorld initWorld = new MazeWorld(20, 20, 20);
        
        initWorld.bigBang(initWorld.getPixelWidth(),
                          initWorld.getPixelHeight(),
                          0.1);
    }
}