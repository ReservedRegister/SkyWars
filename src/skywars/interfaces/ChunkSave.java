package skywars.interfaces;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.bukkit.command.CommandSender;

public interface ChunkSave
{
	public ChunkRestore newChunkRestoreTask(BlockRestore block_restore, Map<Integer, String> maps, String unzip, String chunk_name);
	public BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool);
	public void saveArenaChunks(CommandSender sender, String world_name, int range);
}
