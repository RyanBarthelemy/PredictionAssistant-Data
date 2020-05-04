package com.axlor.predictionassistantdd.gui;

/**
 * Controller class for the main scene.
 */

import com.axlor.predictionassistantdd.service.DataService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Component
@FxmlView("padd_mainScreen.fxml")
public class MainScreenController {

    @Autowired
    DataService dataService;

    private ArrayList<String> infoMessages = new ArrayList<>();

    @FXML // fx:id="nextCheckLabel"
    private Label nextCheckLabel; // Value injected by FXMLLoader

    @FXML // fx:id="titleLabel"
    private Label titleLabel; // Value injected by FXMLLoader

    @FXML // fx:id="pauseButton"
    private Button pauseButton; // Value injected by FXMLLoader

    @FXML // fx:id="timeLeftCountdownLabel"
    private Label timeLeftCountdownLabel; // Value injected by FXMLLoader

    @FXML // fx:id="logListView"
    private ListView<String> logListView = new ListView<>(); // Value injected by FXMLLoader


    @FXML
    void pauseButtonClicked(ActionEvent event) {
        dataService.swapPauseStatus();
    }

    @PostConstruct
    private void initDataService() {
        dataService.init();
    }

    public Label getTimeLeftCountdownLabel() {
        return timeLeftCountdownLabel;
    }

    public void addMessage(String message){
        infoMessages.add(message);
        ObservableList<String> logEntries = FXCollections.observableArrayList(infoMessages);
        logListView.setItems(logEntries);
    }
}
