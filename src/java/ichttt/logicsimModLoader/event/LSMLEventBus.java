package ichttt.logicsimModLoader.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import ichttt.logicsimModLoader.exceptions.MissingDependencyException;
import ichttt.logicsimModLoader.init.LogicSimModLoader;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.util.LSMLUtil;
import logicsim.App;
import logicsim.LSFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Class containing the {@link com.google.common.eventbus.EventBus} were all events are fired
 */
public class LSMLEventBus implements SubscriberExceptionHandler {
    public static final EventBus EVENT_BUS = new EventBus(new LSMLEventBus());

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) { // treat eventbus exceptions as normal exceptions
        LSMLLog.log("----------REPORTING EXCEPTION THROWN----------", Level.SEVERE, exception);
        if (exception instanceof MissingDependencyException) {
            LSMLUtil.showMessageDialogOnWindowIfAvailable(LogicSimModLoader.translate("missingDeps") + "\n" + exception.getMessage());
        } else {
            LSMLUtil.showMessageDialogOnWindowIfAvailable(LogicSimModLoader.translate("unexpectedError"), LogicSimModLoader.translate("exception"), JOptionPane.ERROR_MESSAGE);
        }
        App app = LogicSimModLoader.getApp();
        if (app != null && app.frame.isVisible()) {
            try {
                Method save = LSFrame.class.getDeclaredMethod("jMenuItem_saveas_actionPerformed", ActionEvent.class);
                save.setAccessible(true);
                //noinspection JavaReflectionInvocation
                save.invoke(app.lsframe, (ActionEvent) null);
            } catch (Exception e) {
                e.printStackTrace();
                LSMLLog.error("Could not save data!");
                return;
            }
        }
        System.exit(-1);
    }
}
