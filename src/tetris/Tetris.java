package tetris;

//import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;

public class Tetris extends JFrame {
    private final AnimatedPanel an;
    public static final float W = 30;
    
    public Tetris() {
        an = new AnimatedPanel();
        this.add(an);
        this.setResizable(false);
        this.pack();
        
        this.setTitle("Tetris");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Tetris x = new Tetris();
            x.setVisible(true);
        });
        //System.out.println(x.an.getClass().getName());
        //System.out.println(Integer.toHexString(Color.lightGray.getRed()));
        //System.out.println(Integer.toHexString(Color.lightGray.getGreen()));
        //System.out.println(Integer.toHexString(Color.lightGray.getBlue()));
    }
    
}
