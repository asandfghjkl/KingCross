package id.ac.its.kb14.kingcross;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import main.game.BoardState;
import main.game.Player;
import main.game.Settings;

public class Board {
    public static final int SIDE_LENGTH = 8;
    public static final int NO_SQUARES = SIDE_LENGTH*SIDE_LENGTH; 
    Piece[] state;
    private int fromPos = -1;
    private int toPos = -1;
    private int doublejumpPos = -1;
    private Player turn;
    public HashMap<Player, Integer> pieceCount;
    private HashMap<Player, Integer> kingCount;

    public Board(){
        state = new Piece[Board.NO_SQUARES];
    }
    
    public static Board InitialState(){
        Board bs = new Board();
        bs.turn = Player.HUMAN;
        for (int i = 0; i < bs.state.length; i++){
            int y = i/SIDE_LENGTH;
            int x = i % SIDE_LENGTH;
            if ((x + y) % 2 == 1 ){
                if (y < 3){
                    bs.state[i] = new Piece(Player.AI, false);
                }
                else if (y  > 4){
                    bs.state[i] = new Piece(Player.HUMAN, false);
                }
            }
        }
        int aiCount = (int) Arrays.stream(bs.state).filter(x -> x != null).filter(x -> x.getPlayer() == Player.AI).count();
        int humanCount = (int) Arrays.stream(bs.state).filter(x -> x != null).filter(x -> x.getPlayer() == Player.HUMAN).count();
        bs.pieceCount = new HashMap<>();
        bs.pieceCount.put(Player.AI, aiCount);
        bs.pieceCount.put(Player.HUMAN,humanCount);
        bs.kingCount = new HashMap<>();
        bs.kingCount.put(Player.AI, 0);
        bs.kingCount.put(Player.HUMAN, 0);
        return bs;
    }

    private Board deepCopy(){
        Board bs = new Board();
        System.arraycopy(this.state, 0, bs.state, 0, bs.state.length);
        return bs;
    }
    public int computeHeuristic(Player player){
        switch (Settings.HEURISTIC){
            case 1:
                return heuristic1(player);
            case 2:
                return heuristic2(player);
        }
        throw new RuntimeException("Invalid heuristic");
    }

    private int heuristic1(Player player){
        // 'infinite' value for winning
        if (this.pieceCount.get(player.getOpposite()) == 0){
            return Integer.MAX_VALUE;
        }
        // 'negative infinite' for losing
        if (this.pieceCount.get(player) == 0){
            return Integer.MIN_VALUE;
        }
        // difference between piece counts with kings counted twice
        return pieceScore(player) - pieceScore(player.getOpposite());
    }


    private int heuristic2(Player player){
        // 'infinite' value for winning
        if (this.pieceCount.get(player.getOpposite()) == 0){
            return Integer.MAX_VALUE;
        }
        // 'negative infinite' for losing
        else if (this.pieceCount.get(player) == 0){
            return Integer.MIN_VALUE;
        }
        else{
            return pieceScore(player)/pieceScore(player.getOpposite());
        }
    }

    private int pieceScore(Player player){
        return this.pieceCount.get(player) + this.kingCount.get(player);
    }

    public ArrayList<Board> getSuccessors(){
    	ArrayList<Board> successors = getSuccessors(true);
        if (Settings.FORCETAKES){
            if (successors.size() > 0){
                // return only jump successors if available (forced)
                return  successors;
            }
            else{
                // return non-jump successors (since no jumps available)
                return getSuccessors(false);
            }
        }
        else{
            // return jump and non-jump successors
            successors.addAll(getSuccessors(false));
            return successors;
        }
    }
    
    public ArrayList<Board> getSuccessors(boolean jump){
        ArrayList<Board> result = new ArrayList<>();
        for (int i = 0; i < this.state.length; i++){
            if (state[i] != null){
                if(state[i].getPlayer() == turn){
                    result.addAll(getSuccessors(i, jump));
                }
            }
        }
        return result;
    }

    public ArrayList<Board> getSuccessors(int position){
    	if (Settings.FORCETAKES){
            // compute jump successors GLOBALLY
            ArrayList<Board> jumps = getSuccessors(true);
            if (jumps.size() > 0){
                // return only jump successors if available (forced)
                return getSuccessors(position, true);
            }
            else{
                // return non-jump successors (since no jumps available)
                return getSuccessors(position, false);
            }
        }
        else{
            // return jump and non-jump successors
            ArrayList<Board> result = new ArrayList<>();
            result.addAll(getSuccessors(position, true));
            result.addAll(getSuccessors(position, false));
            return result;
        }
    }

