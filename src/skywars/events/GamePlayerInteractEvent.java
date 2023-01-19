package skywars.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import skywars.SkyWars;

public class GamePlayerInteractEvent implements Listener
{
	private SkyWars pl;
	
	public GamePlayerInteractEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block clicked_block = event.getClickedBlock();
		Action action = event.getAction();
		
		event.setCancelled(pl.getEventMethods().setLobbyCoordsEvent(player, clicked_block, action));
	}
}
