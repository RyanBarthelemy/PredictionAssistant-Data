package com.axlor.predictionassistantdd;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class PredictionAssistantDataDownloaderApplication {

    public static void main(String[] args) {
        //SpringApplication.run(PredictionAssistantDataDownloaderApplication.class, args);
        Application.launch(PredictionAssistantGUIApplication.class, args);

    }

}
