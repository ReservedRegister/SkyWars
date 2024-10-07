package skywars.legacy;

import org.bukkit.ChunkSnapshot;

import skywars.BlockRestore;
import skywars.ChunkRestore;
import skywars.ChunkRestoreTask;
import skywars.SkyWars;

public class ChunkRestoreMaterialData extends ChunkRestoreTask implements ChunkRestore
{
	private LegacyMethods legacy;
	
	public ChunkRestoreMaterialData(SkyWars plugin, ChunkSnapshot snap_in, BlockRestore blockrestore_task_in, String chunk_data_in)
	{
		super(plugin, snap_in, blockrestore_task_in, chunk_data_in);
		
		legacy = new LegacyMethods(plugin);
	}
	
	@Override
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
			
			String current_materialdata = legacy.getBlockTypeId(super.getChunkSnap(), x, y, z) + "(" + legacy.getBlockData(super.getChunkSnap(), x, y, z) + ")";
			
			if(!block.equals(current_materialdata))
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