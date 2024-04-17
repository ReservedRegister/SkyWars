package skywars;

import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

public class CreateWorldsTask extends BukkitRunnable
{
	private String[] world_names;
	private int current_world;
	
	public CreateWorldsTask(String[] world_names_in)
	{
		world_names = world_names_in;
		current_world = 0;
	}
	
	@Override
	public void run()
	{
		if(current_world < world_names.length)
		{
			new WorldCreator(world_names[current_world]).createWorld();
			current_world++;
		}
	}
}
