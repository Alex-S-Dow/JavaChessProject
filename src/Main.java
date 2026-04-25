import java.awt.*;
import javax.swing.*;

//Main class that pulls the necessary jPanel from the created chessboard class and can be used to modify the main jFrame

public class Main {
    public static void main(String[] args) {
        Chessboard board = new Chessboard();
        int size = 700;
        JFrame f = new JFrame("Chess");
        //Gets the chessboard's jPanel and adds it to the jFrame
        f.add(board.getGUI());
        f.setSize(size, size);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLocationByPlatform(true);
        f.setResizable(false);
        //f.pack();
        f.setMinimumSize(f.getSize());
        f.setVisible(true);
    }
}