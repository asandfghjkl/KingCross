package id.ac.its.kb14.kingcross;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class GUI extends JFrame{

    private Game game;
    private ArrayList<Board> possibleMoves;
    private SquarePanel[] squares;
    private JPanel checkerboardPanel;
    private JPanel contentPane;
    private JTextArea textBox;
    private Board hintMove;
    private List<Integer> helpMoves;
    private HashMap<Integer, Integer> difficultyMapping;
    public static boolean FORCETAKES = true; //
    public static Player FIRSTMOVE = Player.HUMAN; // who moves first
    public static int AI_DEPTH = 7;
    public static final int UNDO_MEMORY = 20;
    public static int HEURISTIC = 1;

    public GUI(){
    	setIcon();
		setTitle();
		difficultyMapping = new HashMap<>();
        difficultyMapping.put(1,1);
        difficultyMapping.put(2, 5);
        difficultyMapping.put(3, 8);
        difficultyMapping.put(4, 12);
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
    	settingsPopup();
        game = new Game();
        possibleMoves = new ArrayList<>();
        hintMove = null;
        setup();
        if (Settings.hintMode){
            onHintClick();
        }
    }

    private void settingsPopup(){
        // panel for options
        JPanel panel = new JPanel(new GridLayout(6,0));
        // difficulty slider
        JLabel text1 = new JLabel("Set Difficulty", 10);
        JSlider slider = new JSlider();
        slider.setSnapToTicks(true);
        slider.setMaximum(4);
        slider.setMinimum(1);
        slider.setMajorTickSpacing(1);
        Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(1, new JLabel("Easy"));
        labels.put(2, new JLabel("Medium"));
        labels.put(3, new JLabel("Hard"));
        labels.put(4, new JLabel("On Fire"));
        slider.setLabelTable(labels);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension(200,50));
        slider.setValue(3);
        // force takes option
        JRadioButton forceTakesButton = new JRadioButton("Force Takes");
        forceTakesButton.setSelected(FORCETAKES);
        // who gets first move?
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton humanFirstRadioButton = new JRadioButton("You Play First");
        JRadioButton aiRadioButton = new JRadioButton("Computer Plays First");
        buttonGroup.add(humanFirstRadioButton);
        buttonGroup.add(aiRadioButton);
        aiRadioButton.setSelected(FIRSTMOVE== Player.AI);
        humanFirstRadioButton.setSelected(FIRSTMOVE== Player.HUMAN);
        // add components to panel
        panel.add(new JLabel(new ImageIcon("images/name.png")));
        panel.add(text1);
        panel.add(slider);
        panel.add(forceTakesButton);
        panel.add(humanFirstRadioButton);
        panel.add(aiRadioButton);
        // pop up
        int result = JOptionPane.showConfirmDialog(null, panel, "Start Panel",
                     JOptionPane.OK_CANCEL_OPTION);
        // process results
        if(result == JOptionPane.OK_OPTION){
            AI_DEPTH =  difficultyMapping.get(slider.getValue());
            System.out.println("AI depth = " + AI_DEPTH);
            FIRSTMOVE = humanFirstRadioButton.isSelected() ? Player.HUMAN : Player.AI;
            FORCETAKES = forceTakesButton.isSelected();
        }
        else if(result == JOptionPane.CANCEL_OPTION) System.exit(0);
    }
    
    public void setup() {
    	switch (FIRSTMOVE){
        case AI:
            Settings.AIcolour = Colour.WHITE;
            break;
        case HUMAN:
            Settings.AIcolour = Colour.BLACK;
            break;
    	}
    	setupMenuBar();
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
        updateText("");
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        if (FIRSTMOVE == Player.AI){
            aiMove();
        }
    }

    private void updateText(String text){
        textBox.setText(text);
    }
    
    private void updateCheckerBoard(){
        checkerboardPanel.removeAll();
        addPieces();
        addSquares();
        addGhostButtons();
        checkerboardPanel.setVisible(true);
        checkerboardPanel.repaint();  
        this.pack();
        this.setVisible(true);
    }

    private void addSquares(){
        squares = new SquarePanel[game.getState().NO_SQUARES];
        int fromPos = -1;
        int toPos = -1;
        if(hintMove != null){
            fromPos = hintMove.getFromPos();
            toPos = hintMove.getToPos();
        }
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
            if (helpMoves != null){
                if (helpMoves.contains(i)){
                    squares[i].setHighlighted();
                }
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
    
    private void addGhostButtons(){
        for (Board state : possibleMoves){
            int newPos = state.getToPos();
            GhostButton button = new GhostButton(state);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    onGhostButtonClick(actionEvent);
                }
            });
            squares[newPos].add(button);
        }
    }
    
    private void setupMenuBar(){

        // ensure exit method is called on window closing
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        onExitClick();
                    }
                }
        );
        // initialize components
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem restartItem = new JMenuItem("Restart");
        JMenuItem quitItem = new JMenuItem("Quit");
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenu viewMenu = new JMenu("View");
        JRadioButtonMenuItem viewItemHelpMode = new JRadioButtonMenuItem("Help mode");
        JRadioButtonMenuItem viewItemHintMode = new JRadioButtonMenuItem("Hint mode");
        viewItemHelpMode.setSelected(Settings.helpMode);
        viewItemHintMode.setSelected(Settings.hintMode);
        JMenu helpMenu = new JMenu("Help");
        JMenuItem rulesItem = new JMenuItem("Game Rules");
        JMenuItem helpItemHint = new JMenuItem("Hint!");
        JMenuItem helpItemMovables = new JMenuItem("Show movable pieces");

        // add action listeners
        quitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onExitClick();
            }
        });
        restartItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onRestartClick();
            }
        });
        rulesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onRulesClick();
            }
        });
        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onUndoClick();
            }
        });
        viewItemHelpMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onHelpModeClick();
            }
        });
        viewItemHintMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onHintModeClick();
            }
        });
        helpItemHint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onHintClick();
            }
        });
        helpItemMovables.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onHelpMovablesClick();
            }
        });


        // add components to menu bar
        fileMenu.add(restartItem);
        fileMenu.add(quitItem);
        editMenu.add(undoItem);
        viewMenu.add(viewItemHelpMode);
        viewMenu.add(viewItemHintMode);
        helpMenu.add(helpItemHint);
        helpMenu.add(helpItemMovables);
        helpMenu.add(rulesItem);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);
    }
    
    public void onMouseRelease(int position, int dx, int dy){
    	MoveFeedback feedback = game.playerMove(position, dx, dy);
        if (feedback == MoveFeedback.SUCCESS){
            updateCheckerBoard();
            aiMove();
        }
        else{
            updateCheckerBoard();
            System.out.println(feedback.toString());
        }
    }


    private void onHintClick(){
        if (!game.isGameOver()){
            AImoves ai = new AImoves(10, Player.HUMAN);
            helpMoves = null;
            hintMove = ai.move(this.game.getState(), Player.HUMAN);
            updateCheckerBoard();
        }
    }

    private void onHelpMovablesClick(){
        hintMove = null;
        helpMoves = game.getState().getSuccessors().stream().map(x -> x.getFromPos()).collect(Collectors.toList());
        updateCheckerBoard();
    }

    private void onHelpModeClick(){
        Settings.helpMode = !Settings.helpMode;
        System.out.println("help mode: " + Settings.helpMode);
    }

    private void onHintModeClick(){
        Settings.hintMode = !Settings.hintMode;
        System.out.println("hint mode: " + Settings.hintMode);
        onHintClick();
    }
    
    private void onPieceClick(ActionEvent actionEvent){
    	if(game.getTurn() == Player.HUMAN ){
            Button button = (Button) actionEvent.getSource();
            int pos = button.getPosition();
            if(button.getPiece().getPlayer() == Player.HUMAN){
                possibleMoves = game.getValidMoves(pos);
                updateCheckerBoard();
                if (possibleMoves.size() == 0){
                    MoveFeedback feedback = game.moveFeedbackClick(pos);
                    updateText(feedback.toString());
                    if (feedback == MoveFeedback.FORCED_JUMP){
                        // show movable jump pieces
                        onHelpMovablesClick();
                    }
                }
                else{
                    updateText("");
                }
            }
        }
    }

    private void onGhostButtonClick(ActionEvent actionEvent){
        if (!game.isGameOver() && game.getTurn() == Player.HUMAN){
            hintMove = null;
            helpMoves = null;
            GhostButton button = (GhostButton) actionEvent.getSource();
            game.playerMove(button.getBoardstate());
            possibleMoves = new ArrayList<>();
            updateCheckerBoard();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    aiMove();
                    if (game.isGameOver()){
                        gameOver();
                    }
                }
            });
        }
    }

    private void gameOver(){
        JOptionPane.showMessageDialog(this,
                game.getGameOverMessage(),
                "",
                JOptionPane.INFORMATION_MESSAGE );
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
    
    private void onRestartClick()
    {
        Object[] options = {"Yes",
                "No", };
        int n = JOptionPane.showOptionDialog(this, "Are you sure you want to restart?",
                "Restart game? ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        if (n == 0){
            start();
        }
    }

    private void onExitClick(){
        Object[] options = {"Yes",
                "No", };
        int n = JOptionPane.showOptionDialog(this,
                        "\nAre you sure you want to leave?",
                "Quit game? ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);
        if (n == 0){
            // close logging file
            this.dispose();
            System.exit(0);
        }
    }
    
    private void onRulesClick(){

        String message =
                "1. Moves are allowed only on the dark squares, so pieces always move diagonally. Single " +
                "pieces are always limited to forward moves (toward the opponent). <br /> <br /> " +

                "2. A piece making a non-capturing move (not involving a jump) may move only one square. <br /> <br />"+

                "3. A piece making a capturing move (a jump) leaps over one of the opponent's pieces, landing in a " +
                "straight diagonal line on the other side. Only one piece may be captured in a single jump; however, " +
                "multiple jumps are allowed during a single turn. <br /> <br />" +

                        "4. When a piece is captured, it is removed from the board. <br /> <br />" +
                "5. If a player is able to make a capture, there is no option; the jump must be made. If more than " +
                   "one capture is available, the player is free to choose whichever he or she prefers. <br /> <br />" +

                "6. When a piece reaches the furthest row from the player who controls that piece, it is crowned and " +
                        "becomes a king. <br /> <br />" +
                "7. Kings are limited to moving diagonally but may move both forward and backward. <br /> <br />" +
                "8. Kings may combine jumps in several directions, forward and backward, on the same turn. Single " +
                        "pieces may shift direction diagonally during a multiple capture turn, but must always jump " +
                        "forward (toward the opponent).";

        JOptionPane.showMessageDialog(this,
                "<html><body><p style='width: 400px;'>"+message+"</p></body></html>",
                "",
                JOptionPane.INFORMATION_MESSAGE );
    }

    private void onUndoClick(){
        game.undo();
        updateCheckerBoard();
        if (Settings.hintMode){
            onHintClick();
        }
    }
    
}
