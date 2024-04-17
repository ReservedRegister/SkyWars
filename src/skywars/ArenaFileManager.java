package skywars;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class ArenaFileManager
{
	private SkyWars pl;
	private final String path_name;
	private Map<String, ArenaCache> cached_arenas;
	
	public ArenaFileManager(SkyWars plugin)
	{
		pl = plugin;
		path_name = "plugins/" + pl.getDescription().getName() + "/";
		cached_arenas = new HashMap<>();
	}
	
	public ArenaCache getArenaCache(String arena_name)
	{
		return cached_arenas.get(arena_name);
	}
	
	public boolean readSpawnpoints(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String spawnpoints_file = "spawnpoints.txt";
		String final_path = file_path + "/" + spawnpoints_file;
		
		List<String> spawnpoints_raw = read(final_path);
		
		for(String spawnpoint_raw : spawnpoints_raw)
		{
			String[] split_spawnpoint = spawnpoint_raw.split(" ");
			
			if(split_spawnpoint.length == 3)
			{
				ArenaCache arena_cache = cached_arenas.get(arena_name);
				
				try
				{
					double x = Double.parseDouble(split_spawnpoint[0]);
					double y = Double.parseDouble(split_spawnpoint[1]);
					double z = Double.parseDouble(split_spawnpoint[2]);
					
					double[] parsed_spawnpoint = {x, y, z};
					
					if(arena_cache != null)
					{
						arena_cache.addSpawnPoint(parsed_spawnpoint);
					}
					else
					{
						ArenaCache new_cache = new ArenaCache();
						new_cache.addSpawnPoint(parsed_spawnpoint);
						cached_arenas.put(arena_name, new_cache);
					}
				}
				catch(NumberFormatException e)
				{
					if(arena_cache != null)
					{
						arena_cache.getSpawnpoints().clear();
					}
					
					pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse arena spawnpoints!");
					return false;
				}
			}
			else
			{
				pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse arena spawnpoints!");
				return false;
			}
		}
		
		return true;
	}
	
	public boolean readMinPlayers(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_main_conf = arena_name + ".txt";
		String final_path = file_path + "/" + arena_main_conf;
		
		String minimum_players = readLine(file_path, arena_main_conf, "minimum_players");
		
		try
		{
			int min_players = Integer.parseInt(minimum_players);
			
			ArenaCache arena_cache = cached_arenas.get(arena_name);
			
			if(arena_cache != null)
			{
				arena_cache.setMinimumPlayers(min_players);
			}
			else
			{
				ArenaCache new_cache = new ArenaCache();
				new_cache.setMinimumPlayers(min_players);
				cached_arenas.put(arena_name, new_cache);
			}
			
			return true;
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse minimum players for arena!");
		}
		
		return false;
	}
	
	public boolean readMaxPlayers(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_main_conf = arena_name + ".txt";
		String final_path = file_path + "/" + arena_main_conf;
		
		String maximum_players = readLine(file_path, arena_main_conf, "maximum_players");
		
		try
		{
			int max_players = Integer.parseInt(maximum_players);
			
			ArenaCache arena_cache = cached_arenas.get(arena_name);
			
			if(arena_cache != null)
			{
				arena_cache.setMaximumPlayers(max_players);
			}
			else
			{
				ArenaCache new_cache = new ArenaCache();
				new_cache.setMaximumPlayers(max_players);
				cached_arenas.put(arena_name, new_cache);
			}
			
			return true;
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse maximum players for arena!");
		}
		
		return false;
	}
	
	public boolean readGameMoveSetting(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_main_conf = arena_name + ".txt";
		String final_path = file_path + "/" + arena_main_conf;
		
		String allow_movement = readLine(file_path, arena_main_conf, "allow_movement");
		
		try
		{
			boolean move = Boolean.parseBoolean(allow_movement);
			
			ArenaCache arena_cache = cached_arenas.get(arena_name);
			
			if(arena_cache != null)
			{
				arena_cache.setMoveSetting(move);
			}
			else
			{
				ArenaCache new_cache = new ArenaCache();
				new_cache.setMoveSetting(move);
				cached_arenas.put(arena_name, new_cache);
			}
			
			return true;
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse maximum players for arena!");
		}
		
		return false;
	}
	
	public boolean readCentre(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_main_conf = arena_name + ".txt";
		String final_path = file_path + "/" + arena_main_conf;
		
		String arena_centre = readLine(file_path, arena_main_conf, "centre");
		
		try
		{
			String[] centre_split = arena_centre.split(" ");
			
			if(centre_split.length == 2)
			{
				double x = Double.parseDouble(centre_split[0]);
				double z = Double.parseDouble(centre_split[1]);
				
				double[] centre_ready = {x, z};
				
				ArenaCache arena_cache = cached_arenas.get(arena_name);
				
				if(arena_cache != null)
				{
					arena_cache.setCentre(centre_ready);
				}
				else
				{
					ArenaCache new_cache = new ArenaCache();
					new_cache.setCentre(centre_ready);
					cached_arenas.put(arena_name, new_cache);
				}
			}
			
			return true;
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse the centre for arena!");
		}
		
		return false;
	}
	
	public boolean readLobbySpawn(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_main_conf = arena_name + ".txt";
		String final_path = file_path + "/" + arena_main_conf;
		
		String lobby_spawn = readLine(file_path, arena_main_conf, "lobby_spawn");
		
		try
		{
			String[] lobby_split = lobby_spawn.split(" ");
			
			if(lobby_split.length == 3)
			{
				double x = Double.parseDouble(lobby_split[0]);
				double y = Double.parseDouble(lobby_split[1]);
				double z = Double.parseDouble(lobby_split[2]);
				
				double[] lobby_ready = {x, y, z};
				
				ArenaCache arena_cache = cached_arenas.get(arena_name);
				
				if(arena_cache != null)
				{
					arena_cache.setLobbySpawn(lobby_ready);
				}
				else
				{
					ArenaCache new_cache = new ArenaCache();
					new_cache.setLobbySpawn(lobby_ready);
					cached_arenas.put(arena_name, new_cache);
				}
			}
			
			return true;
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to parse the lobby spawn for arena!");
		}
		
		return false;
	}
	
	public boolean readChunkMaps(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_maps = "maps.txt";
		String final_path = file_path + "/" + arena_maps;
		
		Map<Integer, String> maps = new HashMap<>();
		
		for(String read_line : read(final_path))
		{
			String[] split_line = read_line.split(" ");
			
			if(split_line.length == 2)
			{
				try
				{
					int block_map = Integer.parseInt(split_line[0]);
					String block = split_line[1];
					
					maps.put(block_map, block);
				}
				catch(NumberFormatException e)
				{
					pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to read chunk maps!");
					return false;
				}
			}
			else
			{
				pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to read chunk maps!");
				return false;
			}
		}
		
		ArenaCache arena_cache = cached_arenas.get(arena_name);
		
		if(arena_cache != null)
		{
			arena_cache.setChunkMaps(maps);
		}
		else
		{
			ArenaCache new_cache = new ArenaCache();
			new_cache.setChunkMaps(maps);
			cached_arenas.put(arena_name, new_cache);
		}
		
		return true;
	}
	
	public boolean readChunks(String arena_name)
	{
		String file_path = "arenas/" + arena_name;
		String arena_bytes = "chunks.txt";
		String final_path = file_path + "/" + arena_bytes;
		
		try(FileInputStream stream = new FileInputStream(path_name + final_path))
		{
			List<Byte> read_bytes = new ArrayList<>();
			byte byte_read = (byte) stream.read();
			
			while(byte_read != -1)
			{
				read_bytes.add(Byte.valueOf(byte_read));
			}
			
			byte[] bytes_ready = new byte[read_bytes.size()];
			int byte_counter = 0;
			
			for(Byte current_byte : read_bytes)
			{
				bytes_ready[byte_counter] = current_byte.byteValue();
				byte_counter++;
			}
			
			ArenaCache arena_cache = cached_arenas.get(arena_name);
			
			if(arena_cache != null)
			{
				arena_cache.setRestoreBytes(bytes_ready);
			}
			else
			{
				ArenaCache new_cache = new ArenaCache();
				new_cache.setRestoreBytes(bytes_ready);
				cached_arenas.put(arena_name, new_cache);
			}
			
			return true;
		}
		catch(FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean loadArenaData(String arena)
	{		
		boolean spawnpoints_ready = readSpawnpoints(arena);
		boolean min_players_ready = readMinPlayers(arena);
		boolean max_players_ready = readMaxPlayers(arena);
		boolean centre_ready = readCentre(arena);
		boolean move_ready = readGameMoveSetting(arena);
		boolean lobby_ready = readLobbySpawn(arena);
		
		boolean chunk_maps_ready = readChunkMaps(arena);
		boolean chunks = readChunks(arena);
		
		ArenaCache cached_arena = cached_arenas.get(arena);
		
		if
		(
				spawnpoints_ready
				&&
				min_players_ready
				&&
				max_players_ready
				&&
				centre_ready
				&&
				move_ready
				&&
				lobby_ready
				&&
				chunk_maps_ready
				&&
				chunks
		)
		{
			if(cached_arena != null)
			{
				int num_spawns = cached_arena.getSpawnpoints().size();
				
				if(num_spawns == cached_arena.getMaximumPlayers())
				{
					boolean success = cached_arena.parseRestoreBytes();
					
					if(success)
					{
						return true;
					}
				}
			}
		}
		
		cached_arenas.remove(arena);
		return false;
	}
	
	public boolean writeLobbySpawn(String arena, Location location)
	{
		List<String> allowed_arenas = read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			return false;
		}
		
		String file_path = "arenas/" + arena;
		String arena_main_conf = arena + ".txt";
		
		String write_line = location.getX() + " " + location.getY() + " " + location.getZ();
		writeLine(file_path, arena_main_conf, "lobby_spawn", write_line);
		
		return true;
	}
	
	public boolean writeSpawnpoint(String arena, Location location)
	{
		List<String> allowed_arenas = read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			return false;
		}
		
		String file_path = "arenas/" + arena;
		String spawnpoints_file = "spawnpoints.txt";
		
		String write_line = location.getX() + " " + location.getY() + " " + location.getZ();
		write(file_path, spawnpoints_file, write_line, true);
		
		return true;
	}
	
	public boolean writeMaxPlayers(String arena, int max)
	{
		List<String> allowed_arenas = read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			return false;
		}
		
		String file_path = "arenas/" + arena;
		String arena_main_conf = arena + ".txt";
		
		writeLine(file_path, arena_main_conf, "maximum_players", String.valueOf(max));
		return true;
	}
	
	public boolean writeMinPlayers(String arena, int min)
	{
		List<String> allowed_arenas = read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			return false;
		}
		
		String file_path = "arenas/" + arena;
		String arena_main_conf = arena + ".txt";
		
		writeLine(file_path, arena_main_conf, "minimum_players", String.valueOf(min));
		return true;
	}
	
	public boolean writeMoveSetting(String arena, boolean movement)
	{
		List<String> allowed_arenas = read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			return false;
		}
		
		String file_path = "arenas/" + arena;
		String arena_main_conf = arena + ".txt";
		
		writeLine(file_path, arena_main_conf, "allow_movement", String.valueOf(movement));
		return true;
	}
	
	public boolean writeCentre(String arena, int chunk_x, int chunk_z)
	{
		List<String> allowed_arenas = read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			return false;
		}
		
		String file_path = "arenas/" + arena;
		String arena_main_conf = arena + ".txt";
		
		writeLine(file_path, arena_main_conf, "centre", chunk_x + " " + chunk_z);
		return true;
	}
	
	public void createFile(String file_path, String file_name, boolean root)
	{
		try
		{
			if(root)
			{
				new File(file_path).mkdirs();
				new File(file_path + "/" + file_name).createNewFile();
			}
			else
			{
				new File(path_name + file_path).mkdirs();
				new File(path_name + file_path + "/" + file_name).createNewFile();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getPluginFolder()
	{
		return path_name;
	}
	
	public void createFile(String file_name, boolean root)
	{
		try
		{
			if(root)
			{
				new File(file_name).createNewFile();
			}
			else
			{
				new File(path_name).mkdirs();
				new File(path_name + file_name).createNewFile();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void createDir(String dir_name, boolean root)
	{
		if(root)
			new File(dir_name).mkdirs();
		else
			new File(path_name + dir_name).mkdirs();
	}
	
	public boolean isDirCreated(String dir_name, boolean root)
	{
		if(root)
		{
			if(new File(dir_name).isDirectory())
				return true;
		}
		else
		{
			if(new File(path_name + dir_name).isDirectory())
				return true;
		}
		
		return false;
	}
	
	public boolean isFileCreated(String file_name, boolean root)
	{
		if(root)
		{
			if(new File(file_name).isFile())
				return true;
		}
		else
		{
			if(new File(path_name + file_name).isFile())
				return true;
		}
		
		return false;
	}
	
	public void deleteFile(String dir_name, boolean root)
	{
		if(root)
			new File(dir_name).delete();
		else
			new File(path_name + dir_name).delete();
	}
	
	public String[] listFiles(String dir_name)
	{
		try
		{
			String[] files = new File(path_name + dir_name).list();
			return files;
		}
		catch(NullPointerException e)
		{
			System.out.println("Pathname argument is null!");
			return null;
		}
	}
	
	public List<String> read(String file_name)
	{
		List<String> lines = new ArrayList<>();
		
		try(Scanner reader = new Scanner(new File(path_name + file_name)))
		{
			while(reader.hasNext())
			{
				lines.add(reader.nextLine());
			}
			
			return lines;
		}
		catch(FileNotFoundException e)
		{
			System.out.println("FILE " + file_name + " NOT FOUND!");
		}
		
		return lines;
	}
	
	public String readLine(String file_path, String file_name, String prefix)
	{
		String read_line = "";
		List<String> lines = read(file_path + "/" + file_name);
		
		for(int i = 0; i < lines.size(); i++)
		{
			String current_line = lines.get(i);
			
			if(current_line.contains(prefix + ":"))
				read_line = current_line.substring(current_line.indexOf(":") + 1).trim();
		}
		
		return read_line;
	}
	
	public List<String> readAllLines(String file_path, String file_name, String prefix)
	{
		List<String> found_lines = new ArrayList<>();
		List<String> lines = read(file_path + "/" + file_name);
		
		for(int i = 0; i < lines.size(); i++)
		{
			String current_line = lines.get(i);
			
			if(current_line.contains(prefix + ":"))
				found_lines.add(current_line.substring(current_line.indexOf(":") + 1).trim());
		}
		
		return found_lines;
	}
	
    public void writeBytes(String file_path, String file_name, byte[] write_bytes, boolean append) 
    {
    	createFile(file_path, file_name, false);
    	
    	try(OutputStream output_stream = new FileOutputStream(path_name + file_path + file_name, append))
    	{
    		output_stream.write(write_bytes);
    		output_stream.close();
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}
    }
	
	public void write(String file_path, String file_name, String[] write_lines, boolean append)
	{
		createFile(file_path, file_name, false);
		
		try(FileWriter writer = new FileWriter(path_name + file_path + file_name, append); BufferedWriter bw = new BufferedWriter(writer))
		{
			for(String write_line : write_lines)
			{
				bw.write(write_line);
				bw.newLine();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void write(String file_path, String file_name, String write_line, boolean append)
	{
		createFile(file_path, file_name, false);
		
		try(FileWriter writer = new FileWriter(path_name + file_path + file_name, append); BufferedWriter bw = new BufferedWriter(writer))
		{
			bw.write(write_line);
			bw.newLine();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeLine(String file_path, String file_name, String prefix, String write_line)
	{
		createFile(file_path, file_name, false);
		
		List<String> lines = read(file_path + "/" + file_name);
		
		try(FileWriter writer = new FileWriter(path_name + file_path + "/" + file_name); BufferedWriter bw = new BufferedWriter(writer))
		{
			if(prefix != "")
			{
				for(int i = 0; i < lines.size(); i++)
				{
					String current_line = lines.get(i);
					
					if(current_line.contains(prefix + ":"))
					{
						lines.remove(i);
					}
				}
				
				lines.add(prefix + ": " + write_line);
			}
			else
			{
				boolean found = false;
				
				for(int i = 0; i < lines.size(); i++)
				{
					String current_line = lines.get(i);
					
					if(current_line.contains(write_line))
					{
						found = true;
					}
				}
				
				if(!found)
				{
					lines.add(write_line);
				}
			}
			
			Iterator<String> write = lines.iterator();
			
			while(write.hasNext())
			{
				bw.write(write.next());
				bw.newLine();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}