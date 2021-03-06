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

package io.github.pulsebeat02.minecraftmedialibrary.resourcepack;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackFormatException;
import io.github.pulsebeat02.minecraftmedialibrary.exception.InvalidPackIconException;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.json.GsonHandler;
import io.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The resourcepack wrapper which can be used to wrap sound files and be hosted as a file. It has
 * the ability to modify the base resourcepack file as well as the sounds.json file. You may also
 * specify other attributes such as the icon, description, and format.
 */
public class ResourcepackWrapper implements PackWrapper {

  private final String path;
  private final Path audio;
  private final String soundName;
  private final Path icon;
  private final String description;
  private final int packFormat;

  /**
   * Instantiates a new Resourcepack wrapper.
   *
   * @param library the library
   * @param path the path
   * @param audio the audio
   * @param icon the icon
   * @param description the description
   * @param packFormat the pack format
   */
  public ResourcepackWrapper(
      @NotNull final MediaLibrary library,
      @NotNull final String path,
      @NotNull final Path audio,
      @Nullable final Path icon,
      @NotNull final String description,
      final int packFormat) {
    this(library.getPlugin().getName().toLowerCase(), path, audio, icon, description, packFormat);
  }

  /**
   * Instantiates a new Resourcepack wrapper.
   *
   * @param name the sound name
   * @param path the path
   * @param audio the audio
   * @param icon the icon
   * @param description the description
   * @param packFormat the pack format
   */
  public ResourcepackWrapper(
      @NotNull final String name,
      @NotNull final String path,
      @NotNull final Path audio,
      @Nullable final Path icon,
      @NotNull final String description,
      final int packFormat) {
    soundName = name;
    this.path = path;
    this.audio = audio;
    this.icon = icon;
    this.description = description;
    this.packFormat = packFormat;
    if (!ResourcepackUtilities.validatePackFormat(packFormat)) {
      throw new InvalidPackFormatException(
          String.format("Invalid Pack Format Exception (%d)", packFormat));
    }
    if (icon != null && !ResourcepackUtilities.validateResourcepackIcon(icon)) {
      throw new InvalidPackIconException(
          String.format("Invalid Pack Icon! Must be PNG (%s)", PathUtilities.getName(icon)));
    }
    Logger.info(String.format("New Resourcepack (%s) was Initialized", path));
  }

  /**
   * Returns a new builder class to use.
   *
   * @return the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a ResourcepackWrapper out of a YoutubeExtractor.
   *
   * @param extractor the YoutubeExtractor
   * @param library the library
   * @return the resulting ResourcepackWrapper
   */
  public static PackWrapper of(
      @NotNull final MediaLibrary library, @NotNull final YoutubeExtraction extractor) {
    return ResourcepackWrapper.builder()
        .audio(extractor.getAudio())
        .description(String.format("Youtube Video: %s", extractor.getVideoTitle()))
        .path(
            String.format(
                "%s/mml/http/resourcepack.zip",
                library.getPlugin().getDataFolder().getAbsolutePath()))
        .packFormat(6)
        .build(library);
  }

  /**
   * Creates a ResourcepackWrapper out of a File.
   *
   * @param audio the audio file
   * @param library the library
   * @return the resulting ResourcepackWrapper
   */
  public static PackWrapper of(@NotNull final MediaLibrary library, @NotNull final Path audio) {
    return ResourcepackWrapper.builder()
        .audio(audio)
        .description(String.format("Media: %s", PathUtilities.getName(audio)))
        .path(
            String.format(
                "%s/mml/http/resourcepack.zip",
                library.getPlugin().getDataFolder().getAbsolutePath()))
        .packFormat(6)
        .build(library);
  }

  /**
   * Deserializes ResourcepackWrapper.
   *
   * @param library the library
   * @param deserialize the deserialize
   * @return the resourcepack wrapper
   */
  @NotNull
  public static PackWrapper deserialize(
      @NotNull final MediaLibrary library, @NotNull final Map<String, Object> deserialize) {
    return new ResourcepackWrapper(
        library,
        String.valueOf(deserialize.get("path")),
        Paths.get(String.valueOf(deserialize.get("audio"))),
        Paths.get(String.valueOf(deserialize.get("icon"))),
        String.valueOf(deserialize.get("description")),
        NumberConversions.toInt(deserialize.get("pack-format")));
  }

  /**
   * Serializes ResourcepackWrapper
   *
   * @return map of serialized values
   */
  @Override
  @NotNull
  public Map<String, Object> serialize() {
    return ImmutableMap.of(
        "path", path,
        "audio", audio.toAbsolutePath().toString(),
        "icon", icon.toAbsolutePath().toString(),
        "description", description,
        "pack-format", packFormat);
  }

