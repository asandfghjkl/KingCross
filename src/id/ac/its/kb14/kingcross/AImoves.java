package id.ac.its.kb14.kingcross;

import java.util.ArrayList;
import java.util.Random;

public class AImoves {
    public AImoves(){
    }

    public AImoves(Player player){
    }

    public Board move(Board state, Player player){
        if (state.getTurn() == player){
            ArrayList<Board> successors = state.getSuccessors();
            Random rand = new Random();
            int i = rand.nextInt(successors.size());
            return successors.get(i);
        }
        else{
            throw new RuntimeException("Cannot generate moves for player if it's not their turn");
        }
    }

  
        
   
}
