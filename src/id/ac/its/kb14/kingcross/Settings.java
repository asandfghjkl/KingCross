package id.ac.its.kb14.kingcross;

public class Settings{

    public static Colour AIcolour = Colour.BLACK; 
    public static int squareSize = 80;
    public static int checkerWidth = 5*squareSize/6;
    public static int checkerHeight = 5*squareSize/6;
    public static int ghostButtonWidth = 30*squareSize/29;
    public static int ghostButtonHeight = 5*squareSize/6;
    public static boolean dragDrop = true;
    
    public static Colour getColour(Player player){
        Colour result = null;
        if (player == Player.AI){
            result = Settings.AIcolour;
        }
        else if (player == Player.HUMAN){
            result = Settings.AIcolour.getOpposite();
        }
        if(result == null){
            throw new RuntimeException("Null player has no piece.");
        }
        return result;
    }
}