  /** Builds the resourcepack based on values. */
  @Override
  public void buildResourcePack() {
    onResourcepackBuild();
    Logger.info("Wrapping Resourcepack...");
    try {

      final Path zipFile = Paths.get(path);
      if (Files.notExists(zipFile)) {
        Files.createFile(zipFile);
      }

      final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile.toFile()));
      final ZipEntry config = new ZipEntry("pack.mcmeta");
      out.putNextEntry(config);
      out.write(getPackJson().getBytes());
      out.closeEntry();

      final ZipEntry sound = new ZipEntry("assets/minecraft/sounds.json");
      out.putNextEntry(sound);
      out.write(getSoundJson().getBytes());
      out.closeEntry();

      final ZipEntry soundFile = new ZipEntry("assets/minecraft/sounds/audio.ogg");
      out.putNextEntry(soundFile);
      out.write(Files.readAllBytes(Paths.get(audio.toAbsolutePath().toString())));
      out.closeEntry();

      if (icon != null && Files.exists(icon)) {
        final ZipEntry iconFile = new ZipEntry("pack.png");
        out.putNextEntry(iconFile);
        out.write(Files.readAllBytes(Paths.get(icon.toAbsolutePath().toString())));
        out.closeEntry();
      }

      out.close();
      Logger.info("Finished Wrapping Resourcepack!");
    } catch (final IOException e) {
      Logger.error("There was an error while wrapping the resourcepack...");
      e.printStackTrace();
    }
  }

  /** Called when the resourcepack is being built. */
  @Override
  public void onResourcepackBuild() {}

  /**
   * Gets pack JSON.
   *
   * @return pack json
   */
  @NotNull
  private String getPackJson() {
    final JsonObject mcmeta = new JsonObject();
    final JsonObject pack = new JsonObject();
    pack.addProperty("pack_format", packFormat);
    pack.addProperty("description", description);
    mcmeta.add("pack", pack);
    return GsonHandler.getGson().toJson(mcmeta);
  }

  /**
   * Gets pack sound JSON.
   *
   * @return sound json
   */
  @NotNull
  private String getSoundJson() {
    final JsonObject category = new JsonObject();
    final JsonObject type = new JsonObject();
    final JsonArray sounds = new JsonArray();
    sounds.add("audio");
    category.add("sounds", sounds);
    type.add(soundName, category);
    return GsonHandler.getGson().toJson(type);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ResourcepackWrapper)) {
      return false;
    }
    final PackWrapper wrapper = (PackWrapper) obj;
    return path.equals(wrapper.getPath())
        && audio.equals(wrapper.getAudio())
        && icon.equals(wrapper.getIcon())
        && description.equals(wrapper.getDescription())
        && packFormat == wrapper.getPackFormat();
  }

  @Override
  public String toString() {
    return GsonHandler.getGson().toJson(this);
  }

  @Override
  public String getSoundName() {
    return soundName;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Path getAudio() {
    return audio;
  }

  @Override
  public Path getIcon() {
    return icon;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getPackFormat() {
    return packFormat;
  }

  /** The type Builder. */
  public static class Builder {

    private Path audio;
    private Path icon;
    private String description;
    private int packFormat;
    private String path;

    private Builder() {}

    /**
     * Sets audio.
     *
     * @param audio the audio
     * @return the audio
     */
    public Builder audio(@NotNull final Path audio) {
      this.audio = audio;
      return this;
    }

    /**
     * Sets icon.
     *
     * @param icon the icon
     * @return the icon
     */
    public Builder icon(@NotNull final Path icon) {
      this.icon = icon;
      return this;
    }

    /**
     * Sets description.
     *
     * @param description the description
     * @return the description
     */
    public Builder description(@NotNull final String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets pack format.
     *
     * @param packFormat the pack format
     * @return the pack format
     */
    public Builder packFormat(final int packFormat) {
      this.packFormat = packFormat;
      return this;
    }

    /**
     * Sets path.
     *
     * @param path the path
     * @return the path
     */
    public Builder path(@NotNull final String path) {
      this.path = path;
      return this;
    }

    /**
     * Create resourcepack hosting provider resourcepack wrapper.
     *
     * @param library the library
     * @return the resourcepack wrapper
     */
    public PackWrapper build(final MediaLibrary library) {
      return new ResourcepackWrapper(library, path, audio, icon, description, packFormat);
    }

    /**
     * Create resourcepack hosting provider resourcepack wrapper (with sound).
     *
     * @param sound the sound
     * @return the resourcepack wrapper
     */
    public PackWrapper build(final String sound) {
      return new ResourcepackWrapper(sound, path, audio, icon, description, packFormat);
    }
  }
}
