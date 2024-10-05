package skywars;

public class ArenaChestItem
{
	private int item_location;
	private int item_count;
	private String item_material;
	
	public ArenaChestItem(int item_loc_in, int item_count_in, String item_material_in)
	{
		item_location = item_loc_in;
		item_count = item_count_in;
		item_material = item_material_in;
	}
	
	public int getItemLocation()
	{
		return item_location;
	}
	
	public String getItemMaterial()
	{
		return item_material;
	}
	
	public int getItemCount()
	{
		return item_count;
	}
}
