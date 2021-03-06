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

package io.github.pulsebeat02.deluxemediaplugin.command;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import io.github.pulsebeat02.deluxemediaplugin.command.audio.AudioCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.dither.DitherCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.ffmpeg.FFmpegCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.image.ImageCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.map.MapCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.screen.ScreenCommand;
import io.github.pulsebeat02.deluxemediaplugin.command.video.VideoCommand;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommandHandler implements TabExecutor {

  private final CommandDispatcher<CommandSender> dispatcher;
  private final RootCommandNode<CommandSender> rootNode;
  private final Set<BaseCommand> commands;
  private final DeluxeMediaPlugin plugin;

  public CommandHandler(@NotNull final DeluxeMediaPlugin plugin) {
    this.plugin = plugin;
    dispatcher = new CommandDispatcher<>();
    rootNode = dispatcher.getRoot();
    commands =
        ImmutableSet.of(
            new ImageCommand(plugin, this),
            new DitherCommand(plugin, this),
            new VideoCommand(plugin, this),
            new AudioCommand(plugin, this),
            new MapCommand(plugin, this),
            new ScreenCommand(plugin, this),
            new FFmpegCommand(plugin, this));
    final CommandMap commandMap = CommandMapHelper.getCommandMap();
    final Commodore commodore =
        CommodoreProvider.isSupported() ? CommodoreProvider.getCommodore(plugin) : null;
    for (final BaseCommand command : commands) {
      rootNode.addChild(command.node());
      commandMap.register(plugin.getName(), command);
      try {
        if (commodore != null) {
          commodore.register(
              CommodoreFileFormat.parse(
                  plugin.getResource(String.format("commodore/%s.commodore", command.getName()))));
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * CommandHandler to read input and execute other commands.
   *
   * @param sender command sender
   * @param command command sent
   * @param label label of command
   * @param args arguments for command
   * @return whether the command usage should be showed up.
   */
  @Override
  public boolean onCommand(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String label,
      final String @NotNull [] args) {
    final Audience audience = plugin.audience().sender(sender);
    try {
      dispatcher.execute(
          dispatcher.parse(
              String.format("%s %s", command.getName(), String.join(" ", args).trim()), sender));
    } catch (final CommandSyntaxException exception) {
      audience.sendMessage(((BaseCommand) command).usage());
    }
    return true;
  }

  /**
   * Tab handler to handle tab completer.
   *
   * @param sender command sender
   * @param command current command
   * @param alias aliases of command
   * @param args arguments of the command
   * @return list of options.
   */
  @Override
  public List<String> onTabComplete(
      @NotNull final CommandSender sender,
      @NotNull final Command command,
      @NotNull final String alias,
      final String @NotNull [] args) {
    return dispatcher
        .getCompletionSuggestions(
            dispatcher.parse(
                String.format("%s %s", command.getName(), String.join(" ", args)), sender))
        .join()
        .getList()
        .stream()
        .map(Suggestion::getText)
        .collect(Collectors.toList());
  }

  /**
   * Gets dispatcher.
   *
   * @return dispatcher
   */
  public CommandDispatcher<CommandSender> getDispatcher() {
    return dispatcher;
  }

  /**
   * Gets root node.
   *
   * @return root node
   */
  public RootCommandNode<CommandSender> getRootNode() {
    return rootNode;
  }

  /**
   * Gets commands.
   *
   * @return commands
   */
  public Set<BaseCommand> getCommands() {
    return commands;
  }
}
