
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;
import javax.swing.SwingWorker;

/**
 * PeerSwingWorker.java
 * Thread that manages a single peer's connection to the client. Used for receiving messages related to downloading, uploading, etc.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */
public class PeerSwingWorker extends SwingWorker<Boolean, Void> {
    ProcessHandler processes;
    Peer peer;
    boolean stillConnected;
    int ID;
    int byte15[];
   
    /**
     * Constructor for objects of class PeerSwingWorker.
     * @param processes defines a current set of processes and properties that provide information about execution
     * @param peer peer to which this PeerSwingWorker manages the connection
     */
    public PeerSwingWorker(ProcessHandler processes, Peer peer,int ID){
        this.processes = processes;
        this.peer = peer;
        this.stillConnected = true;
        this.ID = ID;
        this.byte15 = processes.byte15;
    } 
    
    /**
     * Overrides the doInBackground() method of SwingWorker.
     * Calls connectToPeer() to begin the connection between the client and the peer.
     * returns true if successful, false otherwise.
     */
    @Override
    protected Boolean doInBackground() {
        try {
            //returnVal = connectToPeer();
            connectToPeer();
            //RUBTClient.debugPrint("Closing connections...");
            if(peer.getSocket()!=null){
                peer.getSocket().close();
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
     * Starts the connection between the client and the peer by handshaking with the peer.
     * If the handshake is valid, calls communicate() to begin sending and receiving messages.
     * @return true if connection was successful, false otherwise
     */
    public boolean connectToPeer(){
        peer.initBitfield(processes.getTorrentInfo());
        byte[] myInfoHashBytes = processes.getTorrentInfo().info_hash.array();
        Socket peerSocket;
        DataInputStream input;
        DataOutputStream output;
        try {
                RUBTClient.debugPrint("Local Peer: " + peer);
                //System.out.println("Trying to create socket with "+peer.getIP());
                peerSocket = new Socket(peer.getIP(), peer.getPort());
                System.out.println("Socket created with "+peer.getIP());
                if(peerSocket == null){
                    System.out.println("Connection with "+peer.getIP()+" is refused");
                    return false;
                }
                peer.setSocket(peerSocket);
                
                input = new DataInputStream (peerSocket.getInputStream());
                output = new DataOutputStream(peerSocket.getOutputStream());

                boolean validHandshake = Handshaker.validateHandshake(processes.getClient().getPeerID(), myInfoHashBytes, input, output, peerSocket);
                RUBTClient.debugPrint("Valid Handshake? " + validHandshake + " with peer " + peer);
                if (validHandshake){
                    peer.validHandshake = true;
                    communicate(input, output, peerSocket);
                }

                RUBTClient.debugPrint("Closing connections...");
                input.close();
                output.close();
                peerSocket.close();
        }
        catch (IOException ioE){
            System.err.println(peer.toString());
            ioE.printStackTrace();
            return false;
        }
        return true;
  
    }
    
    /**
     * Maintains communication with the peer using a while loop that runs as long as the socket is open, the process is started, and the peer is still connected.
     * Uses an if-else block to determine the appropriate response to each incoming message.
     * @param input DataInputStream used to manage incoming information from the peer
     * @param output DataOutputStream used to manage outgoing information to the peer
     * @param peerSocket socket of the corresponding peer
     */
    public void communicate(DataInputStream input, DataOutputStream output, Socket peerSocket){
        int fileLength = processes.getTorrentInfo().file_length;
        CommunicationInfo commInfo = new CommunicationInfo(processes.getClient(), fileLength);
        commInfo.requestedIndex = commInfo.client.nextRequest();
        commInfo.msgLength = -1;
        commInfo.torrentInfo = processes.getTorrentInfo();
        boolean firstRun = true;
        long startTimeReceivedMessage = System.nanoTime();
        long currentTimeReceivedMessage;
        long startTimeKeepAlive = System.nanoTime();
        long currentTimeKeepAlive;
        long startTime = System.nanoTime();
        boolean receivedMessage = false;
        long checkTime = System.nanoTime();
        int byte15 = 0;
        boolean choked = false;
        
        try{ 
            int PiecesReceive = 0;
            int count = 1;
            while (!peerSocket.isClosed()&&processes.isStarted() && stillConnected){
                if(choked == true){
                    System.out.println("\n\n\n\n\n\n"+peer.getIP()+" Choked, but still in loop.\n\n\n\n\n");
                }
                
                try{
                    
                    currentTimeReceivedMessage = System.nanoTime()-startTimeReceivedMessage;
                    currentTimeKeepAlive = System.nanoTime()-startTimeKeepAlive;
                    
                    
                    
                    //optimistic unchking.
                    if((System.nanoTime()-checkTime)/1000000000.0 > 15){
                        
                        System.out.println("\n\n\n\nBytes in 15 s of "+peer.getIP()+" is "+byte15+"\n\n\n\n\n\n");
                        int []current = new int [100];
                        current = processes.byte15;
                        for(int j =0;j<99;j++){
                            processes.byte15[j]=0;
                        }
                        Arrays.sort(current);
                        boolean test =false;
                        
                        for(int j=99;j>80;j--){
                            if(byte15 > current[j]){
                                output.write(MessageHandler.P2PMessage.UNCHOKE.bytes());
                                output.flush();
                                if(choked == true){
                                    System.out.println("\n\n\n\n\n\nWe Optimistically Unchoked peer "+peer.getIP()+"\n\n\n\n\n\n");
                                    choked=false;
                                    startTime = System.nanoTime();
                                }
                                test = true;
                                break;
                            }
                        }
                        if(test == false){
                            output.write(MessageHandler.P2PMessage.CHOKE.bytes());
                            output.flush();
                            System.out.println("\n\n\n\n\n\n"+peer.getIP()+" sends too slow, we choked him!\n\n\n");
                        }
                        checkTime = System.nanoTime();
                        byte15=0;
                    }
                    
                    
                    
                    //get rid of free riders.
                    if((System.nanoTime()-startTime)/1000000000.0 > 30.0){
                        try{
                            if(PiecesReceive == 0){
                                output.write(MessageHandler.P2PMessage.CHOKE.bytes());
                                System.out.println("\n\n\n\n\n\n"+peer.getIP()+" never upload to us, I chocked him!\n\n\n");
                                choked = true;
                            }
                            PiecesReceive = 0;
                            startTime=System.nanoTime();
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                    
                    
                    
                    
                    //sends a keep alive message every 90 seconds
                    if((double)currentTimeKeepAlive/1000000000.0 > 90.0){
                        RUBTClient.debugPrint("Sending keep alive message to " + peer);
                        output.writeInt(0); //write keep alive message
                        output.flush();
                        startTimeKeepAlive = System.nanoTime();
                    }
                    //checks if a message has been received in the past 120 seconds; if not, closes connections
                    if(!receivedMessage&&(double)currentTimeReceivedMessage/1000000000.0 > 120.0){ //not received any messages for over 2 minutes
                        processes.getRandomAccessFile().close();
                        return;
                    }
                    
                    //if the download is complete and it's the first time this loop has run,
                    //1. write unchoke and uintinterested messages
                    //2. send have messages to indicate what pieces the client has
                    if(firstRun&&processes.getClient().downloadComplete){
                        //write unchoke
                        output.writeInt(1);
                        output.write(1);
                        output.flush();
                        //write uninterested
                        output.writeInt(1);
                        output.write(3);
                        output.flush();
                        MessageHandler.sendFirstHave(commInfo, processes, peer, peerSocket, output);
                        firstRun = false;
                    }
                    
                    if(firstRun || !peer.receivedFirstHave){
                              /*MessageHandler.sendBitfield(commInfo, peer, input, output);*/
                              output.writeInt(0);
                              MessageHandler.sendFirstHave(commInfo, processes, peer, peerSocket, output);
                              firstRun = false;
                        }
                    
                    RUBTClient.debugPrint("-------------Started? "+ processes.isStarted());
                    /*--------------READ MESSAGE LENGTH-----------------*/
                    byte tmp[] = new byte[4];
                    RUBTClient.debugPrint("reading...");
                    input.readFully(tmp);
                    
                    RUBTClient.debugPrint("Bytes read: " + Arrays.toString(tmp));
                    commInfo.msgLength = java.nio.ByteBuffer.wrap(tmp).getInt();
                    
                    processes.byte15[ID]+=commInfo.msgLength;
                    byte15+=commInfo.msgLength;

                    RUBTClient.debugPrint("Length " + commInfo.msgLength + "("+Arrays.toString(tmp)+")");
                    /*-----------------END READ MESSAGE LENGTH--------------*/
                    
                    if (commInfo.msgLength!=0){
                        /*------------- READ MESSAGE ID ---------------*/
                        commInfo.messageID = input.readByte();
                        RUBTClient.debugPrint("Message " + commInfo.messageID+" from "+peerSocket.getInetAddress());
                        /*-------------END READ MESSAGE ID -----------*/
                        
                        int returnVal = -999;

                        

                        //if client is downloading
                        if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.CHOKE){
                            
                            returnVal = MessageHandler.peerChoked(commInfo, peer, input, output);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.UNCHOKE){
            
                            returnVal = MessageHandler.peerUnchoked(processes.getTorrentInfo(), commInfo, processes, peer, input, output);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.INTERESTED){
                            
                            returnVal = MessageHandler.peerInterested(commInfo, peer, input, output);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.UNINTERESTED){
                            
                            returnVal = MessageHandler.peerUninterested(commInfo, peer, input, output);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.HAVE){
                            
                            returnVal = MessageHandler.peerHave(commInfo, peer, input, output);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.BITFIELD){
                            returnVal = MessageHandler.receivedBitfield(commInfo, peer, input, output);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.REQUEST){
                            returnVal = MessageHandler.peerRequest(processes.getTorrentInfo(), commInfo, processes, peerSocket, input, output);
                            processes.getAnnouncer().updateUploaded(processes.uploaded);
                        }
                        else if (MessageHandler.getMessageID(commInfo.messageID) == MessageHandler.MessageID.PIECE){
                            returnVal = MessageHandler.clientDownloadPiece(processes.getTorrentInfo(),  commInfo, processes, input, output, processes.getRandomAccessFile());
                            PiecesReceive++;
                            if (returnVal==1){ //download is complete
                                commInfo.client.downloadComplete=true;
                                
                                //write uninterested
                                output.writeInt(1);
                                output.write(3);
                                output.flush();
                                
                                processes.setDownloadTime(); 
                                commInfo.event = TrackerRequest.EVENT_COMPLETED;
                                processes.scheduleTimer();
                                return;
                              }
                            else if (returnVal == -1){
                                commInfo.event = TrackerRequest.EVENT_STOPPED;
                            }
                            processes.getAnnouncer().updateDownload(processes.downloaded, processes.left, commInfo.event);
                            MessageHandler.sendRequest(processes.getTorrentInfo(), commInfo, processes, input, output);
                            //System.out.println("Return value: "+returnVal);
                        }
                        RUBTClient.debugPrint("Uploaded: " + processes.uploaded + ", Downloaded: " + processes.downloaded + ", Left: " + processes.left);
                        receivedMessage = true;
                        startTimeReceivedMessage = System.nanoTime();
                    }
                }
                catch (EOFException e){
                    //System.out.println("No message received for more than 2 minutes.");
                    //receivedMessage = false;
                    System.err.println("Reached end of file...");
                    System.err.println(peer);
                    e.printStackTrace();
                    processes.getRandomAccessFile().close();
                    return;
                }
                catch(IOException e){
                    System.err.println("Error: "+e.getMessage());
                    System.err.println(peer);
                    e.printStackTrace();
                    return;
                }
           }
           processes.getRandomAccessFile().close();
        }
        catch(IOException e){
            System.err.println(e);
            e.printStackTrace();
            return;
        }
      }

}
