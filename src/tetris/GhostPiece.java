package tetris;

import java.awt.Graphics;

public class GhostPiece {
    private float x;
    private float y;
    static final float W = Tetris.W;
    
    private Tetromino ct;
    
    public GhostPiece(Tetromino tetr, float x, float y, Grid g) {
        ct = tetr;
        this.x = x;
        this.y = g.findY(Tetromino.getRend(), x, y);
    }
    
    public void render(Graphics g) {
        Tetromino.ghostRender(x, y, ct, g);
    }
    
    public void update(Tetromino tetr, float x, float y, Grid g) {
        ct = tetr;
        this.x = x;
        this.y = g.findY(Tetromino.getRend(), x, y);
    }
    
    public float getY() {
        return this.y;
    }
    
}
