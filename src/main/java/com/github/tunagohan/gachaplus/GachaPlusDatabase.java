package com.github.tunagohan.gachaplus;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

/*
 * GachaPlusDatabase
 * @license    LGPLv3
 * @copyright  Copyright github.tunagohan 2021
 * @author     tunagohan
 */
public class GachaPlusDatabase {
  private GachaPlus gachaPlus;
  private Connection con;
  private List<String> listGachaSignCache = new ArrayList<String>();
  private long expireCache; 
  
  /**
   * Constructor of GachaPlusDatabase.
   * @param GachaPlus gachaPlus
   */
  public GachaPlusDatabase(GachaPlus gachaPlus) {
    this.gachaPlus = gachaPlus;
  }

  /**
   * Get connection.
   * @return Connection Connection
   */
  private Connection getCon(){
    try{
      // Create database folder.
      if(!gachaPlus.getDataFolder().exists()){
        gachaPlus.getDataFolder().mkdir();
      }
      if(con == null) {
        // Select JDBC driver.
        Class.forName("org.sqlite.JDBC");
        String url = "jdbc:sqlite:" + gachaPlus.getDataFolder() + File.separator + "sqlite.db";
        con = DriverManager.getConnection(url);
        con.setAutoCommit(true);
      }
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
      closeCon(con);
    }
    return con;
  }

