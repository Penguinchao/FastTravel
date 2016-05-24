package com.penguinchao.fasttravel;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;

public class FastTravel extends JavaPlugin implements Listener {
	private boolean debugEnabled;
	protected RegionTools regionTools;
	protected SQL sql;
	@Override
	public void onEnable(){
		saveDefaultConfig();
		debugEnabled = getConfig().getBoolean("debug-enabled");
		debugTrace("Registering for events");
		getServer().getPluginManager().registerEvents(this, this);
		debugTrace("Done registering for events");
		regionTools = new RegionTools(this);
		sql = new SQL(this);
	}
	@Override
	public void onDisable(){
		try {
			sql.getConnection().close();
		} catch (SQLException e) {
			getLogger().info("Could not close database connection on disable");
			e.printStackTrace();
		}
	}
	protected void debugTrace(String message){
		if(debugEnabled){
			getLogger().info("[DEBUG] "+message);
		}
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) { 
		if (cmd.getName().equalsIgnoreCase("travel") || cmd.getName().equalsIgnoreCase("tr") ){
			//Check if player
			debugTrace("[onCommand] Checking if player");
			if(!(sender instanceof Player)){
				sender.sendMessage("This command can only be used by a player");
				return false;
			}
			Player player = (Player) sender;
			//Check length
			debugTrace("[onCommand] Checking length");
			if(args.length == 0){
				debugTrace("[onCommand] Length equals zero");
				regionTools.echoDiscoveredLocations(player);
				return false;
			}else if(args.length != 1){
				sender.sendMessage(ChatColor.RED + getConfig().getString("messages.incorrect-syntax"));
				return false;
			}
			//Check if region exists
			debugTrace("[onCommand] Checking if region exists");
			if(!regionTools.isRegionDefined(args[0])){
				sender.sendMessage(ChatColor.RED+getConfig().getString("messages.invalid-location"));
				return false;
			}
			//Check if region discovered
			debugTrace("[onCommand] Checking if region discovered");
			if(!regionTools.isRegionDiscovered(args[0], player.getUniqueId())){
				sender.sendMessage(ChatColor.RED+getConfig().getString("not-discovered"));
				return false;
			}
			//Teleport
			debugTrace("[onCommand] Teleporting");
			regionTools.travelToLocation(args[0], player);
			String teleportString = getConfig().getString("messages.on-travel");
			teleportString = teleportString.replace("%s", getConfig().getString("locations."+args[0]+".display-name"));
			player.sendMessage(ChatColor.GREEN+teleportString);
		}
		return false;
	}
	@EventHandler
	public void onRegionEnter(RegionEnterEvent event){
		String regionName = event.getRegion().getId();
		debugTrace("[onRegionEnter] A region was entered: "+regionName);
		if(regionTools.isRegionDefined(regionName)){
			debugTrace("[onRegionEnter] Region is a fast travel region");
		}else{
			debugTrace("[onRegionEnter] Region is not a fast travel region");
			debugTrace("[onRegionEnter] Done!");
			return;
		}
		UUID player = event.getPlayer().getUniqueId();
		if(regionTools.isRegionDiscovered(regionName, player)){
			debugTrace("[onRegionEnter] Player already discovered region");
			debugTrace("[onRegionEnter] Done!");
			return;
		}else{
			debugTrace("[onRegionEnter] Player has not discovered region");
		}
		debugTrace("[onRegionEnter] Marking region as discovered");
		regionTools.discoverRegion(regionName, player);
		String discoverMessage = getConfig().getString("messages.discovered");
		discoverMessage = discoverMessage.replace("%s", getConfig().getString("locations."+regionName+".display-name"));
		event.getPlayer().sendMessage(ChatColor.GREEN + discoverMessage);
		debugTrace("[onRegionEnter] Done!");
		return;
	}
}