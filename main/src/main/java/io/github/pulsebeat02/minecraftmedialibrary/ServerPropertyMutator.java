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

package io.github.pulsebeat02.minecraftmedialibrary;

import io.github.pulsebeat02.minecraftmedialibrary.utility.ServerPropertyUtilities;
import org.bukkit.Bukkit;

public class ServerPropertyMutator {

  /**
   * Changes the compression threshold in the server.properties file.
   */
  public void mutateCompressionThreshold() {
    if (isProxy()) {
      return;
    }
    try {
      ServerPropertyUtilities.setServerProperty(
          ServerPropertyUtilities.ServerProperty.PACKET_COMPRESSION_LIMIT, -1);
      ServerPropertyUtilities.savePropertiesFile();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns whether the server is a proxy or not.
   *
   * @return if proxy is enabled
   */
  private boolean isProxy() {
    return Bukkit.getOnlineMode();
  }
}
