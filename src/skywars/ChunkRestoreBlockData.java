package skywars;

import java.util.Map;

import org.bukkit.ChunkSnapshot;

import skywars.adt.ChunkRestoreTask;
import skywars.interfaces.BlockRestore;
import skywars.interfaces.ChunkRestore;

public class ChunkRestoreBlockData extends ChunkRestoreTask implements ChunkRestore
{
	private BlockRestore blockrestore_task;
	private ChunkSnapshot snap;
	private int chunk_x;
	private int chunk_z;
	
	public ChunkRestoreBlockData(SkyWars plugin, Map<Integer, String> maps_in, BlockRestore blockrestore_task_in, String chunk_data_in, String chunk_name_in)
	{
		super(plugin, maps_in, blockrestore_task_in.getRestoreWorld().getName(), chunk_data_in, chunk_name_in);
		
		blockrestore_task = blockrestore_task_in;
		snap = getChunkSnap();
		chunk_x = getChunkX();
		chunk_z = getChunkZ();
	}
	
	public void restoreChunk(String block, int y_start, int x_start, int z_start, int x_end, int z_end)
	{	
		boolean iterate = true;
		
		int y = y_start;
		int x = x_start;
		int z = z_start;
		
		while(iterate)
		{
			if(x == x_end && z == z_end)
				iterate = false;
			
			if(!snap.getBlockData(x, y, z).getAsString().equals("minecraft:" + block))
				blockrestore_task.add(x, y, z, chunk_x, chunk_z, block);
			
			z++;
			if(z == 16)
			{
				z = 0;
				x++;
				
				if(x == 16)
					return;
			}
		}
	}
}