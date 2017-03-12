package ichttt.logicsimModLoader.gui;

import ichttt.logicsimModLoader.api.Mod;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

/**
 * Use this to show your mod in the {@link ModListGui}
 * You need to register this using e.g. {@link ichttt.logicsimModLoader.event.loading.LSMLRegistrationEvent#registerModGui(Mod, IModGuiInterface)}
 * @since 0.0.1
 */
public interface IModGuiInterface {
    void setup();

    @Nonnull
    JPanel draw();
}
