import java.awt.FlowLayout;
import java.io.*;
import java.util.Timer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
        }
        else btnStartStop = new JButton("Start");
        //progressBar = new JProgressBar();
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
        //panel.add(progressBar);
        add(panel);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    
}