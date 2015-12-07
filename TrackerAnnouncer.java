import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

/**
 * TrackerAnnouncer.java
 * TimerTask object that manages periodic tracker announces.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */
public class TrackerAnnouncer extends TimerTask {

    private ProcessHandler processes;
    private String myPeerID;
    private TorrentInfo torrentInfo;
    private File file;
    private boolean hasRunPreviously;
    private TrackerRequest request;
    private TrackerResponse previousResponse;
    private List<Peer> peerList;
    private List<Peer> prevPeerList;
    private List<Peer> localPeers;
    private List<Peer> prevLocalPeers;
    /**
     * Constructor for objects of class TrackerAnnouncer.
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param myPeerID client's peerID
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param file the file being downloaded to
     */
    public TrackerAnnouncer(ProcessHandler processes, String myPeerID, TorrentInfo torrentInfo, File file) {
        this.processes = processes;
        this.myPeerID = myPeerID;
        this.torrentInfo = torrentInfo;
        request = new TrackerRequest(myPeerID, torrentInfo);
        previousResponse = null;

        hasRunPreviously = false;
        peerList = null;
        prevPeerList = null;
    }

    /**
     * Updates the download information.
     * @param downloaded current amount of data downloaded in bits
     * @param left current amount of data that still needs to be downloaded in bits
     * @param event current state of downloading (started, stopped, complete)
     */
    public void updateDownload(Integer downloaded, Integer left, String event) {
        request.downloaded = downloaded;
        request.left = left;
        request.event = event;
    }
    /**
     * Updates the uploaded information.
     * @param uploaded the current amounnt of data uploaded in bits
     */
    public void updateUploaded(Integer uploaded){
        request.uploaded = uploaded;
    }

    /**
     * Updates the current event.
     * @param event current state of downloading (started, stopped, complete)
     */
    public void updateEvent(String event) {
        request.event = event;
    }

    /**
     * Returns a List<Peer> of peers retrieved from the tracker.
     * @return peerList
     */
    public List<Peer> getPeerList() {
        return peerList;
    }

    /**
     * Returns the list of local peers retrieved from the tracker.
     * @return localPeers
     */
    public List<Peer> getLocalPeers() {
        return localPeers;
    }

    /**
     * Returns the min interval retrieved from the tracker.
     * If no min interval was specified, return value will be -1.
     * @return min interval
     */
    public Integer getMinInterval() {
        return previousResponse.getMinInterval();
    }

    /**
     * Returns the interval retrieved from the tracker.
     * @return standard interval
     */
    public Integer getInterval() {
        return previousResponse.getInterval();
    }

    /**
     * Sends an http get request to the tracker and then retrieves relevant information from the response
     * Also updates the current list of peer workers.
     */
    @Override
    public void run() {
        byte[] trackerResponseBytes;
        if (hasRunPreviously) {
            trackerResponseBytes = request.sendHttpGet(previousResponse);
            
        } else {
            trackerResponseBytes = request.sendHttpGet();
            hasRunPreviously = true;
        }

        TrackerResponse response = new TrackerResponse(trackerResponseBytes);
        response.decodeResponse();
        previousResponse = response;

        prevPeerList = peerList;
        peerList = response.getPeerList();
        prevLocalPeers = localPeers;
        localPeers = response.getLocalPeers(peerList);
        updateLocalConnections2();
        RUBTClient.debugPrint("\n***********ANNOUNCED***********");
        RUBTClient.debugPrint("Min interval: " + getMinInterval() + "\tInterval: " + getInterval());
    }

