package skywars;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SkyWarsCommand implements CommandExecutor
{
	private SkyWars pl;
	
	public SkyWarsCommand(SkyWars plugin)
	{
		pl = plugin;
		pl.getCommand("skywars").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{	
		if(args.length == 0)
		{
			sender.sendMessage(ChatColor.RED + "Incorrect Usage!");
			return true;
		}
		else if(args.length == 1)
		{
			if(args[0].equalsIgnoreCase("enable"))
			{
				pl.enablePlugin(sender);
			}
			else if(args[0].equalsIgnoreCase("world"))
			{
				sender.sendMessage(((Player) sender).getWorld().getName());
			}
			else if(args[0].equalsIgnoreCase("join"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.playerJoin(player, pl.getFirstArena());
			}
			else if(args[0].equalsIgnoreCase("leave"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.playerLeave(player);
			}
			else if(args[0].equalsIgnoreCase("forcestart"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.forceStartLobby(player);
			}
			else if(args[0].equalsIgnoreCase("setspawnpoint"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				Location location = player.getLocation();
				
				String arena_name = player.getWorld().getName();
				String msg = pl.addSpawnpoint(arena_name, location);
				
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}
			else if(args[0].equalsIgnoreCase("setlobbyspawn"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				Location location = player.getLocation();
				
				String arena_name = player.getWorld().getName();
				String msg = pl.setLobbySpawn(arena_name, location);
				
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}
			else if(args[0].equalsIgnoreCase("setcentre"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				Location location = player.getLocation();
				Chunk chunk = location.getChunk();
				
				String arena_name = player.getWorld().getName();
				String msg = pl.setCentre(arena_name, chunk.getX(), chunk.getZ());
				
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			}
			else if(args[0].equalsIgnoreCase("selector"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				ItemStack item_in_hand = null;
				Method get_item = null;
				Method set_item = null;
				Object chosen_class = null;
				
				try
				{
					get_item = player.getInventory().getClass().getMethod("getItemInMainHand");
					set_item = player.getInventory().getClass().getMethod("setItemInMainHand", ItemStack.class);
					chosen_class = player.getInventory();
				}
				catch(SecurityException e1) {}
				catch(NoSuchMethodException e2)
				{
					try
					{
						get_item = player.getClass().getMethod("getItemInHand");
						set_item = player.getClass().getMethod("setItemInHand", ItemStack.class);
						chosen_class = player;
					}
					catch(SecurityException e1) {}
					catch(NoSuchMethodException e3) 
					{
						System.out.println("failed to find a method to get item from player's hand");
					}
				}
				
				try
				{
					item_in_hand = (ItemStack) get_item.invoke(chosen_class);
					set_item.invoke(chosen_class, pl.getCoordsSelectorItem());
					
					if(item_in_hand.getType() != Material.AIR)
						player.getInventory().addItem(item_in_hand);
					
					player.sendMessage(ChatColor.AQUA + "Selector given");
				}
				catch(IllegalArgumentException e3) {}
				catch(IllegalAccessException e4) {}
				catch(InvocationTargetException e5) {}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Incorrect Usage!");
			}
		}
		else if(args.length == 2)
		{
			if(args[0].equalsIgnoreCase("create"))
			{
				if(!pl.isPluginEnabled())
				{
					sender.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.createArena(sender, args[1]);
			}
			else if(args[0].equalsIgnoreCase("load"))
			{
				if(!pl.isPluginEnabled())
				{
					sender.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.loadArena(sender, args[1]);
			}
			else if(args[0].equalsIgnoreCase("unload"))
			{
				if(!pl.isPluginEnabled())
				{
					sender.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.unloadArena(sender, args[1]);
			}
			else if(args[0].equalsIgnoreCase("join"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				pl.playerJoin(player, args[1]);
			}
			else if(args[0].equalsIgnoreCase("tp"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				String arena_name = args[1];
				
				pl.teleportPlayerToArena(player, arena_name);
			}
			else if(args[0].equalsIgnoreCase("setmax"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				try
				{
					String arena_name = player.getWorld().getName();
					int max_players = Integer.parseInt(args[1]);
					
					String msg = pl.setMaxPlayers(arena_name, max_players);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}
				catch(NumberFormatException e)
				{
					player.sendMessage(ChatColor.RED + "Please enter a number");
				}
			}
			else if(args[0].equalsIgnoreCase("setmin"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				try
				{
					String arena_name = player.getWorld().getName();
					int min_players = Integer.parseInt(args[1]);
					
					String msg = pl.setMinPlayers(arena_name, min_players);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}
				catch(NumberFormatException e)
				{
					player.sendMessage(ChatColor.RED + "Please enter a number");
				}
			}
			else if(args[0].equalsIgnoreCase("setmove"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage(ChatColor.RED + "Command could only be executed from in game");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(!pl.isPluginEnabled())
				{
					player.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				if(args[1].equalsIgnoreCase("true"))
				{
					String msg = pl.setMovement(player.getWorld().getName(), true);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					return true;
				}
				else if(args[1].equalsIgnoreCase("false"))
				{
					String msg = pl.setMovement(player.getWorld().getName(), false);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					return true;
				}
				
				player.sendMessage(ChatColor.RED + "Values 'true' or 'false' only allowed!");
			}
			else if(args[0].equalsIgnoreCase("restore"))
			{
				if(!pl.isPluginEnabled())
				{
					sender.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				try
				{
					pl.getGame(args[1]).restoreArena();
				}
				catch(NullPointerException e) {}
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Incorrect Usage!");
			}
		}
		else if(args.length == 3)
		{
			if(args[0].equalsIgnoreCase("setmax"))
			{
				if(!pl.isPluginEnabled())
				{
					sender.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				try
				{
					String arena_name = args[1];
					int max_players = Integer.parseInt(args[2]);
					
					String msg = pl.setMaxPlayers(arena_name, max_players);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(ChatColor.RED + "Please enter a number");
				}
			}
			else if(args[0].equalsIgnoreCase("setmin"))
			{
				if(!pl.isPluginEnabled())
				{
					sender.sendMessage(ChatColor.RED + "Plugin not enabled!");
					return true;
				}
				
				try
				{
					String arena_name = args[1];
					int min_players = Integer.parseInt(args[2]);
					
					String msg = pl.setMinPlayers(arena_name, min_players);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(ChatColor.RED + "Please enter a number");
				}
			}
			else if(args[0].equalsIgnoreCase("setmove"))
			{
				if(args[2].equalsIgnoreCase("true"))
				{
					String msg = pl.setMovement(args[1], true);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					return true;
				}
				else if(args[2].equalsIgnoreCase("false"))
				{
					String msg = pl.setMovement(args[1], false);
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					return true;
				}
				
				sender.sendMessage(ChatColor.RED + "Values 'true' or 'false' only allowed!");
			}
			else
			{
				sender.sendMessage(ChatColor.RED + "Incorrect Usage!");
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Incorrect Usage!");
		}
		
		return false;
	}
}