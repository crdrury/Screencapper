import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 *  BoxSelect and SelectPanel are used to display a screen capture of the full display
 *  and allow the user to select the recording area. Pressing ESC will exit the program,
 *  SPACE will refresh the screen capture, and ENTER will begin the recording.
 */
public class BoxSelect extends JFrame {
    SelectPanel sPanel;

    public BoxSelect(GraphicsConfiguration conf) {
        super(conf);
        Rectangle screenRect = new Rectangle(0, 0, 0, 0);
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            screenRect = screenRect.union(gd.getDefaultConfiguration().getBounds());
        }
//        Toolkit kit = Toolkit.getDefaultToolkit();
//        Dimension screenSize = kit.getScreenSize();
        setUndecorated(true);
//        setSize(screenSize);
        setSize(screenRect.getSize());
        setResizable(false);

        sPanel = new SelectPanel(this);
        add(sPanel);
        sPanel.addKeyListener(sPanel);
        sPanel.addMouseListener(sPanel);
        sPanel.addMouseMotionListener(sPanel);

        setVisible(true);
        sPanel.requestFocus();

        sPanel.init();
    }
}

class SelectPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    public boolean editable = false;

    BoxSelect frame;
    int x1, y1, x2, y2;                                                     // Opposite corners of the selection
    int pWidth, pHeight;                                                    // The dimensions of the frame

    Robot robot;
    Image desktopImage;                                                     // A screenshot of the screen for selecting the rectangle
    Rectangle selectedRect;                                                 // The selected area
    Color washWhite = new Color(255, 255, 255, 100);           // Semi-transparent white overlay color
    Color overlayGrey = new Color(100, 100, 100, 180);         // Semi-transparent grey overlay for instructions
    RoundRectangle2D instructionsRect = new RoundRectangle2D.Float(50, 50, 300, 100, 10, 10);
    Stroke thinStroke  = new BasicStroke(1);
    Stroke thickStroke = new BasicStroke(3);

    public String dirName;
    public int endKey;

    // Double buffering variables
    Image offscreenImage;
    Graphics2D offscreenGraphics;

    private static final int KEY_EXIT = KeyEvent.VK_ESCAPE;                 // Quit the program
    private static final int KEY_NEWCAP = KeyEvent.VK_SPACE;                // Refresh the desktop image
    private static final int KEY_ENTER = KeyEvent.VK_ENTER;                 // Continue to the main program

    public SelectPanel(BoxSelect frame) {
        this.frame = frame;
    }

    public void init() {
        pWidth = getWidth();
        pHeight = getHeight();

        x1 = 0;
        y1 = 0;
        x2 = pWidth;
        y2 = pHeight;

        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.err.println("GraphicsEnvironment.isHeadless() returned true. Program will now exit.");
            System.exit(0);
        }
        refreshImage();

        offscreenImage = createImage(pWidth, pHeight);
        offscreenGraphics = (Graphics2D)offscreenImage.getGraphics();

        repaint();
    }

    // Hide this frame, recapture the screen, and show the frame again
    public void refreshImage() {
        frame.setVisible(false);
        Rectangle fullDisplayRect = new Rectangle(0, 0, 0, 0);
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            fullDisplayRect = fullDisplayRect.union(gd.getDefaultConfiguration().getBounds());
        }
//        desktopImage = robot.createScreenCapture(new Rectangle(0, 0, pWidth, pHeight));
        desktopImage = robot.createScreenCapture(fullDisplayRect);
        frame.setVisible(true);
    }

    // Handle exit, refresh, and start key presses
    public void keyPressed(KeyEvent e) {
        if (editable) {
            switch (e.getKeyCode()) {
                case KEY_EXIT:
                    System.out.println("Exit key pressed");
                    System.exit(0);
                    break;
                case KEY_NEWCAP:
                    refreshImage();
                    break;
                case KEY_ENTER:
                    new Goliath(dirName, endKey, calculateRect(x1, y1, x2, y2));
                    frame.setVisible(false);
                    break;
            }
        }
    }

    // Reset the selection to a 0x0 rectangle at the mouse position and redraw the box
    public void mousePressed(MouseEvent e) {
        if (editable) {
            int mX = e.getX();
            int mY = e.getY();

            x1 = mX;
            y1 = mY;
            x2 = mX;
            y2 = mY;

            repaint();
        }
    }

    // Set one corner of the box to the mouse position, leaving the other corner fixed
    public void mouseDragged(MouseEvent e) {
        if (editable) {
            int mX = e.getX();
            int mY = e.getY();

            x2 = mX;
            y2 = mY;

            repaint();
        }
    }

    // Use the two corners to return the contained Rectangle
    public Rectangle calculateRect(int x1, int y1, int x2, int y2) {
        return new Rectangle (Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    // Draw the screen capture and the selected rectangle
    public void paint(Graphics g) {
        if (offscreenGraphics != null) {
            offscreenGraphics.drawImage(desktopImage, 0, 0, null);

            selectedRect = calculateRect(x1, y1, x2, y2);
            offscreenGraphics.setStroke(thinStroke);
            offscreenGraphics.setColor(washWhite);
            offscreenGraphics.fill(selectedRect);
            offscreenGraphics.setColor(Color.white);
            offscreenGraphics.draw(selectedRect);

            offscreenGraphics.setColor(overlayGrey);
            offscreenGraphics.setStroke(thickStroke);
            offscreenGraphics.fill(instructionsRect);
            offscreenGraphics.setColor(Color.white);
            offscreenGraphics.draw(instructionsRect);
            offscreenGraphics.drawString("Select the area to capture", 80, 80);
            offscreenGraphics.drawString("Press 'Space' to refresh", 80, 110);
            offscreenGraphics.drawString("Press 'Enter' to begin capturing", 80, 125);
            offscreenGraphics.drawString("Press 'Esc' to exit", 80, 140);

            g.drawImage(offscreenImage, 0, 0, null);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
}