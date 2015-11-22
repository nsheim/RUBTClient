import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 * ButtonListener.java
 * 
 * Used when the button in the GUI is clicked.
 * The resulting operation of the button is determined by the current state of the program.
 * 
 * @author Shuhan Liu (sl1041) 154007082
 * @author Nicole Heimbach (nsh43) 153002353

 * @version RUBTClient Phase 2, 11/04/2015
 * 
 * 
 */

public class ButtonListener implements ActionListener{
    JButton button;
    ProcessHandler processes;
    
    /**
     * Constructor for objects of class ButtonListener.
     * @param button the button this ButtonListener is added to
     * @param processes used to determine the current state of the program and update it when the button is clicked
     */
    public ButtonListener(JButton button, ProcessHandler processes){ 
        this.button = button;
        this.processes = processes;
    }
    /**
     * Uses the current state of the program to determine the next operations to perform when the button is clicked
     * @param ae a click on the button
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!processes.isStarted()){
            button.setText("EXIT");
            processes.restart();
            /*if (processes.getTorrentSwingWorker().isDone()){
                button.setText("Start");
                processes.setStarted(false);   
            }*/
        }
        else{
            processes.stop();
            button.setText("Start");
            System.out.println("---------------Stopped---------------");
        }
    }
}