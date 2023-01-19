package skywars.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import skywars.SkyWars;

public class GamePlayerLeaveEvent implements Listener
{
	private SkyWars pl;
	
	public GamePlayerLeaveEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		pl.removePlayerFromList(pl.getGamePlayer(player));
	}
}