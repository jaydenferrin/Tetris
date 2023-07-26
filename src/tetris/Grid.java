package tetris;

import java.awt.Color;
import java.awt.Graphics;
import static tetris.Tetris.W;

public class Grid {
    private Square[][] grid = new Square[40][10];
    private final float x;
    private final float y;
    //private final float W = Tetris.W;
    private int origLevel = 1;
    private int level = origLevel;
    private int linesCleared = 0;
    
    public Grid(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void render(Graphics g) {
        g.setColor(Color.black);
        g.fillRect((int)(x), (int)(y+20f*W), (int)(10f*W)+2, (int)(20f*W)+2);
        float xBuf = 0f;
        float yBuf = 0f;
        for (Square[] grid1 : grid) {
            for (Square grid11 : grid1) {
                try {
                    if (y+yBuf >= 20*W+y)
                        grid11.draw(x+xBuf, y+yBuf, W, g);
                } catch (NullPointerException e) {}
                xBuf += W;
            }
            yBuf += W;
            xBuf = 0;
        }
    }
    
    public void reset() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = null;
            }
        }
        linesCleared = 0;
        level = origLevel;
    }
    
    private int transformX(float x) {
        return (int)((x-this.x)/W);
    }

    private int transformY(float y) {
        return (int)((y-this.y)/W);
    }

//    private float restoreY(int yb) {
//        return ((float)yb)*W+this.y;
//    }
//
//    private float restoreX(int xb) {
//        return ((float)xb)*W+this.x;
//    }
    
    public boolean checkPos(Square[][] sqs, float x, float y) {
        int xb = transformX(x);
        int yb = transformY(y);
        boolean out = false;
        for (int i = 0; i < sqs.length; i++) {
            for (int j = 0; j < sqs[i].length; j++) {
                if (xb + j < 0 && sqs[i][j] != null) return true;
                if (yb + i < 0 && sqs[i][j] != null) return true;
                if (xb + j >= 10 && sqs[i][j] != null) return true;
                if (yb + i >= 40 && sqs[i][j] != null) return true;
                try {
                    out |= (grid[i+yb][j+xb] != null & sqs[i][j] != null);
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
        return out;
    }
    
    public boolean checkBelow(Square[][] sqs, float x, float y) {
        return checkPos(sqs, x, y+W);
    }
    
    public boolean checkRight(Square[][] sqs, float x, float y) {
        return checkPos(sqs, x+W, y);
    }
    
    public boolean checkLeft(Square[][] sqs, float x, float y) {
        return checkPos(sqs, x-W, y);
    }
    
    public boolean checkPos2(Square[][] sqs, float x, float y) {
        //int xb = transformX(x);
        int yb = transformY(y);
        //boolean out = false;
        for (int i = 0; i < sqs.length; i++) {
            for (Square sq : sqs[i]) {
                if (yb + i >= 20 && sq != null) return false;
                //out &= (yb + i <= 20 && sq != null);
            }
        }
        //return out;
        return true;
    }
    
    public float findY(Square[][] sqs, float x, float y) {
        try {
            for (float i = y; i < this.y+(40f*W); i+=W) {
                if (checkBelow(sqs,x,i)) return i;
            }
        } catch (ArrayIndexOutOfBoundsException e) {System.err.println("this is not good");}
        System.err.println("bad");
        return this.y+18*W;
    }
    
    public void insert(Square[][] sqs, float x, float y, TetroBag tb) {
        this.insert(sqs, x, y, true, tb);
    }
    
    private void insert(Square[][] sqs, float x, float y, boolean check, TetroBag tb) {
        Square[][] tmp = grid;
        int xb = transformX(x);
        int yb = transformY(y);
        for (int i = 0; i < sqs.length; i++) {
            for (int j = 0; j < sqs[i].length; j++) {
                try {
                    if (tmp[i+yb][j+xb] != null && sqs[i][j] != null)
                        throw new RuntimeException("piece intersection");
                    if (tmp[i+yb][j+xb] == null)
                        tmp[i+yb][j+xb] = sqs[i][j];
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
        grid = tmp;
        if (check) checkLines(tb);
    }
    
    private void checkLines(TetroBag tb) {
        boolean[] set = new boolean[40];
        int tru = 0;
        for (int i = 0; i < grid.length; i++) {
            boolean lin = true;
            for (int j = 0; j < grid[i].length; j++) {
                lin &= (grid[i][j] != null);
            }
            if (lin) tru++;
            set[i] = lin;
        }
        if (tru > 0) {
            linesCleared += tru;
            increaseLevel();
            tb.setGravity(level);
            fall(set,tb);
        }
    }

    private void fall(boolean[] set, TetroBag tb) {
        for (int i = 0; i < set.length; i++) {
            if (set[i]) {
                for (int j = 0; j < grid[i].length; j++) {
                    grid[i][j] = null;
                }
            }
        }
        
//        int lastTrue = 19;
//        for (int i = set.length-1; i >= 0; i--) {
//            if (set[i]) {
//                lastTrue = i;
//                break;
//            }
//        }
        
        //Square[][] gridCopy = makeGridCopy(lastTrue);
        for (int i = grid.length-1; i >= 0; i--) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != null) {
                    Square[][] group = new Square[grid.length][grid[0].length];
                    floodFill(group,j,i);
                    insert(group,x,findY(group,x,y),false,tb);
                }
            }
        }
        checkLines(tb);
    }
    
    private void floodFill(Square[][] group, int x, int y) {
        try {
            if (grid[y][x] == null) return;
        } catch (ArrayIndexOutOfBoundsException e) {return;}
        group[y][x] = grid[y][x];
        //gridCopy[y][x] = null;
        grid[y][x] = null;
        floodFill(group, x, y+1);
        floodFill(group, x, y-1);
        floodFill(group, x-1, y);
        floodFill(group, x+1, y);
    }
    
//    private Square[][] makeGridCopy(int to) {
//        Square[][] out = new Square[to][10];
//        for (int i = 0; i < out.length; i++) {
//            for (int j = 0; j < out[i].length; j++) {
//                out[i][j] = grid[i][j];
//            }
//        }
//        return out;
//    }

    private void increaseLevel() {
        level = linesCleared/10+origLevel;
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public int getLinesCleared() {
        return this.linesCleared;
    }
    
//    public int getOrigLevel() {
//        return this.origLevel;
//    }
}
