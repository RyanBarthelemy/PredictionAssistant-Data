package com.axlor.predictionassistantdd;

import com.axlor.predictionassistantdd.gui.MainScreenController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

//Using JavaFX Weaver to allow Spring to manage JavaFX controllers.
//This gives us access to Spring IoC features we need, namely dependency injection.
//https://www.vojtechruzicka.com/javafx-spring-boot/

public class PredictionAssistantGUIApplication extends Application {

    private ConfigurableApplicationContext applicationContext;
    private boolean firstTime;
    private TrayIcon trayIcon;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        createTrayIcon(primaryStage);
        primaryStage.getIcons().add(new javafx.scene.image.Image(this.getClass().getClassLoader().getResourceAsStream("com/axlor/predictionassistantdd/gui/pa_data_taskbarIcon.jpg")));
        primaryStage.setTitle("Prediction Assistant Data");
        firstTime = true;
        Platform.setImplicitExit(false);

        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent parent = fxWeaver.loadView(MainScreenController.class);
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //called after start(), this init method is where we start the Spring Boot application that creates all our Services (etc) that we use.
    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                .sources(PredictionAssistantDataDownloaderApplication.class)
                .run(args);
    }

    //do tear down here
    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit(); //Causes the JavaFX application to terminate (gracefully).
        System.exit(111); //Causes the Spring application to close including the downAndSave loop thread that was spawned.

    }

    private void createTrayIcon(final Stage stage) {
        System.setProperty("java.awt.headless", "false"); //need this so SystemTray is supported correctly. (might just be a windows 10 thing?)
        if (SystemTray.isSupported()) {
            //get the SystemTray instance
            SystemTray tray = SystemTray.getSystemTray();

            //load an image
            java.awt.Image image = null;
            try {
                image = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("com/axlor/predictionassistantdd/gui/pa_data_icon.jpg")); //todo: fix this, it is temperamental.
            } catch (IOException e) {
                System.out.println("Couldn't read in image.");
            }

            stage.setOnCloseRequest(t -> hide(stage));
            //create a action listener to listen for default action executed on the tray icon
            final ActionListener closeListener = actionEvent -> System.exit(111 - 2);

            ActionListener showListener = actionEvent -> Platform.runLater(stage::show);
            //create a popup menu
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);

            MenuItem closeItem = new MenuItem("Close");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            //construct a TrayIcon
            trayIcon = new TrayIcon(image, "Prediction Assistant Data", popup);
            //set the TrayIcon properties
            trayIcon.addActionListener(showListener);
            //add the tray image
            try {
                tray.add(trayIcon);
            } catch (AWTException ignored) {
            }
        }
    }

    private void showProgramIsMinimizedMsg() {
        if (firstTime) {
            trayIcon.displayMessage("PA Data minimized to system tray.",
                    "PA Data is still running, right click and close to exit. Show to reopen window.",
                    TrayIcon.MessageType.INFO);
            firstTime = false;
        }
    }

    private void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                stage.hide();
                showProgramIsMinimizedMsg();
            } else {
                System.exit(111 - 1);
            }
        });
    }
}
