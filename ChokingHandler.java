import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingWorker;

/**
 * Write a description of class ChokingHandler here.
 * 
 *  @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353
 */
public class ChokingHandler extends SwingWorker<Boolean, Void> 
{
    static final int MAX_UNCHOKED = 3;
    private List<PeerSwingWorker> peerWorkers;
    private ArrayList<Integer> unchokedIndexes;
    private ArrayList<Integer> chokedIndexes;
    private ProcessHandler processes;
    private long startTime;
    /**
     * Constructor for objects of class ChokingHandler
     */
    public ChokingHandler(ProcessHandler processes)
    {
        this.processes = processes;
        peerWorkers = processes.getPeerWorkers();
        unchokedIndexes = new ArrayList<Integer>();
        chokedIndexes = new ArrayList<Integer>();
    }
    
    /**
     * Overrides the doInBackground() method of SwingWorker.
     * returns true if successful, false otherwise.
     */
    @Override
    protected Boolean doInBackground() {
        try {
            RUBTClient.debugPrint("STARTING CHOKING HANDLER...");
            startTime = System.currentTimeMillis();
            while(processes.isStarted()){
                peerWorkers = processes.getPeerWorkers();
                updatePeers();
                int count = 0;
                while(unchokedIndexes.size() <= peerWorkers.size() && unchokedIndexes.size() < MAX_UNCHOKED && count<1){
                    unchokeRandom();
                    count++;
                }
                updatePeers();
                
                if((System.currentTimeMillis()-(double)startTime)/1000.0 > 30.0){
                    
                    //CHOKE THE SLOWEST PEER
                    PeerReversePriorityQueue queue;
                    if (processes.getClient().downloadComplete){
                        queue = downloadedSort();
                    }
                    else {
                        queue = uploadedSort();
                    }
                    //only choke the slowest peer only if you are already connected to max number of peers
                    RUBTClient.debugPrint("NUMBER OF UNCHOKED PEERS: " + unchokedIndexes.size());
                    RUBTClient.debugPrint("NUMBER OF CHOKED PEERS: " + chokedIndexes.size());
                    for (PeerSwingWorker peerWorker:peerWorkers){
                        RUBTClient.debugPrint(peerWorker.peer + ", uploaded: " + peerWorker.peer.uploaded 
                        + ", downloaded: " + peerWorker.peer.downloaded);
                    }
                    if (unchokedIndexes.size() >= 1){//MAX_UNCHOKED) {
                        Peer slowestPeer = queue.dequeue();
                        if (slowestPeer!=null){
                            RUBTClient.debugPrint("CHOKING SLOWEST PEER... " + slowestPeer);
                            slowestPeer.choked = true;
                            DataOutputStream slowestPeerOutput = new DataOutputStream(slowestPeer.getSocket().getOutputStream());
                            //write choke message
                            slowestPeerOutput.writeInt(1);
                            slowestPeerOutput.write(0);
                            slowestPeerOutput.flush();
                        }
                    }
                    
                    
                    //OPTIMISTICALLY UNCHOKE A RANDOM PEER
                    unchokeRandom();
                    
                    //UPDATE UNCHOKED/CHOKED PEER LISTS
                    updatePeers();
                    resetRates();
                    
                    //UPDATE START TIME
                    startTime = System.nanoTime();
                }
            }
            return true;
        }
        catch(Exception e){
            System.err.println(e);
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Sorts the currently unchoked peers by their upload rate.
     * @return PeerReversePriorityQueue of unchoked peers, ordered  by slowest rate to fastest rate
     */
    public PeerReversePriorityQueue uploadedSort(){
        PeerReversePriorityQueue queue = new PeerReversePriorityQueue(true);
        for (int i = 0; i < unchokedIndexes.size();i++){
            queue.add(peerWorkers.get(unchokedIndexes.get(i)).peer);
        }
        return queue;
    }
    
    /**
     * Sorts the currently unchoked peers by their download rate.
     * @return PeerReversePriorityQueue of unchoked peers, ordered  by slowest rate to fastest rate
     */
    public PeerReversePriorityQueue downloadedSort(){
        PeerReversePriorityQueue queue = new PeerReversePriorityQueue(false);
        for (int i = 0; i < unchokedIndexes.size();i++){
            queue.add(peerWorkers.get(unchokedIndexes.get(i)).peer);
        }
        return queue;
    }
    
    /**
     * Checks if lists of unchoked/choked peer indexes are still valid and updates them accordingly
     */
    public void updatePeers(){
        unchokedIndexes.clear();
        chokedIndexes.clear();
        peerWorkers = processes.getPeerWorkers();
        for (int i = 0; i < peerWorkers.size(); i++){
            if (peerWorkers.get(i).peer.choked){
                chokedIndexes.add(i);
            }
            else {
                unchokedIndexes.add(i);
            }
        }
    }
    
    /**
     * Selects a random choked index for unchoking and updates the choked/unchoked lists accordingly
     */
    public void unchokeRandom(){
        //no peers available to unchoke
        if (chokedIndexes.size()==0){
            return;
        }
        int randIndex = (int)(Math.random() * chokedIndexes.size());
        Peer unchokedPeer = peerWorkers.get(chokedIndexes.get(randIndex)).peer;
        unchokedPeer.choked = false;
        RUBTClient.debugPrint("OPTIMISTICALLY UNCHOKING RANDOM PEER... " + unchokedPeer);
        try {
            Socket peerSocket = unchokedPeer.getSocket();
            if (peerSocket==null){
                return;
            }
            DataOutputStream unchokedPeerOutput = new DataOutputStream(unchokedPeer.getSocket().getOutputStream());
            //write unchoke message
            unchokedPeerOutput.writeInt(1);
            unchokedPeerOutput.write(1);
            if (processes.getClient().downloadComplete){
                //write uninterested if the download is complete
                unchokedPeerOutput.writeInt(1);
                unchokedPeerOutput.write(3);
            }
            else {
                //write interested if the download is not complete
                unchokedPeerOutput.writeInt(1);
                unchokedPeerOutput.write(2);
            }
            unchokedPeerOutput.flush();
            updatePeers();
        }
        catch(IOException e){
            System.err.println(unchokedPeer);
            e.printStackTrace();
        }
    }
    
    /**
     * Resets all downloaded/uploaded values to zero
     */
    public void resetRates(){
        for (PeerSwingWorker peerWorker:peerWorkers){
            peerWorker.peer.uploaded = 0;
            peerWorker.peer.downloaded = 0;
        }
    }
}
