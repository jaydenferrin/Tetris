package tetris;

import java.awt.Color;
import java.awt.Graphics;
import static tetris.Tetris.W;

public enum Tetromino {
    I(Color.CYAN, new boolean[][] {{false,false,false,false,false},{false,false,false,false,false},{false,true,true,true,true},
        {false,false,false,false,false},{false,false,false,false,false}},3,2.5f),
    O(Color.YELLOW, new boolean[][] {{false,true,true},{false,true,true},{false,false,false}},2,1),
    T(Color.magenta, new boolean[][] {{false, true, false},{true,true,true},{false,false,false}},1.5f,1),
    J(Color.BLUE, new boolean[][] {{true, false, false},{true,true,true},{false,false,false}},1.5f,1),
    L(new Color(0xff,0x88,0), new boolean[][] {{false,false,true},{true,true,true},{false,false,false}},1.5f,1),
    S(Color.GREEN, new boolean[][] {{false,true,true},{true,true,false},{false,false,false}},1.5f,1),
    Z(Color.RED, new boolean[][] {{true,true,false},{false,true,true},{false,false,false}},1.5f,1);
  
    private Color col;
    //private final boolean[][] boxes;
    private final Square[][] ren;
    private Square rs;
    
    private static Square[][] rend;
    private static Square[][] rendBuf;
    private static int rot = 0;
    private static int rotBuf = 0;
    private static final float TOP = 60f;
    
    private final float cx;
    private final float cy;
    
    //private static Tetromino selected;
    
    private Tetromino(Color c, boolean[][] box, float cx, float cy) {
        this.col = c;
        //this.boxes = box;
        this.rs = new Square(Color.black, c, 2);
        this.ren = new Square[box.length][box[0].length];
        for (int i = 0; i < box.length; i++) {
            for (int j = 0; j < box[i].length; j++) {
                if (box[i][j]) ren[i][j] = rs;
            }
        }
        this.cx = cx * W;
        this.cy = cy * W;
    }
    
    public Color getColor() {
        return this.col;
    }
    
    private static Square[][] copyArr(Square[][] from) {
        Square[][] to = new Square[from.length][from[0].length];
        for (int i = 0; i < from.length; i++) {
            System.arraycopy(from[i], 0, to[i], 0, from.length);
        }
        return to;
    }
    
    public static void select(Tetromino tetr) {
        //selected = tetr;
        //rend = tetr.ren;
        rend = copyArr(tetr.ren);
        //rendBuf = rend;
        rendBuf = copyArr(rend);
        rot = 0;
        rotBuf = rot;
    }
    
    private void render(float x, float y, Square[][] toRender, boolean invert, Graphics g) {
        float xBuf = 0f;
        float yBuf = 0f;
        Square rendSquare = (invert) ? rs.invert() : rs;
        for (int j = 0; j < toRender.length; j++) {
            for (int k = 0; k < toRender[j].length; k++) {
                if (toRender[j][k] != null && y+yBuf >= TOP) rendSquare.draw(x+xBuf, y+yBuf, Tetris.W, g);
                xBuf += Tetris.W;
            }
            yBuf += Tetris.W;
            xBuf = 0;
        }
    }
    
    public void render(float x, float y, Graphics g) {
        render(x-cx, y-cy, this.ren, false, g);
    }
    
    public static void render(float x, float y, Tetromino tetr, Graphics g) {
        tetr.render(x, y, rend, false, g);
    }
    
    public static void ghostRender(float x, float y, Tetromino tetr, Graphics g) {
        tetr.render(x, y, rend, true, g);
    }
    
    public static int rotateRight(int amount) {
        //amount = convert(amount);
        //rotBuf = (rotBuf+amount)%4;
        //Square[][] tmp = copyArr(rend);
        //rendBuf = copyArr(rend);
        for (int k = 0; k < convert(amount); k++) {
            // following loops rotate the rendBuf array right once
            Square[][] tmp = copyArr(rendBuf);
            for (int i = 0; i < rend.length; i++) {
                for (int j = 0; j < rend[i].length; j++) {
                    rendBuf[j][rend[i].length-1-i] = tmp[i][j];
                }
            }
            rotBuf = (rotBuf+1)%4;
            //rend = tmp;
            //System.out.printf("I've rotated %d times.\n",k);
        }
        //rendBuf = copyArr(tmp);
        return rotBuf;
    }
    
    public static void applyRotate() {
        //rend = rendBuf;
        rend = copyArr(rendBuf);
        rot = rotBuf;
    }
    
    public static void failRotate() {
        rendBuf = copyArr(rend);
        rotBuf = rot;
    }
    
    private static int convert(int rotationTimes) {
        if ((rotationTimes %= 4) < 0) rotationTimes += 4;
        //System.out.println(rotationTimes);
        return rotationTimes;
    }
    
    public static int getRot() {
        return rot;
    }
    
    @Deprecated
    public static void setRot(int rot) {
        Tetromino.rot = rot;
    }
    
    public static Square[][] getRend() {
        return rend;
    }
    
    public static void setRend(Square[][] r) {
        //rend = r;
        rend = copyArr(r);
    }
    
    public static Square[][] getRendBuf() {
        return rendBuf;
    }
}
