package tetris;

import java.awt.Color;
import java.awt.Graphics;
import static tetris.Tetris.W;

public class SavedPiece {
    private int counter = 0;
    private Tetromino hold = null;
    private Tetromino toHold = null;
    //private TetroBag ref;
    
    private float x;
    private float y;
    
    public SavedPiece(float x, float y) {
        this.x = x;
        this.y = y;
        //this.ref = ref;
    }
    
    public void render(Graphics g) {
        g.setColor(Color.black);
        g.fillRect((int)(x-W*2.5f), (int)(y-W*1.5f), (int)(5*W+2), (int)(3*W+2));
        try {
            hold.render(x, y, g);
        } catch (NullPointerException e) {}
    }
    
    public void reset() {
        hold = null;
        toHold = null;
        counter = 0;
    }
    
    private void hold(TetroBag tb) {
        if (counter != 0) return;
        Tetromino t = tb.getCurrentTetromino();
        if (hold == null) {
            hold = t;
            tb.nextPiece(true);
        } else {
            tb.setCurrentTetromino(hold);
            hold = t;
        }
        counter++;
        tb.resetPos();
    }
    
    public void queueHold(Tetromino tetr) {
        toHold = tetr;
    }
    
    public void update(TetroBag tb) {
        if (toHold == null) return;
        hold(tb);
        toHold = null;
    }
    
    public void lock() {
        counter = 1;
    }
    
    public void unlock() {
        counter = 0;
    }
}
