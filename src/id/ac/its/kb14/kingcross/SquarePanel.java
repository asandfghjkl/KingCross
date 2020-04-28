package id.ac.its.kb14.kingcross;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class SquarePanel extends JPanel {

    private Color color;

    public SquarePanel(int i, int j){
        this.setPreferredSize(new Dimension(Settings.squareSize,Settings.squareSize));
        if( ((i % 2) + (j % 2)) % 2 == 0){
            color = Color.WHITE;
        }
        else{
            color = Color.BLACK;
        }
    }

    public void setHighlighted(){
        color = Color.GRAY;
    }


    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

}