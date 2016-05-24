package com.penguinchao.fasttravel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class RegionTools {
	private FastTravel main;
	public RegionTools(FastTravel passedMain){
		main = passedMain;
	}
	protected void discoverRegion(String regionName, UUID player){
		//Mark a region as discovered for a player
		String query = "INSERT INTO `"+main.sql.getPrefix()+"discoveredlocations` (`player_id`, `region`) VALUES ('playid', 'regionnames')";
		PreparedStatement statement;
		try {
			statement = main.sql.getConnection().prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		try {
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	protected boolean isRegionDiscovered(String regionName, UUID player){
		//Check if a player has discovered a region
		Set<String> discoveredLocations;
		try {
			discoveredLocations = getDiscoveredLocations(player);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		for(String loc : discoveredLocations){
			if(loc.equals(regionName)){
				return true;
			}
		}
		return false;
	}
	protected Set<String> getDiscoveredLocations(UUID player) throws SQLException{
		//Get a list of all of a player's discovered regions
		String query = "SELECT DISTINCT `region` FROM `"+main.sql.getPrefix()+"discoveredlocations` WHERE `player_id` = '"+player.toString()+"' ";
		main.debugTrace("[getDiscoveredLocations] Preparing statement");
		PreparedStatement statement;
		try {
			statement = main.sql.getConnection().prepareStatement(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		main.debugTrace("[getDiscoveredLocations] Executing: "+query);
		ResultSet results;
		try {
			results = statement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		Set<String> returnMe = null;
		while(results.next()){
			if(returnMe == null){
				returnMe = new HashSet<String>();
			}
			returnMe.add(results.getString("region"));
		}
		return returnMe;
	}
	protected void echoDiscoveredLocations(Player player){
		Set<String> locs;
		try {
			locs = getDiscoveredLocations(player.getUniqueId());
		} catch (SQLException e) {
			e.printStackTrace();
			player.sendMessage(ChatColor.RED+main.getConfig().getString("messages.internal-error"));
			return;
		}
		if(locs == null){
			player.sendMessage(ChatColor.RED+main.getConfig().getString("messages.no-locations"));
			return;
		}
		player.sendMessage(ChatColor.YELLOW+"Discovered Locations:");
		int i = 1;
		for(String loc : locs){
			player.sendMessage(ChatColor.YELLOW+""+i+") "+ChatColor.GREEN+loc);
		}
	}
	protected boolean isRegionDefined(String regionName){
		//Check if a region is a fast travel one
		main.debugTrace("[isRegionDefined] Checking if "+regionName+" is defined");
		ConfigurationSection sections = main.getConfig().getConfigurationSection("locations");
		Set<String> keys = sections.getKeys(false);
		for(String key : keys){
			main.debugTrace("[isRegionDefined] Checking "+key);
			if(regionName == key){
				main.debugTrace("[isRegionDefined] Region found");
				main.debugTrace("[isRegionDefined] Done");
				return true;
			}
		}
		main.debugTrace("[isRegionDefined] Region not found");
		main.debugTrace("[isRegionDefined] Done");
		return false;
	}
	protected void travelToLocation(String regionName, Player player){
		//Send a player to the location that a region discovers
		main.debugTrace("[travelToLocation] Teleporting "+player.getDisplayName()+" to "+regionName);
		String worldName = main.getConfig().getString("locations."+regionName+".world");
		if(worldName == null){
			//World is not loaded
			main.debugTrace("[travelToLocation] World string is null");
			player.sendMessage(ChatColor.RED+main.getConfig().getString("messages.world-null"));
			main.debugTrace("[travelToLocation] Done");
			return;
		}
		World world = main.getServer().getWorld(worldName);
		if(world == null){
			//World is not loaded
			main.debugTrace("[travelToLocation] World is null");
			player.sendMessage(ChatColor.RED+main.getConfig().getString("messages.world-null"));
			main.debugTrace("[travelToLocation] Done");
			return;
		}
		main.debugTrace("[travelToLocation] Getting location");
		double x = main.getConfig().getDouble("locations."+regionName+".x");
		double y = main.getConfig().getDouble("locations."+regionName+".y");
		double z = main.getConfig().getDouble("locations."+regionName+".z");
		double pitch = main.getConfig().getDouble("locations."+regionName+".pitch");
		double yaw = main.getConfig().getDouble("locations."+regionName+".yaw");
		main.debugTrace("[travelToLocation] Teleporting...");
		Location loc = new Location(world, x, y, z, (float) yaw, (float) pitch);
		player.teleport(loc);
		main.debugTrace("[travelToLocation] Done");
	}
}
