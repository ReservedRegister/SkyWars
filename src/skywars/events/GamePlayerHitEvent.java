package skywars.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import skywars.SkyWars;

public class GamePlayerHitEvent implements Listener
{
	private SkyWars pl;
	
	public GamePlayerHitEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent event)
	{
		Entity entity = event.getEntity();
		
		if(entity instanceof Player)
			event.setCancelled(pl.getEventMethods().playerDamageEvent((Player) entity));
	}
}
