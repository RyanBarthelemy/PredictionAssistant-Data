package com.axlor.predictionassistantdd.service;

import com.axlor.predictionassistantdd.gui.MainScreenController;
import com.axlor.predictionassistantdd.model.Snapshot;
import com.axlor.predictionassistantdd.resository.SnapshotRepository;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

//todo: make sout statements Logger statements.

/**
 * This service class handles the PredictIt API data from start to finish.
 * It uses DataDownloaderService to download the market snapshot Json.
 * It then uses MapperService to map the Json String to a Snapshot object
 * Finally, it persists that object to a database using the SnapshotRepository repo.
 * <p>
 * It performs this series of tasks in a loop, continually saving new data to the database in order to form historical data for each market.
 * The time between attempts to get new data and save it is set by the waitTime field and set in application.properties key 'my.waitTime'.
 */
@Service
public class DataService {
    private static final Logger LOGGER = Logger.getGlobal();

    @Autowired
    DataDownloaderService dataDownloader;

    @Autowired
    MapperService mapperService;

    @Autowired
    SnapshotRepository snapshotRepository;

    @Autowired
    MainScreenController mainScreenController;

    @Value("${my.hoursToKeepSnapshot:24}")
    long hoursToKeepSnapshot;

    /**
     * The amount of time in milliseconds to wait between each request to PredictIt for data. Set in application.properties key 'my.waitTime'
     * Default is 60 seconds
     */
    @Value("${my.waitTime:40000}")
    long waitTime;

    private boolean paused = false;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy"); //todo: 12h clock format with am/pm

    /**
     * Init. Sets specified wait time from application.properties and begins the downloadAndSave loop.
     * Should never be called manually. It is automatically called by Spring after the DataService is constructed.
     *
     */
    //@PostConstruct  --decided to have gui begin this rather than it start on its own.
    public void init() {

        addMessage("DataService: Initializing data download and save loop:");
        addMessage("DataService: waitTime=" + waitTime);
        Runnable runnable =
                () -> {
                    try {
                        runDownloadAndSaveLoop(mainScreenController);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
        Thread newThread = new Thread(runnable);
        newThread.start();

        Runnable runnableDelete =
                () -> {
                    try {
                        runDeleteLoop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
        Thread deleteThread = new Thread(runnableDelete);
        deleteThread.start();

    }

    private void runDeleteLoop() throws InterruptedException {
        long timeToKeepSnapshots = hoursToKeepSnapshot*60*60*1000; //convert to milli
        while(true){
            List<Long> timestamps = snapshotRepository.getTimestamps();
            for (Long timestamp: timestamps){
                if(System.currentTimeMillis()-timestamp > timeToKeepSnapshots){
                    Snapshot snapshot = snapshotRepository.findSnapshotByTimestamp(timestamp);
                    addMessage("Deleting Snapshot[" + snapshot.getHashId() + "]");
                    snapshotRepository.delete(snapshot);
                }
            }
            Thread.sleep(1000*60*60);//check once per hour to delete things
        }
    }

    private void runDownloadAndSaveLoop(MainScreenController mainScreenController) throws InterruptedException {
        long startTime = 0;
        boolean pauseFlagSet = false;

        while (true) {
            System.out.println("paused status = " + paused); //why is this solving the pause bug?? Need to investigate more... // race condition?
            if (System.currentTimeMillis() - startTime > waitTime) {  //wait time has elapsed
                if (!paused) {
                    pauseFlagSet = false;
                    startTime = System.currentTimeMillis();
                    new Thread(this::downloadAndSave).start();

                    if (paused) {
                        addMessage("DataService: Waiting for unpause before continuing to check for new data.");
                        startTime = 0;
                    }
                }
            }
            if (!paused) {
                long timeLeft = waitTime - (System.currentTimeMillis() - startTime);
                if (timeLeft % 100 == 0 && mainScreenController != null && mainScreenController.getTimeLeftCountdownLabel() != null) {
                    Platform.runLater(() -> mainScreenController.getTimeLeftCountdownLabel().setText(String.valueOf(timeLeft / 1000.0)));
                }
            }
            else{ //we are paused
                if(!pauseFlagSet){
                    startTime = 0;
                    Platform.runLater(() -> {
                        assert mainScreenController != null; //if the pause button is pressed then it means the stage and scene elements are instantiated/initialized and therefore not null
                        mainScreenController.getTimeLeftCountdownLabel().setText("Paused");
                    });
                    pauseFlagSet = true;
                }
            }
        }
    }

    private void downloadAndSave() {
        addMessage("DataService: " + dtf.format(LocalDateTime.now()));

        addMessage("DataService: Attempting to download data from PredictIt API...");
        String json = dataDownloader.download();
        if (json == null) {
            addMessage("DataService: Data Downloader did not get status 200 response. Trying again soon.");
            return;
        }
        addMessage("DataService: Status 200: Data successfully downloaded from PredictIt API.");

        //json is correct, map to snapshot
        addMessage("DataService: Attempting to map Json text to Snapshot object...");
        Snapshot snapshot = mapperService.mapToSnapshot(json);
        if (snapshot == null) {
            addMessage("DataService: Downloaded json text failed to map to Snapshot object. Trying again soon.");
            return;
        }
        addMessage("DataService: Snapshot object successfully created.");

        if (!snapshotRepository.findById(snapshot.getHashId()).isPresent()) {
            addMessage("DataService: Snapshot with unique hashID=" + snapshot.getHashId() + " not found in database.");
            addMessage("DataService: Instructing SnapshotRepository to persist Snapshot object...");
            snapshotRepository.save(snapshot);
            addMessage("DataService: Snapshot saved with primary key hashID=" + snapshot.getHashId());
        } else {
            addMessage("DataService: Snapshot with unique hashID=" + snapshot.getHashId() + " already in database, discarding redundant data.");
        }
        addMessage("Waiting... next check soon.");
        addMessage("---------------------------------------------------------");
    }

    public void swapPauseStatus() {
        paused = !paused;
        mainScreenController.addMessage("Pause Button clicked: Pause status = " + paused);
    }

    private void addMessage(String message){
        Platform.runLater(() -> mainScreenController.addMessage(message));
    }
}
