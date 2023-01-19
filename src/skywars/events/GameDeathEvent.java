package skywars.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import skywars.GamePlayer;
import skywars.SkyWars;

public class GameDeathEvent implements Listener
{
	private SkyWars pl;
	
	public GameDeathEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event)
	{
		Entity entity = event.getEntity();
		
		if(!(entity instanceof Player))
			return;
		
		event.setDeathMessage("");
		Player player = (Player) entity;
		GamePlayer gamep = pl.getGamePlayer(player);
		
		try
		{
			pl.removePlayerFromGame(gamep, player.getKiller(), null, false, false);
		}
		catch(NullPointerException e)
		{
			pl.removePlayerFromGame(gamep, null, null, false, false);
		}
	}
}
