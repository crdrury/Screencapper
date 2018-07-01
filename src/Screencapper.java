import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 *   Screencapper contains the main recording loop for the program.
 */
public class Screencapper implements NativeKeyListener, Runnable {
    Robot robot;
    Thread captureThread;
    boolean isRunning;
    Image capImage;                         // Stores the current screenshot
    Rectangle capRect;                      // The selection rectangle
    int shotNum;                            // Incrementing value for the current frame
    String dirName;                         // Name of the directory for saving shots
    int endKey;                             // Hotkey to end the recording
    long startTime;                         // Time when recording began in ms

    public Screencapper(String dirName, int endKey, Rectangle capRect) {
        // Store capture area, output directory, and end hotkey
        this.capRect = capRect;
        this.dirName = dirName;
        this.endKey = endKey;

        startTime = System.currentTimeMillis();

        // If the output directory doesn't exist yet, create it
        File directory = new File(dirName);
        if (!directory.exists()) {
            try {
                directory.mkdir();
            } catch (SecurityException e) {
                System.err.println("Security exception creating directory /" + dirName + "/");
            }
        }

        // The Robot class can automate various things, but can also create screen captures
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("GraphicsEnvironment.isHeadless() returned true. Program will now exit.");
            System.exit(0);
        }

        // Start a separate thread for creating the captures
        captureThread = new Thread(this);
        isRunning = true;
        captureThread.start();

        // Add the JNativeHook listener
        GlobalScreen.addNativeKeyListener(this);
    }

    // End the program if the hotkey has been pressed
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == endKey) {
            isRunning = false;
            System.out.println("End Capture");
            new EndDialog(1000f / ((System.currentTimeMillis() - startTime) / shotNum), dirName);
        }
    }

    // Store a screenshot of the selection area and save it
    public void run() {
        while (isRunning) {
            capImage = robot.createScreenCapture(capRect);
            writeFrame();
        }
    }

    // Save the current shot as a PNG in the chosen directory, then increment shotNum
    public void writeFrame() {
        try {
            File output = new File(dirName + "/shot" + formatShotNum(shotNum, 6) + ".png");
            ImageIO.write(convertImage(capImage), "png", output);
            shotNum++;
        } catch (IOException e) {
            System.err.println("Failed to write " + dirName + "/" + formatShotNum(shotNum, 6) + ".png");
        }
    }

    // Add leading 0s to match the provided string length
    // For instance, formateShotNum(25, 5) returns "00025"
    public static String formatShotNum(int s, int length) {
        String sString = s + "";
        String result = "";

        for (int i = 0; i < length - sString.length(); i++) {
            result += "0";
        }
        result += sString;

        return result;
    }

    // Return a BufferedImage containing the provided Image
    public BufferedImage convertImage(Image im) {
        BufferedImage buffer = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics bg = buffer.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();;
        return buffer;
    }

    // Program entry point. Start with the selection screen and the startup options box
    public static void main(String[] args) {
        BoxSelect b = new BoxSelect();
        new OptionsBox(b.sPanel);
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }
}