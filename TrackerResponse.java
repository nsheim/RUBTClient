import java.util.ArrayList;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
/**
 * TrackerResponse.java
 * Keeps track of and manages information related to a response received from the tracker.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */
public class TrackerResponse
{
    static byte[] response; //the entire response
    
    static ByteBuffer failureReason;
    static ByteBuffer warningMessage;
    static Integer interval; //interval in seconds
    static Integer minInterval; //minimum announce interval
    static Integer downloaded;
    static String trackerID;
    static Integer complete; //number of peers with the entire file (seeders)
    static Integer incomplete; //number of non-seeder peers (leechers)
    static ArrayList<Peer> peerList;
    

    
    /**
     * Constructor for objects of class TrackerResponse.
     * @param response response received from the tracker, read as a byte array
     */
    public TrackerResponse(byte[] response)
    {
        this.response = response;
        peerList = new ArrayList<Peer>();
        minInterval = -1;
    }
    
    /**
     * Returns the byte array response received from the tracker.
     * @return response byte array response received from tracker
     */
    public byte[] getResponse(){
        return response;
    }
    
    /**
     * Returns a ByteBuffer reason for failure.
     * @return the reason for failure
     */
    public ByteBuffer getFailureReason(){
        return failureReason;
    }
    
    /**
     * Returns a ByteBuffer warning message.
     * @return warning message
     */
    public ByteBuffer getWarningMessage(){
        return warningMessage;
    }
    
    /**
     * Returns the interval that requests should be sent to the tracker.
     * @return interval that requests should be sent to the tracker
     */
    public Integer getInterval(){
        return interval;
    }
    
    /**
     * Returns the minimum interval that requests should be sent to the tracker.
     * @return minimum interval that requests should be sent to the tracker
     */ 
    public Integer getMinInterval(){
        return minInterval;
    }
    
     /**
     * Returns current amount downloaded
     * @return downloaded amount
     */
    public Integer getDownloaded(){
        return downloaded;
    }
     /**
     * Returns the tracker ID as a String.
     * @return the tracker ID
     */
    public String getTrackerID(){
        return trackerID;
    }
    
     /**
     * Returns the number of seeders (i.e., the number of people who have finished downloading and have all the pieces).
     * @return the number of seeders
     */
    public Integer getNumSeeders(){
        return complete;
    }
     /**
     * Returns the number of leechers (i.e., the number of people who are still downloading pieces).
     * @return the number of lechers
     */
    public Integer getNumLeechers(){
        return incomplete;
    }
     /**
     * Returns the peer list that was received from the tracker.
     * @return the current peer list
     */
    public ArrayList<Peer> getPeerList(){
        return peerList;
    }
    
    
    /**
     * Uses Bencoder2 to decode the response from the tracker.
     * @return true if successfully decoded the response; false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean decodeResponse(){
        try{
            Object obj = Bencoder2.decode(response);
            HashMap <ByteBuffer, Object> dictionary = (HashMap<ByteBuffer, Object>) obj;
            
             for (ByteBuffer key:dictionary.keySet()){
                String keystr = new String(key.array(), Charset.forName("UTF-8"));
                Object value = dictionary.get(key);
                //System.out.println("Keystr: " + keystr);
                if (keystr.equals("failure reason")){
                    failureReason = (ByteBuffer)value;
                }
                else if (keystr.equals("warning message")){
                    warningMessage = (ByteBuffer)value;
                }
                else if (keystr.equals("interval")) {
                    interval = (Integer)value;
                }
                else if (keystr.equals("min interval")) {
                    minInterval = (Integer)value;
                }
                else if (keystr.equals("downloaded")){
                    downloaded = (Integer)value;
                }
                else if(keystr.equals("tracker id")){
                    trackerID = (String)value;
                }
                else if (keystr.equals("complete")) {
                    complete = (Integer)value;
                }
                else if (keystr.equals("incomplete")) {
                    incomplete = (Integer)value;
                }
                else if (keystr.equals("peers")) {
                    ArrayList<Object> objList = (ArrayList<Object>)value;
                    for (Object peer : objList) {
                        HashMap<ByteBuffer, Object> peerDictionary = (HashMap<ByteBuffer, Object>)peer;
                        
                        String peerID = null;
                        String ip = null;
                        Integer port = null;
                        
                        for (ByteBuffer peerKey : peerDictionary.keySet()) {
                            String peerKeystr = new String(peerKey.array(), Charset.forName("UTF-8"));
                            //System.out.println("\tPeerkeystr: " + peerKeystr);
                            
                            if (peerKeystr.equals("ip")) {
                                ip = new String(((ByteBuffer)peerDictionary.get(peerKey)).array(), Charset.forName("UTF-8"));
                                //System.out.println("\t\tvalueIP : " + valueIP);
                            }
                            else if (peerKeystr.equals("peer id")) {
                                peerID = new String(((ByteBuffer)peerDictionary.get(peerKey)).array(), Charset.forName("UTF-8"));
                                //System.out.println("\t\tvaluePeerId: " + valuePeerId);
                            }
                            else if (peerKeystr.equals("port")) {
                                port = (Integer)peerDictionary.get(peerKey);
                                //System.out.println("\t\tvaluePort: " + valuePort);
                            }
                        }
                        
                        peerList.add(new Peer(peerID, ip, port));
                    }
                }
                else {
                    System.err.println("ERROR: " + keystr + " is invalid key");
                }
                
            }
            //System.out.println ("PEER LIST: " + Arrays.toString(valuePeers.toArray()));
            return true;
        }
        catch(BencodingException e){
            System.err.println(e);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets the local peers from the list of peers returned by the tracker (i.e., the peers specified in the assignment description).
     * @param peerList the entire list of peers returned by the tracker
     * @return the list of local peers specified in the assignment description
     */
    public ArrayList<Peer> getLocalPeers(List<Peer> peerList) {
      for (int i = 0; i<peerList.size(); i++){
          RUBTClient.debugPrint("index: " + i + " -- " + peerList.get(i));
      }
      ArrayList<Peer> localPeers = new ArrayList();
      for (Peer peer:peerList) {
          if (peer.getPeerID().startsWith("RU") || peer.getPeerID().startsWith("-RU")){
              localPeers.add(peer);
          }
          /*if (peer.getIP().equals("128.6.171.132")||peer.getIP().equals("128.6.171.131")){
              localPeers.add(peer);
          }*/
          //localPeers.add(peer);
      }
      return localPeers;
  }
}