    /**
     * Supposed to update local connections (i.e., connections to peers that are specified as 
     * "local" in the assignment description).
     * Note: Doesn't work because override of equals() in Peer isn't implemented correctly. Do not use
     */
    public void updateLocalConnections() {
        List<PeerSwingWorker> peerWorkers = processes.getPeerWorkers();
        Set<Peer> prevOnlySet = Collections.synchronizedSet(new HashSet<Peer>());
        Set<Peer> currentOnlySet = Collections.synchronizedSet(new HashSet<Peer>());
        if (prevLocalPeers == null) {
            return;
        }
        if (prevLocalPeers.size() > localPeers.size()) {
            int index = 0;
            while (index < localPeers.size()) {
                prevOnlySet.add(prevLocalPeers.get(index));
                prevOnlySet.add(localPeers.get(index));

                currentOnlySet.add(prevLocalPeers.get(index));
                prevOnlySet.add(localPeers.get(index));

                index++;
            }
            while (index < prevLocalPeers.size()) {
                prevOnlySet.add(prevLocalPeers.get(index));
                currentOnlySet.add(prevLocalPeers.get(index));
                index++;
            }
        } else if (prevLocalPeers.size() > localPeers.size()) {
            int index = 0;
            while (index < prevLocalPeers.size()) {
                prevOnlySet.add(prevLocalPeers.get(index));
                prevOnlySet.add(localPeers.get(index));

                currentOnlySet.add(prevLocalPeers.get(index));
                prevOnlySet.add(localPeers.get(index));
                index++;
            }
            while (index < localPeers.size()) {
                prevOnlySet.add(localPeers.get(index));
                prevOnlySet.add(localPeers.get(index));
                index++;
            }
        } else {
            int index = 0;
            while (index < prevLocalPeers.size()) {
                prevOnlySet.add(prevLocalPeers.get(index));
                prevOnlySet.add(localPeers.get(index));

                currentOnlySet.add(prevLocalPeers.get(index));
                prevOnlySet.add(localPeers.get(index));
                index++;
            }
        }
        prevOnlySet.removeAll(localPeers);
        RUBTClient.debugPrint("Local peers: ");
        for (Peer peer : localPeers) {
            RUBTClient.debugPrint("\t" + peer);
        }
        currentOnlySet.removeAll(prevLocalPeers);
        RUBTClient.debugPrint("PrevLocalPeers Only: ");
        for (Peer peer : prevLocalPeers) {
            RUBTClient.debugPrint("\t" + peer);
        }

        //union = set of peers in both prev and current
        //prevOnly = set of peers in only prev
        //curOnly = set of peers in only current
        //add both sets together
        //subtract current to get prev only
        //subtract prev to get current only

        //remove connections from old peers
        for (int i = 0; i < peerWorkers.size(); i++) {
            if (prevOnlySet.contains(peerWorkers.get(i).peer)) {
                peerWorkers.get(i).stillConnected = false;
                peerWorkers.get(i).cancel(true);
            }
        }
        RUBTClient.debugPrint("Prev Only: ");
        for (Peer peer : prevOnlySet) {
            RUBTClient.debugPrint("\t" + peer);
        }
        //remove old peers from peerWorkers
        peerWorkers.removeAll(prevOnlySet);
        
        int i=peerWorkers.size();
        //add connections to new peers
        for (Peer peer : currentOnlySet) {
            i++;
            PeerSwingWorker peerWorker = new PeerSwingWorker(processes, peer,i);
            peerWorker.execute();
            peerWorkers.add(peerWorker);
        }
        RUBTClient.debugPrint("Current Only: ");
        for (Peer peer : currentOnlySet) {
            RUBTClient.debugPrint("\t" + peer);
        }

        RUBTClient.debugPrint("Updated peerlist: ");
        for (int j = 0; j < peerWorkers.size(); j++) {
            RUBTClient.debugPrint("\t" + peerWorkers.get(i).peer);
        }
        processes.updatePeerWorkers(peerWorkers);
    }
    
    /**
     * Updates local connections (i.e., connections to peers that are specified as 
     * "local" in the assignment description).
     * Uses the list of peerWorkers obtained from processes in order to determine new connections to the tracker,
     * as well as determine old connections.
     * For connections that are new, creates new PeerSwingWorker objects, 
     * adds them to the list of peerWorkers, and executes them
     * For ongoing connections, does nothing.
     * For old connections (i.e., peers that are no longer connected to the tracker), 
     * ends the corresponding thread and removes it from the list of peersWorkers.
     * 
     * Updates the list of peerWorkers in the processes variable to match the newly determined list.
     */
    public void updateLocalConnections2() {
        if (prevLocalPeers == null) {
            return;
        }
        List<PeerSwingWorker> peerWorkers = processes.getPeerWorkers();
        HashMap<String, Peer> prevPeersDict = new HashMap();
        HashMap<String, Peer> newPeersDict = new HashMap();
        HashMap<String, Peer> bothPeersDict = new HashMap();
        //add the list of previous local peers to a dictionary of previous local peers and a dictionary
        //containing both previous and current peers, using their peerID as the key and the Peer object as
        //the value.
        for (int i = 0; i < prevLocalPeers.size(); i++) {
            prevPeersDict.put(prevLocalPeers.get(i).getPeerID(), prevLocalPeers.get(i));
            bothPeersDict.put(prevLocalPeers.get(i).getPeerID(), prevLocalPeers.get(i));
        }
        //do the same for new local peers.
        for (int i = 0; i < localPeers.size(); i++) {
            newPeersDict.put(localPeers.get(i).getPeerID(), localPeers.get(i));
            bothPeersDict.put(localPeers.get(i).getPeerID(), localPeers.get(i));
        }
        
        Set<String> bothPeersKeySet = bothPeersDict.keySet();
        HashMap<String, Peer> prevOnlyDict = new HashMap();
        HashMap<String, Peer> newOnlyDict = new HashMap();
        for(String peerID:bothPeersKeySet){
            //peer disconnected
            if(prevPeersDict.containsKey(peerID)&&!newPeersDict.containsKey(peerID)){
                prevOnlyDict.put(peerID, prevPeersDict.get(peerID));
            }
            //peer connected
            else if(!prevPeersDict.containsKey(peerID)&&newPeersDict.containsKey(peerID)){
                newOnlyDict.put(peerID, prevPeersDict.get(peerID));
            }
        }
        
        //remove old connections
        for (int i = 0; i < peerWorkers.size(); i++) {
            if (prevOnlyDict.containsKey(peerWorkers.get(i).peer.getPeerID())) {
                peerWorkers.get(i).stillConnected = false;
                peerWorkers.get(i).cancel(true);
                peerWorkers.remove(i);
            }
        }
        //add new connections
        Set<String> newOnlyKeySet = newOnlyDict.keySet();
        int i = peerWorkers.size();
        for (String peerID : newOnlyKeySet) {
            i++;
            PeerSwingWorker peerWorker = new PeerSwingWorker(processes, newOnlyDict.get(peerID),i);
            peerWorker.execute();
            peerWorkers.add(peerWorker);
        }
        
        //update peerWorkers in processes
        processes.updatePeerWorkers(peerWorkers);
    }

}
