package skywars;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.data.BlockData;

import skywars.adt.ChunkSaveCommon;
import skywars.interfaces.BlockRestore;
import skywars.interfaces.ChunkRestore;
import skywars.interfaces.ChunkSave;

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
	public ChunkRestore newChunkRestoreTask(BlockRestore block_restore, Map<Integer, String> maps, String unzip, String chunk_name)
	{
		return new ChunkRestoreBlockData(super.getPlugin(), maps, block_restore, unzip, chunk_name);
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
