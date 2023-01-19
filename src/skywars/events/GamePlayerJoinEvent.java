package skywars.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import skywars.SkyWars;

public class GamePlayerJoinEvent implements Listener
{
	private SkyWars pl;
	
	public GamePlayerJoinEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', "&4&l" + player.getName() + " has joined the game!"));
		pl.teleportPlayerToSpawn(player);
	}
}
