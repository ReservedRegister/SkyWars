package skywars;

import org.bukkit.ChunkSnapshot;

public class ChunkRestoreBlockData extends ChunkRestoreTask implements ChunkRestore
{	
	public ChunkRestoreBlockData(SkyWars plugin, ChunkSnapshot snap_in, BlockRestore blockrestore_task_in, String chunk_data_in)
	{
		super(plugin, snap_in, blockrestore_task_in, chunk_data_in);
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
			
			if(!super.getChunkSnap().getBlockData(x, y, z).getAsString().equals("minecraft:" + block))
				super.getBlockRestoreTask().add(x, y, z, super.getChunkSnap().getX(), super.getChunkSnap().getZ(), block);
			
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