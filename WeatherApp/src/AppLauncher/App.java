package AppLauncher;
import Swing.GUI;
import javax.swing.*;


public abstract class App {

    public static void runApp(){
        System.out.println("****************************************\n" +
                           "☀️ Welcome to my Weather Application! ☀️\n" +
                           "****************************************\n");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // display the weather app gui
                new GUI().setVisible(true);
            }
        });
    }

}
