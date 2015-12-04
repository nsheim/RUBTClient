
import java.util.ArrayList;

/**
 * CommunicationInfo.java
 * Keeps track of information related to communicating with peers.
 *  
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 */

public class CommunicationInfo {
      TorrentInfo torrentInfo;
      Client client;
      int requestedIndex;
      int downloadOffset;
      int fileLength;
      
      String event;
      
      int msgLength;
      byte messageID;
      /**
       * Constructor for objects of class CommunicationInfo.
       */
      public CommunicationInfo(Client client, int fileLength){
          this.client = client;
          requestedIndex = 0;
          downloadOffset = 0;
          this.fileLength = fileLength;
          
          msgLength = 0;
          messageID = -1;
          event = TrackerRequest.EVENT_STARTED;
      }
      
    }
