package skywars.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import skywars.Game;
import skywars.GamePlayer;
import skywars.SkyWars;

public class EventMethods
{
	private SkyWars pl;
	
	public EventMethods(SkyWars plugin)
	{
		pl = plugin;
	}
	
	public boolean playerDamageEvent(Player player)
	{
		GamePlayer gamep = pl.getGamePlayer(player);
		
		if(gamep != null)
		{
			Game game = pl.getGame(gamep.getGameName());
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) || (gamep.getPlayerState().equals(SkyWars.GameState.GAME) && game.isInPreGame()))
				return true;
		}
		
		return false;
	}
	
	public boolean blockPlaceEvent(Player player)
	{
		GamePlayer gamep = pl.getGamePlayer(player);
		
		try
		{
			Game game = pl.getGame(gamep.getGameName());
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) || (gamep.getPlayerState().equals(SkyWars.GameState.GAME) && game.isInPreGame()))
				return true;
		}
		catch(NullPointerException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to service BlockPlaceEvent");
		}
		
		return false;
	}
	
	public boolean blockBreakEvent(Player player)
	{
		GamePlayer gamep = pl.getGamePlayer(player);
		
		try
		{
			Game game = pl.getGame(gamep.getGameName());
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) || (gamep.getPlayerState().equals(SkyWars.GameState.GAME) && game.isInPreGame()))
				return true;
		}
		catch(NullPointerException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to service BlockBreakEvent");
		}
		
		return false;
	}
	
	public String setGameChatFormatEvent(Player player, String msg, String format)
	{
		GamePlayer gamep = pl.getGamePlayer(player);
		
		try
		{
			format = "" + ChatColor.YELLOW + gamep.getCoins() + ChatColor.RESET + " | " + gamep.getPlayer().getName() + " > " + msg;
		}
		catch(NullPointerException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to service SetGameChatFormatEvent");
		}
		
		return format;
	}
	
	public boolean cancelMovement(Player player)
	{
		GamePlayer gamep = pl.getGamePlayer(player);
		
		if(gamep != null)
		{
			Game game = pl.getGame(gamep.getGameName());
			
			if(game != null)
			{
				if(gamep.getPlayerState().equals(SkyWars.GameState.GAME) && !game.isAllowedToMove())
					return true;
			}
		}
		
		return false;
	}
	
	public void worldChangeEvent(Player player, World world_from)
	{
		pl.removePlayerFromGame(pl.getGamePlayer(player));
	}
	
	public boolean setLobbyCoordsEvent(Player player, Block block, Action action)
	{
		if(block == null)
			return false;
		
		ItemStack item_in_hand = null;
		String arena_name = block.getWorld().getName();
		String file_path = "arenas/" + arena_name + "/";
		String file_name = arena_name + ".conf";
		int x,y,z;
		x = block.getX();
		y = block.getY();
		z = block.getZ();

		try
		{
			Method get_item_in_main_hand = player.getInventory().getClass().getMethod("getItemInMainHand");

			try
			{
				item_in_hand = (ItemStack) get_item_in_main_hand.invoke(player.getInventory());
			}
			catch(IllegalArgumentException e3)
			{
				e3.printStackTrace();
			}
			catch(IllegalAccessException e4)
			{
				e4.printStackTrace();
			}
			catch(InvocationTargetException e5)
			{
				e5.printStackTrace();
			}
		}
		catch(SecurityException e1)
		{
			e1.printStackTrace();
		}
		catch(NoSuchMethodException e2)
		{
			try
			{
				Method get_item_in_hand = player.getClass().getMethod("getItemInHand");

				try
				{
					item_in_hand = (ItemStack) get_item_in_hand.invoke(player);
				}
				catch(IllegalArgumentException e3)
				{
					e3.printStackTrace();
				}
				catch(IllegalAccessException e4)
				{
					e4.printStackTrace();
				}
				catch(InvocationTargetException e5)
				{
					e5.printStackTrace();
				}
			}
			catch(SecurityException e1)
			{
				e1.printStackTrace();
			}
			catch(NoSuchMethodException e3) 
			{
				pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to find a method to get item from player's hand");
			}
		}
		
		if(item_in_hand != null)
		{
			if(item_in_hand.getItemMeta().equals(pl.getCoordsSelectorItem().getItemMeta()))
			{
				if(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
				{
					if(pl.getFileManager().read("arenas.txt").contains(block.getWorld().getName()))
					{
						if(pl.getFileManager().readLine(file_path, file_name, "lobby_location_one").isEmpty())
						{
							pl.getFileManager().writeLine(file_path, file_name, "lobby_location_one", x + " " + y + " " + z);
							player.sendMessage(ChatColor.GREEN + "Point A set!");
						}
						else if(pl.getFileManager().readLine(file_path, file_name, "lobby_location_two").isEmpty())
						{
							pl.getFileManager().writeLine(file_path, file_name, "lobby_location_two", x + " " + y + " " + z);
							player.sendMessage(ChatColor.DARK_GREEN + "Point B set!");
						}
						else
							player.sendMessage(ChatColor.RED + "Coordinates have already been set");
					}
					else
						player.sendMessage(ChatColor.RED + "This world is not in the allowed arenas file");
				}

				return true;
			}
		}
		
		return false;
	}
}
