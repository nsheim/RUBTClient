
import java.io.*;
import java.net.*;


import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * MessageHandler.java
 * Used to send to and receive messages from peers. 
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */
public class MessageHandler
{
    
    static enum P2PMessage{
        KEEP_ALIVE (new byte[] {0,0,0,0}), 
        CHOKE (new byte[] {0,0,0,1,0}), 
        UNCHOKE (new byte[] {0,0,0,1,1}), 
        INTERESTED (new byte[] {0,0,0,1,2}), 
        UNINTERESTED (new byte[] {0,0,0,1,3}), 
        HAVE (new byte[] {0,0,0,5,4}), 
        BITFIELD (new byte[]{0,0,0,1,5}),
        REQUEST (new byte[]{0,0,0,13,6}), 
        PIECE (new byte[]{0,0,0,9,7}), //doesn't work?
        CANCEL(new byte[]{0,0,0,13,8}), 
        PORT(new byte[]{0,0,0,3,9});
        
        private byte[] message;
        private P2PMessage(byte[] message){
            this.message = message;
        }
        private P2PMessage(byte[] message, int length){
            this.message=message;
            
            //message[3] is the length of the message
            this.message[3]= (byte)(message[3]+length);
        }
        public byte[] bytes(){
            return message;
        }
        public byte[] msgWithLength(int length){
            byte[] messageWithLength = bytes();
            int lengthIndex = messageWithLength.length-2;
            messageWithLength[lengthIndex]+=length;
            return messageWithLength; 
        }
    }
    
    static enum MessageID{
        CHOKE(0), UNCHOKE(1), INTERESTED(2), UNINTERESTED(3), HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), CANCEL(8), PORT(9), INVALID(-1);
        private int messageID;
        
