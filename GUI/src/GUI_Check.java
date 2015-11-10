import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * GUI_Check.java - a GUI for Card game
 *
 * @author Group 5
 * @version 1.0
 */

public class GUI_Check extends JFrame {
    private static final boolean VERBOSE = true;

   
    private static final int SIZE = 7;

   
    private static PegSolitaireState state = new PegSolitaireState();

    /**
     * variable <code>panel</code> - the panel for drawing the pegs
     */
    private JPanel panel;

    /**
     * variable <code>buffer</code> - Image object for
     * double-buffering */
    private Image buffer;

    /**
     * variable <code>bufferGraphics</code> - Graphics2D object for
     * double-buffering */
    private Graphics2D bufferGraphics;

    /**
     * variable <code>initialized</code> - whether or not the game is
     * fully initialized */
    private boolean initialized = false;


    /**
     * variable <code>stateStack</code> - a history of states leading
     * up to but not including the current state */
    private Stack stateStack = new Stack();

    
    
    public GUI_Check(){
	// Create the panel and add it to the frame.
        Container contentPane = getContentPane();
	panel = new PegSolitairePanel();
        contentPane.add(panel, "Center");

	// Create listeners.
        addWindowListener(new WindowCloser());

	initialized = true;
    }
    

    
   
    private class PegSolitairePanel extends JPanel 
    {
	
	public String[] imageFilenames = {"blueWood", "redWood", 
					  "yellowWood", "greenWood", "wood2"};
 
	public Image[] images = new Image[imageFilenames.length];

	
	public final int IMAGE_SIZE;

	
	public final int PANEL_SIZE;
	
	
	public PegSolitairePanel() {
	    // Load the image.
	    if (VERBOSE) System.out.print("Loading images...");
	    String path = "/Courses/cs104/images/pegSolitaire/";
	    MediaTracker tracker = new MediaTracker(this);
	    for (int i = 0; i < images.length; i++) {
		String filename = path + imageFilenames[i] + ".png";
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		images[i] = toolkit.getImage(filename);
		tracker.addImage(images[i],i);		
	    }
	    try {
		tracker.waitForAll();
	    } catch (InterruptedException e) {
		System.out.println(e);
	    }
	    if (VERBOSE) System.out.println("done.");
	    IMAGE_SIZE = images[0].getHeight(this);
	    PANEL_SIZE = SIZE*IMAGE_SIZE;
	    setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
	    addMouseListener(new PlayListener());
	}
	
	
	public void paint(Graphics g) 
	{
	    Graphics2D g2 = (Graphics2D) g;
	    if (initialized) {
		// If necessary, create double-buffering objects.
		if (buffer == null) {
		    buffer = createImage(getWidth(),getHeight());
		    bufferGraphics = (Graphics2D) buffer.getGraphics();
		}

		// Paint background
		bufferGraphics.drawImage
		    (images[4], 0, 0, null, this);
		
		// Paint all pegs into buffer.
		for (int row = 0; row < SIZE; row++)
		    for (int col = 0; col < SIZE; col++) {
			if (VERBOSE) System.out.println("state.getPos("+row+","+col+")");
			
			int pos = state.getPos(row, col);
			int x = col * IMAGE_SIZE;
			int y = row * IMAGE_SIZE;
			if (VERBOSE) System.out.println("state.onBoard("+pos+")");
			if (state.onBoard(pos)) {
			    // draw peg (if there)
			    if (VERBOSE) System.out.println("state.hasPeg("+pos+")");
			    if (state.hasPeg(pos)) {
				int color = (row % 2) + 2 * (col % 2);
				bufferGraphics.drawImage
				    (images[color], x, y, 
				     Color.white, this);
			    }
			    
			    // draw border for peg spaces
			    for (int line = 0; line < 4; line++) {
				int[] xPoints = {x, x+IMAGE_SIZE-1, 
						 x+IMAGE_SIZE-1, x, x};
				int[] yPoints = {y, y, y+IMAGE_SIZE-1, 
						 y+IMAGE_SIZE-1, y};
				for (int i = 0; i < 5; i++) {
				    xPoints[i] += line % 2;
				    yPoints[i] += line / 2;
				}
				bufferGraphics.setColor(Color.black);
				bufferGraphics.drawPolyline
				    (xPoints, yPoints, 5);
			    }
			}
		    }
	    }
		
	    // Draw buffer
	    g2.drawImage(buffer, 0, 0, this);
	}    
	

