package com.example.splayerbeta;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ThemeController {
    private BorderPane borderPane;
    private ComboBox<String> themeComboBox;
    private VBox topVBox;
    private HBox topHBox;
    private VBox bottomVBox;
    private HBox bottomHBox1;
    private HBox bottomHBox2;
    private VBox leftVBox;
    private StackPane videoContainer;
    private VBox equalizerContainer;
    private HBox equalizerBox;
    private ComboBox<String> presetComboBox;
    private Button btnPlay, btnPause, btnStop, btnPrevious, btnNext, btnRepeat, btnShuffle, btnFullScreen;
    private Button btnPlaylist, btnLyrics, btnAddToPlaylist, btnSavePlaylist, btnLoadPlaylist, btnPip, btnSelectFolder;
    private Button btnEqualizer, btnSearch, btnSleepTimer;
    private ComboBox<String> btnfilelist, mediaTypeFilter;
    private ComboBox<Integer> sleepTimerCombo;
    private Label currentFileLabel, currentTimeLabel, totalTimeLabel, speedLabel, volumeLabel;
    private Label lyricsLabel;
    private TextField searchField;
    private Slider progressSlider, volumeSlider;
    private ComboBox<Double> speedComboBox;
    private Text emptyPlaylistText;

    public ThemeController(BorderPane borderPane, ComboBox<String> themeComboBox, VBox topVBox, HBox topHBox,
                           VBox bottomVBox, HBox bottomHBox1, HBox bottomHBox2, VBox leftVBox,
                           StackPane videoContainer, VBox equalizerContainer, HBox equalizerBox,
                           ComboBox<String> presetComboBox, Button btnPlay, Button btnPause, Button btnStop,
                           Button btnPrevious, Button btnNext, Button btnRepeat, Button btnShuffle,
                           Button btnFullScreen, Button btnPlaylist, Button btnLyrics, Button btnAddToPlaylist,
                           Button btnSavePlaylist, Button btnLoadPlaylist, Button btnPip, Button btnSelectFolder,
                           Button btnEqualizer, Button btnSearch, Button btnSleepTimer,
                           ComboBox<String> btnfilelist, ComboBox<String> mediaTypeFilter,
                           ComboBox<Integer> sleepTimerCombo, Label currentFileLabel, Label currentTimeLabel,
                           Label totalTimeLabel, Label speedLabel, Label volumeLabel, Label lyricsLabel,
                           TextField searchField, Slider progressSlider, Slider volumeSlider, ComboBox<Double> speedComboBox,
                           Text emptyPlaylistText) {
        this.borderPane = borderPane;
        this.themeComboBox = themeComboBox;
        this.topVBox = topVBox;
        this.topHBox = topHBox;
        this.bottomVBox = bottomVBox;
        this.bottomHBox1 = bottomHBox1;
        this.bottomHBox2 = bottomHBox2;
        this.leftVBox = leftVBox;
        this.videoContainer = videoContainer;
        this.equalizerContainer = equalizerContainer;
        this.equalizerBox = equalizerBox;
        this.presetComboBox = presetComboBox;
        this.btnPlay = btnPlay;
        this.btnPause = btnPause;
        this.btnStop = btnStop;
        this.btnPrevious = btnPrevious;
        this.btnNext = btnNext;
        this.btnRepeat = btnRepeat;
        this.btnShuffle = btnShuffle;
        this.btnFullScreen = btnFullScreen;
        this.btnPlaylist = btnPlaylist;
        this.btnLyrics = btnLyrics;
        this.btnAddToPlaylist = btnAddToPlaylist;
        this.btnSavePlaylist = btnSavePlaylist;
        this.btnLoadPlaylist = btnLoadPlaylist;
        this.btnPip = btnPip;
        this.btnSelectFolder = btnSelectFolder;
        this.btnEqualizer = btnEqualizer;
        this.btnSearch = btnSearch;
        this.btnSleepTimer = btnSleepTimer;
        this.btnfilelist = btnfilelist;
        this.mediaTypeFilter = mediaTypeFilter;
        this.sleepTimerCombo = sleepTimerCombo;
        this.currentFileLabel = currentFileLabel;
        this.currentTimeLabel = currentTimeLabel;
        this.totalTimeLabel = totalTimeLabel;
        this.speedLabel = speedLabel;
        this.volumeLabel = volumeLabel;
        this.lyricsLabel = lyricsLabel;
        this.searchField = searchField;
        this.progressSlider = progressSlider;
        this.volumeSlider = volumeSlider;
        this.speedComboBox = speedComboBox;
        this.emptyPlaylistText = emptyPlaylistText;

        setupThemeSelector();
    }

    private void setupThemeSelector() {
        themeComboBox.getItems().addAll("Black", "White", "SkyBlue", "Pinkish", "Hazel");
        themeComboBox.setValue("Black"); // Default theme
        applyTheme("Black"); // Set initial theme
        themeComboBox.setOnAction(event -> applyTheme(themeComboBox.getValue()));
    }

    private void applyTheme(String theme) {
        String backgroundColor, secondaryColor, buttonColor, textColor;

        switch (theme.toLowerCase()) {
            case "black":
                backgroundColor = "#333";
                secondaryColor = "#222";
                buttonColor = "#444";
                textColor = "#FFFFFF"; // White text for black theme
                break;
            case "white":
                backgroundColor = "#FFFFFF";
                secondaryColor = "#F0F0F0";
                buttonColor = "#D3D3D3";
                textColor = "#000000"; // Black text for white theme
                break;
            case "skyblue":
                backgroundColor = "#87CEEB";
                secondaryColor = "#70B8D3";
                buttonColor = "#5AA9C2";
                textColor = "#000000"; // Black text for skyblue theme
                break;
            case "pinkish":
                backgroundColor = "#FFB6C1";
                secondaryColor = "#F89FAB";
                buttonColor = "#F28495";
                textColor = "#000000"; // Black text for pinkish theme
                break;
            case "hazel":
                backgroundColor = "#8B7D6B";
                secondaryColor = "#7A6C5A";
                buttonColor = "#695B49";
                textColor = "#000000"; // Black text for hazel theme
                break;
            default:
                backgroundColor = "#333";
                secondaryColor = "#222";
                buttonColor = "#444";
                textColor = "#FFFFFF"; // Default to white text
                break;
        }

        // Apply background colors to main containers
        borderPane.setStyle("-fx-background-color: " + backgroundColor + ";");
        topVBox.setStyle("-fx-background-color: " + secondaryColor + ";");
        topHBox.setStyle("-fx-background-color: " + secondaryColor + ";");
        bottomVBox.setStyle("-fx-background-color: " + secondaryColor + ";");
        bottomHBox1.setStyle("-fx-background-color: " + secondaryColor + ";");
        bottomHBox2.setStyle("-fx-background-color: " + secondaryColor + ";");
        leftVBox.setStyle("-fx-background-color: " + secondaryColor + ";");
        videoContainer.setStyle("-fx-background-color: " + (theme.equalsIgnoreCase("black") ? "#000" : backgroundColor) + ";");
        equalizerContainer.setStyle("-fx-background-color: " + secondaryColor + ";");
        equalizerBox.setStyle("-fx-background-color: " + secondaryColor + ";");

        // Apply button styles
        String buttonStyle = "-fx-base: " + buttonColor + "; -fx-text-fill: " + textColor + ";";
        btnPlay.setStyle(buttonStyle);
        btnPause.setStyle(buttonStyle);
        btnStop.setStyle(buttonStyle);
        btnPrevious.setStyle(buttonStyle);
        btnNext.setStyle(buttonStyle);
        btnRepeat.setStyle(isButtonActive(btnRepeat) ? "-fx-background-color: #4CAF50; -fx-text-fill: " + textColor + ";" : buttonStyle);
        btnShuffle.setStyle(isButtonActive(btnShuffle) ? "-fx-background-color: #4CAF50; -fx-text-fill: " + textColor + ";" : buttonStyle);
        btnFullScreen.setStyle(buttonStyle);
        btnPlaylist.setStyle(buttonStyle);
        btnLyrics.setStyle(isButtonActive(btnLyrics) ? "-fx-background-color: #4CAF50; -fx-text-fill: " + textColor + ";" : buttonStyle);
        btnAddToPlaylist.setStyle(buttonStyle);
        btnSavePlaylist.setStyle(buttonStyle);
        btnLoadPlaylist.setStyle(buttonStyle);
        btnPip.setStyle(isButtonActive(btnPip) ? "-fx-background-color: #4CAF50; -fx-text-fill: " + textColor + ";" : buttonStyle);
        btnSelectFolder.setStyle(buttonStyle);
        btnEqualizer.setStyle(isButtonActive(btnEqualizer) ? "-fx-background-color: #4CAF50; -fx-text-fill: " + textColor + ";" : buttonStyle);
        btnSearch.setStyle(buttonStyle);
        btnSleepTimer.setStyle(isButtonActive(btnSleepTimer) ? "-fx-background-color: #4CAF50; -fx-text-fill: " + textColor + ";" : buttonStyle);

        // Apply ComboBox styles with black text for all themes
        String controlStyle = "-fx-base: " + buttonColor + "; -fx-text-fill: #000000;";
        btnfilelist.setStyle(controlStyle);
        mediaTypeFilter.setStyle(controlStyle);
        themeComboBox.setStyle(controlStyle);
        sleepTimerCombo.setStyle(controlStyle);
        presetComboBox.setStyle(controlStyle);
        speedComboBox.setStyle(controlStyle);
        progressSlider.setStyle("-fx-base: " + buttonColor + ";");
        volumeSlider.setStyle("-fx-base: " + buttonColor + ";");

        // Apply text styles
        currentFileLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; -fx-font-size: 14;");
        currentTimeLabel.setStyle("-fx-text-fill: " + textColor + ";");
        totalTimeLabel.setStyle("-fx-text-fill: " + textColor + ";");
        speedLabel.setStyle("-fx-text-fill: " + textColor + ";");
        volumeLabel.setStyle("-fx-text-fill: " + textColor + ";");
        lyricsLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14; -fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 10;");
        emptyPlaylistText.setStyle("-fx-fill: " + textColor + ";");
        searchField.setStyle("-fx-background-color: " + buttonColor + "; -fx-text-fill: " + textColor + ";");

        // Update labels within HBoxes (like "Speed:", "Sleep:", "Volume:")
        updateLabelsInHBox(topHBox, textColor);
        updateLabelsInHBox(bottomHBox1, textColor);
        updateLabelsInHBox(bottomHBox2, textColor);
    }

    private boolean isButtonActive(Button button) {
        return button.getStyle().contains("#4CAF50");
    }

    private void updateLabelsInHBox(HBox hbox, String textColor) {
        hbox.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label label = (Label) node;
                // Check if the label is not one of the explicitly managed labels
                boolean isManagedLabel = "currentFileLabel".equals(label.getId()) || "currentTimeLabel".equals(label.getId()) ||
                        "totalTimeLabel".equals(label.getId()) || "speedLabel".equals(label.getId()) ||
                        "volumeLabel".equals(label.getId()) || "lyricsLabel".equals(label.getId());
                if (!isManagedLabel) {
                    label.setStyle("-fx-text-fill: " + textColor + ";");
                }
            }
        });
    }
}