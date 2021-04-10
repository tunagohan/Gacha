package com.github.tunagohan.gachaplus;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * GachaPlus
 * @license    LGPLv3
 * @copyright  Copyright github.tunagohan 2021
 * @author     tunagohan
 */
public class GachaPlus extends JavaPlugin{
  private GachaPlusDatabase database;
  private GachaPlusCommand command;
  private GachaPlusListener listener;

  /**
   * Get GachaPlusDatabase instance.
   */
  public GachaPlusDatabase getDatabase() {
    return database;
  }

  /**
   * Get GachaPlusCommand instance.
   */
  public GachaPlusCommand getCommand() {
    return command;
  }

  /**
   * Get GachaPlusListener instance.
   */
  public GachaPlusListener getListener() {
    return listener;
  }

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable(){
    try{
      getLogger().log(Level.INFO, "The Plugin Has Been Enabled!");

      // If there is no setting file, it is created
      if(!getDataFolder().exists()){
        getDataFolder().mkdir();
      }

      File configFile = new File(getDataFolder(), "config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }

      ArrayList<String> langFileNameList = new ArrayList<String>(
        Arrays.asList(
          "config.yml"
        )
      );
      for (String curFileName : langFileNameList) {
        InputStream in = getResource(curFileName);
        Files.copy(in, new File(getDataFolder(), curFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();
      }

      // Initialize the database.
      database = new GachaPlusDatabase(this);
      database.initialize();

      // Register event listener.
      PluginManager pm = getServer().getPluginManager();
      HandlerList.unregisterAll(this);    // clean up
      listener = new GachaPlusListener(this);
      pm.registerEvents(listener, this);

      // Instance prepared of GachaPlusCommand.
      command = new GachaPlusCommand(this);

    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }

  /**
   * JavaPlugin method onCommand.
   *
   * @return boolean true:Success false:Display the usage dialog set in plugin.yml
   */
  public boolean onCommand( CommandSender sender, Command commandInfo, String label, String[] args) {
    boolean hideUsageFlag = true;  // true:Success false:Display the usage dialog set in plugin.yml
    try{
      if(!commandInfo.getName().equals("gacha")) {
        return hideUsageFlag;
      }

      if(args.length <= 0) {
        return hideUsageFlag;
      }
      String subCommand = args[0];

      command.initialize(sender, args);
      switch(subCommand) {
        case "list":
          if(sender.hasPermission("gacha.list")) {
            hideUsageFlag = command.list();
          }
          break;

        case "modify":
          if(sender.hasPermission("gacha.modify")) {
            hideUsageFlag = command.modify();
          }
          break;

        case "delete":
          if(sender.hasPermission("gacha.delete")) {
            hideUsageFlag = command.delete();
          }
          break;

        case "ticket":
          if((sender instanceof BlockCommandSender) || (sender instanceof ConsoleCommandSender) || sender.isOp()) {
            for(Player p: GachaPlusUtility.getTarget(this, args[1], sender)) {  // @a @p @s @r or playername
              command.ticket(p);
            }
            hideUsageFlag = true;
          }
          break;

        case "enable":
          if(sender.isOp()) {
            hideUsageFlag = command.enable();
          }
          break;

        case "reload":
          if(sender.isOp()) {
            hideUsageFlag = command.reload();
          }
          break;

        case "disable":
          if(sender.isOp()) {
            hideUsageFlag = command.disable();
          }
          break;

        default:
          hideUsageFlag = false;
      }
    }catch(Exception e){
      GachaPlusUtility.logStackTrace(e);
    }finally{
      command.finalize();
    }
    return hideUsageFlag;
  }

  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable(){
    try{
      database.finalize();
      command.finalize();

      // Unregister all event listener.
      HandlerList.unregisterAll(this);

      getLogger().log(Level.INFO, "The Plugin Has Been Disabled!");
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }
}
