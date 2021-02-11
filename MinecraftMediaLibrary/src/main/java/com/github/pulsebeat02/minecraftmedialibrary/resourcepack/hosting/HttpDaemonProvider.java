/*
 * ============================================================================
 * Copyright (C) PulseBeat_02 - All Rights Reserved
 *
 * This file is part of MinecraftMediaLibrary
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Written by Brandon Li <brandonli2006ma@gmail.com>, 2/11/2021
 * ============================================================================
 */

package com.github.pulsebeat02.minecraftmedialibrary.resourcepack.hosting;

import com.github.pulsebeat02.minecraftmedialibrary.http.HttpDaemon;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class HttpDaemonProvider extends AbstractHostingProvider {

  private static final String SERVER_IP = Bukkit.getIp();
  private final int port;
  private HttpDaemon daemon;

  public HttpDaemonProvider(@NotNull final String path, final int port) {
    this.port = port;
    try {
      this.daemon = new HttpDaemon(port, path);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static String getServerIp() {
    return SERVER_IP;
  }

  public void startServer() {
    daemon.start();
  }

  @Override
  public String generateUrl(@NotNull final String file) {
    return "http://" + SERVER_IP + ":" + port + "/" + file;
  }

  @Override
  public String generateUrl(@NotNull final Path path) {
    return "http://" + SERVER_IP + ":" + port + "/" + path.getFileName();
  }

  public HttpDaemon getDaemon() {
    return daemon;
  }

  public int getPort() {
    return port;
  }
}
