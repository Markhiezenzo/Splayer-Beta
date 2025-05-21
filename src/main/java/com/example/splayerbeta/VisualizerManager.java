package com.example.splayerbeta;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VisualizerManager {
    private StackPane visualizerPane;
    private boolean isVisualizerActive = false;
    private ScheduledExecutorService visualizerExecutor;
    private double[] visualizerData = new double[10];
    private List<Rectangle> visualizerBars = new ArrayList<>();
    private boolean isPlaying;
    private MediaPlayer mediaPlayer;

    public VisualizerManager(StackPane visualizerPane) {
        this.visualizerPane = visualizerPane;
        initVisualizerBars();
        setupVisualizer();
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isVisualizerActive() {
        return isVisualizerActive;
    }

    public void setVisualizerActive(boolean isVisualizerActive) {
        this.isVisualizerActive = isVisualizerActive;
    }

    private void setupVisualizer() {
        visualizerPane.setVisible(false);
    }

    public void initVisualizerBars() {
        visualizerPane.getChildren().clear();
        visualizerBars.clear();
        for (int i = 0; i < 20; i++) {
            Rectangle bar = new Rectangle(8, 2, Color.LIGHTBLUE);
            bar.setArcWidth(5);
            bar.setArcHeight(5);
            bar.setTranslateX((i - 10) * 10);
            visualizerBars.add(bar);
            visualizerPane.getChildren().add(bar);
        }
    }

    public void startVisualizer() {
        if (visualizerExecutor != null) {
            visualizerExecutor.shutdownNow();
        }
        visualizerExecutor = Executors.newSingleThreadScheduledExecutor();

        if (mediaPlayer != null) {
            // Configure audio spectrum listener
            mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {
                if (isPlaying && isVisualizerActive) {
                    // Process spectrum data
                    for (int i = 0; i < visualizerData.length; i++) {
                        // Map magnitudes to visualizerData, scaling for visual effect
                        int index = i * (magnitudes.length / visualizerData.length);
                        if (index < magnitudes.length) {
                            // Magnitudes are in dB (typically -60 to 0), normalize and scale
                            double magnitude = Math.max(0, magnitudes[index] + 60) * 2; // Scale to 0-120 range
                            visualizerData[i] = Math.min(magnitude, 100); // Cap at 100 for bar height
                        } else {
                            visualizerData[i] = 2; // Fallback minimum height
                        }
                    }
                }
            });

            // Schedule visual updates
            visualizerExecutor.scheduleAtFixedRate(() -> {
                if (mediaPlayer != null && isPlaying && isVisualizerActive) {
                    Platform.runLater(() -> {
                        for (int i = 0; i < visualizerBars.size(); i++) {
                            Rectangle bar = visualizerBars.get(i);
                            double height = visualizerData[i % visualizerData.length];
                            bar.setHeight(Math.max(2, height)); // Ensure minimum height
                            // Dynamic color based on height and index
                            bar.setFill(Color.hsb((i * 360.0 / visualizerBars.size() + (System.currentTimeMillis() / 50) % 360),
                                    0.8, Math.min(0.8, height / 100), 0.8));
                        }
                    });
                }
            }, 0, 50, TimeUnit.MILLISECONDS); // Update every 50ms for smoother animation
        }
    }

    public void stopVisualizer() {
        if (visualizerExecutor != null) {
            visualizerExecutor.shutdownNow();
            try {
                visualizerExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            visualizerExecutor = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.setAudioSpectrumListener(null); // Remove listener
        }
        for (Rectangle bar : visualizerBars) {
            bar.setHeight(2); // Reset bars to minimum height
        }
        visualizerPane.setVisible(false);
    }
}