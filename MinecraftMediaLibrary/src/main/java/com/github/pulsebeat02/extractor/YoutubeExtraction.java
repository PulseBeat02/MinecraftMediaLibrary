package com.github.pulsebeat02.extractor;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.pulsebeat02.logger.Logger;
import com.github.pulsebeat02.utility.ExtractorUtilities;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.io.IOException;

public class YoutubeExtraction implements AbstractVideoExtractor {

    private final String url;
    private final String directory;
    private VideoDetails details;
    private File video;
    private File audio;

    public YoutubeExtraction(@NotNull final String url, @NotNull final String directory) {
        this.url = url;
        this.directory = directory;
    }

    @Override
    public File downloadVideo() {
        onVideoDownload();
        File videoFile = null;
        final YoutubeDownloader downloader = new YoutubeDownloader();
        final String ID = ExtractorUtilities.getVideoID(url);
        Logger.info("Downloading Video at URL (" + url + ")");
        if (ID != null) {
            try {
                final YoutubeVideo video = downloader.getVideo(ID);
                details = video.details();
                videoFile = video.download(video.videoWithAudioFormats().get(0), new File(directory),
                        "video",
                        true);
                Logger.info("Successfully Downloaded Video at URL: (" + url + ")");
            } catch (IOException | YoutubeException e) {
                Logger.info("Could not Download Video at URL!: (" + url + ")");
                e.printStackTrace();
            }
        }
        return videoFile;
    }

    @Override
    public File extractAudio() {
        if (video == null) {
            downloadVideo();
        }
        onAudioExtraction();
        Logger.info("Extracting Audio from Video File (" + video.getAbsolutePath() + ")");
        File sound = new File(directory + "/audio.ogg");
        AudioAttributes attributes = new AudioAttributes();
        attributes.setCodec("libvorbis");
        attributes.setBitRate(160000);
        attributes.setChannels(2);
        attributes.setSamplingRate(44100);
        attributes.setVolume(48);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("ogg");
        attrs.setAudioAttributes(attributes);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(new MultimediaObject(video), sound, attrs);
            Logger.info("Successfully Extracted Audio from Video File! (Target: " + audio.getAbsolutePath() + ")");
        } catch (EncoderException e) {
            Logger.error("Couldn't Extract Audio from Video File! (Video: " + video.getAbsolutePath() + ")");
            e.printStackTrace();
        }
        return sound;
    }

    @Override
    public void onVideoDownload() {
    }

    @Override
    public void onAudioExtraction() {
    }

    public String getDirectory() {
        return directory;
    }

    public VideoDetails getDetails() {
        return details;
    }

    public File getVideo() {
        return video;
    }

    public File getAudio() {
        return audio;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return details.author();
    }

    public String getVideoTitle() {
        return details.title();
    }

    public String getVideoDescription() {
        return details.description();
    }

    public String getVideoId() {
        return details.videoId();
    }

    public int getVideoRating() {
        return details.averageRating();
    }

    public long getViewerCount() {
        return details.viewCount();
    }

    public boolean isLive() {
        return details.isLive();
    }

    public boolean isLiveContent() {
        return details.isLiveContent();
    }

}