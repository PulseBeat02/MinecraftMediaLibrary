/*............................................................................................
. Copyright © 2021 Brandon Li                                                               .
.                                                                                           .
. Permission is hereby granted, free of charge, to any person obtaining a copy of this      .
. software and associated documentation files (the “Software”), to deal in the Software     .
. without restriction, including without limitation the rights to use, copy, modify, merge, .
. publish, distribute, sublicense, and/or sell copies of the Software, and to permit        .
. persons to whom the Software is furnished to do so, subject to the following conditions:  .
.                                                                                           .
. The above copyright notice and this permission notice shall be included in all copies     .
. or substantial portions of the Software.                                                  .
.                                                                                           .
. THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,                           .
.  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                       .
.   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                                   .
.   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS                     .
.   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN                      .
.   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                       .
.   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE                        .
.   SOFTWARE.                                                                               .
............................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.ffmpeg;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.dependency.task.CommandTask;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.DependencyUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.RuntimeUtilities;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * A special handling class specific to the JAVE2 library. JAVE2 is an extension to * "JAVE", an
 * audio transcribing framework which is useful for converting to OGG Vorbis files * and adjusting
 * audio quality to necessary settings. Because JAVE2 uses modules which are * operating system
 * dependent, this special class will handle the correct installation based on * the environment of
 * the library. We use these separate modules instead of using one whole combined one to save some
 * space for the user. We then end up overriding the get path method to point it to the correct
 * ffmpeg location.
 *
 * <p>JAVE2 Github: https://github.com/a-schild/jave2
 */
public class FFmpegDependencyInstallation {

  private static Path FFMPEG_PATH;

  private final Path ffmpegFolder;
  private Path file;

  /**
   * Instantiates a new FFmpegDependencyInstallation
   *
   * @param library library
   */
  public FFmpegDependencyInstallation(@NotNull final MediaLibrary library) {
    this(library.getDependenciesFolder());
  }

  /**
   * Instantiates a new FFmpegDependencyInstallation
   *
   * @param dependency directory path
   */
  public FFmpegDependencyInstallation(@NotNull final Path dependency) {
    ffmpegFolder = Paths.get(String.format("%s/ffmpeg/", dependency.normalize()));
    try {
      Files.createDirectories(ffmpegFolder);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the path of the executable ffmpeg binary.
   *
   * @return the path of the ffmpeg binary
   */
  public static Path getFFmpegPath() {
    return FFMPEG_PATH;
  }

  /**
   * Sets the path of the executable ffmpeg binary.
   *
   * @param ffmpegPath the path of the ffmpeg binary
   */
  public static void setFfmpegPath(final Path ffmpegPath) {
    FFMPEG_PATH = ffmpegPath;
  }

  /** Starts installation of FFmpeg Resource */
  public void start() {
    try {
      file = downloadFFmpeg();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    FFMPEG_PATH = file.toAbsolutePath();
    Logger.info(String.format("FFMPEG Path: %s", FFMPEG_PATH));
  }

  /**
   * Downloads the proper FFMPEG binary file.
   *
   * @return the FFMPEG file
   * @throws IOException if an issue occurred during downloading
   */
  @NotNull
  private Path downloadFFmpeg() throws IOException {
    Path file = searchFFmpeg(ffmpegFolder);
    if (file != null) {
      return file;
    }
    final String fileUrl = RuntimeUtilities.getFFmpegUrl();
    file = ffmpegFolder.resolve(FilenameUtils.getName(new URL(fileUrl).getPath()));
    if (Files.notExists(file)) {
      Files.createFile(file);
    }
    DependencyUtilities.downloadFile(file, fileUrl);
    if (RuntimeUtilities.isMac() || RuntimeUtilities.isLinux()) {
      // Change permissions so JAVE2 can access the file
      new CommandTask("chmod", "-R", "777", file.toAbsolutePath().toString()).run();
    }
    return file;
  }

  /**
   * Searches for existing FFMPEG dependency file.
   *
   * @param folder the folder file
   * @return file
   */
  @Nullable
  private Path searchFFmpeg(@NotNull final Path folder) {
    try (final Stream<Path> paths = Files.walk(folder)) {
      paths
          .filter(x -> Files.isRegularFile(x) && PathUtilities.getName(x).contains("ffmpeg"))
          .filter(x -> !folder.equals(x))
          .findFirst()
          .ifPresent(path -> file = path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return file;
  }
}