        private MessageID(int messageID){
            this.messageID = messageID;
        }
    }
    /*public static int sendKeepAlive(DataOutputStream output){
        try {
            output.writeInt(0);
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    public static int sendMessage(MessageID messageID, DataOutputStream output){
        try{
            switch(messageID){
                case CHOKE:
                    output.writeInt(1); //write length
                    output.write(0); //write ID
                case UNCHOKE:
                    output.writeInt(1); //write length
                    output.write(1); //write ID
                case INTERESTED:
                    output.writeInt(1); //write length
                    output.write(2); //write ID
                case UNINTERESTED:
                    output.writeInt(1); //write length
                    output.write(3); //write ID
                case HAVE:
                    output.writeInt(5); //write length
                    output.write(4); //write ID
                case REQUEST:
                    output.writeInt(13); //write length
                    output.write(6); //write ID
                case CANCEL:
                    output.writeInt(13); //write length
                    output.write(8); //write ID
                case PORT:
                    output.writeInt(3); //write length
                    output.write(9); //write ID
            }
            return 0;
        }
        catch(IOException e){
            System.err.println(e);
            e.printStackTrace();
            return -1;
        }
    }
    public static int sendMessage(MessageID messageID, int pieceIndex, DataOutputStream output){
        try{
            switch(messageID){
                case HAVE:
                    output.writeInt(5); //write length
                    output.write(4); //write ID
                    output.writeInt(pieceIndex);
                case CANCEL:
                    output.writeInt(13); //write length
                    output.write(8); //write ID
                case PORT:
                    output.writeInt(3); //write length
                    output.write(9); //write ID
            }
            return 0;
        }
        catch(IOException e){
            System.err.println(e);
            e.printStackTrace();
            return -1;
        }
    }*/

    /**
     * Returns the MessageID that corresponds with messageID
     * @param messageID a byte that represents a MessageID
     * @returns the MessageID associated with messageID
     */
    public static MessageID getMessageID(byte messageID){
        switch(messageID){
            case 0:
                return MessageID.CHOKE;
            case 1:
                return MessageID.UNCHOKE;
            case 2:
                return MessageID.INTERESTED;
            case 3:
                return MessageID.UNINTERESTED;
            case 4:
                return MessageID.HAVE;
            case 5:
                return MessageID.BITFIELD;
            case 6:
                return MessageID.REQUEST;
            case 7:
                return MessageID.PIECE;
            case 8: 
                return MessageID.CANCEL;
            case 9: 
                return MessageID.PORT;
            default:
                System.err.println("ERROR: Invalid message type");
                return null;
      }
    }
    /**
     * Gets the sha1 hash of an array of bytes.
     * @param bytesToHash bytes that will be used to generate an sha1 hash
     * @return the sha1 hash as an array of bytes
     */
    public static byte[] sha1Bytes(byte[] bytesToHash){
      try{
          MessageDigest msgDigest = MessageDigest.getInstance("SHA-1");
          msgDigest.update(bytesToHash);
          
          byte[] sha1Hash = msgDigest.digest();
          return sha1Hash;
      }
      catch(NoSuchAlgorithmException e){
          System.err.println(e);
          e.printStackTrace();
          return null;
      }
    }
    //WORKS
    /**
     * Receive a message of Message ID 1 (choke).
     * @param commInfo information used for communicating with the peer
     * @param peer the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if EOFException or IOException
     * @return 0 if success
     */
    public static int peerChoked(CommunicationInfo commInfo, Peer peer, DataInputStream input, DataOutputStream output){
        try{
              peer.choked = true;
              output.write(MessageHandler.P2PMessage.KEEP_ALIVE.bytes());
              output.write(MessageHandler.P2PMessage.INTERESTED.bytes());
              output.flush();
          }
          catch (EOFException e){
              RUBTClient.debugPrint("Reached end of file...");
              e.printStackTrace();
              return -1;
          }
          catch(IOException e){
              System.err.println(e);
              e.printStackTrace();
              return -1;
          }
          return 0;
    }
    //WORKS
    /**
     * Receive a message of Message ID 2 (unchoke).
     * If the client has not finished downloading, follows up by sending a request for a piece.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param commInfo information used for communicating with the peer
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if EOFException or IOException
     * @return 0 if success
     */
    public static int peerUnchoked(TorrentInfo torrentInfo, CommunicationInfo commInfo, ProcessHandler processes, Peer peer, DataInputStream input, DataOutputStream output){
          RUBTClient.debugPrint("-----Unchoked :)  ----------");
          RUBTClient.debugPrint("-----------Starting request file :D ------------");
          peer.choked = false;
          int len = 0; //the length of the file we are going to download this time.
          if ((torrentInfo.file_length - processes.downloaded) < torrentInfo.piece_length)
                len = torrentInfo.file_length - processes.downloaded;
          else{
                len = torrentInfo.piece_length;
          }
          try{
              if(!commInfo.client.downloadComplete){
                RUBTClient.debugPrint("download is complete in peer unchoked");
                output.write(MessageHandler.P2PMessage.REQUEST.bytes());
                //total number of indexes - index left to download.
                output.writeInt(commInfo.requestedIndex);
                output.writeInt(commInfo.downloadOffset);
                output.writeInt(len);

                output.flush();
                RUBTClient.debugPrint("Request for "+len+" bytes from ["+commInfo.requestedIndex+"]["+commInfo.downloadOffset+"] sent!!!");
              }
          }
          catch (EOFException e){
              RUBTClient.debugPrint("Reached end of file...");
              e.printStackTrace();
              return -1;
          }
          catch(IOException e){
              System.err.println(e);
              e.printStackTrace();
              return -1;
          }
          return 0;
    }
    
    //open a connection
    /**
     * Receive a message of ID 2(interested message).
     * @param commInfo information used for communicating with the peer
     * @param peer the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if IOException
     * @return 0 if success
     */
    public static int peerInterested(CommunicationInfo commInfo, Peer peer, DataInputStream input, DataOutputStream output){
        peer.interested = true;
        try{
            output.write(MessageHandler.P2PMessage.UNCHOKE.bytes());
            output.flush();
        }
        catch(IOException e){
            System.out.println(e);
            return -1;
        }
        return 0;
    }
    
    //close a connection
    /**
     * Receive message of ID 3 (Uninterested message).
     * @param commInfo information used for communicating with the peer
     * @param peer the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return 0 on success
     */
    public static int peerUninterested(CommunicationInfo commInfo, Peer peer, DataInputStream input, DataOutputStream output){
        peer.interested = false;
        return 0;
    }
    
    //NOT IMPORTANT FOR PHASE2
    /**
     * Receives a message of ID 4 (have message) and updates the peer's bitfield accordingly.
     * @param commInfo information used for communicating with the peer
     * @param peer the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if EOFException or IOException
     * @return 0 if success
     */
    public static int peerHave(CommunicationInfo commInfo, Peer peer, DataInputStream input, DataOutputStream output){
        try{
              int bytes = commInfo.msgLength;
                
              byte[] buffer = new byte[bytes];
              input.readFully(buffer);
              int pieceIndex = input.readInt();
              
              if (!peer.getBitfield()[pieceIndex]){
                  commInfo.client.getPiece(pieceIndex).incrementRarity();
                  commInfo.client.getRarityQueue().updatePiecePriority(commInfo.client.getPiece(pieceIndex));
              }
              peer.setBitfieldValue(pieceIndex, true);
              
              commInfo.client.getPiece(pieceIndex).incrementRarity();
              commInfo.client.getRarityQueue().updatePiecePriority(commInfo.client.getPiece(pieceIndex));
              
              RUBTClient.debugPrint("Peer: " + peer.getIP()+ " -- " + peer.getBitfield());
              output.write(MessageHandler.P2PMessage.INTERESTED.bytes());
              output.flush();
          }
          catch (EOFException e){
              RUBTClient.debugPrint("Reached end of file...");
              e.printStackTrace();
              return -1;
          }
          catch(IOException e){
              System.err.println(e);
              e.printStackTrace();
              return -1;
          }
        return 0;
    }
    
    //WORKS
    /**
     * Sends a single Have message (message ID 4) to all of the client's local peers to indicate a new piece the client has obtained
     * Executed after the successful download of a piece.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param commInfo information used for communicating with the peer
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param pieceIndex index of the newly obtained piece
     * @return -1 if EOFException or IOException
     * @return 0 if success
     */
    public static int sendHave(CommunicationInfo commInfo, ProcessHandler processes, int pieceIndex){
        try{
            List<PeerSwingWorker> peerWorkers = processes.getPeerWorkers();
            DataOutputStream output;
            for (int i = 0; i<peerWorkers.size();i++){
                Socket peerSocket = peerWorkers.get(i).peer.getSocket();
                if (peerSocket!=null && !peerSocket.isClosed() && peerWorkers.get(i).peer.validHandshake 
                && peerWorkers.get(i).peer.receivedFirstHave && !peerWorkers.get(i).peer.getSentHave()[pieceIndex]){
                    output = new DataOutputStream(peerWorkers.get(i).peer.getSocket().getOutputStream());
                    output.write(MessageHandler.P2PMessage.HAVE.bytes());
                    output.writeInt(pieceIndex);
                    output.flush(); 
              
                    peerWorkers.get(i).peer.getSentHave()[i] = true;
                    RUBTClient.debugPrint("Sent have message for index " + pieceIndex + " to peer: " + peerWorkers.get(i).peer);
                }
            }
              
          }
          catch (EOFException e){
              RUBTClient.debugPrint("Reached end of file...");
              e.printStackTrace();
              return -1;
          }
          catch(IOException e){
              System.err.println(e);
              e.printStackTrace();
              return -1;
          }
        return 0;
    }
    /**
     * Sends a series of Have messages (message ID 4) to a single peer to indicate the pieces the client currently has.
     * Only executed right after the initial handshake.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param commInfo information used for communicating with the peer
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param peer the corresponding peer
     * @param peerSocket socket of the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @param peerSocket socket of the corresponding peer
     * @return -1 if EOFException or IOException
     * @return 0 if success
     */
    public static int sendFirstHave(CommunicationInfo commInfo, ProcessHandler processes, Peer peer, Socket peerSocket, DataOutputStream output){
        try{
            for (int i = 0; i<processes.getClient().getBitfield().length; i++){
                if (processes.getClient().getBitfield()[i]){
                    if (peerSocket!=null && !peerSocket.isClosed() && !peer.getSentHave()[i]){
                        output.write(MessageHandler.P2PMessage.HAVE.bytes());
                        output.writeInt(i);
                        output.flush(); 
                        peer.getSentHave()[i]=true;
                        RUBTClient.debugPrint("Sent have message for index " + i + " to peer: " + peer);
                    }
                }
            }
            peer.receivedFirstHave = true;
            RUBTClient.debugPrint(peer.toString());
            RUBTClient.debugPrint("received first have.");
          }
          catch (EOFException e){
              RUBTClient.debugPrint("Reached end of file...");
              e.printStackTrace();
              return -1;
          }
          catch(IOException e){
              System.err.println(e);
              e.printStackTrace();
              return -1;
          }
        return 0;
    }
    
    //WORKS
    /**
     * Sends a message of message ID 5 (bitfield message).
     * Sends the client's bitfield to the peer. Only executed immediately after the handshake message.
     * @param commInfo information used for communicating with the peer
     * @param peer the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if IOException
     * @return 0 if success
     */
    public static int sendBitfield(CommunicationInfo commInfo, Peer peer, DataInputStream input, DataOutputStream output){
        try {
            byte[] clientBitfieldBytes = Bitfield.toByteArray(commInfo.client.getBitfield());
            RUBTClient.debugPrint("Sending bitfield to Peer: " + peer + "\n\t" + Arrays.toString(clientBitfieldBytes));
            output.write(P2PMessage.BITFIELD.msgWithLength(clientBitfieldBytes.length));
            output.write(clientBitfieldBytes);
            RUBTClient.debugPrint("bitfield sent");
            
            for (int i = 0; i<commInfo.client.getBitfield().length; i++){
                if (commInfo.client.getBitfield()[i]){
                    peer.getSentHave()[i]=true;
                }
            }
            
            return 0;
        } catch (IOException ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    //WORKS
    /**
     * Receives message of message ID 5 (bitfield).
     * Reads in the bitfield and saves it as the peer's bitfield.
     * @param commInfo information used for communicating with the peer
     * @param peer the corresponding peer
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if EOFException or IOException
     * @return 0 if downloading was a success, but download is not yet complete
     */
    public static int receivedBitfield(CommunicationInfo commInfo, Peer peer, DataInputStream input, DataOutputStream output){
        try{
              int bytes = commInfo.msgLength-1;
              RUBTClient.debugPrint("Bitfield length: " + (int)bytes);
              
              byte[] buffer = new byte[bytes];
              input.readFully(buffer);
              int numPieces = commInfo.torrentInfo.piece_hashes.length;
              Bitfield peerBitfield = new Bitfield(numPieces, buffer);
              peer.setBitfield(peerBitfield.toBooleanArray());
              
              for (int i = 0; i < peer.getBitfield().length; i++){
                  commInfo.client.getPiece(i).incrementRarity();
                  commInfo.client.getRarityQueue().updatePiecePriority(commInfo.client.getPiece(i));
              }
              //RUBTClient.debugPrint("Peer bitfield: " + peerBitfield);
              //RUBTClient.debugPrint("Peer bitfield boolean: " + Arrays.toString(peerBitfield.toBooleanArray()));
              
              output.write(MessageHandler.P2PMessage.INTERESTED.bytes());
              output.flush();
          }
          catch (EOFException e){
              RUBTClient.debugPrint("Reached end of file...");
              e.printStackTrace();
              return -1;
          }
          catch(IOException e){
              System.err.println(e);
              e.printStackTrace();
              return -1;
          }
          return 0;
    }
    
    //WORKS
    /**
     * Response to Message ID 6 (request).
     * Reads in the information from the request message and uploads the appropriate block to the peer.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param commInfo information used for communicating with the peer
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @param peerSocket socket of the corresponding peer
     * @return -1 if IOException
     * @return 0 if success
     */
    public static int peerRequest(TorrentInfo torrentInfo, CommunicationInfo commInfo, ProcessHandler processes, Socket peerSocket, 
                                        DataInputStream input, DataOutputStream output){
        try {
            int pieceIndex = input.readInt();
            
            RUBTClient.debugPrint("Peer Requested index: " + pieceIndex);
            //byte[] clientBitfieldBytes = Bitfield.toByteArray(commInfo.client.getBitfield());
            if(!commInfo.client.getBitfield()[pieceIndex]){
                RUBTClient.debugPrint("We don't have piece at index: " + pieceIndex);
                output.write(MessageHandler.P2PMessage.CHOKE.bytes());
            }
            else{
                
                int begin = input.readInt();
                int pieceLength = input.readInt();
                RUBTClient.debugPrint("Begin: " + begin + " -- Length: " + pieceLength);
                //send requested piece
                byte[] bytesToSend = new byte[pieceLength];
                for(int i = 0; i<pieceLength; i++){ 
                    bytesToSend[i] = commInfo.client.getPieces()[pieceIndex].getDataByte(i+begin); 
                }
                
                output.writeInt(9+pieceLength);
                output.write((byte)7);
                output.writeInt(pieceIndex);
                output.writeInt(begin);
                output.write(bytesToSend);
                
                processes.uploaded+=pieceLength;
                RUBTClient.debugPrint("UPLOADED INDEX: " + pieceIndex);
                RUBTClient.debugPrint("UPLOADED: " + processes.uploaded);
            }
                return 0;
           
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
            return -1;
        }
    } 
    //WORKS
    /**
     * Sends message of Message ID 6 (request).
     * Requests that the peer send a particular block to the client.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param commInfo information used for communicating with the peer
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @return -1 if IOException
     * @return 0 if success
     */
    public static int sendRequest(TorrentInfo torrentInfo, CommunicationInfo commInfo, ProcessHandler processes, 
                                        DataInputStream input, DataOutputStream output){
        try {
            int len;
            output.write(MessageHandler.P2PMessage.KEEP_ALIVE.bytes());
            output.write(MessageHandler.P2PMessage.REQUEST.bytes());
            output.writeInt(commInfo.requestedIndex);
            RUBTClient.debugPrint("Index Request: "+ commInfo.requestedIndex);
            output.writeInt(commInfo.downloadOffset);
            //if requested the last index
            if (commInfo.requestedIndex==torrentInfo.piece_hashes.length-1){
                len = torrentInfo.file_length - (torrentInfo.piece_hashes.length-1)*torrentInfo.piece_length;
            }
            else {
                len = torrentInfo.piece_length;
            }
            RUBTClient.debugPrint("Requested length "+len);
            output.writeInt(len);
            output.flush();
        } catch (IOException e) {
                System.err.println(e);
                e.printStackTrace();        
        }
        return 0;
    }
    
    /**
     * Response to Message ID 7 (piece).
     * Downloads a block sent by the peer.
     * @param torrentInfo information retrieved from the parsed torrent file
     * @param commInfo information used for communicating with the peer
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @param raFile RandomAccessFile used for writing downloaded blocks to the file
     * @return -1 if EOFException or IOException
     * @return 0 if downloading was a success, but download is not yet complete
     * @return 1 if download is complete
     */
    public static int clientDownloadPiece(TorrentInfo torrentInfo, CommunicationInfo commInfo, ProcessHandler processes,
                                        DataInputStream input, DataOutputStream output, RandomAccessFile raFile){
        RUBTClient.debugPrint("------------We can download now :D---------------"); 
        
          try{
            /*--------------------DOWNLOAD--------------------*/
            byte[] block;
            int blockLength = commInfo.msgLength-9;
            int pieceIndex = input.readInt();
            int offset = input.readInt();
            block = new byte[blockLength];

            RUBTClient.debugPrint("READ FIRST BLOCK +"+ offset+" of "+blockLength+" bytes!!!!!!!!!!!!");
            RUBTClient.debugPrint("piece length: " + torrentInfo.piece_length);
            int bytesRead = 0;
            while (bytesRead < blockLength) {
                bytesRead += input.read(block, bytesRead, blockLength-bytesRead);
            }
            RUBTClient.debugPrint("  Total Read: " + bytesRead + " bytes");
            /*--------------------END DOWNLOAD--------------------*/
            
            /*--------------------VERIFY PIECE HASH--------------------*/
            byte[] dataHash = sha1Bytes(block);

            boolean isValid = Arrays.equals(dataHash,torrentInfo.piece_hashes[pieceIndex].array());
            
            if (!isValid){
                RUBTClient.debugPrint("DATA HASH: " + Arrays.toString(dataHash));
                RUBTClient.debugPrint("PIECE HASH: " + Arrays.toString(torrentInfo.piece_hashes[pieceIndex].array()));
            
                RUBTClient.debugPrint("INVALID PIECE HASH. DOWNLOAD WAS UNSUCCESSFUL.");
                System.err.println("ERROR: invalid piece index: " + pieceIndex);
                return -1;
              }
            
            /*--------------------END VERIFY PIECE HASH--------------------*/
            
            /*--------------------WRITE BLOCK--------------------*/
            commInfo.client.addBlock(pieceIndex, block, offset);
            
            commInfo.client.getPiece(pieceIndex).setVerified(isValid); //assumes we downloaded an entire piece... should edit this later
            raFile.seek(commInfo.requestedIndex*torrentInfo.piece_length);
            raFile.write(block);
            RUBTClient.debugPrint("-----------download this part successfully------------");
            /*--------------------END WRITE BLOCK--------------------*/
            
            /*--------------------UPDATE BITFIELD--------------------*/
            sendHave(commInfo, processes, pieceIndex); //edit this later to only send a have message if we've downloaded the entire piece and not just a block of it
            
            commInfo.client.setBitfieldValue(pieceIndex, true);
            //RUBTClient.debugPrint("BITFIELD: " + Arrays.toString(commInfo.client.getBitfield()));
            /*--------------------END UPDATE BITFIELD--------------------*/
            
            /*--------------------UPDATE COMM INFO--------------------*/
            
            processes.downloaded+=block.length;
            /*if(block.length < torrentInfo.piece_length){
                processes.downloaded+= block.length;
            }
            else{
                processes.downloaded += torrentInfo.piece_length;
            }*/
            processes.left = torrentInfo.file_length - processes.downloaded;
            RUBTClient.debugPrint("Total length: "+ torrentInfo.file_length);
            RUBTClient.debugPrint("Downloaded length: "+ processes.downloaded);
            RUBTClient.debugPrint("Percent: "+ (int)(100*processes.downloaded/torrentInfo.file_length) + "%");
            RUBTClient.debugPrint("total index "+ (torrentInfo.piece_hashes.length));
            /*--------------------END UPDATE COMM INFO--------------------*/
            
            if (processes.downloaded >= torrentInfo.file_length) {
                processes.downloaded = torrentInfo.file_length;
                processes.left = 0;
                commInfo.client.downloadComplete=true;
                commInfo.client.getRarityQueue().clear();
                RUBTClient.debugPrint("Download complete!!!");
                return 1;
            }
            
            /*--------------------UPDATE REQUESTED INDEX, RARITY QUEUE--------------------*/
            RUBTClient.debugPrint(commInfo.client.getRarityQueue().toString());
            Piece temp = commInfo.client.getPieces()[commInfo.requestedIndex];
            commInfo.client.getRarityQueue().remove(temp);
            
            commInfo.requestedIndex = commInfo.client.nextRequest();
        }

        catch (EOFException e){
            RUBTClient.debugPrint("Reached end of file...");
            e.printStackTrace();
            return -1;
        }
          catch(IOException e) {
              //System.err.println(e);
              //e.printStackTrace();
              return -1;
        }
        return 0;
    }
    //NOT IMPLEMENTED YET
    public static int clientCancel(){
        return 0;
    }
    //NOT IMPLEMENTED YET
    public static int getPeerPort(){
        return 0;
    }
}
