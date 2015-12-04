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
                if(x.getRarity() == y.getRarity()) return 0;
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
        queue.remove(piece);
        return queue.add(piece);
    }
    public Piece dequeue(){
        Piece rarestPiece = null;
        try {
            rarestPiece = queue.take();
        }
        catch(InterruptedException e){
            System.out.println(e);
            e.printStackTrace();
        }
        return rarestPiece;
    }
    public Piece peek(){
        return queue.peek();
    }
    public void clear(){
        queue.clear();
    }
}
