package skywars.legacy;

import java.util.concurrent.ExecutorService;

import org.bukkit.block.BlockState;

import skywars.BlockQueue;
import skywars.BlockRestoreTask;
import skywars.SkyWars;

public class BlockRestoreMaterialData extends BlockRestoreTask
{
	private LegacyMethods legacy;
	
	public BlockRestoreMaterialData(SkyWars plugin, String world_name_in, ExecutorService threadpool_in)
	{
		super(plugin, world_name_in, threadpool_in);
		
		legacy = new LegacyMethods(plugin);
	}
	
	@Override
	public void restoreBlocks()
	{
		BlockQueue block_queue = null;
		
		while((block_queue = super.removeWithLimit()) != null)
		{
			String materialdata = block_queue.getBlock();
			String material = null;
			String bytedata = null;
			int x = block_queue.getX();
			int y = block_queue.getY();
			int z = block_queue.getZ();
			int chunk_x = block_queue.getChunkX();
			int chunk_z = block_queue.getChunkZ();
			
			try
			{
				material = materialdata.substring(0, materialdata.indexOf("("));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				getPlugin().getServer().getConsoleSender().sendMessage("failed to obtain material");
				cancel();
				return;
			}
			
			try
			{
				bytedata = materialdata.substring(materialdata.indexOf("(") + 1, materialdata.indexOf(")"));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				getPlugin().getServer().getConsoleSender().sendMessage("failed to obtain bytedata");
				cancel();
				return;
			}
			
			BlockState current_state = getRestoreWorld().getChunkAt(chunk_x, chunk_z).getBlock(x, y, z).getState();
			
			legacy.setTypeId(current_state, Integer.parseInt(material));
			legacy.setRawData(current_state, Byte.parseByte(bytedata));
			
			current_state.update(true, false);
			incrementBlocksBy(1);
		}
		
		if(super.isThreadPoolDone())
		{
			super.restoreComplete();
		}
	}
}
