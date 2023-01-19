package skywars.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import skywars.SkyWars;

public class GamePlayerMoveEvent implements Listener
{
	private SkyWars pl;
	public GamePlayerMoveEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		event.setCancelled(pl.getEventMethods().cancelMovement(event.getPlayer()));
	}
	
}
