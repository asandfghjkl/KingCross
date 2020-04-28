package id.ac.its.kb14.kingcross;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Stack;

public class Game{

    private Stack<Board> state;
    private int memory;
    private AImoves ai;

    public Game(){
        state = new Stack<>();
        state.push(Board.InitialState());
        ai = new AImoves();
    }
    

    public void playerMove(Board newState){
        if (state.peek().getTurn() == Player.HUMAN){
            updateState(newState);
        }
    }

    public ArrayList<Board> getValidMoves(int pos) {
        return state.peek().getSuccessors(pos);
    }

    public void aiMove(){
        if (state.peek().getTurn() == Player.AI){
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
}