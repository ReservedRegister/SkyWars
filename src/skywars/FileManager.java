package skywars;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class FileManager
{
	private SkyWars pl;
	private final String path_name;
	
	public FileManager(SkyWars plugin)
	{
		pl = plugin;
		path_name = "plugins/" + pl.getDescription().getName() + "/";
	}
	
	public void createFile(String file_path, String file_name, boolean root)
	{
		try
		{
			if(root)
			{
				new File(file_path).mkdirs();
				new File(file_path + file_name).createNewFile();
			}
			else
			{
				new File(path_name + file_path).mkdirs();
				new File(path_name + file_path + file_name).createNewFile();
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
		List<String> lines = read(file_path + file_name);
		
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
		List<String> lines = read(file_path + file_name);
		
		for(int i = 0; i < lines.size(); i++)
		{
			String current_line = lines.get(i);
			
			if(current_line.contains(prefix + ":"))
				found_lines.add(current_line.substring(current_line.indexOf(":") + 1).trim());
		}
		
		return found_lines;
	}
	
	public List<String> readSectionSingle(String file_path, String file_name, String stopLine, String searchLine)
	{
		List<String> found_lines = new ArrayList<>();
		boolean found = false;
		
		try(Scanner reader = new Scanner(new File(path_name + file_path + file_name)))
		{
			while(reader.hasNext())
			{
				String line = reader.nextLine();
				
				if(found)
				{
					if(!line.startsWith(stopLine))
						found_lines.add(line);
					else
						break;
				}
				else if(line.equals(searchLine))
					found = true;
			}
		}
		catch(FileNotFoundException e)
		{
			System.out.println("FILE " + file_name + " NOT FOUND!");
		}
		
		return found_lines;
	}
	
	public List<String> readSection(String file_path, String file_name, String[] sections)
	{
		List<String> section_lines = new ArrayList<>();
		List<String> lines = read(file_path + file_name);
		boolean found = false;
		int section_counter = 0;
		
		if(sections == null)
			return section_lines;
		
		for(int i = 0; i < lines.size(); i++)
		{
			String current_line = lines.get(i);
			
			if(found)
			{
				if(current_line.charAt(0) == '-')
				{
					section_lines.add(current_line.replaceFirst("-", "").trim());
					continue;
				}
				else
					break;
			}
			
			if(current_line.equals(sections[section_counter]))
			{
				section_counter++;
				
				if(section_counter == sections.length)
					found = true;
			}
		}
		
		return section_lines;
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
		
		List<String> lines = read(file_path + file_name);
		
		try(FileWriter writer = new FileWriter(path_name + file_path + file_name); BufferedWriter bw = new BufferedWriter(writer))
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
	
	public void writeToSection(String file_path, String file_name, String[] sections, String[] write_lines)
	{
		createFile(file_path, file_name, false);
		
		List<String> lines = read(file_path + file_name);
		
		try(FileWriter writer = new FileWriter(path_name + file_path + file_name); BufferedWriter bw = new BufferedWriter(writer))
		{
			int section_counter = 0;
			List<Integer> section_start = new ArrayList<>();
			
			if(sections == null)
			{
				System.out.println("Sections were empty not saving!");
				return;
			}
			
			for(int i = 0; i < lines.size(); i++)
			{
				String current_line = lines.get(i);
				
				if(current_line.equals(sections[section_counter]))
				{
					section_start.add(i);
					section_counter++;
					
					if(section_counter == sections.length)
						break;
				}
			}
			
			if(section_counter == sections.length)
			{
				int start = section_start.get(section_start.size() - 1);
				for(int i = 0; i < lines.size(); i++)
				{
					String current_line = lines.get(i);
					
					bw.write(current_line);
					bw.newLine();
					
					if(i == start+1)
					{
						for(int j = 0; j < write_lines.length; j++)
						{
							bw.write("- " + write_lines[j]);
							bw.newLine();
						}
					}
				}
			}
			else
			{
				int start = -1;
				List<String> new_sections = new ArrayList<>();
				
				for(int i = 0; i < sections.length; i++)
				{
					try
					{
						start = section_start.get(i);
					}
					catch(IndexOutOfBoundsException e)
					{
						new_sections.add(sections[i]);
					}
				}
				
				if(start != -1)
				{
					for(int i = 0; i < lines.size(); i++)
					{
						String current_line = lines.get(i);
						
						if(i == start+1)
						{
							for(int k = 0; k < new_sections.size(); k++)
							{
								bw.write(new_sections.get(k));
								bw.newLine();
							}
							
							for(int j = 0; j < write_lines.length; j++)
							{
								bw.write("- " + write_lines[j]);
								bw.newLine();
							}
						}
						
						bw.write(current_line);
						bw.newLine();
					}
				}
				else
				{
					for(int i = 0; i < lines.size(); i++)
					{
						bw.write(lines.get(i));
						bw.newLine();
					}
					
					for(int i = 0; i < sections.length; i++)
					{
						bw.write(sections[i]);
						bw.newLine();
					}
					
					for(int j = 0; j < write_lines.length; j++)
					{
						bw.write("- " + write_lines[j]);
						bw.newLine();
					}
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}