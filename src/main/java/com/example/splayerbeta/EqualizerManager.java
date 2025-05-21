package com.example.splayerbeta;

import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

public class EqualizerManager {
    private MediaPlayer mediaPlayer;
    private AudioEqualizer equalizer;
    private List<Slider> equalizerSliders = new ArrayList<>();
    private List<Label> equalizerLabels = new ArrayList<>();
    private HBox equalizerBox;
    private ComboBox<String> presetComboBox;

    public EqualizerManager(HBox equalizerBox, ComboBox<String> presetComboBox) {
        this.equalizerBox = equalizerBox;
        this.presetComboBox = presetComboBox;
        setupEqualizer();
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public List<Slider> getEqualizerSliders() {
        return equalizerSliders;
    }

    public List<Label> getEqualizerLabels() {
        return equalizerLabels;
    }

    private void setupEqualizer() {
        equalizerBox.setSpacing(5);
        equalizerBox.setAlignment(Pos.CENTER);
        String[] frequencies = {"60Hz", "230Hz", "910Hz", "4kHz", "14kHz"};
        for (int i = 0; i < frequencies.length; i++) {
            VBox bandBox = new VBox(5);
            bandBox.setAlignment(Pos.CENTER);
            Label freqLabel = new Label(frequencies[i]);
            freqLabel.setStyle("-fx-text-fill: white;");
            equalizerLabels.add(freqLabel);
            Slider bandSlider = new Slider(-24, 12, 0);
            bandSlider.setOrientation(javafx.geometry.Orientation.VERTICAL);
            bandSlider.setPrefHeight(100);
            bandSlider.setStyle("-fx-base: #555;");
            equalizerSliders.add(bandSlider);
            bandSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateEqualizer());
            bandBox.getChildren().addAll(bandSlider, freqLabel);
            equalizerBox.getChildren().add(bandBox);
        }
        presetComboBox.getItems().addAll("Flat", "Pop", "Rock", "Jazz", "Classical", "Bass Boost");
        presetComboBox.setValue("Flat");
        presetComboBox.setOnAction(e -> applyEqualizerPreset(presetComboBox.getValue()));
    }

    private void updateEqualizer() {
        if (mediaPlayer != null && mediaPlayer.getAudioEqualizer() != null) {
            equalizer = mediaPlayer.getAudioEqualizer();
            equalizer.setEnabled(true);
            for (int i = 0; i < equalizerSliders.size(); i++) {
                if (equalizer.getBands().size() > i) {
                    equalizer.getBands().get(i).setGain(equalizerSliders.get(i).getValue());
                }
            }
        }
    }

    private void applyEqualizerPreset(String preset) {
        switch (preset) {
            case "Flat": setEqualizerValues(0, 0, 0, 0, 0); break;
            case "Pop": setEqualizerValues(4, 2, -2, 3, 4); break;
            case "Rock": setEqualizerValues(6, 3, -3, 2, 5); break;
            case "Jazz": setEqualizerValues(3, 4, 1, 2, 3); break;
            case "Classical": setEqualizerValues(5, 4, 3, 2, 5); break;
            case "Bass Boost": setEqualizerValues(8, 5, 0, 0, 2); break;
        }
    }

    private void setEqualizerValues(double... values) {
        for (int i = 0; i < Math.min(values.length, equalizerSliders.size()); i++) {
            equalizerSliders.get(i).setValue(values[i]);
        }
    }
}