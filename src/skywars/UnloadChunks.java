package skywars;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

public class UnloadChunks extends BukkitRunnable
{
	private SkyWars pl;
	private String world_name;
	
	public UnloadChunks(SkyWars plugin, String world_name_in)
	{
		pl = plugin;
		world_name = world_name_in;
	}
	
	@Override
	public void run()
	{
		boolean chunks_unloaded = true;
		
		for(Chunk chunk : pl.getServer().getWorld(world_name).getLoadedChunks())
		{
			chunks_unloaded = chunk.unload(false);
		}
		
		if(!chunks_unloaded)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Chunk unload failed!");
			pl.getServer().unloadWorld(world_name, false);
			
			WorldCreator creator = new WorldCreator(world_name);
			World world_reloaded = creator.createWorld();
			world_reloaded.setAutoSave(false);
		}
	}
}
