package tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import static tetris.Tetris.W;

public class TetroBag {
    private int bagPointer = 0;
    private Tetromino[] bag = Tetromino.values();
    private Tetromino[] queue = Tetromino.values();
    private float x;
    private float y;
    private float dx;
    private float dy;
    private final float X_INITIAL;
    private final float Y_INITIAL;
    private final float TOP = 60f;
    private Tetromino currentTetromino;
    private SavedPiece save;
    private GhostPiece ghost;
    private Grid gr;
    //private final float W = Tetris.W;
    private int rot;
    private float gravityWatch = 0f;
    private float gravity = gravityWatch; // gravity is in units/sec
    private final float softGravity = 30f;
    private final float movSpeed = 10f;
    private final int das = 200;    // millisecond value for the delay between pushing
                                    // a movement button and the movement auto-repeating
    
    public TetroBag(float x, float y) {
        notInPlay = true;
        shuffle();
        shuffle();
        this.currentTetromino = bag[0];
        this.x = x;
        this.y = y;
        this.X_INITIAL = x;
        this.Y_INITIAL = y;
        Tetromino.select(currentTetromino);
        if (this.currentTetromino == Tetromino.I) {
            this.x -= Tetris.W;
            this.y -= Tetris.W;
        }
        gr = new Grid(350f,-540f);
        save = new SavedPiece(200f,150f);
        ghost = new GhostPiece(this.currentTetromino,this.x,this.y,gr);
        startPiece();
        setGravity(gr.getLevel());
        timerStart();
    }
    
    private void shuffle() {
        System.arraycopy(queue, 0, bag, 0, bag.length);
        queue = Tetromino.values();
        Random rn = new Random();
        int ri;
        Tetromino ti;
        for (int i = 0; i < queue.length; i++) {
            ri = rn.nextInt(queue.length - i);
            ti = queue[ri];
            for (int j = ri; j < queue.length-i-1; j++) {
                queue[j] = queue[j+1];
            }
            queue[queue.length-i-1] = ti;
        }
    }
    
//    private void shuffle() {
//        System.arraycopy(queue,0,bag,0,queue.length);
//        Random r = new Random();
//        int ri;
//        Tetromino ti;
//        for (int i = 0; i < this.queue.length; i++) {
//            ri = r.nextInt(this.queue.length - i);
//            //System.out.println(ri);
//            ti = this.queue[ri];
//            //System.out.println(ti);
//            for (int j = ri; j < this.queue.length-i-1; j++) {
//                this.queue[j] = this.queue[j+1];
//                //System.out.println(this);
//            }
//            this.queue[this.queue.length-i-1] = ti;
//        }
//    }
    
    public void nextPiece() {
        //Tetromino.setRot(0);
        x = this.X_INITIAL;
        y = this.Y_INITIAL;
        bagPointer++;
        if (bagPointer >= bag.length) {
            bagPointer = 0;
            shuffle();
        }
        this.currentTetromino = bag[bagPointer];
        Tetromino.select(currentTetromino);
        if (this.currentTetromino == Tetromino.I) {
            this.x -= Tetris.W;
            this.y -= Tetris.W;
        }
        softDropStop();
        startPiece();
        save.unlock();
        fallAmount = 0f;
        lastTime = System.currentTimeMillis();
        lockTime = lastTime;
    }
    
    public void nextPiece(boolean hold) {
        nextPiece();
        if (!hold) return;
        save.lock();
    }
    
    private void startPiece() {
        lockTime = System.currentTimeMillis();
        if (gr.checkPos(Tetromino.getRend(), x, y)) {
            gameOver();
            return;
        }
        while (this.y < TOP - ((this.currentTetromino == Tetromino.I) ? W : 0)) {
            if (gr.checkBelow(Tetromino.getRend(), x, y)) break;
            y += W;
        }
        if (gr.checkBelow(Tetromino.getRend(), x, y)) {
            lockTime = System.currentTimeMillis();
            lock(true);
        }
    }
    
    public void resetPos() {
        x = this.X_INITIAL;
        y = this.Y_INITIAL;
        //if (this.currentTetromino == Tetromino.O) x += W;
        if (this.currentTetromino == Tetromino.I) {
            this.x -= Tetris.W;
            this.y -= Tetris.W;
        }
        startPiece();
        fallAmount = 0f;
        movAmount = 0f;
        lastTime = System.currentTimeMillis();
        movTime = lastTime;
        lockTime = lastTime;
    }
    
