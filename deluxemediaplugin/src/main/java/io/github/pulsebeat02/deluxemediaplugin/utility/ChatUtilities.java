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

package io.github.pulsebeat02.deluxemediaplugin.utility;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.TextComponent.ofChildren;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class ChatUtilities {

  private static final ComponentLike PREFIX;
  private static final ComponentLike EXTERNAL_PROCESS;

  static {
    PREFIX =
        text()
            .color(AQUA)
            .append(
                text('['), text("DeluxeMediaPlugin", GOLD), text(']'), space(), text("»", GRAY));
    EXTERNAL_PROCESS =
        text()
            .color(AQUA)
            .append(
                text('['),
                text("DeluxeMediaPlugin External Process", GOLD),
                text(']'),
                space(),
                text("»", GRAY));
  }

  public static Component format(@NotNull final TextComponent message) {
    return ofChildren(PREFIX, space(), message);
  }

  public static Component formatFFmpeg(@NotNull final TextComponent message) {
    return ofChildren(EXTERNAL_PROCESS, space(), message);
  }

  public static Optional<int[]> checkDimensionBoundaries(
      @NotNull final Audience sender, @NotNull final String str) {
    final String[] dims = str.split(":");
    final String message;
    final OptionalInt width = ChatUtilities.checkIntegerValidity(dims[0]);
    final OptionalInt height = ChatUtilities.checkIntegerValidity(dims[1]);
    if (!width.isPresent()) {
      message = dims[0];
    } else if (!height.isPresent()) {
      message = dims[1];
    } else {
      return Optional.of(new int[] {width.getAsInt(), height.getAsInt()});
    }
    sender.sendMessage(
        text()
            .color(RED)
            .append(text("Argument '"))
            .append(text(str, GOLD))
            .append(text("' "))
            .append(text(message))
            .append(text(" is not a valid argument!"))
            .append(text(" (Must be Integer)")));
    return Optional.empty();
  }

  public static OptionalInt checkIntegerValidity(@NotNull final String num) {
    try {
      return OptionalInt.of(Integer.parseInt(num));
    } catch (final NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  public static TextComponent getCommandUsage(@NotNull final Map<String, String> usages) {
    final TextComponent.Builder builder =
        text().append(text("------------------", AQUA)).append(newline());
    for (final Map.Entry<String, String> entry : usages.entrySet()) {
      builder.append(
          join(
              space(),
              text(entry.getKey(), LIGHT_PURPLE),
              text("-", GOLD),
              text(entry.getValue(), AQUA),
              newline()));
    }
    builder.append(text("------------------", AQUA));
    return builder.build();
  }
}
