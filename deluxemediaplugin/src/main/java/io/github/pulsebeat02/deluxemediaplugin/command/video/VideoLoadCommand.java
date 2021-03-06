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

package io.github.pulsebeat02.deluxemediaplugin.command.video;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.ffmpeg.FFmpegAudioExtractionHelper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.utility.PathUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class VideoLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final VideoCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public VideoLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final VideoCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    node =
        literal("load")
            .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadVideo))
            .then(literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private int loadVideo(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final String mrl = context.getArgument("mrl", String.class);
    final String folder = String.format("%s/mml/", plugin.getDataFolder().getAbsolutePath());
    final AtomicBoolean completion = attributes.getCompletion();
    audience.sendMessage(
        format(
            text(
                "Setting up resourcepack for video... this may take a while depending on the length/quality of the video.",
                GOLD)));
    if (!VideoExtractionUtilities.getYoutubeID(mrl).isPresent()) {
      final Path file = Paths.get(mrl);
      if (Files.exists(file)) {
        CompletableFuture.runAsync(() -> completion.set(false))
            .thenRunAsync(() -> wrapResourcepack(setAudioFileAttributes(folder, file)))
            .thenRun(this::sendResourcepackFile)
            .thenRunAsync(() -> sendSuccessfulLoadMessage(audience, mrl))
            .thenRunAsync(() -> completion.set(true));
      } else if (mrl.startsWith("http")) {
        audience.sendMessage(
            format(text(String.format("Link %s is not a valid Youtube video link!", mrl), RED)));
      } else {
        audience.sendMessage(
            format(
                text(String.format("File %s cannot be found!", PathUtilities.getName(file)), RED)));
      }
    } else {
      CompletableFuture.runAsync(() -> completion.set(false))
          .thenRunAsync(() -> wrapResourcepack(setYoutubeAttributes(mrl, folder)))
          .thenRun(this::sendResourcepackFile)
          .thenRunAsync(() -> sendSuccessfulLoadMessage(audience, mrl))
          .thenRunAsync(() -> completion.set(true));
    }
    return SINGLE_SUCCESS;
  }

  private Path setAudioFileAttributes(@NotNull final String folder, @NotNull final Path file) {
    final Path audio = Paths.get(folder, "custom.ogg");
    new FFmpegAudioExtractionHelper(plugin.getEncoderConfiguration().getSettings(), file, audio)
        .extract();
    attributes.setExtractor(null);
    attributes.setVideo(file);
    attributes.setYoutube(false);
    attributes.setAudio(audio);
    return audio;
  }

  private YoutubeExtraction setYoutubeAttributes(
      @NotNull final String mrl, @NotNull final String folder) {
    final YoutubeExtraction extraction =
        new YoutubeExtraction(
            mrl, Paths.get(folder), plugin.getEncoderConfiguration().getSettings());
    extraction.extractAudio();
    attributes.setYoutube(true);
    attributes.setVideo(extraction.getVideo());
    attributes.setAudio(extraction.getAudio());
    attributes.setExtractor(extraction);
    return extraction;
  }

  private void wrapResourcepack(@NotNull final Path audio) {
    final PackWrapper wrapper = ResourcepackWrapper.of(plugin.library(), audio);
    wrapper.buildResourcePack();
    final Path path = Paths.get(wrapper.getPath());
    attributes.setResourcepackUrl(plugin.getHttpConfiguration().getDaemon().generateUrl(path));
    attributes.setHash(VideoExtractionUtilities.createHashSHA(path));
  }

  private void wrapResourcepack(@NotNull final YoutubeExtraction extraction) {
    final PackWrapper wrapper = ResourcepackWrapper.of(plugin.library(), extraction);
    wrapper.buildResourcePack();
    attributes.setResourcepackUrl(
        plugin.getHttpConfiguration().getDaemon().generateUrl(wrapper.getPath()));
    attributes.setHash(VideoExtractionUtilities.createHashSHA(Paths.get(wrapper.getPath())));
  }

  private void sendSuccessfulLoadMessage(
      @NotNull final Audience audience, @NotNull final String mrl) {
    audience.sendMessage(format(text(String.format("Successfully loaded video %s", mrl), GOLD)));
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    if (unloadedResourcepack(audience)) {
      return SINGLE_SUCCESS;
    }
    sendResourcepackFile();
    audience.sendMessage(
        format(
            text(
                String.format(
                    "Sent Resourcepack URL! (%s with hash %s)",
                    attributes.getResourcepackUrl(), new String(attributes.getHash())),
                GOLD)));
    return SINGLE_SUCCESS;
  }

  private void sendResourcepackFile() {
    ResourcepackUtilities.forceResourcepackLoad(
        plugin, Bukkit.getOnlinePlayers(), attributes.getResourcepackUrl(), attributes.getHash());
  }

  private boolean unloadedResourcepack(@NotNull final Audience audience) {
    if (attributes.getResourcepackUrl() == null && attributes.getHash() == null) {
      audience.sendMessage(
          format(text("Please load a resourcepack before executing this command!", RED)));
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