    public ArrayList<Board> getSuccessors(int position, boolean jump){
        if (this.getPiece(position).getPlayer() != turn){
            throw new IllegalArgumentException("No such piece at that position");
        }
        Piece piece = this.state[position];
        if(jump){
            return jumpSuccessors(piece, position);
        }
        else{
            return nonJumpSuccessors(piece, position);
        }
    }

    private ArrayList<Board> nonJumpSuccessors(Piece piece, int position){
        ArrayList<Board> result = new ArrayList<>();
        int x = position % SIDE_LENGTH;
        int y = position / SIDE_LENGTH;
        for (int dx : piece.getXMovements()){
            for (int dy : piece.getYMovements()){
                int newX = x + dx;
                int newY = y + dy;
                if (isValid(newY, newX)) {
                    if (getPiece(newY, newX) == null) {
                        int newpos = SIDE_LENGTH*newY + newX;
                        result.add(createNewState(position, newpos, piece, false, dy,dx));
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<Board> jumpSuccessors(Piece piece, int position){
        ArrayList<Board> result = new ArrayList<>();
        if (doublejumpPos > 0 && position != doublejumpPos){
            return result;
        }
        int x = position % SIDE_LENGTH;
        int y = position / SIDE_LENGTH;
        for (int dx : piece.getXMovements()){
            for (int dy : piece.getYMovements()){
                int newX = x + dx;
                int newY = y + dy;
                if (isValid(newY, newX)) {
                    if (getPiece(newY,newX) != null && getPiece(newY, newX).getPlayer() == piece.getPlayer().getOpposite()){
                        newX = newX + dx; newY = newY + dy;
                        if (isValid(newY, newX)){
                            if (getPiece(newY,newX) == null) {
                                int newpos = SIDE_LENGTH*newY + newX;
                                result.add(createNewState(position, newpos, piece, true, dy, dx));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private Board createNewState(int oldPos, int newPos, Piece piece, boolean jumped, int dy, int dx){
        Board result = this.deepCopy();
        result.pieceCount = new HashMap<>(pieceCount);
        result.kingCount = new HashMap<>(kingCount);
        boolean kingConversion = false;
        if (isKingPosition(newPos, piece.getPlayer())){
            piece = new Piece(piece.getPlayer(), true);
            kingConversion = true;
            result.kingCount.replace(piece.getPlayer(), result.kingCount.get(piece.getPlayer()) + 1);
        }
        result.state[oldPos] = null;
        result.state[newPos] = piece;
        result.fromPos = oldPos;
        result.toPos = newPos;
        Player oppPlayer = piece.getPlayer().getOpposite();
        result.turn = oppPlayer;
        if (jumped){
            result.state[newPos - SIDE_LENGTH*dy - dx] = null;
            result.pieceCount.replace(oppPlayer, result.pieceCount.get(oppPlayer) - 1);
            if (result.jumpSuccessors(piece, newPos).size() > 0 && kingConversion == false){
                result.turn = piece.getPlayer();
                result.doublejumpPos = newPos;
            }
        }
        return result;
    }

    private boolean isKingPosition(int pos, Player player){
        int y = pos / SIDE_LENGTH;
        if (y == 0 && player == Player.HUMAN){
            return true;
        }
        else return y == SIDE_LENGTH - 1 && player == Player.AI;
    }

    public int getToPos(){
        return this.toPos;
    }

    public int getFromPos(){
        return this.fromPos;
    }

    public Player getTurn() {
        return turn;
    }
    
    public Piece getPiece(int i){
        return state[i];
    }

    private Piece getPiece(int y, int x){
        return getPiece(SIDE_LENGTH*y + x);
    }
    
    public boolean isGameOver(){
        return (pieceCount.get(Player.AI) == 0 || pieceCount.get(Player.HUMAN) == 0);
    }
    
    private boolean isValid(int y, int x){
        return (0 <= y) && (y < SIDE_LENGTH) && (0 <= x) && (x < SIDE_LENGTH);
    }

}