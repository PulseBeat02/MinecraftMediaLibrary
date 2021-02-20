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

package com.github.pulsebeat02.deluxemediaplugin.config;

import com.github.pulsebeat02.deluxemediaplugin.DeluxeMediaPlugin;
import com.github.pulsebeat02.minecraftmedialibrary.image.MapImage;
import com.github.pulsebeat02.minecraftmedialibrary.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PictureConfiguration extends AbstractConfiguration {

  private final Set<MapImage> images;

  public PictureConfiguration(@NotNull final DeluxeMediaPlugin plugin) {
    super(plugin, "picture.yml");
    this.images = new HashSet<>();
  }

  public void addPhoto(final int map, @NotNull final File file, final int width, final int height) {
    images.add(new MapImage(getPlugin().getLibrary(), map, file, width, height));
  }

  @Override
  public void deserialize() {
    final FileConfiguration configuration = getFileConfiguration();
    for (final MapImage image : images) {
      final long key = image.getMap();
      configuration.set(key + ".location", image.getImage().getAbsolutePath());
      configuration.set(key + ".width", image.getWidth());
      configuration.set(key + ".height", image.getHeight());
    }
    saveConfig();
  }

  @Override
  public void serialize() {
    final FileConfiguration configuration = getFileConfiguration();
    for (final String key : configuration.getKeys(false)) {
      final long id = Long.parseLong(key);
      final File file = new File(Objects.requireNonNull(configuration.getString(id + ".location")));
      if (!file.exists()) {
        Logger.error("Could not read " + file.getAbsolutePath() + " at id " + id + "!");
        continue;
      }
      final int width = configuration.getInt(id + "width");
      final int height = configuration.getInt(id + "height");
      images.add(new MapImage(getPlugin().getLibrary(), id, file, width, height));
    }
  }

  public Set<MapImage> getImages() {
    return images;
  }
}
