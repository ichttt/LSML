package ichttt.logicsimModLoader.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import ichttt.logicsimModLoader.exceptions.MissingDependencyException;
import ichttt.logicsimModLoader.internal.LSMLLog;
import ichttt.logicsimModLoader.util.LSMLUtil;

import javax.swing.*;
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
            LSMLUtil.showMessageDialogOnWindowIfAvailable("Could not continue because some mods are missing dependencies\n" + exception.getMessage());
        } else {
            LSMLUtil.showMessageDialogOnWindowIfAvailable("There was an unexpected error and LSML could not continue. Further information can be found in the log", "Exception in app", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(-1);
    }
}
