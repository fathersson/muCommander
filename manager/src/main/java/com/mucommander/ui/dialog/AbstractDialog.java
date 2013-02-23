package com.mucommander.ui.dialog;

import com.mucommander.commons.runtime.OsFamilies;
import com.mucommander.ui.helper.FocusRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Eugene Morozov
 */
public class AbstractDialog extends JDialog implements WindowListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDialog.class);

    /**
     * Minimum dimensions of this dialog, may be null
     */
    private Dimension minimumDimension;

    /**
     * Maximum dimensions of this dialog, may be null
     */
    private Dimension maximumDimension;

    /**
     * Has this window been activated yet ?
     */
    private boolean firstTimeActivated;

    /**
     * The component that will receive the focus when this window is activated for the first time, may be null
     */
    private JComponent initialFocusComponent;

    private Component locationRelativeComp;

    private boolean keyboardDisposalEnabled = true;

    private final static String CUSTOM_DISPOSE_EVENT = "CUSTOM_DISPOSE_EVENT";


    public AbstractDialog(Frame owner, String title, Component locationRelativeComp, boolean modal) {
        super(owner, title, modal);
        init(locationRelativeComp);
    }

    public AbstractDialog(Dialog owner, String title, Component locationRelativeComp, boolean modal) {
        super(owner, title, modal);
        init(locationRelativeComp);
    }

    private void init(Component locationRelativeComp) {
        this.locationRelativeComp = locationRelativeComp;
        setLocationRelativeTo(locationRelativeComp);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setBorder(new EmptyBorder(6, 8, 6, 8));
        setResizable(true);

        // Important: dispose (release resources) window on close, default is HIDE_ON_CLOSE
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Catch escape key presses and have them close the dialog by mapping the escape keystroke to a custom dispose Action
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = contentPane.getActionMap();
        AbstractAction disposeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (keyboardDisposalEnabled)
                    cancel();
            }
        };

        // Maps the dispose action to the 'Escape' keystroke
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CUSTOM_DISPOSE_EVENT);
        actionMap.put(CUSTOM_DISPOSE_EVENT, disposeAction);

        // Maps the dispose action to the 'Apple+W' keystroke under Mac OS X
        if (OsFamilies.MAC_OS_X.isCurrent())
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.META_MASK), CUSTOM_DISPOSE_EVENT);

        // Under Windows, Alt+F4 automagically disposes the dialog, nothing to do
    }

    /**
     * Method called when the user has canceled through the escape key.
     * <p>
     * This method is equivalent to a call to {@link #dispose()}. It's meant to be
     * overriden by those implementations of <code>FocusDialog</code> that need to run
     * code before canceling the dialog.
     * </p>
     */
    public void cancel() {
        dispose();
    }


    /**
     * Sets the component that will receive focus once this dialog has been made visible.
     *
     * @param initialFocusComponent the component that will receive focus once this dialog has been made visible, if
     *                              null, the first component in the dialog will receive focus.
     */
    public void setInitialFocusComponent(JComponent initialFocusComponent) {
        this.initialFocusComponent = initialFocusComponent;

        if (initialFocusComponent == null)
            removeWindowListener(this);
        else
            addWindowListener(this);
    }


    /**
     * Sets a maximum width and height for this dialog.
     */
    @Override
    public void setMaximumSize(Dimension dimension) {
        this.maximumDimension = dimension;
    }

    /**
     * Sets a minium width and height for this dialog.
     */
    @Override
    public void setMinimumSize(Dimension dimension) {
        this.minimumDimension = dimension;
    }


    /**
     * Specifies whether this dialog can be automatically disposed using the 'Escape' key and 'Apple+W' under Mac OS X.
     * If enabled, {@link #dispose()} will be called when one of those keystrokes is pressed from any component
     * within this dialog.
     *
     * @param enabled true to enable automatic keyboard disposal, false to disable it
     */
    public void setKeyboardDisposalEnabled(boolean enabled) {
        this.keyboardDisposalEnabled = enabled;
    }


    /**
     * Overrides Window.pack() to take into account minimum and maximum dialog size (if specified).
     */
    @Override
    public void pack() {
        super.pack();
        if (maximumDimension != null)
            DialogToolkit.fitToMaxDimension(this, maximumDimension);
        else
            DialogToolkit.fitToScreen(this);

        if (minimumDimension != null)
            DialogToolkit.fitToMinDimension(this, minimumDimension);
    }


    /**
     * Packs this dialog, makes it non-resizable and visible.
     */
    public void showDialog() {
        pack();

        if (locationRelativeComp == null)
            DialogToolkit.centerOnScreen(this);
        else
            setLocation(locationRelativeComp.getX() + (locationRelativeComp.getWidth() - getWidth()) / 2, locationRelativeComp.getY() + (locationRelativeComp.getHeight() - getHeight()) / 2);
        setVisible(true);
    }

    /**
     * Return <code>true</code> if the dialog has been activated (see WindowListener.windowActivated()).
     *
     * @return <code>true</code> if the dialog has been activated
     */
    public boolean isActivated() {
        return firstTimeActivated;
    }


    ////////////////////////////
    // WindowListener methods //
    ////////////////////////////

    public void windowOpened(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
        // (this method is called each time the dialog is activated)
        if (!firstTimeActivated && initialFocusComponent != null) {
            LOGGER.trace("requesting focus on initial focus component");

            // First try using requestFocusInWindow() which is preferred over requestFocus(). If it fails
            // (returns false), call requestFocus:
            // "The focus behavior of this method can be implemented uniformly across platforms, and thus developers are
            // strongly encouraged to use this method over requestFocus when possible. Code which relies on requestFocus
            // may exhibit different focus behavior on different platforms."
            if (!initialFocusComponent.requestFocusInWindow()) {
                LOGGER.trace("requestFocusInWindow failed, calling requestFocus");
                FocusRequester.requestFocus(initialFocusComponent);
            }

            firstTimeActivated = true;
        }
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }
}
