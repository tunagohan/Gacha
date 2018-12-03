package space.gorogoro.gacha;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

/*
 * GachaUtility
 * @license    LGPLv3
 * @copyright  Copyright gorogoro.space 2018
 * @author     kubotan
 * @see        <a href="http://blog.gorogoro.space">Kubotan's blog.</a>
 */
public class GachaUtility {

  protected static final String NUMALPHA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
  /**
   * Output stack trace to log file.
   * @param Exception Exception
   */
  public static void logStackTrace(Exception e){
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    pw.flush();
    Bukkit.getLogger().log(Level.WARNING, sw.toString());
  }

  /**
   * Determine whether punch is being processed.
   * @param Player Player
   * @return boolean true:That's right false:That's not it
   */
  public static boolean isInPunch(Player player){
    if( player.hasMetadata(GachaCommand.META_CHEST)){
      return true;
    }
    return false;
  }

  /**
   * get gacha name in punch.
   * @param Player Player
   * @return String gacha name
   */
  public static String getGachaNameInPunch(Player player){
  	for(MetadataValue mv:player.getMetadata(GachaCommand.META_CHEST)) {
  		return mv.asString();
  	}
		return null;
  }
  
  /**
   * Set punch processing.
   * @param Player Player
   * @param Gacha Gacha
   * @param String Gacha Name
   */
  public static void setPunch(Player player, Gacha gacha, String gachaName){
  	removePunch(player, gacha);
  	player.setMetadata(GachaCommand.META_CHEST, new FixedMetadataValue(gacha, gachaName));
  }

  /**
   * Remove punch processing.
   * @param Player Player
   * @param Gacha Gacha
   */
  public static void removePunch(Player player, Gacha gacha){
    player.removeMetadata(GachaCommand.META_CHEST, gacha);
  }
  
  /**
   * Send message to player
   * @param CommandSender CommandSender
   * @param String message
   */
  public static void sendMessage(CommandSender sender, String message){
    sender.sendMessage((Object)ChatColor.DARK_RED + "[Gacha]" + " " + (Object)ChatColor.RED + message);
  }
  
  /**
   * Generate random code
   * @return String code
   */
  public static String generateCode(){
  	return String.format("%s-%s-%s",
  			RandomStringUtils.random(4, NUMALPHA),
  			RandomStringUtils.random(6, NUMALPHA),
  			RandomStringUtils.random(4, NUMALPHA)
    );
  }

  /**
   * Scan Format
   * @param String format
   * @param String str
   * @return String value
   */
  public static String scanf(String format, String str) {
  	return StringUtils.difference(format, str);
  }
}