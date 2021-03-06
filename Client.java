
/**
 * Client.java
 * 
 * Keeps track of and manages information related to the client. Extends peer.
 * 
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 * 
 */
public class Client extends Peer{
    
    Piece[] pieces;
    boolean downloadComplete;
    
    private RarityQueue rarityQueue;
    /**
     * Constructor for objects of class Client.
     */
    public Client(String peer_id, String ip, int port) {
        super(peer_id, ip, port);
        downloadComplete = false;
    }
    
    /**
     * Returns a randomly generated PeerID.
   * @ return a randomly generated String using 20 (mostly) alphanumeric characters; cannot start with "RU"
   */
  public static String generatePeerID(){
      String peerid = "";
      int i = 0;
          while (i < 20) {
              int random = (int)(Math.random()*62);
              char character;
              if (random < 10) { //number
                  character = (char) (random+48);
                  peerid+="" + character; //48 is the first value for ascii numbers
              }
              else { // 10 <= random < 62; letters
                  character = (char) (random+65);
                  peerid+= "" + character; // 65 is the first value for ascii letters
              }
              //if peerid starts with RUBT, then start over.
              if (peerid.length() == 2 && peerid.equals("RU")){
                  i = 0;
                  peerid = "";
              }
              i++;
          }
          
          return peerid;
  }
  
  /**
   * Inititalizes the array of pieces based off of the information in the torrent file.
   * Also initializes the RarityQueue that will keep track of the order to request pieces from peers.
   * @param torrentInfo information retrieved from the parsed torrent file
   */
  public void initPieces(TorrentInfo torrentInfo){
      //each piece hash index points to the block
      pieces = new Piece[torrentInfo.piece_hashes.length];
      for (int i = 0; i<pieces.length-1;i++){
          pieces[i] = new Piece(i,torrentInfo.piece_length);
      }
      
      int numPieces = torrentInfo.piece_hashes.length;
      //filelength - piece_length*(numPieces-1) should be the size of the last piece
      pieces[pieces.length-1] = new Piece(pieces.length-1,torrentInfo.file_length-torrentInfo.piece_length*(numPieces-1));
      
      initRarityQueue(torrentInfo);
  }
  
  private void initRarityQueue(TorrentInfo torrentInfo){
      rarityQueue = new RarityQueue(torrentInfo.piece_hashes.length);
      for(int i = 0; i<torrentInfo.piece_hashes.length;i++){
          rarityQueue.add(pieces[i]);
      }
  }
  
  /**
   * Returns the next piece index that will be requested.
   * @return next piece index that will be requested
   */
  public int nextRequest(){
      return rarityQueue.peek().getIndex();
  }
  
  /**
   * Returns the RarityQueue
   * @return the RarityQueue
   */
  public RarityQueue getRarityQueue(){
      return rarityQueue;
  }
  
  /**
   * Returns a 2D array of bytes representing the pieces the client currently has.
   * @return byte[index of piece][] pieces
   */
  public Piece[] getPieces(){
      return pieces;
  }
  
  /**
   * Returns a piece specified by index
   * @return pieces[index]
   */
  public Piece getPiece(int index){
      return pieces[index];
  }
  
  /**
   * Add a piece to the array of pieces that the client currently has.
   * Precondition: the block has already been verified.
   * @param pieceIndex index at which to add the piece
   * @param piece the block to add
   * @param offset offset within the piece at which to add the block
   */
  public void addBlock(int pieceIndex, byte[] block, int offset){
      for(int i = 0; i<block.length;i++){
          pieces[pieceIndex].setData(offset+i, block[i]);
      }
  }
  
  /**
   * Checks if the download has been completed using the amount of data left to download.
   * Sets downloadComplete to true if left = 0;
   * Else sets downloadComplete to false.
   * @param processes defines a current set of processes and properties that provide information about execution
   * 
   */
  public void updateDownloadComplete(ProcessHandler processes){
      if(processes.left==0) downloadComplete = true;
      else downloadComplete = false;
  }
  
  /**
   * Overrides the toString() method in the Object class.
   */
  @Override
  public String toString(){
        return "My PeerID: " + super.getPeerID() + "\tIP: " + super.getIP() + "\tport: " + super.getPort();  
  }
    
  
  
}
