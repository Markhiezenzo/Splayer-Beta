package com.example.splayerbeta;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class ThumbnailManager {
    // Thumbnail settings
    private static final int LIST_THUMBNAIL_SIZE = 50;
    private static final int GRID_THUMBNAIL_WIDTH = 150;
    private static final int GRID_THUMBNAIL_HEIGHT = 100;
    private static final int MAX_THUMBNAIL_CACHE = 100;
    private static final long MAX_CACHE_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB
    private File thumbnailCacheDir;
    private Map<String, Image> thumbnailCache = new LinkedHashMap<>(MAX_THUMBNAIL_CACHE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            return size() > MAX_THUMBNAIL_CACHE;
        }
    };
    private ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(Math.min(2, Runtime.getRuntime().availableProcessors()));
    private Map<File, ImageView> thumbnailImageViews = new HashMap<>();

    public ThumbnailManager() {
        thumbnailCacheDir = new File(System.getProperty("user.home"), ".splayer/thumbnails");
        if (!thumbnailCacheDir.exists()) {
            thumbnailCacheDir.mkdirs();
        }
        cleanThumbnailCache();
    }

    public ExecutorService getThumbnailExecutor() {
        return thumbnailExecutor;
    }

    public Map<File, ImageView> getThumbnailImageViews() {
        return thumbnailImageViews;
    }

    public void cleanThumbnailCache() {
        File[] cacheFiles = thumbnailCacheDir.listFiles();
        if (cacheFiles == null) return;

        Arrays.sort(cacheFiles, Comparator.comparingLong(File::lastModified));

        long totalSize = 0;
        for (File file : cacheFiles) {
            totalSize += file.length();
        }

        int index = 0;
        while (totalSize > MAX_CACHE_SIZE_BYTES && index < cacheFiles.length) {
            File file = cacheFiles[index];
            totalSize -= file.length();
            file.delete();
            index++;
        }
    }

    public String generateThumbnailFilename(File file, int width, int height) {
        try {
            String filePath = file.getAbsolutePath();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = filePath + "_" + width + "x" + height;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString() + ".rgba";
        } catch (Exception e) {
            System.err.println("Error generating thumbnail filename: " + e.getMessage());
            return file.getName().replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + width + "x" + height + ".rgba";
        }
    }

    public void saveImageToFile(Image image, File file) {
        try (FileOutputStream fos = new FileOutputStream(file);
             FileChannel channel = fos.getChannel()) {
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            ByteBuffer header = ByteBuffer.allocate(8);
            header.putInt(width);
            header.putInt(height);
            header.flip();
            channel.write(header);

            byte[] pixelData = new byte[width * height * 4];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = image.getPixelReader().getColor(x, y);
                    int index = (y * width + x) * 4;
                    pixelData[index] = (byte)(color.getRed() * 255);
                    pixelData[index + 1] = (byte)(color.getGreen() * 255);
                    pixelData[index + 2] = (byte)(color.getBlue() * 255);
                    pixelData[index + 3] = (byte)(color.getOpacity() * 255);
                }
            }
            channel.write(ByteBuffer.wrap(pixelData));
        } catch (Exception e) {
            System.err.println("Error saving thumbnail to disk: " + e.getMessage());
        }
    }

    public Image loadImageFromFile(File file, int width, int height) {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel channel = fis.getChannel()) {
            ByteBuffer header = ByteBuffer.allocate(8);
            channel.read(header);
            header.flip();
            int storedWidth = header.getInt();
            int storedHeight = header.getInt();

            if (storedWidth != width || storedHeight != height) {
                return null;
            }

            int pixelBytes = width * height * 4;
            ByteBuffer pixelBuffer = ByteBuffer.allocate(pixelBytes);
            channel.read(pixelBuffer);
            pixelBuffer.flip();
            byte[] pixelData = new byte[pixelBytes];
            pixelBuffer.get(pixelData);

            WritableImage image = new WritableImage(width, height);
            PixelWriter writer = image.getPixelWriter();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = (y * width + x) * 4;
                    double r = (pixelData[index] & 0xFF) / 255.0;
                    double g = (pixelData[index + 1] & 0xFF) / 255.0;
                    double b = (pixelData[index + 2] & 0xFF) / 255.0;
                    double a = (pixelData[index + 3] & 0xFF) / 255.0;
                    writer.setColor(x, y, new Color(r, g, b, a));
                }
            }
            return image;
        } catch (Exception e) {
            System.err.println("Error loading thumbnail from disk: " + e.getMessage());
            return null;
        }
    }

    public void loadThumbnail(File file, ImageView imageView, int width, int height) {
        String filePath = file.getAbsolutePath();
        String cacheKey = filePath + "_" + width + "x" + height;
        File thumbnailFile = new File(thumbnailCacheDir, generateThumbnailFilename(file, width, height));

        if (thumbnailCache.containsKey(cacheKey)) {
            Image cachedImage = thumbnailCache.get(cacheKey);
            if (cachedImage.getWidth() == width && cachedImage.getHeight() == height) {
                imageView.setImage(cachedImage);
                return;
            }
        }

        if (thumbnailFile.exists()) {
            Image image = loadImageFromFile(thumbnailFile, width, height);
            if (image != null && !image.isError()) {
                thumbnailCache.put(cacheKey, image);
                Platform.runLater(() -> imageView.setImage(image));
                return;
            }
        }

        imageView.setImage(createPlaceholderImage(width, height));

        Task<Image> thumbnailTask = new Task<>() {
            @Override
            protected Image call() {
                try {
                    if (!file.exists() || !file.canRead()) {
                        return createFileTypeIcon(file, width, height);
                    }
                    if (isImageFile(file)) {
                        return new Image(file.toURI().toString(), width, height, true, true, true);
                    } else if (isVideoFile(file)) {
                        return generateVideoThumbnail(file, width, height);
                    } else if (isAudioFile(file)) {
                        return generateAudioThumbnail(file, width, height);
                    } else {
                        return createFileTypeIcon(file, width, height);
                    }
                } catch (Exception e) {
                    System.err.println("Error generating thumbnail for " + file.getName() + ": " + e.getMessage());
                    return createFileTypeIcon(file, width, height);
                }
            }

            @Override
            protected void succeeded() {
                Image thumbnail = getValue();
                if (thumbnail != null && !thumbnail.isError()) {
                    thumbnailCache.put(cacheKey, thumbnail);
                    saveImageToFile(thumbnail, thumbnailFile);
                    Platform.runLater(() -> imageView.setImage(thumbnail));
                } else {
                    Platform.runLater(() -> imageView.setImage(createFileTypeIcon(file, width, height)));
                }
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> imageView.setImage(createFileTypeIcon(file, width, height)));
            }
        };

        thumbnailExecutor.submit(thumbnailTask);
    }

    public Image generateVideoThumbnail(File videoFile, int width, int height) {
        try {
            String uri = videoFile.toURI().toString();
            Media media = new Media(uri);
            MediaPlayer tempMediaPlayer = new MediaPlayer(media);
            MediaView tempMediaView = new MediaView(tempMediaPlayer);
            tempMediaView.setFitWidth(width);
            tempMediaView.setFitHeight(height);
            tempMediaView.setPreserveRatio(true);

            CompletableFuture<Image> future = new CompletableFuture<>();

            tempMediaPlayer.setOnReady(() -> {
                try {
                    double totalDurationSeconds = media.getDuration().toSeconds();
                    double seekTime = Math.max(10.0, totalDurationSeconds * 0.25);
                    tempMediaPlayer.seek(Duration.seconds(seekTime));
                    WritableImage snapshot = new WritableImage(width, height);
                    tempMediaView.snapshot(null, snapshot);
                    tempMediaPlayer.stop();
                    tempMediaPlayer.dispose();
                    boolean isBlack = true;
                    for (int x = 0; x < width; x += 10) {
                        for (int y = 0; y < height; y += 10) {
                            if (snapshot.getPixelReader().getColor(x, y).getOpacity() > 0) {
                                isBlack = false;
                                break;
                            }
                        }
                        if (!isBlack) break;
                    }
                    if (isBlack) {
                        future.complete(createFallbackVideoThumbnail(width, height));
                    } else {
                        future.complete(snapshot);
                    }
                } catch (Exception e) {
                    tempMediaPlayer.stop();
                    tempMediaPlayer.dispose();
                    future.complete(createFallbackVideoThumbnail(width, height));
                }
            });

            tempMediaPlayer.setOnError(() -> {
                tempMediaPlayer.stop();
                tempMediaPlayer.dispose();
                future.complete(createFallbackVideoThumbnail(width, height));
            });

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!future.isDone()) {
                        tempMediaPlayer.stop();
                        tempMediaPlayer.dispose();
                        future.complete(createFallbackVideoThumbnail(width, height));
                    }
                }
            }, 5000);

            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return createFallbackVideoThumbnail(width, height);
        }
    }

    public Image createFallbackVideoThumbnail(int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, Color.DARKSLATEBLUE);
            }
        }
        int playSize = Math.min(width, height) / 3;
        int startX = (width - playSize) / 2;
        int startY = (height - playSize) / 2;
        for (int y = 0; y < playSize; y++) {
            for (int x = 0; x < y; x++) {
                writer.setColor(startX + x, startY + y, Color.YELLOW);
            }
        }
        return image;
    }

    public Image generateAudioThumbnail(File audioFile, int width, int height) {
        String fileName = audioFile.getName().toLowerCase();
        if (fileName.endsWith(".flac")) {
            return createWaveformThumbnail(audioFile, width, height);
        }

        try {
            Media media = new Media(audioFile.toURI().toString());
            CountDownLatch latch = new CountDownLatch(1);

            media.getMetadata().addListener((javafx.collections.MapChangeListener.Change<? extends String, ? extends Object> change) -> {
                if (change.wasAdded() && "image".equals(change.getKey())) {
                    latch.countDown();
                }
            });

            latch.await(5, TimeUnit.SECONDS);

            Object imageObj = media.getMetadata().get("image");
            if (imageObj instanceof Image) {
                return (Image) imageObj;
            } else {
                return createWaveformThumbnail(audioFile, width, height);
            }
        } catch (Exception e) {
            return createWaveformThumbnail(audioFile, width, height);
        }
    }

    public Image createWaveformThumbnail(File audioFile, int width, int height) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, Color.DARKSLATEGRAY);
            }
        }
        Random random = new Random(audioFile.getName().hashCode());
        int centerY = height / 2;
        for (int x = 0; x < width; x++) {
            int amplitude = random.nextInt(height / 4) + 5;
            for (int y = centerY - amplitude; y < centerY + amplitude; y++) {
                if (y >= 0 && y < height) {
                    writer.setColor(x, y, Color.LIGHTBLUE);
                }
            }
        }
        return image;
    }

    public Image createFileTypeIcon(File file, int width, int height) {
        WritableImage icon = new WritableImage(width, height);
        PixelWriter writer = icon.getPixelWriter();
        Color baseColor = isAudioFile(file) ? Color.LIGHTBLUE : isVideoFile(file) ? Color.LIGHTCORAL : isImageFile(file) ? Color.LIGHTGREEN : Color.LIGHTGRAY;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, baseColor.darker());
            }
        }
        return icon;
    }

    public Image createPlaceholderImage(int width, int height) {
        WritableImage placeholder = new WritableImage(width, height);
        PixelWriter writer = placeholder.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, Color.LIGHTGRAY);
            }
        }
        int textSize = Math.min(width, height) / 5;
        int startX = (width - textSize) / 2;
        int startY = (height - textSize) / 2;
        for (int y = 0; y < textSize; y++) {
            for (int x = 0; x < textSize; x++) {
                if (x == y || x + y == textSize - 1) {
                    writer.setColor(startX + x, startY + y, Color.BLACK);
                }
            }
        }
        return placeholder;
    }

    public boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    public boolean isVideoFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv") || name.endsWith(".mov");
    }

    public boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac") || name.endsWith(".m4a");
    }
}