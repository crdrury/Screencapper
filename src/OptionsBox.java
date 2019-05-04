import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *  OptionsBox is a simple window where the user can choose a directory
 *  for the captures to be saved to and the hotkey to be used for
 *  ending the recording. It is also the entry point for the program.
 */
public class OptionsBox extends JFrame implements ActionListener, NativeKeyListener, FocusListener {
    // GUI elements
    JLabel errorLabel = new JLabel("", SwingConstants.CENTER);
    JLabel nameLabel = new JLabel("Directory name:");
    JTextField nameText = new JTextField("shots", 20);
    JLabel endLabel = new JLabel("Exit Key:");
    JTextField endText = new JTextField(20);
    JButton okButton = new JButton("OK");
    SelectPanel sPanel;

    int endInt;                                             // Hotkey used to end recording
    boolean endTextSelected = false;                        // Is the hotkey being set right now?
    String originalEndText = "Click to set key";            // Used to determine if the hotkey has been set yet

    public OptionsBox(SelectPanel sPanel) {
        // JFrame setup
        super("Startup Options");
        this.sPanel = sPanel;
        setSize(400, 140);
        setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
        setAlwaysOnTop(true);

        errorLabel.setForeground(Color.red);

        // Grid Layout Setup
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.ipadx = 10;

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        getContentPane().add(nameLabel, constraints);

        constraints.gridx = 1;
        getContentPane().add(nameText, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        getContentPane().add(endLabel, constraints);

        constraints.gridx = 1;
        getContentPane().add(endText, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        getContentPane().add(errorLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        getContentPane().add(okButton, constraints);

        // Register JNativeHook for global input
        try {
            GlobalScreen.registerNativeHook();

            LogManager.getLogManager().reset();
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.WARNING);
        } catch (NativeHookException e) {
            System.err.println("Error registering Native Hook");
            System.err.println(e.getMessage());

            System.exit(0);
        }

        // Add listeners, including JNativeHook
        GlobalScreen.addNativeKeyListener(this);
        okButton.addActionListener(this);
        endText.addFocusListener(this);
        endText.setEditable(false);
        endText.setText(originalEndText);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Set variables and hide this window if both parameters have been set
    public void actionPerformed(ActionEvent e) {
        if (!nameText.getText().equals("")) {
            if (!endText.getText().equals(originalEndText)) {
                sPanel.dirName = nameText.getText();
                sPanel.endKey = endInt;
                sPanel.editable = true;
                setVisible(false);
            } else {
                errorLabel.setText("Please set an exit hotkey.");
            }
        } else {
            errorLabel.setText("Please provide a name for this recording.");
        }
    }

    // Store the keycode of the hotkey as well as the name to display
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (endTextSelected) {
            endInt = e.getKeyCode();
            String keyString = e.paramString();
            String lookFor = "keyText=";
            String lookForEnd = ",";
            int start = keyString.indexOf(lookFor) + lookFor.length();
            int end = keyString.indexOf(lookForEnd, start);
            endText.setText(keyString.substring(start, end));
        }
    }

    public void focusGained(FocusEvent e) {
        endTextSelected = true;
        endText.setText("Listening for key...");
    }

    public void focusLost(FocusEvent e) {
        endTextSelected = false;
        if (endInt == 0)
            endText.setText(originalEndText);
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }
}