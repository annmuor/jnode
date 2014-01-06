package jnode;

import javax.swing.*;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public class BaseFrame extends JInternalFrame {
    public static final int SIZE = 300;
    private static final int X_OFFSET = 30;
    private static final int Y_OFFSET = 30;
    private static int openFrameCount = 0;

    public BaseFrame() {
        super("Document #" + (++openFrameCount),
                true, //resizable
                true, //closable
                true, //maximizable
                true);//iconifiable

        //...Create the GUI and put it in the window...

        //...Then set the window size or call pack...
        setSize(SIZE, SIZE);

        //Set the window's location.
        setLocation(X_OFFSET * openFrameCount, Y_OFFSET * openFrameCount);
    }
}
