import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Handshaker.java
 * Keeps track of and manages information related to handshaking.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */
public class Handshaker
{
    //string length of <pstr>, as a single raw byte
    static byte ptrstrlen; 
    
    //string identifier of the protocol
    //static final String BIT_PROTOCOL = "BitTorrent protocol";
    static byte[] ptrBytes = 
        {'B','i','t','T','o','r','r','e','n','t',' ','p','r','o','t','o','c','o','l'};
         
    
    // 8 reserved bytes. All current implementations use all zeroes. 
    //Each bit in these bytes can be used to change the behavior of the protocol.
    static byte[] reservedBytes = {0,0,0,0,0,0,0,0}; 
    
    static final int RESERVED_LENGTH = 8;
    static final int INFOHASH_LENGTH=20;
    static final int PEERID_LENGTH=20;
    static final int HANDSHAKE_LENGTH=68;
    
    static final int PTRSTRLEN_INDEX = 0;
    static final int PTR_START_INDEX = 1;
    static final int RESERVED_START_INDEX = 20;
    static final int INFOHASH_START_INDEX = 28;
    static final int PEERID_START_INDEX = 48;

    /**
     * Constructor for objects of class Handshake.
     */
    public Handshaker(){
        ptrstrlen = (byte)(ptrBytes.length); // ptrstrlen is 19
    }

    /**
     * Sends the handshake message and checks if the handshake message is valid.
     * @param myPeerID client's peerID
     * @param myInfoHashBytes client's info hash bytes
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @param peerSocket socket of the corresponding peer
     */
    public static boolean validateHandshake(String myPeerID, byte[] myInfoHashBytes, 
                                            DataInputStream input, DataOutputStream output, 
                                            Socket peerSocket){
         byte[] myHandshake = generateHandshake(myPeerID, myInfoHashBytes);
         byte[] peerHandshake = getHandshakeMessage(input);
         sendHandshake(myHandshake, peerSocket);
         return handshakeValid(myInfoHashBytes, getInfohash(peerHandshake));
    }
    
    /**
     * Compares client's infohash with the peer's handshake infohash.
     * @return true if match, else false
     */
    public static boolean handshakeValid(byte[] myInfohash, byte[]handshakeInfohash)
    {
        return (Arrays.equals(myInfohash, handshakeInfohash));
    }
    
    /**
     * Generates the handshake message to be sent to the peer(s).
     * @param peerID client's peerID
     * @param infohashBytes client's infohash bytes
     * @return byte array representing the client's handshake message
     */
    public static byte[] generateHandshake(String peerID, byte[] infohashBytes){
       byte [] handshakeMsg;
        int ptrstrlen = (byte)(ptrBytes.length);
        handshakeMsg = new byte[HANDSHAKE_LENGTH];
        
        //handshakeMsg[0] is the length of ptr
        handshakeMsg[PTRSTRLEN_INDEX]= 0x13; //19 in hex
        try {
            //copies ptr bytes into handshakeMsg from index [1-19]
            System.arraycopy(ptrBytes, 0, handshakeMsg, PTR_START_INDEX, ptrstrlen);
            
            //copies reserved bytes into handshakeMsg from index [20-27]
            System.arraycopy(reservedBytes, 0, handshakeMsg, RESERVED_START_INDEX, RESERVED_LENGTH);
            
            //copies infohash bytes into handshakeMsg from index[28-47]
            System.arraycopy(infohashBytes, 0, handshakeMsg, INFOHASH_START_INDEX, INFOHASH_LENGTH);
            
            //copies peerID bytes into handshakeMsg from index [48-67]
            System.arraycopy(peerID.getBytes("ASCII"), 0, handshakeMsg, PEERID_START_INDEX, PEERID_LENGTH);
            
            RUBTClient.debugPrint("My handshake: "+ Arrays.toString(handshakeMsg));
        }
        catch (UnsupportedEncodingException e){
            System.err.println("ERROR: Invalid handshake format");
            System.err.println(e);
            e.printStackTrace();
        }
        return handshakeMsg;
    }
    
    /**
     * Retrieves the infohash message from a handshake message
     * @return the infohash message
     */
    public static byte[] getInfohash(byte[] handshakeMsg){
        byte[] infohashBytes = new byte[INFOHASH_LENGTH];
        for (int i = 0; i<INFOHASH_LENGTH;i++){
            infohashBytes[i] = handshakeMsg[INFOHASH_START_INDEX+i];
        }
        RUBTClient.debugPrint("peer handshake infohash: " + Arrays.toString(infohashBytes));
        return infohashBytes;
    }
    
    /**
     * Sends the client's handshake to a peer
     * @param handshakeBytes the client's handshake
     * @param peerSocket socket of the corresponding peer
     */
    public static void sendHandshake(byte[] handshakeBytes, Socket peerSocket){
        try{
            DataOutputStream output = new DataOutputStream(peerSocket.getOutputStream());
            output.write(handshakeBytes);
            output.flush();
        }
        catch(IOException e){
            System.err.println(e);
            e.printStackTrace();
        }
        
    }
    
    /**
     * Gets a peer's handshake message from the input stream.
     * @param input DataInputStream used to manage incoming information from the peer
     * @return peer's handshake message
     */
    public static byte[] getHandshakeMessage(DataInputStream input){
      Handshaker handshaker = new Handshaker();
        
      //handshakeMsg[0] is the length of ptr
      byte[] handshakeMsg = new byte[handshaker.HANDSHAKE_LENGTH];
      try {
          input.read(handshakeMsg);
          RUBTClient.debugPrint("Peer handshake message: "+ Arrays.toString(handshakeMsg));
      }
      catch(IOException e){
          System.err.println(e);
          e.printStackTrace();
      }
      return handshakeMsg;
  }
}
