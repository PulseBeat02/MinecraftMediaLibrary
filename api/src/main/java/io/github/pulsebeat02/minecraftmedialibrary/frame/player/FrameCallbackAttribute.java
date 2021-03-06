/*.........................................................................................
 . Copyright © 2021 Brandon Li
 .                                                                                        .
 . Permission is hereby granted, free of charge, to any person obtaining a copy of this
 . software and associated documentation files (the “Software”), to deal in the Software
 . without restriction, including without limitation the rights to use, copy, modify, merge,
 . publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 . persons to whom the Software is furnished to do so, subject to the following conditions:
 .
 . The above copyright notice and this permission notice shall be included in all copies
 . or substantial portions of the Software.
 .
 . THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 . EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 . MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 . NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 . BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 . ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 . CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 .  SOFTWARE.
 .                                                                                        .
 .........................................................................................*/

package io.github.pulsebeat02.minecraftmedialibrary.frame.player;

import io.github.pulsebeat02.minecraftmedialibrary.MediaLibrary;

/** Attributes used for FrameCallbacks. */
public interface FrameCallbackAttribute {

  /**
   * Gets width.
   *
   * @return the width
   */
  int getWidth();

  /**
   * Gets height.
   *
   * @return the height
   */
  int getHeight();

  /**
   * Gets delay.
   *
   * @return the delay
   */
  int getDelay();

  /**
   * Gets library.
   *
   * @return the library
   */
  MediaLibrary getLibrary();

  /**
   * Gets video width.
   *
   * @return the video width
   */
  int getVideoWidth();

  /**
   * Gets last updated.
   *
   * @return the last updated
   */
  long getLastUpdated();

  /**
   * Sets the time of the last updated frame.
   *
   * @param lastUpdated the last updated frame in ms
   */
  void setLastUpdated(long lastUpdated);
}
