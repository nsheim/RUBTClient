import java.net.*;
import java.io.*;
import java.util.ArrayList;
/**
 * TrackerRequest.java
 * Keeps track of and manages information related to sending a request to the tracker.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */
public class TrackerRequest
{
    static TorrentInfo torrentInfo;
    static URL announceURL;
    
    static String infohash;
    static String myPeerID;
    static int port;
    static Integer uploaded;
    static Integer downloaded;
    static Integer left;
    static Integer compact; 
    static boolean noPeerID; //ignored if compact is enabled
    static String event;
    static String ip; //optional
    static Integer numWant; //optional; number of peers that the client would like to receive from the tracker
                    //permitted to be 0; defaults to 50 peers
    static String key;
    static String trackerID; //optional; if a prev announce contained a trackerID, it should be set here

    static final String EVENT_STARTED = "started";
    static final String EVENT_STOPPED = "stopped";
    static final String EVENT_COMPLETED = "completed";
    /**
     * Constructor for objects of class TrackerRequest.
     * @param myPeerID client's peerID
     * @param torrentInfo information retrieved from the parsed torrent file
     */
    public TrackerRequest(String myPeerID, TorrentInfo torrentInfo)
    {
        // initialise instance variables
        this.torrentInfo = torrentInfo;
        announceURL = torrentInfo.announce_url;
        
        infohash = sha1Format(torrentInfo.info_hash.array());
        this.myPeerID = myPeerID;
        this.port = announceURL.getPort();
        uploaded = 0;
        downloaded = 0;
        left = torrentInfo.file_length;
        compact = 0;
        noPeerID = false;
        event = EVENT_STARTED;
    }
    
    /**
     * Takes an sha1 hashed byte array and converts it into String format.
    * @ param bytesToHash - array of bytes to be converted into sha1 String format
    * @ return a String of the formatted sha1 bytes
    */
    public static String sha1Format(byte[] bytesToHash){
      String infohash = "";
        for (byte b : bytesToHash) {
            infohash += "%"+String.format("%02x", b & 0xff);
        }
      return infohash;
    }
    
    /**
     * Used for sending the initial http get request. 
     * @return a string representing the torrent's response
       */
    public static byte[] sendHttpGet(){
      try {
          // get the info hash
          RUBTClient.debugPrint("INFOHASH: "+infohash);
          
          RUBTClient.debugPrint("peerid = " + myPeerID);
         
          URL url = new URL("http://" + announceURL.getHost()+":"+ port
                            +"/announce?info_hash="+infohash
                            +"&peer_id="+myPeerID+"&port="+port
                            +"&uploaded="+uploaded
                            +"&downloaded="+downloaded
                            +"&left="+left
                            +"&event="+event);
          RUBTClient.debugPrint("URL: "+url);
          HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
          httpConnection.setRequestMethod("GET");
    
          int responseCode = httpConnection.getResponseCode();
          if (responseCode==-1) {
              System.err.println("ERROR: Invalid response code. No code can be discerned from the response.");
              return null;
            }
          RUBTClient.debugPrint("\nSending 'GET' request to URL : " + announceURL);
          RUBTClient.debugPrint("Response Code: " + responseCode);
          
          DataInputStream in = new DataInputStream(httpConnection.getInputStream());
          ArrayList<Byte> data = new ArrayList<Byte>();
          byte[] databytes;
          boolean reachedEndOfStream = false;
          
          try{
              while (!reachedEndOfStream){
                  data.add(in.readByte());
                }
            }
            catch(EOFException e){
                databytes = new byte[data.size()];
                for (int i = 0; i < databytes.length; i++){
                    databytes[i] = data.get(i).byteValue();
                }
                return databytes;
          }
          
          in.close();  
          return null;
    }
    catch (IOException e) {
        System.err.println(e);
        e.printStackTrace();
    }
    return null;
  }
  /**
   * Used for sending an http get request. Uses information from a previous http get request to send 
   * the tracker more detailed information than was sent in the initial http get request.
   * Precondition: A prior http get request has already been sent.
   * @param trackerResponse a response from a previous http get request
   * @return a string representing the torrent's response
   */
  public static byte[] sendHttpGet(TrackerResponse trackerResponse){
      trackerID = trackerResponse.getTrackerID();
      try {
          // get the info hash
          RUBTClient.debugPrint("INFOHASH: "+infohash);
          
          RUBTClient.debugPrint("peerid = " + myPeerID);
         
          URL url = new URL("http://" + announceURL.getHost()+":"+ port
                            +"/announce?info_hash="+infohash
                            +"&peer_id="+myPeerID
                            +"&port="+port
                            +"&uploaded="+uploaded
                            +"&downloaded="+downloaded
                            +"&left="+left
                            +"&compact="+compact
                            +"&no_peer_id="+noPeerID
                            +"&event="+event
                            +"&trackerid="+trackerID
                            );
          RUBTClient.debugPrint("URL: "+url);
          HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
          httpConnection.setRequestMethod("GET");
    
          int responseCode = httpConnection.getResponseCode();
          if (responseCode==-1) {
              System.err.println("ERROR: Invalid response code. No code can be discerned from the response.");
              return null;
            }
          RUBTClient.debugPrint("\nSending 'GET' request to URL : " + announceURL);
          RUBTClient.debugPrint("Response Code: " + responseCode);
          
          DataInputStream in = new DataInputStream(httpConnection.getInputStream());
          ArrayList<Byte> data = new ArrayList<Byte>();
          byte[] databytes;
          boolean reachedEndOfStream = false;
          
          try{
              while (!reachedEndOfStream){
                  data.add(in.readByte());
                }
            }
            catch(EOFException e){
                databytes = new byte[data.size()];
                for (int i = 0; i < databytes.length; i++){
                    databytes[i] = data.get(i).byteValue();
                }
                return databytes;
          }
          
          in.close();  
          return null;
    }
    catch (IOException e) {
        System.err.println(e);
        e.printStackTrace();
    }
    return null;
  }
  
}