	/**
	 * class <code>PlayListener</code> - mouse listener for our GUI. */
	private class PlayListener extends MouseAdapter {
	    private int pressRow = -1;
	    private int pressCol = -1;
	    
	    /**
	     * <code>mousePressed</code> - record mouse press 
	     *
	     * @param event a <code>MouseEvent</code> value */
	    public void mousePressed(MouseEvent event) {
		pressRow = event.getY() / IMAGE_SIZE;
		pressCol = event.getX() / IMAGE_SIZE;
	    }

	    /**
	     * <code>mouseReleased</code> - process drag-drop or
	     * peg click
	     *
	     * @param event a <code>MouseEvent</code> value */
	    public void mouseReleased(MouseEvent event) {
		int releaseRow = event.getY() / IMAGE_SIZE;
		int releaseCol = event.getX() / IMAGE_SIZE;
		if (VERBOSE) System.out.println("state.getPos("+pressRow+","+pressCol+")");
		int pressPos = state.getPos(pressRow, pressCol);
		if (VERBOSE) System.out.println("state.getPos("+releaseRow+","+releaseCol+")");
		int releasePos 
		    = state.getPos(releaseRow, releaseCol);
		
		if (pressPos == releasePos) {
		    if (PegSolitaireState.onBoard(releasePos)) {
			if (VERBOSE) System.out.println("state.clone()");
			stateStack.push(state.clone());
			if (VERBOSE) System.out.println("state.toggle("+pressPos+")");
			state.toggle(pressPos);
		    }
		    else if (releasePos == 11) {
			if (stateStack.size() > 0)
			    state = (PegSolitaireState) stateStack.pop();
		    }
		    else if (releasePos == 71) {
			stateStack = new Stack();
			if (VERBOSE) System.out.println("new PegSolitaireState()");
			state = new PegSolitaireState();
			if (VERBOSE) System.out.println("state.toggle(42,45)");
			state.toggle(42,45);
			if (VERBOSE) System.out.println("state.toggle(33)");
			state.toggle(33);
			if (VERBOSE) System.out.println("state.toggle(53)");
			state.toggle(53);
		    }
		    else if (releasePos == 17) {
			stateStack = new Stack();
			if (VERBOSE) System.out.println("new PegSolitaireState()");
			state = new PegSolitaireState();
			if (VERBOSE) System.out.println("state.toggle(42,46)");
			state.toggle(42,46);
			if (VERBOSE) System.out.println("state.toggle(24,64)");
			state.toggle(24,64);
			if (VERBOSE) System.out.println("state.toggle(44)");
			state.toggle(44);
		    }
		    else if (releasePos == 77) {
			stateStack = new Stack();
			if (VERBOSE) System.out.println("new PegSolitaireState()");
			state = new PegSolitaireState();
			if (VERBOSE) System.out.println("state.toggle(31,57)");
			state.toggle(31,57);
			if (VERBOSE) System.out.println("state.toggle(13,75)");
			state.toggle(13,75);
			if (VERBOSE) System.out.println("state.toggle(33,55)");
			state.toggle(33,55);
			if (VERBOSE) System.out.println("state.toggle(44)");
			state.toggle(44);
		    }
		}
		else {		    
		    if (VERBOSE) System.out.println("state.clone()");
		    stateStack.push(state.clone());
		    if (VERBOSE) System.out.println("state.makeJump("+pressPos+","+releasePos+")");
		    if (!state.makeJump(pressPos, releasePos))
			stateStack.pop();
		}
		repaint();
	    }
	}
    }


    /**
     * class <code>WindowCloser</code> - a simple WindowAdapter for
     * the sole purpose of closing the window.  */
    private class WindowCloser extends WindowAdapter {
	public void windowClosing(WindowEvent event) {
	    System.exit(0);
	}
    }

    /**
     *
     * @param args a <code>String[]</code> value
     */
    public static void main(String[] args){
	GUI_Check game = new GUI_Check();
	game.setTitle("Peg Solitaire!");
	game.pack();
	game.show();
    }
    
} 