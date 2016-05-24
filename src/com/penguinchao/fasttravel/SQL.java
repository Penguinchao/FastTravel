package com.penguinchao.fasttravel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

public class SQL {
	private FastTravel main;
	private boolean databaseConnected;
	private Connection connection;
	public SQL(FastTravel passedMain){
		main = passedMain;
		main.getLogger().info("Connecting to database");
		databaseConnect();
		main.debugTrace("[SQL] Checking tables");
		checkTables();
		main.getLogger().info("Database Connected");
	}
	protected Connection getConnection(){
		if(!databaseConnected){
			return null;
		}
		return connection;
	}
	protected boolean isConnected(){
		return databaseConnected;
	}
	public void checkTables(){
		String query = "CREATE TABLE IF NOT EXISTS `"+getPrefix()+"discoveredlocations` ( `player_id` VARCHAR(36) NOT NULL , `region` VARCHAR(60) NOT NULL ) ENGINE = InnoDB;";
		if(!isConnected()){
			main.getLogger().info("Could not check tables. Disabling Plugin.");
			main.getServer().getPluginManager().disablePlugin(main);
			return;
		}
		try {
			PreparedStatement sql = connection.prepareStatement(query);
			sql.executeUpdate();
		} catch (SQLException e) {
			main.getLogger().info("[ERROR] Could not check database tables");
			e.printStackTrace();
		}
	}
	public String getPrefix(){
		String prefix = main.getConfig().getString("mysqlPrefix");
		if(prefix == null){
			return "";
		}else{
			return prefix + "_";
		}
	}
	public void databaseConnect(){
		String mysqlHostName= main.getConfig().getString("mysqlHostName");
		String mysqlPort	= main.getConfig().getString("mysqlPort");
		String mysqlUsername= main.getConfig().getString("mysqlUsername");
		String mysqlPassword= main.getConfig().getString("mysqlPassword");
		String mysqlDatabase= main.getConfig().getString("mysqlDatabase");
		String dburl = "jdbc:mysql://" + mysqlHostName + ":" + mysqlPort + "/" + mysqlDatabase;
		main.debugTrace("Attempting to connect to the database "+mysqlDatabase+" at "+mysqlHostName);
		try{
			connection = DriverManager.getConnection(dburl, mysqlUsername, mysqlPassword);
		}catch(Exception exception){
			main.getLogger().info("[ERROR] Could not connect to the database -- disabling FastTravel");
			exception.printStackTrace();
			databaseConnected = false;
			Bukkit.getPluginManager().disablePlugin(main);
			return;
		}
		databaseConnected = true;
	}
}
