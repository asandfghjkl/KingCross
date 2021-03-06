package id.ac.its.kb14.kingcross;

import java.util.ArrayList;
import java.util.Random;

public class AImoves {
    private int depth;
    private Player player;
    
    public AImoves(){
    	depth = GUI.AI_DEPTH;
        player = Player.AI;
    }

    public AImoves(int depth, Player player){
    	 this.depth = depth;
         this.player = player;
    }
    public Board move(Board state, Player player){
        if (state.getTurn() == player){
            ArrayList<Board> successors = state.getSuccessors();
            return minimaxMove(successors);
        }
        else{
            throw new RuntimeException("Cannot generate moves for player if it's not their turn");
        }
    }

    private Board minimaxMove(ArrayList<Board> successors){
        if (successors.size() == 1){
            return successors.get(0);
        }
        int bestScore = Integer.MIN_VALUE;
        ArrayList<Board> equalBests = new ArrayList<>();
        for (Board succ : successors){
            int val = minimax(succ, this.depth);
            if (val > bestScore){
                bestScore = val;
                equalBests.clear();
            }
            if (val == bestScore){
                equalBests.add(succ);
            }
        }
        if(equalBests.size() > 1){
            System.out.println(player.toString() + " choosing random best move");
        }
        // choose randomly from equally scoring best moves
        return randomMove(equalBests);
    }

    private Board randomMove(ArrayList<Board> successors){
        if (successors.size() < 1){
            throw new RuntimeException("Can't randomly choose from empty list.");
        }
        Random rand = new Random();
        int i = rand.nextInt(successors.size());
        return successors.get(i);
    }


    private int minimax(Board node, int depth){
        // initialize alpha (computed as a max)
        int alpha = Integer.MIN_VALUE;
        // initialize beta (computed as a min)
        int beta = Integer.MAX_VALUE;
        // call minimax
        return minimax(node, depth, alpha, beta);
    }

    private int minimax(Board node, int depth, int alpha, int beta){
        if (depth == 0 || node.isGameOver()){
            return node.computeHeuristic(this.player);
        }
        // MAX player = player
        if (node.getTurn() == player){
            // player tries to maximize this value
            int v = Integer.MIN_VALUE;
            for (Board child : node.getSuccessors()){
                v = Math.max(v, minimax(child, depth-1, alpha, beta));
                alpha = Math.max(alpha, v);
                // prune
                if (alpha >= beta){
                    break;
                }
            }
            return v;
        }
        // MIN player = opponent
        if (node.getTurn() == player.getOpposite()){
            // opponent tries to minimize this value
            int v = Integer.MAX_VALUE;
            for (Board child : node.getSuccessors()){
                v = Math.min(v, minimax(child, depth-1, alpha, beta));
                beta = Math.min(beta, v);
                // prune
                if (alpha >= beta){
                    break;
                }
            }
            return v;
        }
        throw new RuntimeException("Error in minimax algorithm");
    }
}