  /**
   * Get statement.
   * @return Statement Statement
   */
  private Statement getStmt(){
    Statement stmt = null;
    try{
      if(stmt == null) {
        stmt = getCon().createStatement();
        stmt.setQueryTimeout(gachaPlus.getConfig().getInt("query-timeout"));
      }
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
    return stmt;
  }

  /**
   * Close connection.
   * @param Connection Connection
   */
  private static void closeCon(Connection con){
    try{
      if(con != null){
        con.close();
      }
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }

  /**
   * Close result set.
   * @param ResultSet Result set
   */
  private static void closeRs(ResultSet rs) {
    try{
      if(rs != null){
        rs.close();
      }
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }

  /**
   * Close statement.
   * @param Statement Statement
   */
  private static void closeStmt(Statement stmt) {
    try{
      if(stmt != null){
        stmt.close();
      }
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }

  /**
   * Close prepared statement.
   * @param PreparedStatement PreparedStatement
   */
  private static void closePrepStmt(PreparedStatement prepStmt){
    try{
      if(prepStmt != null){
        prepStmt.close();
      }
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }

  /**
   * Finalize
   */
  public void finalize() {
    try{
      closeCon(getCon());
      listGachaSignCache  = new ArrayList<String>();
      expireCache = System.currentTimeMillis();
    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    }
  }

  /**
   * Initialize
   */
  public void initialize() {
    ResultSet rs = null;
    Statement stmt = null;
    try{
      stmt = getStmt();
      
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS gachaPlus ("
        + "  id INTEGER PRIMARY KEY AUTOINCREMENT"
        + "  ,gacha_name STRING NOT NULL"
        + "  ,gacha_display_name STRING NOT NULL"
        + "  ,gacha_detail STRING NOT NULL"
        + "  ,world_name STRING NOT NULL"
        + "  ,sign_x INTEGER NOT NULL"
        + "  ,sign_y INTEGER NOT NULL"
        + "  ,sign_z INTEGER NOT NULL"
        + "  ,chest_x INTEGER NOT NULL DEFAULT 0"
        + "  ,chest_y INTEGER NOT NULL DEFAULT 0"
        + "  ,chest_z INTEGER NOT NULL DEFAULT 0"
        + "  ,updated_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(updated_at LIKE '____-__-__ __:__:__')"
        + "  ,created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ");"
      );
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS gacha_name_uindex ON gachaPlus (gacha_name);");
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS world_name_sign_xyz_uindex ON gachaPlus (world_name, sign_x, sign_y, sign_z);");
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS world_name_chest_xyz_uindex ON gachaPlus (world_name, chest_x, chest_y, chest_z);");

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticket ("
        + "  id INTEGER PRIMARY KEY AUTOINCREMENT"
        + "  ,ticket_code STRING NOT NULL"
        + "  ,created_at DATETIME NOT NULL DEFAULT (datetime('now','localtime')) CHECK(created_at LIKE '____-__-__ __:__:__')"
        + ");"
      );
      stmt.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS ticket_code_uindex ON ticket (ticket_code);");
    
      closeStmt(stmt);

      refreshCache();

    } catch (Exception e){
      GachaPlusUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closeStmt(stmt);
    }
  }
  
  /**
   * list
   * @return ArrayList
   */
  public List<String> list(){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<String> ret = new ArrayList<String>();
    try {
      prepStmt = getCon().prepareStatement("SELECT"
        + "  gacha_name "
        + "  ,world_name "
        + "  ,sign_x "
        + "  ,sign_y "
        + "  ,sign_z "
        + "  ,chest_x "
        + "  ,chest_y "
        + "  ,chest_z "
        + "FROM"
        + "  gachaPlus "
        + "ORDER BY"
        + "  id DESC"
        );
      rs = prepStmt.executeQuery();
      while(rs.next()){
        ret.add(
          String.format(
            "gacha_name:%s world:%s sign[x,y,z]:%d,%d,%d chest[x,y,z]:%d,%d,%d"
            ,rs.getString(1)
            ,rs.getString(2)
            ,rs.getInt(3)
            ,rs.getInt(4)
            ,rs.getInt(5)
            ,rs.getInt(6)
            ,rs.getInt(7)
            ,rs.getInt(8)
          )
        );
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ret;
  }
  
  /**
   * Delete gachaPlus
   * @param String gachaName
   * @return boolean true:Success false:Failure
   */
  public boolean deleteGacha(String gachaName) {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = getCon().prepareStatement("DELETE FROM gachaPlus WHERE gacha_name = ?;");
      prepStmt.setString(1, gachaName);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closePrepStmt(prepStmt);
      refreshCache();

    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
      return false;
    }
    return true;
  }

  /**
   * Get gachaPlus id
   * @param String gachaName
   * @return Integer|null GachaPlus id.
   */
  public Integer getGacha(String gachaName){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer gachaId = null;
    try {
      prepStmt = getCon().prepareStatement("SELECT id FROM gachaPlus WHERE gacha_name=?");
      prepStmt.setString(1, gachaName);
      rs = prepStmt.executeQuery();
      while(rs.next()){
        gachaId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    }
    return gachaId;
  }

  /**
   * Get gachaPlus chest
   * @param Location signLoc
   * @return Chest|null GachaPlus chest.
   */
  public Chest getGachaChest(Location signLoc){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = getCon().prepareStatement("SELECT world_name, chest_x, chest_y, chest_z FROM gachaPlus WHERE world_name=? AND sign_x=? AND sign_y=? AND sign_z=?");
      prepStmt.setString(1, signLoc.getWorld().getName());
      prepStmt.setInt(2, signLoc.getBlockX());
      prepStmt.setInt(3, signLoc.getBlockY());
      prepStmt.setInt(4, signLoc.getBlockZ());
      rs = prepStmt.executeQuery();
      String worldName = "";
      Integer chestX = null;
      Integer chestY = null;
      Integer chestZ = null;
      boolean isChestNothing = false;
      while(rs.next()){
        worldName = rs.getString(1);
        chestX = rs.getInt(2);
        if (rs.wasNull()) {
          isChestNothing = true;
        }
        chestY = rs.getInt(3);
        if (rs.wasNull()) {
          isChestNothing = true;
        }
        chestZ = rs.getInt(4);
        if (rs.wasNull()) {
          isChestNothing = true;
        }
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      
      if(isChestNothing) {
        return null;
      }
      
      Block b = new Location(gachaPlus.getServer().getWorld(worldName), chestX, chestY, chestZ).getBlock();
      if(!b.getType().equals(Material.CHEST)) {
        return null;
      }
      
      return (Chest)b.getState();
    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    }
    return null;
  }

  /**
   * Get gachaPlus id
   * @param Location loc
   * @return Integer|null GachaPlus id.
   */
  public boolean isGacha(Location loc){
    try {
      boolean cacheClear = false;
      if(expireCache > System.currentTimeMillis()) {
        cacheClear = true;
      }
      return isGacha(loc, cacheClear);
    } catch (Exception e) {
      GachaPlusUtility.logStackTrace(e);
    }
    return false;
  }
  
  /**
   * Get gachaPlus id
   * @param Location loc
   * @param boolean cacheClear
   * @return Integer|null GachaPlus id.
   */
  public boolean isGacha(Location loc, boolean cacheClear){
    try {
      if( cacheClear ) {
        refreshCache();
      }
      String searchIndex = String.join(
        "_"
        ,loc.getWorld().getName()
        ,String.valueOf(loc.getBlockX())
        ,String.valueOf(loc.getBlockY())
        ,String.valueOf(loc.getBlockZ())
      );
      if(listGachaSignCache.contains(searchIndex)) {
        // no database & cache hit.
        return true;
      }

    } catch (Exception e) {
      GachaPlusUtility.logStackTrace(e);
    }
    return false;
  }
  
  /**
   * Refresh Cache
   * @return boolean Success:true Failure:false
   */
  public boolean refreshCache(){
    try {
      PreparedStatement prepStmt = getCon().prepareStatement("SELECT world_name, sign_x, sign_y, sign_z FROM gachaPlus");
      ResultSet rs = prepStmt.executeQuery();
      String cacheIndex;
      listGachaSignCache.clear();
      while(rs.next()){
        cacheIndex = String.join(
          "_"
          ,rs.getString(1)
          ,String.valueOf(rs.getInt(2))
          ,String.valueOf(rs.getInt(3))
          ,String.valueOf(rs.getInt(4))
        );
        listGachaSignCache.add(cacheIndex);
      }
      expireCache = System.currentTimeMillis() + (gachaPlus.getConfig().getInt("cache-expire-seconds") * 1000);
      closeRs(rs);
      closePrepStmt(prepStmt);
      return true;
    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    }
    return false;
  }

  /**
   * Get gachaPlus
   * @param String gachaName
   * @param String gachaDisplayNam
   * @param String gachaDetail
   * @param Integer worldId
   * @param Integer sign_x
   * @param Integer sign_y
   * @param Integer sign_z
   * @return Integer GachaPlus id.
   */
  public Integer getGacha(String gachaName, String gachaDisplayName, String gachaDetail, String worldName, Integer signX, Integer signY, Integer signZ){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Integer gachaId = null;
    try {
      gachaId = getGacha(gachaName);
      if(gachaId != null){
        return gachaId;
      }
      
      prepStmt = getCon().prepareStatement("INSERT INTO gachaPlus("
        + "  gacha_name"
        + ", gacha_display_name"
        + ", gacha_detail"
        + ", world_name"
        + ", sign_x"
        + ", sign_y"
        + ", sign_z"
        + ") VALUES (?,?,?,?,?,?,?)");
      prepStmt.setString(1, gachaName);
      prepStmt.setString(2, gachaDisplayName);
      prepStmt.setString(3, gachaDetail);
      prepStmt.setString(4, worldName);
      prepStmt.setInt(5, signX);
      prepStmt.setInt(6, signY);
      prepStmt.setInt(7, signZ);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      rs = prepStmt.getGeneratedKeys();
      if (rs.next()) {
        gachaId = rs.getInt(1);
      }
      closeRs(rs);
      closePrepStmt(prepStmt);
      refreshCache();

    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return gachaId;
  }

  /**
   * Update gachaPlus chest
   * @param gachaName
   * @param Integer chestX
   * @param Integer chestY
   * @param Integer chestZ
   * @return Integer GachaPlus id.
   */
  public boolean updateGachaChest(String gachaName, Integer chestX, Integer chestY, Integer chestZ){
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      Integer gachaId = getGacha(gachaName);
      if(gachaId == null){
        return false;
      }
      
      prepStmt = getCon().prepareStatement("UPDATE gachaPlus SET chest_x = ?, chest_y = ?, chest_z = ? WHERE gacha_name = ?;");
      prepStmt.setInt(1, chestX);
      prepStmt.setInt(2, chestY);
      prepStmt.setInt(3, chestZ);
      prepStmt.setString(4, gachaName);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closePrepStmt(prepStmt);
      return true;
    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return false;
  }

  /**
   * Issue a ticket
   * @param Player player
   * @return String|null ticketCode
   */
  public String getTicket() {
    return getTicket(0);
  }
  
  /**
   * Issue a ticket
   * @param Player player
   * @param Integer countRetry
   * @return String|null ticketCode
   */
  private String getTicket(Integer countRetry) {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String ticketCode = null;
    try {
      if(countRetry > 3) {
        return null;
      }
      countRetry++;      
      String curTicketCode = GachaPlusUtility.generateCode();

      prepStmt = getCon().prepareStatement("INSERT INTO ticket(ticket_code) VALUES (?);");
      prepStmt.setString(1, curTicketCode);
      prepStmt.addBatch();
      prepStmt.executeBatch();
      closeRs(rs);
      closePrepStmt(prepStmt);
      ticketCode = curTicketCode;
      
    } catch (SQLException e) {
      GachaPlusUtility.logStackTrace(e);
    } finally {
      closeRs(rs);
      closePrepStmt(prepStmt);
    }
    return ticketCode;
  }
}
