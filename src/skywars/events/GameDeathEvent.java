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
		
		if(gamep != null)
		{
			if(gamep.getPlayerState().equals(SkyWars.GameState.GAME))
			{
				pl.sendMessageToPlayers(SkyWars.GameState.GAME, gamep.getGameName(), gamep.getPlayer().getName() + " was eliminated from the game");
				pl.removePlayerFromGame(gamep);
			}
			else if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY))
			{
				pl.playerLeave(player);
			}
		}
	}
}
