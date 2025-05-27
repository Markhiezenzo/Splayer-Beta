package com.example.splayerbeta;

import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
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
    private double[] visualizerData = new double[20]; // Increased for finer resolution
    private double[] targetHeights = new double[20]; // For smooth interpolation
    private List<Rectangle> visualizerBars = new ArrayList<>();
    private boolean isPlaying;
    private MediaPlayer mediaPlayer;
    private final int barCount = 20;
    private final double barWidthFactor = 0.025; // Reduced for smaller bars
    private final double barSpacingFactor = 0.005; // Reduced for tighter spacing

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
        visualizerPane.setVisible(isVisualizerActive);
    }

    private void setupVisualizer() {
        visualizerPane.setVisible(false);
        // Bind pane size listener for responsive bars
        visualizerPane.widthProperty().addListener((obs, oldVal, newVal) -> updateBarLayout());
        visualizerPane.heightProperty().addListener((obs, oldVal, newVal) -> updateBarLayout());
        // Set initial height constraint to align with media player controls
        visualizerPane.setPrefHeight(50); // Adjust this value based on media player height
        visualizerPane.setTranslateY(-20); // Move up to align with controls
    }

    private void updateBarLayout() {
        double paneWidth = visualizerPane.getWidth();
        double barWidth = paneWidth * barWidthFactor;
        double spacing = paneWidth * barSpacingFactor;
        double totalWidth = barCount * barWidth + (barCount - 1) * spacing;
        double startX = -totalWidth / 2;

        for (int i = 0; i < visualizerBars.size(); i++) {
            Rectangle bar = visualizerBars.get(i);
            bar.setWidth(barWidth);
            bar.setTranslateX(startX + i * (barWidth + spacing));
        }
    }

    public void initVisualizerBars() {
        visualizerPane.getChildren().clear();
        visualizerBars.clear();
        for (int i = 0; i < barCount; i++) {
            Rectangle bar = new Rectangle(8, 2, Color.WHITE);
            bar.setArcWidth(3);
            bar.setArcHeight(3);
            // Add subtle glow effect
            DropShadow glow = new DropShadow(10, Color.rgb(255, 255, 255, 0.8));
            bar.setEffect(glow);
            visualizerBars.add(bar);
            visualizerPane.getChildren().add(bar);
        }
        updateBarLayout();
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
                        int index = i * (magnitudes.length / visualizerData.length);
                        if (index < magnitudes.length) {
                            // Normalize and scale magnitudes (-60 to 0 dB) to 0-100 range for smaller bars
                            double magnitude = Math.max(0, magnitudes[index] + 60) * 1.66;
                            targetHeights[i] = Math.min(magnitude, 100); // Cap at 100
                        } else {
                            targetHeights[i] = 4; // Minimum height
                        }
                    }
                }
            });

            // Schedule visual updates at 60 FPS (16.67ms)
            visualizerExecutor.scheduleAtFixedRate(() -> {
                if (mediaPlayer != null && isPlaying && isVisualizerActive) {
                    Platform.runLater(() -> {
                        double paneHeight = visualizerPane.getHeight();
                        for (int i = 0; i < visualizerBars.size(); i++) {
                            Rectangle bar = visualizerBars.get(i);
                            // Interpolate for smooth height transitions
                            visualizerData[i] += (targetHeights[i] - visualizerData[i]) * 0.2;
                            double height = Math.max(2, visualizerData[i] * (paneHeight / 200));
                            bar.setHeight(height);
                            bar.setTranslateY(-height / 40); // Center vertically
                            bar.setFill(Color.WHITE); // Pure white bars
                        }
                    });
                }
            }, 0, 16, TimeUnit.MILLISECONDS); // 60 FPS
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
            mediaPlayer.setAudioSpectrumListener(null);
        }
        for (Rectangle bar : visualizerBars) {
            bar.setHeight(2);
            bar.setTranslateY(-1); // Reset position
        }
        visualizerPane.setVisible(false);
    }
}