package id.ac.its.kb14.kingcross;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class GUI extends JFrame{

    private Game game;
    private SquarePanel[] squares;
    private JPanel checkerboardPanel;
    private JPanel contentPane;
    private JTextArea textBox;
    public static Player FIRSTMOVE = Player.HUMAN; 
    public static int AI_DEPTH = 7;

    public GUI(){
    	setIcon();
		setTitle();
        start();
    }

	public void setIcon() {
		ImageIcon img = new ImageIcon("images/icon.png");
		this.setIconImage(img.getImage());
	}
	
	public void setTitle() {
		this.setTitle("KingCross");
	}
	
    private void start(){
        game = new Game();
        new ArrayList<>();
        setup();
    }

    public void setup() {
        Settings.AIcolour = Colour.BLACK;

        contentPane = new JPanel();
        checkerboardPanel = new JPanel(new GridBagLayout());
        JPanel textPanel = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        this.setContentPane(contentPane);
        contentPane.add(checkerboardPanel);
        contentPane.add(textPanel);
        textBox = new JTextArea();
        textBox.setEditable(false);
        textBox.setLineWrap(false);
        textBox.setWrapStyleWord(true);
        textBox.setAutoscrolls(true);
        textPanel.add(textBox);

        updateCheckerBoard();
        this.pack();
        this.setVisible(true);
    }

    private void updateCheckerBoard(){
        checkerboardPanel.removeAll();
        addPieces();
        addSquares();
        checkerboardPanel.setVisible(true);
        checkerboardPanel.repaint();
        this.pack();
        this.setVisible(true);
    }

    private void addSquares(){
        squares = new SquarePanel[game.getState().NO_SQUARES];
        int fromPos = -1;
        int toPos = -1;
        
        GridBagConstraints c = new GridBagConstraints();
        for (int i = 0; i < game.getState().NO_SQUARES; i++){
            c.gridx = i % game.getState().SIDE_LENGTH;
            c.gridy = i / game.getState().SIDE_LENGTH;
            squares[i] = new SquarePanel(c.gridx, c.gridy);
            if (i == fromPos){
                squares[i].setHighlighted();
            }
            if(i == toPos){
                squares[i].setHighlighted();
            }
            checkerboardPanel.add(squares[i], c);
        }
    }

    private void addPieces(){
        GridBagConstraints c = new GridBagConstraints();
        for (int i = 0; i < game.getState().NO_SQUARES; i++){
            c.gridx = i % game.getState().SIDE_LENGTH;
            c.gridy = i / game.getState().SIDE_LENGTH;
            if(game.getState().getPiece(i) != null){
                Piece piece = game.getState().getPiece(i);
                Button button = new Button(i, piece, this, game);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        onPieceClick(actionEvent);
                    }
                });
                checkerboardPanel.add(button, c);
            }
        }
    }
    
    public void onMouseRelease(int position, int dx, int dy){
        updateCheckerBoard();
        aiMove();
    }

    private void onPieceClick(ActionEvent actionEvent){
        if(game.getTurn() == Player.HUMAN ){
            Button button = (Button) actionEvent.getSource();
            int pos = button.getPosition();
            if(button.getPiece().getPlayer() == Player.HUMAN){
                game.getValidMoves(pos);
                updateCheckerBoard();
            }
        }
    }

    private void aiMove(){
        long startTime = System.nanoTime();
        game.aiMove();
        long aiMoveDurationInMs = (System.nanoTime() - startTime)/1000000;
        long delayInMs = Math.max(0, 800 - aiMoveDurationInMs);
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.schedule(new Runnable(){
            @Override
            public void run(){
                invokeAiUpdate();
            }
        }, delayInMs, TimeUnit.MILLISECONDS);
    }

    private void invokeAiUpdate(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateCheckerBoard();
                if (game.getTurn() == Player.AI){
                    aiMove();
                }
            }
        });
    }
}
