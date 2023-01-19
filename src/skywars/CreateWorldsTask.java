package skywars;

import org.bukkit.ChatColor;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class CreateWorldsTask extends BukkitRunnable
{
	private CommandSender sender;
	private String msg;
	private String[] world_names;
	private int current_world;
	
	public CreateWorldsTask(CommandSender sender_in, String[] world_names_in, String msg_in)
	{
		sender = sender_in;
		msg = msg_in;
		world_names = world_names_in;
		current_world = 0;
	}
	
	@Override
	public void run()
	{
		try
		{
			WorldCreator creator = new WorldCreator(world_names[current_world]);
			creator.createWorld();
			
			current_world++;
		}
		catch(IndexOutOfBoundsException e)
		{
			if(msg != "")
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
			
			cancel();
		}
	}
}
