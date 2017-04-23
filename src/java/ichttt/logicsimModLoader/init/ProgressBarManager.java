package ichttt.logicsimModLoader.init;

import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @since 0.0.4
 */
public class ProgressBarManager {
    private static JLabel label;
    private static JProgressBar bar;
    private static JFrame frame;

    static void init() {
        bar = new JProgressBar(1, 6);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        frame = new JFrame("Loading LogicSim mods...");
        bar.setValue(1);
        label = new JLabel("Core init...");
        panel.add(label);
        panel.add(bar);
        frame.add(panel);
        frame.setMinimumSize(new Dimension(16*20, 9*20));
        frame.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        frame.setVisible(true);
        LSMLLog.fine("ProgressBar is activated");
    }

    /**
     * DO NOT CALL FROM MOD CODE!
     */
    public static void stepBar(String text) {
        if (LSMLUtil.isCalledFromModCode()) {
            LSMLLog.warning("A mod tried stepping the progress bar!");
            return;
        }
        LSMLLog.fine("BAR STEP - " + text);
        if (label == null)
            return;
        label.setText(text);
        bar.setValue(bar.getValue() + 1);
    }

    static void destroyWindow() {
        if (frame != null)
            frame.dispose();
    }
}
