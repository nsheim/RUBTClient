
import java.net.Socket;
import java.util.BitSet;
import java.util.Objects;



/**
 * Peer.java
 * 
 * Keeps track of and manages information related to the peer.
 * 
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 * 
 */
public class Peer
{
    private String peer_id;
    private String ip;
    private int port;
    private boolean[] bitfield;
    private boolean[] sentHave;
    private Socket socket;
    
    boolean choked;
    boolean interested;
    boolean validHandshake;
    boolean receivedFirstHave;
    /**
     * Constructor for objects of class Peer.
     * @param peer_id the peer id of this peer
     * @param ip the ip address of this peer
     * @param port the port of this peer
     */
    public Peer(String peer_id, String ip, int port) {
        this.peer_id = peer_id;
        this.ip = ip;
        this.port = port;
        choked = true;
        interested = false;
        validHandshake = false;
        receivedFirstHave = false;
    }
    
    /**
     * Returns the peer ID of this peer.
     * @return peer id
     */
    public String getPeerID(){
        return peer_id;
    }
    /**
     * Returns the IP Address of this peer.
     * @return ip address
     */
    public String getIP(){
        return ip;
    }
    /**
     * Returns the port number of this peer.
     * @return port number
     */
    public int getPort(){
        return port;
    }
    /**
     * Returns the socket of this peer.
     * @return socket
     */
    public Socket getSocket(){
        return socket;
    }
    /**
     * Sets the socket of this peer to a different socket.
     * @param socket socket that this peer will use to connect to other peers
     */
    public void setSocket(Socket socket){
        this.socket = socket;
    }
    /**
     * Initializes the bitfield of this peer.
     * @param torrentInfo information retrieved from the parsed torrent file
     */
    public void initBitfield(TorrentInfo torrentInfo){
        bitfield = new boolean[torrentInfo.piece_hashes.length];
        sentHave = new boolean[torrentInfo.piece_hashes.length];
    }
    /**
     * Sets the bitfield of this peer.
     * @param bitfield the bitfield to set this peer's bitfield value to
     */
    public void setBitfield(boolean[] bitfield){
        this.bitfield = bitfield;
    }
    /**
     * Sets a specified value in the bitfield.
     * @param index index of the value
     * @param value true/false value that bitfield[index] will be set to
     */
    public void setBitfieldValue(int index, boolean value){
        bitfield[index] = value;
    }
    
    public boolean[] getSentHave(){
        return sentHave;
    }
    
    /**
     * Returns the bitfield of this peer.
     * @return the bitfield as a boolean array
     */
    public boolean[] getBitfield(){
        return bitfield;
    }
    
    /**
     * Returns information regarding what pieces the client and peer have.
     * @param client the client 
     * @param peer the peer
     * @return int[] with values:
     *          -1 : client has piece at index[i]; peer does not
     *           1 : client does not have piece at index[i]; peer does
     *           0 : client and peer both have piece, or neither have the piece
     */
    public static int[] getPieceInfo(Client client, Peer peer){
        boolean[] clientPieces = client.getBitfield();
        boolean[] peerPieces = peer.getBitfield();
        int[] receivablePieces = new int[client.getBitfield().length];
        for (int i = 0; i < receivablePieces.length; i++){
            if (!clientPieces[i] && peerPieces[i]){
                receivablePieces[i] = 1;
            }
            else if (clientPieces[i] && !peerPieces[i]){
                receivablePieces[i] = -1;
            }
            else{ 
                //neither client nor peer has piece
                //both client and peer both have piece
                receivablePieces[i]=0;
            }
        }
        return receivablePieces;
    }
    /**
     * Overrides the equals() method in the Object class.
     * Note: Doesn't work.
     * @param peerObj the object to which this peer's logical equality will be tested
     */
    @Override
    public boolean equals(Object peerObj){
        if (peerObj==null) return false;
        if (this.getClass() != peerObj.getClass()) return false;
        Peer peer2;
        try{
            peer2 = (Peer)peerObj;
            if(!peer_id.equals(peer2.getPeerID())) return false;
            else if(!ip.equals(peer2.getIP())) return false;
            else if (port!=peer2.getPort()) return false;
            return true;
        }
        catch(ClassCastException e){
            System.err.println(e);
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Overrides the toString() method in the Object class.
     */
    @Override
    public String toString(){
        return "PeerID: " + peer_id + "\tIP: " + ip + "\tport: " + port; 
    }
}
