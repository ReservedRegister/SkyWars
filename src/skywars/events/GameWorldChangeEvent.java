package skywars.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import skywars.SkyWars;

public class GameWorldChangeEvent implements Listener
{
	private SkyWars pl;
	
	public GameWorldChangeEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event)
	{
		pl.getEventMethods().worldChangeEvent(event.getPlayer(), event.getFrom());
	}
}