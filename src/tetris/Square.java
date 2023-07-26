package tetris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class Square {
    private Color strokeColor = Color.black;
    private Color fillColor = Color.white;
    private float strokeWidth = 2;
    
    public Square() {}
    
    public Square(Color s, Color f, float w) {
        this.strokeColor = s;
        this.fillColor = f;
        this.strokeWidth = w;
    }
    
    public void fill(Color col) {
        this.fillColor = col;
    }
    
    public void stroke(Color col) {
        this.strokeColor = col;
    }
    
    public void width(float w) {
        this.strokeWidth = w;
    }
    
    public Square invert() {
        return new Square(this.fillColor, this.strokeColor, this.strokeWidth);
    }
    
    public void draw(float x, float y, float w, Graphics g) {
        int offset = (int)(strokeWidth / 2);
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setPaint(fillColor);
        g2.fill(new Rectangle2D.Float(x+this.strokeWidth, y+this.strokeWidth, w-this.strokeWidth, w-this.strokeWidth));
        g2.setPaint(this.strokeColor);
        g2.setStroke(new BasicStroke(strokeWidth));
        g2.draw(new Rectangle2D.Float(x+offset,y+offset,w+1-this.strokeWidth,w+1-this.strokeWidth));
    }
}
