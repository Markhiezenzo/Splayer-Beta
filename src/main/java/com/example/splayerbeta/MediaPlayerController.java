package com.example.splayerbeta;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MediaPlayerController implements Initializable {

    // UI components
    @FXML private ListView<String> mediaListView;
    @FXML private GridPane mediaGridView;
    @FXML private StackPane videoContainer;
    @FXML private MediaView mediaView;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label currentFileLabel;
    @FXML private Button btnPlay, btnPause, btnStop;
    @FXML private Button btnPrevious, btnNext, btnSelectFolder;
    @FXML private Button btnRepeat, btnShuffle, btnFullScreen;
    @FXML private Button btnEqualizer;
    @FXML private VBox equalizerContainer;
    @FXML private ScrollPane mediaScrollPane;
    @FXML private TextField searchField;
    @FXML private Button btnSearch;
    @FXML private ComboBox<String> btnfilelist;
    @FXML private BorderPane borderPane;
    @FXML private StackPane emptyPlaylistPane;
    @FXML private ImageView emptyPlaylistIcon;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private VBox topVBox;
    @FXML private HBox topHBox;
    @FXML private VBox bottomVBox;
    @FXML private HBox bottomHBox1;
    @FXML private HBox bottomHBox2;
    @FXML private VBox leftVBox;
    @FXML private Text emptyPlaylistText;

    // New UI components
    @FXML private HBox equalizerBox;
    @FXML private ComboBox<String> presetComboBox;
    @FXML private ComboBox<Double> speedComboBox;
    @FXML private Label speedLabel;
    @FXML private ComboBox<Integer> sleepTimerCombo;
    @FXML private Button btnSleepTimer;
    @FXML private StackPane visualizerPane;
    @FXML private Button btnPlaylist;
    @FXML private Button btnLyrics;
    @FXML private Label lyricsLabel;
    @FXML private Button btnAddToPlaylist;
    @FXML private Button btnSavePlaylist;
    @FXML private Button btnLoadPlaylist;
    @FXML private Label volumeLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ComboBox<String> mediaTypeFilter;
    @FXML private Button btnPip;
    @FXML private Button btnSelectFolder1;

    // Default image for when nothing is playing
    private ImageView defaultImageView;

    // Media player variables
    private MediaPlayer mediaPlayer;
    private final ObservableList<File> mediaFiles = FXCollections.observableArrayList();
    private final ObservableList<String> fileNames = FXCollections.observableArrayList();
    private final ObservableList<File> filteredMediaFiles = FXCollections.observableArrayList();
    private final ObservableList<String> filteredFileNames = FXCollections.observableArrayList();
    private int currentMediaIndex = 0;
    private boolean isPlaying = false;
    private boolean isRepeating = false;
    private boolean isShuffling = false;
    private boolean isListView = false;
    private boolean isFullScreen = false;
    private boolean isEqualizerVisible = false;
    private Stage fullScreenStage;
    private MediaView fullScreenMediaView;
    private Stage pipStage;
    private MediaView pipView;

    // New feature variables
    private double lastVolume = 0.5;

    // Managers for extracted functionality
    private ThumbnailManager thumbnailManager;
    private EqualizerManager equalizerManager;
    private PlaylistManager playlistManager;
    private VisualizerManager visualizerManager;

    // Timer for sleep functionality
    private Timer sleepTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        thumbnailManager = new ThumbnailManager();
        equalizerManager = new EqualizerManager(equalizerBox, presetComboBox);
        playlistManager = new PlaylistManager(mediaFiles, fileNames, filteredMediaFiles, filteredFileNames,
                currentMediaIndex, btnSavePlaylist, btnAddToPlaylist, this);
        visualizerManager = new VisualizerManager(visualizerPane);
        new ThemeController(
                borderPane, themeComboBox, topVBox, topHBox, bottomVBox, bottomHBox1, bottomHBox2, leftVBox,
                videoContainer, equalizerContainer, equalizerBox, presetComboBox,
                btnPlay, btnPause, btnStop, btnPrevious, btnNext, btnRepeat, btnShuffle, btnFullScreen,
                btnPlaylist, btnLyrics, btnAddToPlaylist, btnSavePlaylist, btnLoadPlaylist, btnPip, btnSelectFolder,
                btnEqualizer, btnSearch, btnSleepTimer, btnfilelist, mediaTypeFilter, sleepTimerCombo,
                currentFileLabel, currentTimeLabel, totalTimeLabel, speedLabel, volumeLabel, lyricsLabel,
                searchField, progressSlider, volumeSlider, speedComboBox, emptyPlaylistText
        );

        initializeDefaultImageView();
        initializeEmptyPlaylistIcon();

        setupMediaControls();
        setupViewToggle();
        setupProgressSlider();
        setupVolumeSlider();
        setupListView();
        setupPlaybackSpeed();
        setupSleepTimer();
        setupVolumeLabel();

        videoContainer.setAlignment(Pos.CENTER);
        mediaView.setPreserveRatio(true);
        mediaListView.setVisible(false);
        mediaGridView.setVisible(true);

        filteredMediaFiles.addAll(mediaFiles);
        filteredFileNames.addAll(fileNames);

        searchField.setOnAction(_ -> handleSearch(null));
        searchField.textProperty().addListener((_, _, newVal) -> filterMedia(newVal));

        mediaScrollPane.widthProperty().addListener((_, _, newVal) -> {
            if (!isListView) {
                Platform.runLater(this::populateGridView);
            }
        });

        mediaScrollPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (isListView) {
                mediaListView.requestLayout();
            }
        });

        borderPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (isListView) {
                Platform.runLater(() -> {
                    mediaListView.requestLayout();
                    mediaScrollPane.requestLayout();
                });
            }
        });
        borderPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (isListView) {
                Platform.runLater(() -> {
                    mediaListView.requestLayout();
                    mediaScrollPane.requestLayout();
                });
            }
        });

        videoContainer.widthProperty().addListener((obs, oldVal, newVal) -> adjustMediaViewSize(mediaPlayer != null ? mediaPlayer.getMedia() : null));
        videoContainer.heightProperty().addListener((obs, oldVal, newVal) -> adjustMediaViewSize(mediaPlayer != null ? mediaPlayer.getMedia() : null));

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Platform.runLater(() -> {
                try {
                    // Stop and dispose of the visualizer
                    if (visualizerManager != null) {
                        visualizerManager.stopVisualizer();
                    }

                    // Stop and dispose of the thumbnail executor
                    if (thumbnailManager != null) {
                        thumbnailManager.getThumbnailExecutor().shutdownNow();
                        try {
                            thumbnailManager.getThumbnailExecutor().awaitTermination(100, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("Interrupted while stopping thumbnail executor: " + e.getMessage());
                        }
                    }

                    // Close PiP stage if open
                    if (pipStage != null && pipStage.isShowing()) {
                        pipStage.close();
                    }

                    // Stop and dispose of the media player
                    if (mediaPlayer != null) {
                        try {
                            mediaPlayer.stop();
                            mediaPlayer.dispose();
                        } catch (Exception e) {
                            System.err.println("Error disposing MediaPlayer: " + e.getMessage());
                        }
                        mediaPlayer = null;
                    }

                    // Cancel sleep timer if active
                    if (sleepTimer != null) {
                        sleepTimer.cancel();
                        sleepTimer = null;
                    }
                } catch (Exception e) {
                    System.err.println("Error in shutdown hook: " + e.getMessage());
                }
            });
        }));

        // Window close handler
        borderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getWindow().setOnCloseRequest(e -> {
                    try {
                        // Stop the visualizer
                        if (visualizerManager != null) {
                            visualizerManager.stopVisualizer();
                        }

                        // Stop and dispose of the media player
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.dispose();
                            mediaPlayer = null;
                        }

                        // Stop the thumbnail executor
                        if (thumbnailManager != null) {
                            thumbnailManager.getThumbnailExecutor().shutdownNow();
                            try {
                                thumbnailManager.getThumbnailExecutor().awaitTermination(100, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                System.err.println("Interrupted while stopping thumbnail executor: " + ex.getMessage());
                            }
                        }

                        // Close PiP stage if open
                        if (pipStage != null && pipStage.isShowing()) {
                            pipStage.close();
                        }

                        // Cancel sleep timer if active
                        if (sleepTimer != null) {
                            sleepTimer.cancel();
                            sleepTimer = null;
                        }
                    } catch (Exception ex) {
                        System.err.println("Error during window close: " + ex.getMessage());
                    }
                });
            }
        });

        showDefaultImage();

        borderPane.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        borderPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (file.isDirectory()) {
                    loadMediaFiles(file);
                    showInfoAlert("Folder Loaded", "Loaded media from: " + file.getName());
                }
            }
            event.setDropCompleted(true);
            event.consume();
        });

        borderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener((obs2, oldOwner, newOwner) -> {
                    if (!(newOwner instanceof TextField)) {
                        borderPane.requestFocus();
                    }
                });
            }
        });

        // Clear existing items before adding to avoid duplication
        mediaTypeFilter.getItems().clear();
        mediaTypeFilter.getItems().addAll("All", "Audio", "Video");
        mediaTypeFilter.setValue("All");
        mediaTypeFilter.setOnAction(e -> filterMediaByType(mediaTypeFilter.getValue()));

        currentFileLabel.textProperty().addListener((obs, oldVal, newVal) -> {
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setTitle(newVal.isEmpty() ? "SPlayer Media Player" : newVal);
        });

        pipStage = new Stage();
        pipStage.initStyle(StageStyle.UTILITY);
        pipStage.setAlwaysOnTop(true);
        pipStage.setResizable(false);
        pipView = new MediaView();
        pipView.setFitWidth(320);
        pipView.setFitHeight(180);
        pipView.setPreserveRatio(true);
        StackPane pipRoot = new StackPane(pipView);
        pipStage.setScene(new Scene(pipRoot, 320, 180));
        pipStage.setOnCloseRequest(e -> {
            Platform.runLater(() -> {
                if (pipStage.isShowing()) {
                    handlePip(null);
                }
            });
        });
        pipStage.setTitle("SPlayer PiP");

        updateEmptyPlaylistVisibility();
    }

    private void initializeDefaultImageView() {
        Image defaultImage;
        try {
            defaultImage = new Image(getClass().getResourceAsStream("/icon/iconMEDIA.png"));
        } catch (Exception e) {
            System.err.println("Error loading default image: " + e.getMessage());
            defaultImage = thumbnailManager.createPlaceholderImage(200, 200);
        }

        defaultImageView = new ImageView(defaultImage);
        defaultImageView.setPreserveRatio(true);
        defaultImageView.setFitWidth(200);
        defaultImageView.setFitHeight(200);
        defaultImageView.setVisible(false);

        videoContainer.getChildren().add(0, defaultImageView);
    }

    private void initializeEmptyPlaylistIcon() {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon/downloadIcon.png"), 100, 100, true, true);
            emptyPlaylistIcon.setImage(icon);
        } catch (Exception e) {
            System.err.println("Error loading empty playlist icon: " + e.getMessage());
            emptyPlaylistIcon.setImage(thumbnailManager.createPlaceholderImage(100, 100));
        }
    }

    private void showDefaultImage() {
        defaultImageView.setVisible(true);
        mediaView.setVisible(false);
        visualizerPane.setVisible(false);
    }

    private void hideDefaultImage() {
        defaultImageView.setVisible(false);
        mediaView.setVisible(true);
    }

    private void setupMediaControls() {
        btnPlay.setOnAction(this::handlePlay);
        btnPause.setOnAction(this::handlePause);
        btnStop.setOnAction(this::handleStop);
        btnPrevious.setOnAction(this::handlePrevious);
        btnNext.setOnAction(this::handleNext);
        btnSelectFolder.setOnAction(this::handleSelectFolder);
        btnRepeat.setOnAction(this::handleRepeat);
        btnShuffle.setOnAction(this::handleShuffle);
        btnFullScreen.setOnAction(this::handleFullScreen);
        btnPlaylist.setOnAction(playlistManager::handlePlaylist);
        btnLyrics.setOnAction(this::handleLyrics);
        btnAddToPlaylist.setOnAction(playlistManager::handleAddToPlaylist);
        btnSavePlaylist.setOnAction(playlistManager::handleSavePlaylist);
        btnLoadPlaylist.setOnAction(playlistManager::handleLoadPlaylist);
        btnEqualizer.setOnAction(this::handleEqualizer);
        btnSearch.setOnAction(this::handleSearch);
        btnPip.setOnAction(this::handlePip);

        btnPlay.setDisable(true);
        btnPause.setDisable(true);
        btnStop.setDisable(true);
        btnPrevious.setDisable(true);
        btnNext.setDisable(true);
    }

    private void handleEqualizer(ActionEvent event) {
        isEqualizerVisible = !isEqualizerVisible;
        equalizerContainer.setVisible(isEqualizerVisible);
        btnEqualizer.setStyle(isEqualizerVisible ? "-fx-background-color: #4CAF50;" : "-fx-base: #444;");
    }

    private void setupPlaybackSpeed() {
        ObservableList<Double> speedOptions = FXCollections.observableArrayList();
        for (double i = 0.5; i <= 2.0; i += 0.1) {
            speedOptions.add(Math.round(i * 10) / 10.0);
        }
        speedComboBox.setItems(speedOptions);
        speedComboBox.setValue(1.0);
        speedLabel.setText("1.0x");

        // Customize display format
        speedComboBox.setCellFactory(lv -> new ListCell<Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1fx", item));
            }
        });
        speedComboBox.setButtonCell(new ListCell<Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1fx", item));
            }
        });

        speedComboBox.setOnAction(e -> {
            Double speed = speedComboBox.getValue();
            if (speed != null) {
                speedLabel.setText(String.format("%.1fx", speed));
                if (mediaPlayer != null) {
                    mediaPlayer.setRate(speed);
                }
            }
        });
    }

    public MediaView getMediaView() {
        return mediaView;
    }

    public Label getCurrentFileLabel() {
        return currentFileLabel;
    }

    private void setupSleepTimer() {
        sleepTimerCombo.getItems().clear(); // Clear existing items
        sleepTimerCombo.getItems().addAll(5, 10, 15, 30, 45, 60, 90, 120);
        sleepTimerCombo.setValue(30);
        btnSleepTimer.setOnAction(e -> {
            if (sleepTimer != null) {
                sleepTimer.cancel();
                sleepTimer = null;
                btnSleepTimer.setText("Sleep Timer");
                btnSleepTimer.setStyle("-fx-base: #444;");
            } else {
                int minutes = sleepTimerCombo.getValue();
                sleepTimer = new Timer();
                sleepTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            handleStop(null);
                            btnSleepTimer.setText("Sleep Timer");
                            btnSleepTimer.setStyle("-fx-base: #444;");
                        });
                    }
                }, minutes * 60 * 1000L);
                btnSleepTimer.setText("Cancel (" + minutes + "m)");
                btnSleepTimer.setStyle("-fx-background-color: #4CAF50;");
            }
        });
    }

    private void setupVolumeLabel() {
        volumeLabel.setText(String.format("%d%%", (int) volumeSlider.getValue()));
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeLabel.setText(String.format("%d%%", newVal.intValue()));
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100);
            }
        });
    }

    private void handleLyrics(ActionEvent event) {
        lyricsLabel.setText("Lyrics feature coming soon!\nSample lyrics would appear here...");
        lyricsLabel.setVisible(!lyricsLabel.isVisible());
        btnLyrics.setStyle(lyricsLabel.isVisible() ? "-fx-background-color: #4CAF50;" : "-fx-base: #444;");
    }

    private void handlePlay(ActionEvent event) {
        if (mediaPlayer != null) {
            if (mediaPlayer.getCurrentTime().greaterThanOrEqualTo(mediaPlayer.getTotalDuration())) {
                mediaPlayer.seek(Duration.ZERO);
            }
            mediaPlayer.play();
            isPlaying = true;
            btnPlay.setDisable(true);
            btnPause.setDisable(false);
            btnStop.setDisable(false);
            File currentFile = filteredMediaFiles.get(currentMediaIndex);
            if (thumbnailManager.isAudioFile(currentFile)) {
                visualizerManager.setVisualizerActive(true);
                visualizerManager.setPlaying(true);
                visualizerPane.setVisible(true);
                visualizerManager.startVisualizer();
            } else {
                visualizerManager.setVisualizerActive(false);
                visualizerManager.setPlaying(false);
                visualizerPane.setVisible(false);
                visualizerManager.stopVisualizer();
            }
            if (isFullScreen && fullScreenMediaView != null) {
                fullScreenMediaView.setMediaPlayer(mediaPlayer);
            } else if (pipStage.isShowing()) {
                pipView.setMediaPlayer(mediaPlayer);
                mediaView.setMediaPlayer(null);
                // Force re-render
                pipView.setFitWidth(321); // Slightly adjust size
                pipView.setFitWidth(320);
            } else {
                mediaView.setMediaPlayer(mediaPlayer);
                pipView.setMediaPlayer(null);
            }
        } else if (!filteredMediaFiles.isEmpty()) {
            loadAndPlayMedia(currentMediaIndex);
        }
    }

    private void handlePause(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlay.setDisable(false);
            btnPause.setDisable(true);
            visualizerManager.setPlaying(false);
            visualizerManager.stopVisualizer();
            visualizerPane.setVisible(false);
        }
    }

    private void handleStop(ActionEvent event) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.ZERO);
                visualizerManager.stopVisualizer(); // Ensure visualizer is stopped
                isPlaying = false;
                btnPlay.setDisable(false);
                btnPause.setDisable(true);
                btnStop.setDisable(true);
                progressSlider.setValue(0);
                updateTimeLabels();
                lyricsLabel.setVisible(false);
                btnLyrics.setStyle("-fx-base: #444;");
                // Ensure MediaPlayer is unassigned from all views
                mediaView.setMediaPlayer(null);
                if (isFullScreen && fullScreenMediaView != null) {
                    fullScreenMediaView.setMediaPlayer(null);
                }
                if (pipStage.isShowing()) {
                    pipView.setMediaPlayer(null);
                    pipStage.hide();
                    btnPip.setStyle("-fx-base: #444;");
                }
                showDefaultImage();
                // Dispose of the MediaPlayer
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.err.println("Error in handleStop: " + e.getMessage());
            } finally {
                mediaPlayer = null;
            }
        }
    }

    private void setupMediaPlayer(File file) {
        equalizerManager.setMediaPlayer(mediaPlayer);
        visualizerManager.setMediaPlayer(mediaPlayer);
        mediaPlayer.setOnReady(() -> {
            Platform.runLater(() -> {
                Media media = mediaPlayer.getMedia();
                adjustMediaViewSize(media);
                progressSlider.setValue(0);
                updateTimeLabels();
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                mediaPlayer.setRate(speedComboBox.getValue());
                if (thumbnailManager.isAudioFile(file) && isPlaying) {
                    visualizerManager.setVisualizerActive(true);
                    visualizerManager.setPlaying(true);
                    visualizerPane.setVisible(true);
                    visualizerManager.startVisualizer();
                } else {
                    visualizerManager.setVisualizerActive(false);
                    visualizerManager.setPlaying(false);
                    visualizerPane.setVisible(false);
                    visualizerManager.stopVisualizer();
                }
            });
        });

        mediaPlayer.setOnError(() -> {
            Platform.runLater(() -> handleMediaLoadError(file, mediaPlayer.getError()));
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            if (!progressSlider.isValueChanging()) {
                double progress = newVal.toMillis() / mediaPlayer.getTotalDuration().toMillis() * 100;
                progressSlider.setValue(progress);
                updateTimeLabels();
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            Platform.runLater(() -> {
                if (isRepeating) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                } else {
                    handleNext(null);
                }
            });
        });
    }

    private void setupViewToggle() {
        btnfilelist.getItems().clear(); // Clear existing items to avoid duplication
        btnfilelist.getItems().addAll("Grid View", "List View");
        btnfilelist.setValue("Grid View");
        btnfilelist.setOnAction(e -> {
            String selectedView = btnfilelist.getValue();
            if ("List View".equals(selectedView)) {
                switchToListView();
            } else if ("Grid View".equals(selectedView)) {
                switchToGridView();
            }
        });
    }

    private void switchToListView() {
        if (!isListView) {
            System.out.println("Switching to ListView...");
            mediaGridView.setVisible(false);
            mediaGridView.setManaged(false);
            mediaListView.setVisible(true);
            mediaListView.setManaged(true);
            isListView = true;

            Platform.runLater(() -> {
                mediaListView.setItems(null); // Clear to force redraw
                mediaListView.setItems(filteredFileNames); // Rebind items
                mediaListView.refresh();
                mediaScrollPane.requestLayout();
                borderPane.requestLayout();
                System.out.println("Switched to ListView. Items: " + filteredFileNames.size() +
                        ", ScrollPane Width: " + mediaScrollPane.getWidth() +
                        ", ScrollPane Height: " + mediaScrollPane.getHeight() +
                        ", ListView Width: " + mediaListView.getWidth() +
                        ", ListView Height: " + mediaListView.getHeight());
            });

            updateEmptyPlaylistVisibility();
        }
    }

    private void switchToGridView() {
        if (isListView) {
            mediaListView.setVisible(false);
            mediaListView.setManaged(false);
            mediaGridView.setVisible(true);
            mediaGridView.setManaged(true);
            isListView = false;
            thumbnailManager.getThumbnailImageViews().clear();
            populateGridView();
            mediaScrollPane.requestLayout();
            mediaGridView.requestLayout();
            updateEmptyPlaylistVisibility();
        }
    }

    private void setupProgressSlider() {
        progressSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) mediaPlayer.pause();
        });
        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(progressSlider.getValue() / 100));
                if (isPlaying) mediaPlayer.play();
            }
        });
    }

    private void setupVolumeSlider() {
        volumeSlider.setValue(50);
    }

    private void setupListView() {
        mediaListView.setItems(filteredFileNames);
        mediaListView.setMinWidth(200); // Prevent collapsing
        mediaListView.setMinHeight(200); // Prevent collapsing
        System.out.println("ListView items set: " + filteredFileNames.size());

        mediaListView.setCellFactory(lv -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();
            private final HBox content = new HBox(10, imageView, label);

            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true);
                content.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(label, Priority.ALWAYS);
                label.setMaxWidth(Double.MAX_VALUE);
                label.setVisible(true);
                label.setManaged(true);
                label.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
                setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 5;");

                // Add click event handler
                setOnMouseClicked(e -> {
                    System.out.println("ListView cell clicked: Item = " + getItem() + ", Index = " + getIndex() + ", Click count = " + e.getClickCount());
                    String item = getItem();
                    if (item == null || isEmpty()) {
                        showErrorAlert("Error", "No file selected.");
                        return;
                    }

                    int index = getIndex();
                    if (index < 0 || index >= filteredMediaFiles.size()) {
                        showErrorAlert("Error", "File not found in playlist.");
                        return;
                    }

                    File file = filteredMediaFiles.get(index);
                    if (file == null || !filteredMediaFiles.contains(file)) {
                        showErrorAlert("Error", "Invalid file selected.");
                        return;
                    }

                    currentMediaIndex = index;
                    currentFileLabel.setText(item);
                    btnAddToPlaylist.setDisable(false);
                    mediaListView.getSelectionModel().select(index); // Highlight selected item

                    if (e.getClickCount() == 2) {
                        loadAndPlayMedia(currentMediaIndex);
                    }
                });

                // Add keyboard support for Enter key
                setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        int index = getIndex();
                        if (index >= 0 && index < filteredMediaFiles.size()) {
                            currentMediaIndex = index;
                            currentFileLabel.setText(filteredFileNames.get(index));
                            btnAddToPlaylist.setDisable(false);
                            loadAndPlayMedia(currentMediaIndex);
                        }
                    }
                });

                // Visual feedback on hover
                setOnMouseEntered(e -> setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 5;"));
                setOnMouseExited(e -> setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 5;"));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    System.out.println("ListView cell: empty");
                } else {
                    System.out.println("ListView cell: rendering item - " + item);
                    label.setText(item);
                    setGraphic(content);

                    int index = getIndex();
                    if (index >= 0 && index < filteredMediaFiles.size()) {
                        File file = filteredMediaFiles.get(index);
                        thumbnailManager.getThumbnailImageViews().put(file, imageView);
                        thumbnailManager.loadThumbnail(file, imageView, 50, 50);
                    }
                }
            }
        });

        mediaListView.setFixedCellSize(80);
        mediaListView.prefWidthProperty().bind(mediaScrollPane.widthProperty().subtract(10));
        mediaListView.prefHeightProperty().bind(mediaScrollPane.heightProperty().subtract(10));
        mediaListView.setStyle("-fx-background-color: #333; -fx-control-inner-background: #444; -fx-border-color: #555; -fx-border-width: 2;");
        mediaListView.setFocusTraversable(true);

        mediaListView.itemsProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("ListView items changed: " + (newVal != null ? newVal.size() : 0));
            Platform.runLater(() -> {
                mediaListView.refresh();
                mediaListView.requestLayout();
                mediaScrollPane.requestLayout();
            });
        });
    }

    private VBox createMediaCell(File file, String fileName) {
        VBox cell = new VBox(5);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(5));
        cell.setStyle("-fx-background-color: #444; -fx-background-radius: 5;");
        cell.setPrefSize(160, 140);

        ImageView thumbnailView = new ImageView();
        thumbnailView.setFitWidth(150);
        thumbnailView.setFitHeight(100);
        thumbnailView.setPreserveRatio(true);
        thumbnailView.setSmooth(true);
        thumbnailView.setCache(true);

        thumbnailManager.getThumbnailImageViews().put(file, thumbnailView);
        thumbnailManager.loadThumbnail(file, thumbnailView, 150, 100);

        Label label = new Label(fileName);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 12;");
        label.setMaxWidth(150);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        cell.getChildren().addAll(thumbnailView, label);

        cell.setOnMouseClicked(e -> {
            if (file == null || !filteredMediaFiles.contains(file)) {
                showErrorAlert("Error", "Invalid file selected.");
                return;
            }

            int index = filteredMediaFiles.indexOf(file);
            if (index < 0 || index >= filteredMediaFiles.size()) {
                showErrorAlert("Error", "File not found in playlist.");
                return;
            }

            currentMediaIndex = index;
            currentFileLabel.setText(fileName);
            btnAddToPlaylist.setDisable(false);

            if (e.getClickCount() == 2) {
                loadAndPlayMedia(currentMediaIndex);
            }
        });

        cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #555; -fx-background-radius: 5;"));
        cell.setOnMouseExited(e -> cell.setStyle("-fx-background-color: #444; -fx-background-radius: 5;"));

        return cell;
    }

    public boolean isSupportedMediaFile(File file) {
        return thumbnailManager.isAudioFile(file) || thumbnailManager.isVideoFile(file) || thumbnailManager.isImageFile(file);
    }

    private void handlePrevious(ActionEvent event) {
        if (filteredMediaFiles.isEmpty()) return;
        currentMediaIndex = (currentMediaIndex - 1 + filteredMediaFiles.size()) % filteredMediaFiles.size();
        playlistManager.setCurrentMediaIndex(currentMediaIndex);
        loadAndPlayMedia(currentMediaIndex);
        btnAddToPlaylist.setDisable(false);
    }

    private void handleNext(ActionEvent event) {
        if (filteredMediaFiles.isEmpty()) return;
        currentMediaIndex = isShuffling ? new Random().nextInt(filteredMediaFiles.size()) : (currentMediaIndex + 1) % filteredMediaFiles.size();
        playlistManager.setCurrentMediaIndex(currentMediaIndex);
        loadAndPlayMedia(currentMediaIndex);
        btnAddToPlaylist.setDisable(false);
    }

    private void handleSelectFolder(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Media Folder");
        File folder = chooser.showDialog(mediaView.getScene().getWindow());
        if (folder != null) {
            loadMediaFiles(folder);
            btnAddToPlaylist.setDisable(filteredMediaFiles.isEmpty());
        }
    }

    private void handleRepeat(ActionEvent event) {
        isRepeating = !isRepeating;
        btnRepeat.setStyle(isRepeating ? "-fx-background-color: #4CAF50;" : "-fx-base: #444;");
    }

    private void handleShuffle(ActionEvent event) {
        isShuffling = !isShuffling;
        btnShuffle.setStyle(isShuffling ? "-fx-background-color: #4CAF50;" : "-fx-base: #444;");
    }

    private void handleFullScreen(ActionEvent event) {
        if (isFullScreen) {
            exitFullScreen();
        } else {
            enterFullScreen();
        }
    }

    private void enterFullScreen() {
        if (mediaPlayer == null) return;
        fullScreenStage = new Stage();
        fullScreenStage.initStyle(StageStyle.UNDECORATED);
        fullScreenStage.setFullScreen(true);
        fullScreenMediaView = new MediaView(mediaPlayer);
        fullScreenMediaView.setPreserveRatio(true);
        fullScreenMediaView.fitWidthProperty().bind(fullScreenStage.widthProperty());
        fullScreenMediaView.fitHeightProperty().bind(fullScreenStage.heightProperty());
        StackPane root = new StackPane(fullScreenMediaView);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root);
        fullScreenStage.setScene(scene);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) exitFullScreen();
        });
        fullScreenStage.setOnCloseRequest(e -> exitFullScreen());
        fullScreenStage.show();
        isFullScreen = true;
        btnFullScreen.setText("Exit Fullscreen");
        if (pipStage != null && pipStage.isShowing()) {
            pipView.setMediaPlayer(null);
            pipStage.hide();
            btnPip.setStyle("-fx-base: #444;");
        }
    }

    private void exitFullScreen() {
        if (!isFullScreen) return;
        fullScreenStage.close();
        fullScreenStage = null;
        fullScreenMediaView = null;
        isFullScreen = false;
        btnFullScreen.setText("Fullscreen");
        if (mediaPlayer != null) {
            if (pipStage.isShowing()) {
                pipView.setMediaPlayer(mediaPlayer);
                // Force re-render
                pipView.setFitWidth(321);
                pipView.setFitWidth(320);
            } else {
                mediaView.setMediaPlayer(mediaPlayer);
            }
        }
    }

    private void handlePip(ActionEvent event) {
        if (mediaPlayer == null) {
            showErrorAlert("Error", "No media loaded to display in Picture-in-Picture.");
            return;
        }

        Platform.runLater(() -> {
            try {
                visualizerManager.stopVisualizer(); // Pause visualizer to reduce CPU load
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause(); // Pause to stabilize state
                }

                if (!pipStage.isShowing()) {
                    // Transfer MediaPlayer to pipView
                    mediaView.setMediaPlayer(null); // Clear main view first
                    pipView.setMediaPlayer(mediaPlayer);
                    if (isFullScreen) {
                        exitFullScreen();
                    }
                    // Ensure video renders
                    pipView.setFitWidth(321); // Slightly adjust size
                    pipView.setFitWidth(320);
                    pipStage.show();
                    btnPip.setStyle("-fx-background-color: #4CAF50;");
                } else {
                    // Transfer MediaPlayer back to mediaView
                    pipView.setMediaPlayer(null); // Clear pip view first
                    mediaView.setMediaPlayer(mediaPlayer);
                    pipStage.hide();
                    btnPip.setStyle("-fx-base: #444;");
                    // Restart visualizer if needed
                    if (isPlaying && thumbnailManager.isAudioFile(filteredMediaFiles.get(currentMediaIndex))) {
                        visualizerManager.setVisualizerActive(true);
                        visualizerManager.setPlaying(true);
                        visualizerPane.setVisible(true);
                        visualizerManager.startVisualizer();
                    }
                }

                // Resume playback if it was playing
                if (isPlaying) {
                    mediaPlayer.play();
                }
            } catch (Exception e) {
                showErrorAlert("PiP Error", "Failed to toggle Picture-in-Picture: " + e.getMessage());
            }
        });
    }

    private void handleMute() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getVolume() > 0) {
                lastVolume = mediaPlayer.getVolume();
                mediaPlayer.setVolume(0);
                volumeSlider.setValue(0);
            } else {
                mediaPlayer.setVolume(lastVolume);
                volumeSlider.setValue(lastVolume * 100);
            }
        }
    }

    private void adjustVolume(double delta) {
        if (mediaPlayer != null) {
            double newVolume = volumeSlider.getValue() + delta;
            newVolume = Math.max(0, Math.min(100, newVolume));
            volumeSlider.setValue(newVolume);
            mediaPlayer.setVolume(newVolume / 100);
        }
    }

    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (borderPane.getScene().getFocusOwner() instanceof TextField) {
            return;
        }

        switch (event.getCode()) {
            case SPACE:
                if (isPlaying) handlePause(null);
                else handlePlay(null);
                event.consume();
                break;
            case F:
                handleFullScreen(null);
                event.consume();
                break;
            case RIGHT:
                if (mediaPlayer != null) {
                    mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5)));
                }
                event.consume();
                break;
            case LEFT:
                if (mediaPlayer != null) {
                    mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5)));
                }
                event.consume();
                break;
            case UP:
                adjustVolume(5);
                event.consume();
                break;
            case DOWN:
                adjustVolume(-5);
                event.consume();
                break;
            case M:
                handleMute();
                event.consume();
                break;
            case P:
                handlePip(null);
                event.consume();
                break;
            case N:
                handleNext(null);
                event.consume();
                break;
            case B:
                handlePrevious(null);
                event.consume();
                break;
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim().toLowerCase();
        filterMedia(searchText);
    }

    private void filterMedia(String searchText) {
        filterMediaByType(mediaTypeFilter.getValue());
    }

    private void filterMediaByType(String type) {
        filteredMediaFiles.clear();
        filteredFileNames.clear();
        String searchText = searchField.getText().trim().toLowerCase();
        for (int i = 0; i < mediaFiles.size(); i++) {
            File file = mediaFiles.get(i);
            String fileName = fileNames.get(i).toLowerCase();
            boolean matchesType = type.equals("All") ||
                    (type.equals("Audio") && thumbnailManager.isAudioFile(file)) ||
                    (type.equals("Video") && thumbnailManager.isVideoFile(file));
            if (matchesType && (searchText.isEmpty() || fileName.contains(searchText))) {
                filteredMediaFiles.add(file);
                filteredFileNames.add(fileNames.get(i));
            }
        }
        updateMediaViews();
        updateEmptyPlaylistVisibility();
        System.out.println("Filtered items - Type: " + type + ", Search: " + searchText + ", Resulting items: " + filteredFileNames.size());
    }

    private void updateEmptyPlaylistVisibility() {
        boolean isEmpty = filteredFileNames.isEmpty();
        System.out.println("Updating empty playlist visibility. IsEmpty: " + isEmpty + ", isListView: " + isListView);
        emptyPlaylistPane.setVisible(isEmpty);
        emptyPlaylistPane.setManaged(isEmpty);
        mediaScrollPane.setVisible(!isEmpty);
        mediaScrollPane.setManaged(!isEmpty);

        if (!isEmpty) {
            mediaListView.setVisible(isListView);
            mediaListView.setManaged(isListView);
            mediaGridView.setVisible(!isListView);
            mediaGridView.setManaged(!isListView);
            if (isListView) {
                Platform.runLater(() -> {
                    mediaListView.setItems(null); // Clear to force redraw
                    mediaListView.setItems(filteredFileNames); // Rebind items
                    mediaListView.refresh();
                    mediaListView.requestLayout();
                    mediaScrollPane.requestLayout();
                    borderPane.requestLayout();
                    System.out.println("ListView refreshed in visibility check. Items: " + filteredFileNames.size() +
                            ", ScrollPane Width: " + mediaScrollPane.getWidth() +
                            ", ScrollPane Height: " + mediaScrollPane.getHeight());
                });
            }
        } else {
            mediaListView.setVisible(false);
            mediaListView.setManaged(false);
            mediaGridView.setVisible(false);
            mediaGridView.setManaged(false);
        }
    }

    public void updateMediaViews() {
        System.out.println("Updating media views...");
        Platform.runLater(() -> {
            mediaListView.setItems(null); // Clear to force redraw
            mediaListView.setItems(filteredFileNames); // Rebind to ensure updates
            mediaListView.refresh();
            mediaListView.requestLayout();
            System.out.println("updateMediaViews - ListView items: " + filteredFileNames.size());
            if (!isListView) {
                populateGridView();
            }
            mediaScrollPane.requestLayout();
            updateEmptyPlaylistVisibility();
        });
    }

    private void loadMediaFiles(File folder) {
        thumbnailManager.getThumbnailExecutor().shutdownNow();
        thumbnailManager.getThumbnailExecutor();
        thumbnailManager.getThumbnailImageViews().clear();

        mediaFiles.clear();
        fileNames.clear();
        filteredMediaFiles.clear();
        filteredFileNames.clear();

        File[] files = folder.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return name.endsWith(".mp3") || name.endsWith(".mp4") || name.endsWith(".wav") ||
                    name.endsWith(".m4a") || name.endsWith(".flac") || name.endsWith(".aac") ||
                    name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
        });

        if (files != null) {
            for (File file : files) {
                mediaFiles.add(file);
                fileNames.add(file.getName());
                filteredMediaFiles.add(file);
                filteredFileNames.add(file.getName());
            }

            System.out.println("Loaded " + mediaFiles.size() + " media files.");
            if (!filteredMediaFiles.isEmpty()) {
                currentMediaIndex = 0;
                currentFileLabel.setText(filteredFileNames.get(0));
                loadMediaWithoutPlaying(currentMediaIndex);
                btnPlay.setDisable(false);
                btnPrevious.setDisable(false);
                btnNext.setDisable(false);
                btnAddToPlaylist.setDisable(false);
                Platform.runLater(() -> {
                    updateMediaViews();
                    mediaListView.setItems(null); // Clear to force redraw
                    mediaListView.setItems(filteredFileNames); // Rebind
                    mediaListView.refresh();
                    mediaListView.requestLayout();
                    mediaScrollPane.requestLayout();
                    borderPane.requestLayout();
                });
            } else {
                Platform.runLater(this::updateEmptyPlaylistVisibility);
            }
        } else {
            System.out.println("No files found in folder: " + folder.getAbsolutePath());
            Platform.runLater(this::updateEmptyPlaylistVisibility);
        }
    }

    void loadMediaWithoutPlaying(int index) {
        if (index < 0 || index >= filteredMediaFiles.size()) {
            showErrorAlert("Error", "Invalid media index.");
            return;
        }

        File file = filteredMediaFiles.get(index);
        loadingIndicator.setVisible(true);

        // Pause current playback and visualizer
        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                visualizerManager.stopVisualizer();
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing MediaPlayer in loadMediaWithoutPlaying: " + e.getMessage());
            }
            mediaPlayer = null;
        }

        Platform.runLater(() -> {
            try {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                setupMediaPlayer(file);

                // Assign MediaPlayer to the appropriate view
                if (pipStage.isShowing()) {
                    pipView.setMediaPlayer(mediaPlayer);
                    mediaView.setMediaPlayer(null);
                    // Force re-render
                    pipView.setFitWidth(321);
                    pipView.setFitWidth(320);
                } else {
                    mediaView.setMediaPlayer(mediaPlayer);
                    pipView.setMediaPlayer(null);
                }

                equalizerManager.setMediaPlayer(mediaPlayer);
                visualizerManager.setMediaPlayer(mediaPlayer);
                progressSlider.setValue(0);
                updateTimeLabels();
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                mediaPlayer.setRate(speedComboBox.getValue());
                hideDefaultImage();
            } catch (MediaException e) {
                handleMediaLoadError(file, e);
            } finally {
                loadingIndicator.setVisible(false);
            }
        });
    }

    private void loadAndPlayMedia(int index) {
        loadMediaWithoutPlaying(index);
        if (mediaPlayer != null) {
            handlePlay(null);
        }
    }

    private void populateGridView() {
        mediaGridView.getChildren().clear();
        mediaGridView.getRowConstraints().clear();
        mediaGridView.getColumnConstraints().clear();

        if (!filteredMediaFiles.isEmpty()) {
            double availableWidth = mediaScrollPane.getWidth() - 20;
            int columns = Math.max(1, (int)(availableWidth / (150 + 20)));

            for (int i = 0; i < columns; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(100.0 / columns);
                mediaGridView.getColumnConstraints().add(col);
            }

            for (int i = 0; i < filteredFileNames.size(); i++) {
                String fileName = filteredFileNames.get(i);
                File file = filteredMediaFiles.get(i);
                int row = i / columns;
                int col = i % columns;

                VBox cell = createMediaCell(file, fileName);
                mediaGridView.add(cell, col, row);

                while (mediaGridView.getRowConstraints().size() <= row) {
                    RowConstraints rowConst = new RowConstraints();
                    rowConst.setVgrow(Priority.ALWAYS);
                    mediaGridView.getRowConstraints().add(rowConst);
                }
            }
        }
        updateEmptyPlaylistVisibility();
    }

    private void adjustMediaViewSize(Media media) {
        if (media == null || mediaView == null) {
            mediaView.setFitWidth(0);
            mediaView.setFitHeight(0);
            return;
        }

        double containerWidth = videoContainer.getWidth();
        double containerHeight = videoContainer.getHeight();

        double mediaWidth = media.getWidth();
        double mediaHeight = media.getHeight();

        if (mediaWidth <= 0 || mediaHeight <= 0) {
            mediaWidth = containerWidth;
            mediaHeight = containerHeight;
        }

        double aspectRatio = mediaWidth / mediaHeight;
        double containerAspectRatio = containerWidth / containerHeight;

        double fitWidth, fitHeight;
        if (aspectRatio > containerAspectRatio) {
            fitWidth = containerWidth;
            fitHeight = containerWidth / aspectRatio;
        } else {
            fitHeight = containerHeight;
            fitWidth = containerHeight * aspectRatio;
        }

        mediaView.setFitWidth(fitWidth);
        mediaView.setFitHeight(fitHeight);

        videoContainer.setAlignment(Pos.CENTER);
    }

    private void updateTimeLabels() {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getTotalDuration();
            currentTimeLabel.setText(formatDuration(current));
            totalTimeLabel.setText(formatDuration(total));
        } else {
            currentTimeLabel.setText("0:00");
            totalTimeLabel.setText("0:00");
        }
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.equals(Duration.UNKNOWN)) return "0:00";
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void handleMediaLoadError(File file, MediaException error) {
        String message = "Could not load media file: " + file.getName() + "\nError: " + (error != null ? error.getMessage() : "Unknown error");
        showErrorAlert("Media Error", message);
        if (filteredMediaFiles.size() > 1) {
            filteredMediaFiles.remove(file);
            filteredFileNames.remove(file.getName());
            currentMediaIndex = Math.min(currentMediaIndex, filteredMediaFiles.size() - 1);
            updateMediaViews();
            if (!filteredMediaFiles.isEmpty()) {
                loadAndPlayMedia(currentMediaIndex);
            } else {
                handleStop(null);
            }
        } else {
            handleStop(null);
        }
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Button getBtnSelectFolder1() {
        return btnSelectFolder1;
    }

    public void setBtnSelectFolder1(Button btnSelectFolder1) {
        this.btnSelectFolder1 = btnSelectFolder1;
    }
}