    private boolean notInPlay = false;
    
    public void gameOver() {
        System.out.println("game over");
        System.out.printf("Level: %d\nLines Cleared: %d\n",gr.getLevel(),gr.getLinesCleared());
        notInPlay = true;
        restart();
    }
    
    public void restart() {
        gr.reset();
        save.reset();
        setGravity(gr.getLevel());
        shuffle();
        shuffle();
        bagPointer = 0;
        this.currentTetromino = bag[0];
        Tetromino.select(currentTetromino);
        resetPos();
        //notInPlay = false;
        timerStart();
    }
    
    private float pauseFallAmount = 0f;
    private int pauseFallTimeDif = 0;
    private int pauseLockTimeDif = 0;
    
    public void pause() {
        //notInPlay = !notInPlay;
        if (notInPlay) 
            timerStart();
        else {
            notInPlay = true;
            pauseFallAmount = fallAmount;
            pauseFallTimeDif = (int) (System.currentTimeMillis() - lastTime);
            pauseLockTimeDif = (int) (System.currentTimeMillis() - lockTime);
        }
    }
    
    public Tetromino getCurrentTetromino() {
        return this.currentTetromino;
    }
    
    public void setCurrentTetromino(Tetromino tetr) {
        bag[bagPointer] = tetr;
        this.currentTetromino = tetr;
        Tetromino.select(tetr);
    }
    
    public void render(Graphics g) {
        gr.render(g);
        save.render(g);
        ghost.render(g);
        renderNext(800,150,g);
        Tetromino.render(x, y, currentTetromino, g);
    }
    
    private Tetromino[] getNext() {
        Tetromino[] tets = new Tetromino[3];
        for (int i = 1; i <= 3; i++) {
            if (bagPointer+i > 6) {
                tets[i-1] = queue[bagPointer-7+i];
            } else {
                tets[i-1] = bag[bagPointer+i];
            }
        }
        return tets;
    }
    
    public void renderNext(float x, float y, Graphics g) {
        g.setColor(Color.black);
        g.fillRect((int)(x-W*2.5f), (int)(y-W*1.5f), (int)(5*W+2), (int)(8*W+2));
        Tetromino[] tets = getNext();
        for (int i = 0; i < tets.length; i++) {
            tets[i].render(x, y+2.5f*W*i, g);
        }
    }
    
    public void queueRotate(int amount) {
        rot = amount;
    }
    
    private float rx;
    private float ry;
    
    private void rotate() {
        int tRot = rot;
        rot = 0;
        //if (this.currentTetromino == Tetromino.O) return;
        //Square[][] oRend = Tetromino.getRend();
        int oRot = Tetromino.getRot();
        int nRot = Tetromino.rotateRight(tRot);
        //System.out.printf("oRot: %d, nRot : %d\n",oRot,nRot);
        //boolean fail = kickTests(nRot,oRot,Tetromino.getRendBuf());
        //if (gr.checkPos(Tetromino.getRend(), x, y)) fail = kickTests(nRot,oRot,Tetromino.getRendBuf());
        //if (!fail) Tetromino.applyRotate();
        //if (kickTests(nRot,oRot,Tetromino.getRendBuf()) || gr.checkPos(Tetromino.getRendBuf(), x+rx, y+ry)) { //|| gr.checkPos(Tetromino.getRendBuf(), x+rx, y+ry)) {
        if (offset(oRot,nRot,Tetromino.getRendBuf())) {
            Tetromino.failRotate();
        } else {
            Tetromino.applyRotate();
            x += rx;
            y += ry;
        }
        rx = 0;
        ry = 0;
    }
    
    private final int[][][] GEN_OFFSET_DATA =   {{{0,0,0,0,0},{0,0,0,0,0}},
                                                {{0,1,1,0,1},{0,0,-1,2,2}},
                                                {{0,0,0,0,0},{0,0,0,0,0}},
                                                {{0,-1,-1,0,-1},{0,0,-1,2,2}}};
    
