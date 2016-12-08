package engineers.workshop.gui.container.slot;

import engineers.workshop.gui.page.Page;
import engineers.workshop.table.TileTable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class SlotFuel extends SlotTable {
	public SlotFuel(TileTable table, Page page, int id, int x, int y) {
		super(table, page, id, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {
		
		return super.isItemValid(itemstack) && TileEntityFurnace.isItemFuel(itemstack);
	}

	@Override
	public boolean canShiftClickInto(ItemStack item) {
		return !item.getItem().equals(Item.getItemFromBlock(Blocks.CRAFTING_TABLE));
	}
}
