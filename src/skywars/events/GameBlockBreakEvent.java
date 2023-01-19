package skywars.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import skywars.SkyWars;

public class GameBlockBreakEvent implements Listener
{
	private SkyWars pl;
	
	public GameBlockBreakEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event)
	{
		event.setCancelled(pl.getEventMethods().blockBreakEvent(event.getPlayer()));
	}
}