    private final int[][][] I_OFFSET_DATA = {{{0,-1,2,-1,2},{0,0,0,0,0}},
                                            {{-1,0,0,0,0},{0,0,0,1,-2}},
                                            {{-1,1,-2,1,-2},{1,1,1,0,0}},
                                            {{0,0,0,0,0},{1,1,1,-1,2}}};
    
    private final int[][][] O_OFFSET_DATA = {{{0},{0}},
                                            {{0},{-1}},
                                            {{-1},{-1}},
                                            {{-1},{0}}};
    
    private int[][] getOffsetValues(int rotFrom, int rotTo) {
        int[][] offsetFrom;
        int[][] offsetTo;
        int[][] out;
        switch (this.currentTetromino) {
            case O:
                offsetFrom = O_OFFSET_DATA[rotFrom];
                offsetTo = O_OFFSET_DATA[rotTo];
                break;
            case I:
                offsetFrom = I_OFFSET_DATA[rotFrom];
                offsetTo = I_OFFSET_DATA[rotTo];
                break;
            default:
                offsetFrom = GEN_OFFSET_DATA[rotFrom];
                offsetTo = GEN_OFFSET_DATA[rotTo];
                break;
        }
        out = new int[offsetFrom.length][];
        for (int i = 0; i < offsetTo.length; i++) {
            out[i] = new int[offsetFrom[i].length];
            for (int j = 0; j < offsetTo[i].length; j++) {
                out[i][j] = offsetFrom[i][j] - offsetTo[i][j];
            }
        }
        return out;
    }
    
    private boolean offset(int rotFrom, int rotTo, Square[][] rend) {
        int[][] offsets = getOffsetValues(rotFrom,rotTo);
        for (int i = 0; i < offsets[0].length; i++) {
            if (!gr.checkPos(rend, x+(offsets[0][i]*W), y-(offsets[1][i]*W))) {
                rx = offsets[0][i]*W;
                ry = -offsets[1][i]*W;
                return false;
            }
        }
        return true;
    }
    
    public final void setGravity(float level) {
        double time = Math.pow((0.8-((level-1)*0.007)),level-1);
        gravityWatch = 1f/(float)time;
        gravity = gravityWatch;
        //gravityWatch += level;
        //gravity = gravityWatch;
    }
    
    public void resetGravity() {
        fallAmount = 0;
        lastTime = System.currentTimeMillis();
    }
    
    private float fallAmount = 0f;
    private long lastTime = System.currentTimeMillis();
    
    public void queueGravity() {
        long now = System.currentTimeMillis();
        float delta = (float)(now - lastTime)/1000f;   // delta = time in seconds since last call
        fallAmount += gravity*delta;
        lastTime = now;
    }
    
    public void applyGravity() {
        float dyLimit = ghost.getY() - this.y;
        float low = (float) Math.floor(fallAmount);
        dy = low*W;
        fallAmount -= low;
        if (dy > dyLimit) {
            dy = dyLimit;
        }
//        while (gr.checkPos(Tetromino.getRend(), x, y+dy)) {
//            dy--;
//        }
    }
    
    //private boolean rMovReq = false;
    
    public void requestRightMove() {
        //rMovReq = true;
        //tapped = true;
        reqDirection = 1;
    }
    
    //private boolean lMovReq = false;
    
    public void requestLeftMove() {
        //lMovReq = true;
        //tapped = true;
        reqDirection = -1;
        //System.out.println("worked");
    }
    
    public void stopMove() {
        //rMovReq = false;
        //lMovReq = false;
        movAmount = 0f;
        movTime = System.currentTimeMillis();
        direction = 0;
        reqDirection = 0;
    }
    
    private boolean tapped = false;
    private long dasTime = System.currentTimeMillis();
    private int reqDirection = 0;
    
    private void movementManager() {
        if (reqDirection == 0) {
            movAmount = 0f;
            movTime = System.currentTimeMillis();
            return;
        }
        if (direction != reqDirection) {
            direction = reqDirection;
            movAmount = 0f;
            tapped = true;
        }
        if (tapped) {
            dx = (float) direction*W;
            tapped = false;
            dasTime = System.currentTimeMillis();
            movTime = dasTime;
            movAmount = 0f;
            return;
        }
        if (System.currentTimeMillis() - dasTime > das) {
            //movTime = System.currentTimeMillis();
            queueMove();
            applyMove();
        }
    }
    
