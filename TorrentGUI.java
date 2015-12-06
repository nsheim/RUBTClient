import java.awt.FlowLayout;
import java.io.*;
import java.util.Timer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
/**
 * TorrentGUI.java
 * 
 * A GUI that allows the user to click on a button to exit the program and continue later.
 * 
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 * 
 * 
 */
public class TorrentGUI extends JFrame{
    ProcessHandler processes;
    
    JPanel panel;
    JButton btnStartStop;
    JButton btnExitContinue;
    JButton done;
    JProgressBar progressBar;
    
    /**
     * Constructor for objects of class TorrentGUI.
     * @param processes defines a current set of processes and properties that provide information about execution
     */
    public TorrentGUI(ProcessHandler processes){
        this.processes = processes;
        
        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        if(processes.isStarted()){
            btnStartStop = new JButton("Stop");
            
            btnExitContinue = new JButton("EXIT and Continue Next Time");
            progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            add(progressBar);
            
        }
            
        
        else btnStartStop = new JButton("Start");
        //progressBar = new JProgressBar();
    }
    
    public void updateBar(int newValue){
        progressBar.setValue(newValue);
        progressBar.setStringPainted(true);
    }
    
    /**
     * Initializes the GUI.
     */
    public void init(){
        
        setTitle("RUBT");
        
        //btnStartStop.addActionListener(new ButtonListener(btnStartStop, processes)); 
        btnExitContinue.addActionListener(new ButtonListener(btnStartStop, processes)); 
        //progressBar.setBounds(40,40,200,30);
        //progressBar.setValue(0);
        //progressBar.setStringPainted(true);
        
        
        //panel.add(label);
        //panel.add(btnStartStop);
        panel.add(btnExitContinue);
        panel.add(progressBar);
        //panel.add(progressBar);
        add(panel);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setContentPane(progressBar);
        //pack();
        setVisible(true);
       
       while(processes.left >= 0){
            
           int value = (int)(processes.downloaded*100/(processes.downloaded+processes.left));
           final int percent = value;
           //System.out.println("-----------"+processes.downloaded+"---------");
           //System.out.println("-----------"+processes.left+"---------");
           //System.out.println("-----------"+percent+"---------");
           try{
                 SwingUtilities.invokeLater(new Runnable()
                 {
                   public void run(){
                      updateBar(percent);
                   }
                 });
                java.lang.Thread.sleep(100);
            }catch(InterruptedException e){
            }
            if(processes.left == 0){
                btnExitContinue.setText("Done!!!");
                break;
            }
         
            
       }
       
       
   }


}
