import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;
/**
 * Write a description of class PeerPriorityQueue here.
 *  @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353
 */
public class PeerReversePriorityQueue
{
    // instance variables - replace the example below with your own
    private Comparator<Peer> comparer;
    private PriorityBlockingQueue<Peer> queue;
    private boolean uploaded; //determines if queue is used for uploaded or downloaded sort
    
    private final int MAX_UNCHOKED = 10; //max number of unchoked peers = max size of this queue

    /**
     * Constructor for objects of class PeerPriorityQueue
     */
    public PeerReversePriorityQueue(boolean uploaded)
    {
        if (uploaded) {
            comparer = new Comparator<Peer>(){
                public int compare (Peer x, Peer y){
                    RUBTClient.debugPrint(x.toString() + ", uploaded: " + x.uploaded);
                    RUBTClient.debugPrint(y.toString() + ", uploaded: " + y.uploaded);
                    
                    if(x.uploaded > y.uploaded) return 1;
                    if(x.uploaded == y.uploaded) {
                        double rand = Math.random();
                        if (rand <= .33) return -1;
                        else if (rand <= .66) return 0;
                        return 1;
                    }
                    return -1;
                }
            };
        }
        
        else {
            comparer = new Comparator<Peer>(){
                public int compare (Peer x, Peer y){
                    
                    RUBTClient.debugPrint(x.toString() + ", downloaded: " + x.downloaded);
                    RUBTClient.debugPrint(y.toString() + ", downloaded: " + y.downloaded);
                    if(x.downloaded > y.downloaded) return 1;
                    if(x.downloaded == y.downloaded) {
                        double rand = Math.random();
                        if (rand <= .33) return -1;
                        else if (rand <= .66) return 0;
                        return 1;
                    }
                    return -1;
                }
            };
        }
        queue = new PriorityBlockingQueue<Peer>(MAX_UNCHOKED, comparer);
    }
    public Comparator<Peer> getComparator(){
        return comparer;
    }
    public PriorityBlockingQueue<Peer> getRarityQueue(){
        return queue;
    }
    public boolean add(Peer peer){
        return queue.add(peer);
    }
    public boolean updatePeerPriority(Peer peer){
        if(queue.remove(peer)) return queue.add(peer);
        return false;
    }
    public Peer peek(){
        return queue.peek();
    }
    public void clear(){
        queue.clear();
    }
    public boolean remove(Peer peer){
        return queue.remove(peer);
    }
    public Peer dequeue(){
        Peer slowestPeer = null;
        try {
            slowestPeer = queue.take();
        }
        catch(InterruptedException e){
            //this is fine, do nothing
        }
        return slowestPeer;
    }
}