    private float movAmount = 0f;
    private long movTime = System.currentTimeMillis();
    private int direction = 0;
    
    private void queueMove() {
        long now = System.currentTimeMillis();
        float delta = (float)(now - movTime)/1000f;
        //if (direction > 0) direction = 1;
        //if (direction < 0) direction = -1;
        movAmount += movSpeed*delta;
        movTime = now;
    }
    
    private void applyMove() {
        float low = (float) Math.floor(movAmount);
        dx = low*W*(float)direction;
        movAmount -= low;
    }
    
    public void hardDrop() {
        y = ghost.getY();
        lock(false);
    }
    
    public void softDropStart() {
        if (gravity <= softGravity)
            gravity = softGravity;
    }
    
    public void softDropStop() {
        gravity = gravityWatch;
    }
    
    public void hold() {
        if (!notInPlay)
            save.queueHold(currentTetromino);
    }
    
    private boolean inTimer = false;
    public int timerState = 0;
    
    private void timerStart() {
        inTimer = true;
        notInPlay = true;
        countdownTime = System.currentTimeMillis();
    }
    
    private long countdownTime;
    
    private void countdownTimer() {
        long dif = System.currentTimeMillis() - countdownTime;
        if (dif < 1000) {
            timerState = 3;
        } else if (dif >= 1000 && dif < 2000) {
            timerState = 2;
        } else if (dif >= 2000 && dif < 3000) {
            timerState = 1;
        } else {
            inTimer = false;
            notInPlay = false;
            timerState = 0;
        }
    }
    
    public void update() {
        if (!notInPlay) {
            updateGrav();
            updateMov();
            updateRot();
            ghost.update(currentTetromino, x, y, gr);
            save.update(this);
        } else {
            reqDirection = 0;
            rot = 0;
            movAmount = 0f;
            fallAmount = pauseFallAmount;
            lastTime = System.currentTimeMillis() - pauseFallTimeDif;
            movTime = System.currentTimeMillis();
            lockTime = System.currentTimeMillis() - pauseLockTimeDif;
            if (inTimer) 
                countdownTimer();
        }
    }
    
    private void updateRot() {
        if (rot == 0) return;
//        Square[][] re = Tetromino.getRend();
//        float cx = x;
//        float cy = y;
        rotate();
//        if (gr.checkPos(Tetromino.getRend(), x, y)) {
//            Tetromino.setRend(re);
//            x = cx;
//            y = cy;
//            return;
//        }
        lockTime = System.currentTimeMillis();
        rot = 0;
    }
    
    private void updateMov() {
        movementManager();
        if (gr.checkPos(Tetromino.getRend(), x+dx, y)) dx = 0;
        if (dx == 0) {
            //movAmount = 0f;
            //movTime = System.currentTimeMillis();
            return;
        }
        if ((!gr.checkRight(Tetromino.getRend(), x, y) && dx > 0) || (!gr.checkLeft(Tetromino.getRend(), x, y) && dx < 0))
            x += dx;
        dx = 0;
        lockTime = System.currentTimeMillis();
    }
    
    private void updateGrav() {
        queueGravity();
        applyGravity();
        //if (dy == 0) return;
        //if (gr.checkBelow(Tetromino.getRend(), x, y)) dy = 0;
        float gy = ghost.getY();
        if (y == gy) {
            dy = 0;
            lock(true);
            return;
        }
        if (y+dy > gy) {
            //dy = gy-y;
            dy = 0;
            y = gy;
            lock(true);
            return;
        }
        if (gr.checkPos(Tetromino.getRend(), x, y+dy)) dy = 0;
        y += dy;
        dy = 0;
        lockTime = System.currentTimeMillis();
    }
    
    long lockTime = System.currentTimeMillis();
    
    private void lock(boolean wait) {
        dy = 0;
        if (System.currentTimeMillis() - lockTime < 500 && wait)
            return;
        
        try {
        gr.insert(Tetromino.getRend(), x, y, this);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (gr.checkPos2(Tetromino.getRend(), x, y)) {
            gameOver();
            return;
        }
        nextPiece();
    }
    
    public int getLevel() {
        return gr.getLevel();
    }
    
    public float getGravity() {
        return this.gravity;
    }
    
    public int getLinesCleared() {
        return gr.getLinesCleared();
    }
}
