package id.ac.its.kb14.kingcross;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Stack;

import main.game.BoardState;
import main.game.MoveFeedback;
import main.game.Player;
import main.game.Settings;

public class Game{

    private Stack<Board> state;
    private int memory;
    private AImoves ai;
    private boolean humanWon;

    public Game(){
    	memory = Settings.UNDO_MEMORY;
        state = new Stack<>();
        state.push(Board.InitialState());
        ai = new AImoves();
    }
    

    public void playerMove(Board newState){
        if (!isGameOver() && state.peek().getTurn() == Player.HUMAN){
            updateState(newState);
        }
    }
    

    public MoveFeedback playerMove(int fromPos, int dx, int dy){
        int toPos = fromPos + dx + Board.SIDE_LENGTH*dy;
        if (toPos > getState().state.length){
            return MoveFeedback.NOT_ON_BOARD;
        }
        // check for forced jumped
        ArrayList<Board> jumpSuccessors = this.state.peek().getSuccessors(true);
        boolean jumps = jumpSuccessors.size() > 0;
        if (jumps){
            for (Board succ : jumpSuccessors){
                if (succ.getFromPos() == fromPos && succ.getToPos() == toPos){
                    updateState(succ);
                    return MoveFeedback.SUCCESS;
                }
            }
            return MoveFeedback.FORCED_JUMP;
        }
        // check diagonal
        if (Math.abs(dx) != Math.abs(dy)){
            return MoveFeedback.NOT_DIAGONAL;
        }
        // check for move onto piece
        if (this.getState().state[toPos] != null){
            return MoveFeedback.NO_FREE_SPACE;
        }
        // check for non-jump moves
        ArrayList<Board> nonJumpSuccessors = this.state.peek().getSuccessors(fromPos, false);
        for (Board succ : nonJumpSuccessors){
            if (succ.getFromPos() == fromPos && succ.getToPos() == toPos){
                updateState(succ);
                return MoveFeedback.SUCCESS;
            }
        }    if (dy > 1){
            return MoveFeedback.NO_BACKWARD_MOVES_FOR_SINGLES;
        }
        if (Math.abs(dx)== 2){
            return MoveFeedback.ONLY_SINGLE_DIAGONALS;
        }
        return MoveFeedback.UNKNOWN_INVALID;
    }

    public MoveFeedback moveFeedbackClick(int pos){
        ArrayList<Board> jumpSuccessors = this.state.peek().getSuccessors(true);
        if (jumpSuccessors.size() > 0){
            return MoveFeedback.FORCED_JUMP;
        }
        else{
            return MoveFeedback.PIECE_BLOCKED;
        }

    }

    public ArrayList<Board> getValidMoves(int pos) {
        return state.peek().getSuccessors(pos);
    }

    public void aiMove(){
        // update state with AI move
        if (!isGameOver() && state.peek().getTurn() == Player.AI){
            Board newState = ai.move(this.state.peek(), Player.AI);
            updateState(newState);
        }
    }

    private void updateState(Board newState){
        state.push(newState);
        if(state.size() > memory){
            state.remove(0);
        }
    }

    public Board getState() {
        return state.peek();
    }


    public Player getTurn() {
        return state.peek().getTurn();
    }
    

    public boolean isGameOver(){
        boolean isOver = state.peek().isGameOver();
        if (isOver){
            // get win / lose status
            humanWon = state.peek().pieceCount.get(Player.AI) == 0;
        }
        return isOver;
    }

    public String getGameOverMessage(){
        String result = "Game Over. ";
        if (humanWon == true){
            result += "YOU WIN!";
        }
        else{
            result += "YOU LOSE!";
        }
        return result;
    }

    public void undo(){
        if (state.size() > 2){
            state.pop();
            while(state.peek().getTurn() == Player.AI){
                state.pop();
            }
        }
    }
}