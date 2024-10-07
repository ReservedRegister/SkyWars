package skywars.legacy;

import java.util.concurrent.ExecutorService;

import org.bukkit.ChunkSnapshot;

import skywars.BlockRestore;
import skywars.ChunkRestore;
import skywars.ChunkSave;
import skywars.ChunkSaveTask;
import skywars.SkyWars;

public class ChunkSaveMaterialData extends ChunkSaveTask implements ChunkSave
{
	private LegacyMethods legacy;
	
	public ChunkSaveMaterialData(SkyWars plugin)
	{
		super(plugin);
		
		legacy = new LegacyMethods(plugin);
	}
	
	@Override
	public BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool)
	{
		return new BlockRestoreMaterialData(super.getPlugin(), game_name, threadpool);
	}
	
	@Override
	public ChunkRestore newChunkRestoreTask(BlockRestore block_restore, ChunkSnapshot snap_in, String unzip)
	{
		return new ChunkRestoreMaterialData(super.getPlugin(), snap_in, block_restore, unzip);
	}
	
	@Override
	public String getCurrentBlock(ChunkSnapshot snap, int x, int y, int z)
	{		
		String current_materialdata = null;
		
		int material = (int) legacy.getBlockTypeId(snap, x, y, z);
		int magic_value = (int) legacy.getBlockData(snap, x, y, z);
		current_materialdata = material + "(" + magic_value + ")";
		
		return current_materialdata;
	}
}
