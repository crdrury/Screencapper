import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 *   EndDialog is another small window that is displayed after recording
 *   is completed. It displays the average framerate of the recording
 *   and a button that will open the directory holding the images.
 */
public class EndDialog extends JFrame implements ActionListener {
    JLabel fpsLabel;
    JButton openFiles = new JButton("Open Recording Directory");
    String dirName;

    public EndDialog(float fps, String dirName) {
        super("Recording Complete");
        setSize(400, 200);
        setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);

        fpsLabel = new JLabel ("Average Framerate: " + fps);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(fpsLabel, BorderLayout.NORTH);
        getContentPane().add(openFiles, BorderLayout.SOUTH);

        openFiles.addActionListener(this);

        this.dirName = dirName;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Open the folder of saved screenshots
    public void actionPerformed(ActionEvent e) {
        try {
            Desktop.getDesktop().open(new File(dirName));
        } catch (Exception ex) {
        }
    }
}
