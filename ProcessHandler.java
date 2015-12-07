import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProcessHandler.java
 * Keeps track of and manages information used for downloading, including torrent info, the downloading file, timers, and threads.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */

public class ProcessHandler {
    
    private TorrentInfo torrentInfo;
    private File file;
    private RandomAccessFile raFile;
    private Timer trackerTimer;
    private TrackerAnnouncer announcer;
    
    private boolean started;
    
    private InetAddress addr;
    private Client client;
    
    private List<PeerSwingWorker> peerWorkers;
    private long startTime;
    private long downloadTime;
    private long timerTime;
    
    int uploaded;
    int downloaded;
    int left;
    boolean downloadCompleteFromStart;
    int [] byte15 = new int [100];
    
    /**
     * Constructor for objects of class ProcessHandler.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param downloaded number of pieces already downloaded
     */
    public ProcessHandler(TorrentInfo torrentInfo, File file, boolean fileAlreadyExisted){
        try {
            this.torrentInfo = torrentInfo;
            this.file = file;
            
            raFile = new RandomAccessFile(file, "rw");
            
            started = false;
            
            uploaded = 0;
            downloaded = 0;
            left = torrentInfo.file_length;
            downloadCompleteFromStart = false;
        
            addr = InetAddress.getLocalHost();
            client = new Client(Client.generatePeerID(), addr.getHostAddress(),-1); //your port doesn't matter
            client.initBitfield(torrentInfo);
            client.initPieces(torrentInfo);
            
            //if the file did not already exist, allot memory for it
            if(!fileAlreadyExisted){
                raFile.setLength(torrentInfo.file_length);
            }
            
            //check what pieces the client already has by checking the if there is anything stored in the first
            //byte of each piece
            //i.e., parse the file by checking every pieceLength'th piece, starting at 0.
            else {
                int lastPieceLength = torrentInfo.file_length - (torrentInfo.piece_hashes.length - 1)
                                        *torrentInfo.piece_length;
               
                for (int i = 0; i < torrentInfo.piece_hashes.length - 1; i++){
                    try {
                        byte[] piece = new byte[torrentInfo.piece_length];
                        
                        Integer numBytesRead = raFile.read(piece, 0, torrentInfo.piece_length);
              
                        if (numBytesRead!=null && numBytesRead.intValue() == torrentInfo.piece_length){
                            //make sure the piece has a valid hash before deciding that we have it already
                            byte[] pieceHash = MessageHandler.sha1Bytes(piece);
                            boolean isValid = Arrays.equals(pieceHash,torrentInfo.piece_hashes[i].array());
                            
                            if (isValid){
                                client.addBlock(i,piece,0);
                                client.setBitfieldValue(i,true);
                                client.getRarityQueue().remove(client.getPiece(i));
                                RUBTClient.debugPrint("Piece: " + i + " removed from queue.");
                                left-=torrentInfo.piece_length;
                            }
                            
                        }
                        else {
                            RUBTClient.debugPrint("Missing piece at index: " + i);
                        }
                    }
                    catch(IOException e){
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    catch(IndexOutOfBoundsException e){
                        System.err.println(e);
                        e.printStackTrace();
                    }
                }
                
                try {
                    byte[] piece = new byte[lastPieceLength];
                    Integer numBytesRead = raFile.read(piece, (torrentInfo.piece_hashes.length-1)*torrentInfo.piece_length, lastPieceLength);
                    
                    if(numBytesRead!=null && numBytesRead == lastPieceLength){
                        client.addBlock(torrentInfo.piece_hashes.length - 1, piece, 0);
                        client.setBitfieldValue(torrentInfo.piece_hashes.length -1 , true);
                        left-=lastPieceLength;
                    }
                    else {
                        RUBTClient.debugPrint("Missing piece at index: " + (torrentInfo.piece_hashes.length -1) );
                    }
                }
                catch(IOException e){
                    System.err.println(e);
                    e.printStackTrace();
                }
                catch(IndexOutOfBoundsException e){
                    //this is fine, do nothing
                }
                downloaded = (torrentInfo.file_length - left);
                
                if(left<=0) {
                    downloadCompleteFromStart = true;
                    downloaded = torrentInfo.file_length;
                }
                else {
                    downloadCompleteFromStart = false;
                }
                client.updateDownloadComplete(this);
            }
            RUBTClient.debugPrint("HOST ADDRESS: " + addr.getHostAddress());
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(ProcessHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException e){
            System.err.println(e);
            e.printStackTrace();
        }
    }
    /**
     * Returns a TorrentInfo object that contains
     * @return information retrieved from the parsed torrent file
     */
    public TorrentInfo getTorrentInfo(){
        return torrentInfo;
    }
    /**
     * Returns the file being downloaded.
     * @return the file being downloaded
     */
    public File getFile(){
        return file;
    }
    
    /**
     * Returns the RandomAccessFile used for downloading the file.
     * @return the RandomAccessFile used for downloading the file
     */
    public RandomAccessFile getRandomAccessFile(){
        return raFile;
    }
    /**
     * Returns the timer used to manage announces to the tracker.
     * @return the timer used to manage announces to the tracker
     */
    public Timer getTrackerTimer(){
        return trackerTimer;
    }
    
    /**
     * (Re)starts the torrenting process.
     */
    public void restart(){
        startTime = System.nanoTime();
        started = true;
        peerWorkers = Collections.synchronizedList(new ArrayList());
        List<Peer> localPeers;

        RUBTClient.debugPrint(getTorrentInfo().announce_url.toString());

        scheduleTimer();
        for(int i = 0;i<100;i++){
            byte15[i]=0;
        }
        
        
        localPeers = announcer.getLocalPeers();
        //create and start peerswingworkers
        for (int i = 0; i < localPeers.size(); i++){
            peerWorkers.add(new PeerSwingWorker(this,localPeers.get(i),i));
            peerWorkers.get(i).execute();
        }
    }
    /**
     * Stops the torrenting process and prints the download time.
     */
    public void stop(){
        if(!client.downloadComplete){
            downloadTime += System.nanoTime()-startTime;
        }
        else if (downloadCompleteFromStart){
            downloadTime = 0;
        }
        started = false;
        for (int i = 0; i < peerWorkers.size(); i++){
            peerWorkers.get(i).cancel(true);
        }
        trackerTimer.cancel();
        trackerTimer.purge();
        
        announcer.updateEvent(TrackerRequest.EVENT_STOPPED);
        announcer.run();
        
        System.out.println("\n\n\nYou have pressed EXIT");
        
        System.out.println("Download time: " + (downloadTime/1000000000) + " seconds");
        System.out.println("The program will exit in 5 seconds...\n\n\n");
        try{
            Thread.sleep(5000);
        }
        catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
        System.exit(0);
    }
    
    /**
     * Sets the value of this.started to started
     * @param started
     */
    public void setStarted(boolean started){
        this.started = started;
    }
    /**
     * Checks if the torrenting process has started
     * @return true if started, else false
     */
    public boolean isStarted(){ 
        return started;
    }
    /**
     * Returns the client
     * @return client the client (you) that is connecting to peers
     */
    public Client getClient(){ 
        return client;
    }
    /**
     * Creates a new announcer object and schedules to timer to run based off of intervals received from the announcer.
     */
    public void scheduleTimer(){
        announcer = new TrackerAnnouncer(this, client.getPeerID(), torrentInfo, file);
        announcer.updateDownload(downloaded, left, TrackerRequest.EVENT_STARTED);
        if(client.downloadComplete){
            announcer.updateEvent(TrackerRequest.EVENT_COMPLETED);
        }
        
        announcer.run();
        Integer minInterval = announcer.getMinInterval();
        Integer interval = announcer.getInterval();
        
        if(minInterval>0){
            trackerTimer = new Timer();
            trackerTimer.scheduleAtFixedRate(announcer, (long)(minInterval*1000),(long)(minInterval*1000));
        }
        else {
            trackerTimer = new Timer();
            trackerTimer.scheduleAtFixedRate(announcer, (long)(interval*1000), (long)(interval*1000));
        }
    }
    /**
     * Returns a List<PeerSwingWorker> of the current peer workers.
     * @return list of current peer workers (i.e., peer threads)
     */
    public List<PeerSwingWorker> getPeerWorkers(){
        return peerWorkers;
    }
    
    /**
     * Returns the PeerSwingWorker in the list of peer workers found at [index].
     * @return peerWorkers[index]
     */
    public PeerSwingWorker getPeerWorker(int index){ 
        return peerWorkers.get(index);
    }
    /**
     * Adds a PeerSwingWorker to the current list of peer workers.
     * @param peerWorker peerWorker to be added to the current list of peer workers
     */
    public void addPeerWorker(PeerSwingWorker peerWorker){
        peerWorkers.add(peerWorker);
    }
    /**
     * Updates the current peer workers to match a new list of peer workers.
     * Precondition: the peer workers in this list have already been executed, and those from the previous list that are not in the current list have been ended.
     * @param peerWorkers the new list of peerWorkers
     */
    public void updatePeerWorkers(List<PeerSwingWorker> peerWorkers){
        this.peerWorkers = peerWorkers;
    }
    /**
     * returns the current TrackerAnnouncer object
     * @return announcer the current TrackerAnnouncer object
     */
    public TrackerAnnouncer getAnnouncer(){
        return announcer;
    }
    /**
     * Calculates the downnload time based off of the start time.
     * Precondition: Client has just finished downloading
     */
    public void setDownloadTime(){ 
        downloadTime+=System.nanoTime()-startTime;
    }
    
}
