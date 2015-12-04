import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;

/**
 * RUBTClient.java
 * 
 * Checks for valid command line arguments and parses the torrent file.
 * Then, if the file already exists, continues the download.
 * Otherwise, creates a new file based off the command line arguments and begins the download.
 * 
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 * 
 */

@SuppressWarnings("unchecked")
public class RUBTClient
{
    static boolean debugPrint; //if true, debugPrint() will act as System.out.println()
  
    /**
     * 
     * @param args command line arguments: 
     * @param args[0] file path for the torrent
     * @param args[1] file path for the file being downloaded
     */
    public static void main(String[] args){
        debugPrint = true;
        validArguments(args);
        
        String torrentFilePath = args[0];
        String saveFilePath = args[1];
        File file = new File(saveFilePath);
        
        boolean fileAlreadyExisted = true;
        
        byte[]data = parseTorrent(torrentFilePath);
        TorrentInfo torrentInfo; 
        
        try {
            torrentInfo = new TorrentInfo(data);
            
            //create a new file if it does not already exist
            if (!file.exists() || file.isDirectory()){
                file.createNewFile();
                fileAlreadyExisted = false;
            }
            
            
            if(file.exists()&& !file.isDirectory()){
                if (fileAlreadyExisted){
                    System.out.println("\n\n\n\n\n-------File Already Exists!!!!!!!!!Will Continue Download--------");
                }
                System.out.println("Starting Download In 3 seconds...\n\n");
                try{
                    Thread.sleep(3000);
                }
                catch(InterruptedException ex){
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("Start!");
                ProcessHandler processes = new ProcessHandler(torrentInfo, file, fileAlreadyExisted);
                
                processes.restart();
                TorrentGUI gui = new TorrentGUI(processes);
                gui.init();
            }
            
            
        }
        catch (BencodingException e) {
            System.err.println(e);
            e.printStackTrace();
        }
        catch (IOException e){
            System.err.println(e);
            e.printStackTrace();
        }
  }

    /**
     * Checks if command line arguments are valid.
     * @param  args[]
     * @return true if parameters are valid, else false 
     */
    public static boolean validArguments(String[] args){
    //1.    Take as a command-line argument the name of the .torrent file to be loaded 
    //and the name of the file to save the data to. 
    // Example: java -cp . RUBTClient somefile.torrent video.mov
    if(args.length != 2){
      System.err.println("Error: Invalid input. Invalid number of command line arguments.");
      return false;
    }
    debugPrint("Command line arguments are valid.");
    return true;
  }
  /**
   * Parses the torrent file.
   * @ param torrentName - file path of a torrent file
   * @ return byte[] - array of bytes obtained from parsing the torrent file
   */
   public static byte[] parseTorrent(String torrentName){
    try {
        byte[] data = Files.readAllBytes(Paths.get(torrentName));
        return data;
    }
    catch (FileNotFoundException e) {
        System.err.println(e);
        e.printStackTrace();
    }
    catch (IOException e) {
        System.err.println(e);
        e.printStackTrace();
    }
    return null;
  }
  
  /**
   * Prints if debugPrint is true. Else, does nothing.
   * @param str the string to print
   */
  public static void debugPrint(String str){
      if(debugPrint==true){
          System.out.println(str);
      }
  }
}

