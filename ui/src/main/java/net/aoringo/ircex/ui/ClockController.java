/*
 * Copyright(C) 2014-2015 mikan All rights reserved.
 */
package net.aoringo.ircex.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.aoringo.ircex.receiver.Command;
import net.aoringo.ircex.receiver.CommandCallback;
import net.aoringo.ircex.receiver.CommandReceiver;
import net.aoringo.ircex.ui.plugin.Plugin;
import net.aoringo.ircex.ui.plugin.weather.City;
import net.aoringo.ircex.ui.plugin.weather.WeatherPlugin;

/**
 *
 * @author mikan
 */
public class ClockController implements Initializable, CommandCallback {

    private static final Logger LOG = Logger.getLogger(ClockController.class.getName());
    private Thread updaterThread;
    private Thread animatorThread;
    private CommandReceiver receiver;

    // #################### BASE SCREEN ITEMS ####################
    @FXML
    private Pane wrapper;

    @FXML
    private Label labelClockDate;

    @FXML
    private Label labelClockHour;

    @FXML
    private Label labelClockColon;

    @FXML
    private Label labelClockMinute;

    @FXML
    private Label labelDebug;

    @FXML
    private HBox weatherBox;

    // #################### MENU SCREEN ITEMS ####################
    @FXML
    private Pane menu;

    @FXML
    private Label labelCursorAdd;

    @FXML
    private Label labelCursorRemove;

    @FXML
    private Label labelCursorOption;

    @FXML
    private Label labelCursorQuit;

    private MenuCursor menuCursor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize UI components
        menu.setVisible(false);
        menuCursor = MenuCursor.ADD;

        // Initialize standard plugins
        Plugin weather = new WeatherPlugin(weatherBox, City.YOKOHAMA);
        weather.refresh();

        // Initialize additional plugins
        // Start update threads
        updaterThread = new Thread(new ClockUpdater(labelClockDate, labelClockHour,
                labelClockColon, labelClockMinute));
        updaterThread.setDaemon(true);
        updaterThread.start();
        animatorThread = new Thread(new ColorAnimator(wrapper, labelDebug));
        animatorThread.setDaemon(true);
        animatorThread.start();

        // Start IR receiver
        receiver = new CommandReceiver(this);
        try {
            receiver.start();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Cannot start the command receiver.", ex);
            throw new RuntimeException("Cannot start the command receiver.");
        }
    }

    @FXML
    public void handleMouseAction(MouseEvent event) {
        LOG.info("Mouse clicked.");
        Platform.exit();
        try {
            receiver.close();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Receiver close failed.", ex);
        }
    }

    @FXML
    public void handleKeyAction(KeyEvent event) {
        LOG.log(Level.INFO, "Key typed: {0}", event.getCode());
        switch (event.getCode()) {
            case SPACE:
                doCommand(Command.MENU);
                break;
            case ENTER:
                doCommand(Command.ENTER);
                break;
            case LEFT:
                doCommand(Command.LEFT);
                break;
            case RIGHT:
                doCommand(Command.RIGHT);
                break;
            case UP:
                doCommand(Command.UP);
                break;
            case DOWN:
                doCommand(Command.DOWN);
                break;
            default:
                break; // no action
        }
    }

    @Override
    public void command(Command command) {
        Platform.runLater(() -> {
            doCommand(command);
        });
    }

    private void doCommand(Command command) {
        if (command == Command.MENU) {
            menu.setVisible(!menu.isVisible());
        } else if (command == Command.UP) {
            menuCursor = menuCursor.up();
            labelCursorAdd.setVisible(menuCursor == MenuCursor.ADD);
            labelCursorRemove.setVisible(menuCursor == MenuCursor.REMOVE);
            labelCursorOption.setVisible(menuCursor == MenuCursor.OPTION);
            labelCursorQuit.setVisible(menuCursor == MenuCursor.QUIT);
        } else if (command == Command.DOWN) {
            menuCursor = menuCursor.down();
            labelCursorAdd.setVisible(menuCursor == MenuCursor.ADD);
            labelCursorRemove.setVisible(menuCursor == MenuCursor.REMOVE);
            labelCursorOption.setVisible(menuCursor == MenuCursor.OPTION);
            labelCursorQuit.setVisible(menuCursor == MenuCursor.QUIT);
        } else if (command == Command.ENTER) {
            if (menu.isVisible()) {
                switch (menuCursor) {
                    case QUIT:
                        Platform.exit();
                        break;
                    default:
                        break;
                }
            }
        } else {
            LOG.log(Level.SEVERE, "Sorry, {0} is currently unsupported.", command);
        }
    }
}