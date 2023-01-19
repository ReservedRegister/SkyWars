package skywars.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import skywars.SkyWars;

public class GameAsyncPlayerChatEvent implements Listener
{
	private SkyWars pl;
	
	public GameAsyncPlayerChatEvent(SkyWars plugin)
	{
		pl = plugin;
	}
	
	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		
		String format = event.getFormat();
		String message = event.getMessage();
		
		event.setFormat(pl.getEventMethods().setGameChatFormatEvent(player, message, format));
	}
}
