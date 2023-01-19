package skywars.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import skywars.SkyWars;

public class GameBlockPlaceEvent implements Listener
{
	private SkyWars pl;
	
	public GameBlockPlaceEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event)
	{
		event.setCancelled(pl.getEventMethods().blockPlaceEvent(event.getPlayer()));
	}
}
