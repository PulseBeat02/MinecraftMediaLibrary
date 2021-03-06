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

package io.github.pulsebeat02.deluxemediaplugin.command.audio;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.CommandSegment;
import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;
import io.github.pulsebeat02.minecraftmedialibrary.extractor.YoutubeExtraction;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.PackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.resourcepack.ResourcepackWrapper;
import io.github.pulsebeat02.minecraftmedialibrary.utility.ResourcepackUtilities;
import io.github.pulsebeat02.minecraftmedialibrary.utility.VideoExtractionUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.github.pulsebeat02.deluxemediaplugin.utility.ChatUtilities.format;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class AudioLoadCommand implements CommandSegment.Literal<CommandSender> {

  private final LiteralCommandNode<CommandSender> node;
  private final AudioCommandAttributes attributes;
  private final DeluxeMediaPlugin plugin;

  public AudioLoadCommand(
      @NotNull final DeluxeMediaPlugin plugin, @NotNull final AudioCommandAttributes attributes) {
    this.plugin = plugin;
    this.attributes = attributes;
    node =
        literal("load")
            .then(argument("mrl", StringArgumentType.greedyString()).executes(this::loadAudio))
            .then(literal("resourcepack").executes(this::sendResourcepack))
            .build();
  }

  private int loadAudio(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    final MediaLibrary library = plugin.library();
    final String mrl = context.getArgument("mrl", String.class);
    if (isLoadingSound(audience)) {
      return SINGLE_SUCCESS;
    }
    audience.sendMessage(
        format(
            text(
                "Attempting to load the audio file... this may take a while depending on the length/quality of the audio.",
                GOLD)));
    if (isUrl(mrl)) {
      CompletableFuture.runAsync(
          () ->
              attributes.setResourcepackAudio(
                  new YoutubeExtraction(
                          mrl,
                          library.getAudioFolder(),
                          plugin.getEncoderConfiguration().getSettings())
                      .extractAudio()));
    } else {
      final Path file = Paths.get(mrl);
      if (Files.exists(file)) {
        attributes.setResourcepackAudio(file);
      } else {
        audience.sendMessage(
            format(
                text("The mrl specified is not valid! (Must be Youtube Link or Audio File)", RED)));
        return SINGLE_SUCCESS;
      }
    }
    CompletableFuture.runAsync(
            () -> {
              attributes.setCompletion(true);
              final PackWrapper wrapper =
                  ResourcepackWrapper.of(library, attributes.getResourcepackAudio());
              wrapper.buildResourcePack();
              attributes.setResourcepackLink(
                  plugin.getHttpConfiguration().getDaemon().getRelativePath(wrapper.getPath()));
              attributes.setResourcepackHash(
                  VideoExtractionUtilities.createHashSHA(Paths.get(wrapper.getPath())));
              sendResourcepack();
              audience.sendMessage(
                  format(
                      text(
                          String.format(
                              "Loaded Audio Successfully! (%s)", attributes.getResourcepackLink()),
                          GOLD)));
            })
        .whenCompleteAsync((t, throwable) -> attributes.setCompletion(false));
    return SINGLE_SUCCESS;
  }

  private int sendResourcepack(@NotNull final CommandContext<CommandSender> context) {
    final Audience audience = plugin.audience().sender(context.getSource());
    if (unloadedResourcepack()) {
      audience.sendMessage(format(text("Please load a resourcepack first!", RED)));
      return SINGLE_SUCCESS;
    }
    sendResourcepack();
    audience.sendMessage(
        format(
            text(
                String.format(
                    "Sent Resourcepack URL! (%s with hash %s)",
                    attributes.getResourcepackLink(), new String(attributes.getResourcepackHash())),
                GOLD)));
    return SINGLE_SUCCESS;
  }

  private void sendResourcepack() {
    ResourcepackUtilities.forceResourcepackLoad(
        plugin,
        Bukkit.getOnlinePlayers(),
        attributes.getResourcepackLink(),
        attributes.getResourcepackHash());
  }

  private boolean isUrl(@NotNull final String url) {
    return url.startsWith("http");
  }

  private boolean unloadedResourcepack() {
    return attributes.getResourcepackLink() == null && attributes.getResourcepackHash() == null;
  }

  private boolean isLoadingSound(@NotNull final Audience audience) {
    if (attributes.getCompletion().get()) {
      audience.sendMessage(
          format(
              text(
                  "Please wait for the previous audio to extract first before loading another one!",
                  RED)));
      return true;
    }
    return false;
  }

  @Override
  public @NotNull LiteralCommandNode<CommandSender> node() {
    return node;
  }
}
