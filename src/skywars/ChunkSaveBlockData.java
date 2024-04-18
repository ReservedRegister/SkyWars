package skywars;

import java.util.concurrent.ExecutorService;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.data.BlockData;

public class ChunkSaveBlockData extends ChunkSaveCommon implements ChunkSave
{
	public ChunkSaveBlockData(SkyWars plugin)
	{
		super(plugin);
	}
	
	@Override
	public BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool)
	{
		return new BlockRestoreBlockData(super.getPlugin(), game_name, threadpool);
	}
	
	@Override
	public ChunkRestore newChunkRestoreTask(BlockRestore block_restore, ChunkSnapshot snap_in, String unzip)
	{
		return new ChunkRestoreBlockData(super.getPlugin(), snap_in, block_restore, unzip);
	}
	
	@Override
	public String getCurrentBlock(ChunkSnapshot snap, int x, int y, int z)
	{
		BlockData blockdata = snap.getBlockData(x, y, z);
		String current_blockdata = blockdata.getAsString();
		current_blockdata = current_blockdata.substring(current_blockdata.indexOf(":") + 1);
		return current_blockdata;
	}
}
