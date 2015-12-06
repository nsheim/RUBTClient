import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;
/**
 * Write a description of class RarityQueue here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class RarityQueue
{
    Comparator<Piece> comparer;
    PriorityBlockingQueue<Piece> queue;
    
    private int numPieces;
    
    public RarityQueue(int numPieces){
        this.numPieces = numPieces;
        
        comparer = new Comparator<Piece>(){
            public int compare (Piece x, Piece y){
                if(x.getRarity() > y.getRarity()) return 1;
                if(x.getRarity() == y.getRarity()) {
                    double rand = Math.random();
                    if (rand <= .33) return -1;
                    else if (rand <= .66) return 0;
                    return 1;
                }
                return -1;
            }
        };
        queue = new PriorityBlockingQueue<Piece>(numPieces, comparer);
    }
    
    public Comparator<Piece> getComparator(){
        return comparer;
    }
    public PriorityBlockingQueue<Piece> getRarityQueue(){
        return queue;
    }
    public boolean add(Piece piece){
        return queue.add(piece);
    }
    public boolean updatePiecePriority(Piece piece){
        if(queue.remove(piece)) return queue.add(piece);
        return false;
    }
    public Piece dequeue(){
        Piece rarestPiece = null;
        try {
            rarestPiece = queue.take();
        }
        catch(InterruptedException e){
            //this is fine, do nothing
        }
        return rarestPiece;
    }
    public Piece peek(){
        return queue.peek();
    }
    public void clear(){
        queue.clear();
    }
    public boolean remove(Piece piece){
        return queue.remove(piece);
    }
    public boolean remove(int pieceIndex){
        for (Piece piece:queue){
            if(piece.getIndex()==pieceIndex){
                return queue.remove(piece);
            }
        }
        return false;
    }
    
    @Override
    public String toString(){
        String str = "";
        for (Piece piece:queue){
            str+= "Piece at index: " + piece.getIndex() + " with rarity: " + piece.getRarity() + "\n";
        }
        return str;
    }
}
