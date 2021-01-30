package com.github.pulsebeat02.command;

import com.github.pulsebeat02.DeluxeMediaPlugin;
import com.github.pulsebeat02.image.MapImage;
import com.github.pulsebeat02.utility.ChatUtilities;
import com.github.pulsebeat02.utility.FileUtilities;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ImageCommand extends AbstractCommand implements CommandExecutor, Listener {

    private final Set<MapImage> images;
    private final Set<UUID> listen;

    public ImageCommand(@NotNull DeluxeMediaPlugin plugin) {
        super(plugin);
        this.images = new HashSet<>();
        this.listen = new HashSet<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "You must be a player to use this command!"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "Current Images Loaded");
            sender.sendMessage(ChatColor.GREEN + "[Map ID] " + ChatColor.GOLD + ":" + ChatColor.AQUA + " [MRL]");
            for (MapImage image : images) {
                sender.sendMessage(ChatColor.GREEN + "" + image.getMap() + ChatColor.GOLD + " : " + ChatColor.AQUA + image.getImage().getName());
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("rickroll")) {
                File f = FileUtilities.downloadImageFile("https://lh3.googleusercontent.com/proxy/_H3t3D3huCELuETTgKOU5uGrjxSZ4uT2B0Y1wtrvEaA9XByYfg726rzlFh9ppcGHrO-Gv7_nD6Z6DbP5PQWrQ2NGXPns4TUK8xUUkNbJfdJCu2Lwc-31XBa-LDcU", getPlugin().getDataFolder().getAbsolutePath());
                new MapImage(getPlugin().getLibrary(), 69, f, 1, 1).drawImage();
            }
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (args[1].equalsIgnoreCase("all")) {
                    listen.add(((Player) sender).getUniqueId());
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Are you sure you want to purge and delete all maps? Type YES if you would like to continue."));
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reset")) {
                if (args[1].equalsIgnoreCase("map")) {
                    long id;
                    try {
                        id = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is not a valid argument! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    if (id < 0L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too low! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    } else if (id > 4294967296L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too hight! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    for (MapImage image : images) {
                        if (image.getMap() == id) {
                            images.remove(image);
                            break;
                        }
                    }
                    MapImage.resetMap(getPlugin().getLibrary(), id);
                    sender.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged the map with ID " + id));
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set")) {
                if (args[1].equalsIgnoreCase("map")) {
                    long id;
                    try {
                        id = Long.parseLong(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is not a valid argument! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    if (id < 0L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too low! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    } else if (id > 4294967296L) {
                        sender.sendMessage(ChatUtilities.formatMessage(ChatColor.RED + "Argument '" + args[2] + "' is too hight! (Must be Integer between 0 - 4,294,967,296)"));
                        return true;
                    }
                    // TODO: Add the part where you check if the mrl is a file or an actual link, then render the map
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (listen.contains(p.getUniqueId())) {
            for (MapImage image : images) {
                MapImage.resetMap(getPlugin().getLibrary(), image.getMap());
            }
            images.clear();
            p.sendMessage(ChatUtilities.formatMessage(ChatColor.GOLD + "Successfully purged all image maps"));
        }
    }

}