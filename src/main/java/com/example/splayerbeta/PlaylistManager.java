package com.example.splayerbeta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.*;

public class PlaylistManager {
    private ObservableList<File> currentPlaylist = FXCollections.observableArrayList();
    private ObservableList<File> mediaFiles;
    private ObservableList<String> fileNames;
    private ObservableList<File> filteredMediaFiles;
    private ObservableList<String> filteredFileNames;
    private int currentMediaIndex;
    private Button btnSavePlaylist;
    private Button btnAddToPlaylist;
    private MediaPlayerController controller;

    public PlaylistManager(ObservableList<File> mediaFiles, ObservableList<String> fileNames,
                           ObservableList<File> filteredMediaFiles, ObservableList<String> filteredFileNames,
                           int currentMediaIndex, Button btnSavePlaylist, Button btnAddToPlaylist,
                           MediaPlayerController controller) {
        this.mediaFiles = mediaFiles;
        this.fileNames = fileNames;
        this.filteredMediaFiles = filteredMediaFiles;
        this.filteredFileNames = filteredFileNames;
        this.currentMediaIndex = currentMediaIndex;
        this.btnSavePlaylist = btnSavePlaylist;
        this.btnAddToPlaylist = btnAddToPlaylist;
        this.controller = controller;
    }

    public ObservableList<File> getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void setCurrentMediaIndex(int currentMediaIndex) {
        this.currentMediaIndex = currentMediaIndex;
    }

    public void handlePlaylist(ActionEvent event) {
        if (currentPlaylist.isEmpty()) {
            showInfoAlert("Playlist", "No items in current playlist. Add items using the '+' button.");
        } else {
            StringBuilder playlistContent = new StringBuilder("Current Playlist:\n\n");
            for (File file : currentPlaylist) {
                playlistContent.append(file.getName()).append("\n");
            }
            showInfoAlert("Playlist", playlistContent.toString());
        }
    }

    public void handleAddToPlaylist(ActionEvent event) {
        if (!filteredMediaFiles.isEmpty() && currentMediaIndex >= 0 && currentMediaIndex < filteredMediaFiles.size()) {
            File currentFile = filteredMediaFiles.get(currentMediaIndex);
            if (!currentPlaylist.contains(currentFile)) {
                currentPlaylist.add(currentFile);
                btnSavePlaylist.setDisable(false);
                showInfoAlert("Playlist", "Added to playlist: " + currentFile.getName());
            } else {
                showInfoAlert("Playlist", "File already in playlist: " + currentFile.getName());
            }
        }
    }

    public void handleSavePlaylist(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Playlist");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist Files", "*.m3u"));
        File file = fileChooser.showSaveDialog(controller.getMediaView().getScene().getWindow()); // Updated to use getter
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (File mediaFile : currentPlaylist) {
                    writer.write(mediaFile.getAbsolutePath());
                    writer.newLine();
                }
                showInfoAlert("Playlist", "Playlist saved successfully!");
            } catch (IOException e) {
                showErrorAlert("Error", "Could not save playlist: " + e.getMessage());
            }
        }
    }

    public void handleLoadPlaylist(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Playlist");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist Files", "*.m3u"));
        File file = fileChooser.showOpenDialog(controller.getMediaView().getScene().getWindow()); // Updated to use getter
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                currentPlaylist.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    File mediaFile = new File(line.trim());
                    if (mediaFile.exists() && controller.isSupportedMediaFile(mediaFile)) {
                        currentPlaylist.add(mediaFile);
                    }
                }
                if (!currentPlaylist.isEmpty()) {
                    mediaFiles.clear();
                    fileNames.clear();
                    mediaFiles.addAll(currentPlaylist);
                    fileNames.addAll(currentPlaylist.stream().map(File::getName).toList());
                    filteredMediaFiles.clear();
                    filteredFileNames.clear();
                    filteredMediaFiles.addAll(mediaFiles);
                    filteredFileNames.addAll(fileNames);
                    controller.updateMediaViews();
                    currentMediaIndex = 0;
                    controller.getCurrentFileLabel().setText(fileNames.get(0)); // Updated to use getter
                    controller.loadMediaWithoutPlaying(currentMediaIndex);
                }
                showInfoAlert("Playlist", "Playlist loaded successfully!");
                btnSavePlaylist.setDisable(currentPlaylist.isEmpty());
            } catch (IOException e) {
                showErrorAlert("Error", "Could not load playlist: " + e.getMessage());
            }
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
}