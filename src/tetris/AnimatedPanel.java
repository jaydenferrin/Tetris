package tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
//import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class AnimatedPanel extends JPanel implements Runnable {
    //public Urod ts;
    
    private int width = 350;
    private int height = 350;
    private Color background = Color.BLACK;
    
    private Thread animationThread;
    private float fpsTarget = 60f;
    private long frameRatePeriod = (long) (1e9 / fpsTarget);
    
    public int frameCount = 0;
    protected long frameRateLastNanos = System.nanoTime();
    public float frameRate = 60f;
    
    public TetroBag play;
    
    public Graphics g = this.getGraphics();
    
    public AnimatedPanel() {
        initPane();
    }
    
    public void setWidth(int w) {
        this.width = w;
    }
    
    public void setHeight(int h) {
        this.height = h;
    }
    
    public void background(Color c) {
        this.background = c;
    }
    
    public void setFrameRate(float fps) {
        fpsTarget = fps;
        frameRatePeriod = (long) (1e9 / fpsTarget);
    }
    
    private void initPane() {
        addKeyListener(new TAdapter());
        this.setFocusable(true);
        setupDisplay();
        this.setBackground(background);
        setPreferredSize(new Dimension(width,height));
        play = new TetroBag(440,0);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        
        this.animationThread = new Thread(this);
        this.animationThread.start();
    }

    @Override
    public void run() {
        long beforeTime,timeDiff,sleep;
        beforeTime = System.nanoTime();
        
        while (true) {
            //repaint();
            handleFrame();
            
            long afterTime = System.nanoTime();
            timeDiff = afterTime - beforeTime;
            sleep = (frameRatePeriod - timeDiff);
            
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep/1_000_000L, (int) (sleep%1_000_000L));
                } catch (InterruptedException ex) {
                    Logger.getLogger(AnimatedPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            beforeTime = System.nanoTime();
        }
    }
    
    public void setupDisplay() {
        setWidth(1000);
        setHeight(700);
        background(Color.LIGHT_GRAY);
        //background(new Color(0xc0,0xc0,0xc0,0.5f));
        //setFrameRate(240f);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateDisplay(g);
        Toolkit.getDefaultToolkit().sync();
    }
    
    private int x = 40;
    private int y = 40;
    
    public void updateDisplay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        //g.setColor(Color.RED);
        //g.fillRect(x, y, 40, 40);
        //g2.setPaint(Color.BLACK);
        //g2.setStroke(new BasicStroke(6));
        //g2.draw(new Rectangle2D.Double(x+3, y+3, 25-5, 25-5));
        
        //g2.setStroke(new BasicStroke());
        //g.setColor(Color.white);
        //g.drawLine(0, y, 1000, y);
        //g.drawLine(x, 0, x, 700);
        //g.drawLine(0, y+25, 1000, y+25);
        //g.drawLine(x+25, 0, x+25, 700);
        
        //Square sq = new Square();
        //sq.width(2);
        //sq.fill(Color.cyan);
        //sq.draw(x, y, 25, g);
        //Tetromino.select(Tetromino.J);
        //Tetromino.render(100, 100, Tetromino.J, g);
        y++;
        x += 2;
        // non-testing code
        play.render(g);
        play.update();
        g2.drawString(String.format("Lines Cleared: %d", play.getLinesCleared()), 150, 500);
        g2.drawString(String.format("Level: %d", play.getLevel()), 150, 515);
        g2.drawString(String.format("Gravity: %d", Math.round(play.getGravity())), 150, 530);
        //g2.drawString(String.format("fps: %d", Math.round(frameRate)), 150, 545);
        if (play.timerState != 0) {
            g2.drawString(String.valueOf(play.timerState), 30, 30);
        }
//        g.drawLine(100, 150, 200, 150);
//        g.drawLine(150, 100, 150, 200);
        // non-testing code
//        float W = Tetris.W;
//        g.setColor(Color.lightGray);
//        g.fillRect(350, (int)(-540+18f*W), (int)(10f*W), (int)(2f*W));
        //System.out.println(frameRate);
//        g2.setPaint(Color.RED);
//        g2.fill(new Rectangle2D.Double(x, y, 25, 25));
        
        Toolkit.getDefaultToolkit().sync();
    }
    
    public void handleFrame() {
        repaint();
        long now = System.nanoTime();
        // calculate the exponential moving average to determine framerate
        frameRate = exponentialAveragefps(now);
        //updateDisplay(g);
        frameRateLastNanos = now;
        frameCount++;
    }
    
    private float exponentialAveragefps(long now) {
        // get the frame time of the last frame
        double frameTimeSecs = ((double)(now - frameRateLastNanos)) / 1e9;
        // convert average fps to average frame time
        double avgFrameTimeSecs = 1d / frameRate;
        // calculate exponential moving average of frame time
        final double alpha = 0.05;
        avgFrameTimeSecs = (frameTimeSecs * alpha) + avgFrameTimeSecs*(1d-alpha);
        return (float) (1d / avgFrameTimeSecs);
    }

    private class TAdapter extends KeyAdapter {
        private boolean rotL = true;
        private boolean rotR = true;
        private boolean spce = true;
        private boolean esc = true;
        
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    play.requestLeftMove();
                    break;
                case KeyEvent.VK_RIGHT:
                    play.requestRightMove();
                    break;
                case KeyEvent.VK_UP:
                    if (rotR) {
                        play.queueRotate(1);
                        rotR = false;
                    }
                    break;
                case KeyEvent.VK_Z:
                    if (rotL) {
                        play.queueRotate(-1);
                        rotL = false;
                    }
                    break;
                case KeyEvent.VK_C:
                    play.hold();
                    break;
                case KeyEvent.VK_DOWN:
                    play.softDropStart();
                    break;
                case KeyEvent.VK_SPACE:
                    if (spce) {
                        play.hardDrop();
                        spce = false;
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (esc) {
                        play.pause();
                        esc = false;
                    }
                    break;
            }
        }
        
        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    play.stopMove();
                    break;
                case KeyEvent.VK_RIGHT:
                    play.stopMove();
                    break;
                case KeyEvent.VK_UP:
                    rotR = true;
                    break;
                case KeyEvent.VK_Z:
                    rotL = true;
                    break;
                case KeyEvent.VK_DOWN:
                    play.softDropStop();
                    break;
                case KeyEvent.VK_SPACE:
                    spce = true;
                    break;
                case KeyEvent.VK_ESCAPE:
                    esc = true;
                    break;
            }
        }
    }
}
