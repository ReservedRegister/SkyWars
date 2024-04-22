package skywars;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ChunkWriter
{
	private SkyWars pl;
	private OutputStream write_stream;
	private String path_to_chunks;
	private int bytes_written;
	
	public ChunkWriter(SkyWars plugin, String file_path, String file_name, boolean append)
	{
		pl = plugin;
		path_to_chunks = file_path;
		bytes_written = 0;
		
		createWriteTask(file_path, file_name, append);
	}
	
	public synchronized void writeBytes(byte[] label, byte[] write_lines)
	{
		try
		{
			write_stream.write(label);
			write_stream.write(write_lines);
			
			bytes_written = bytes_written + label.length + write_lines.length;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public synchronized void closeWriter()
	{
		try
		{
			write_stream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeChunkSize()
	{
		pl.getFileManager().write(path_to_chunks, "chunks_size.txt", String.valueOf(bytes_written), false);
	}
	
	private void createWriteTask(String file_path, String file_name, boolean append)
	{
		pl.getFileManager().createFile(file_path, file_name, false);
		
		try
		{
			write_stream = new FileOutputStream(pl.getFileManager().getPluginFolder() + file_path + "/" + file_name, append);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
