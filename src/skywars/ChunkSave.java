package skywars;

import java.util.concurrent.ExecutorService;

import org.bukkit.ChunkSnapshot;
import org.bukkit.command.CommandSender;

public interface ChunkSave
{
	public ChunkRestore newChunkRestoreTask(BlockRestore block_restore, ChunkSnapshot snap_in, String unzip);
	public BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool);
	public void saveArenaChunks(String world_name, CommandSender sender, int range);
}
