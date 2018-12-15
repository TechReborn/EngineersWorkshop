package engineers.workshop;

import engineers.workshop.common.items.ItemUpgrade;
import engineers.workshop.common.table.BlockTable;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;



public class EngineersWorkshop implements ModInitializer {

	public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

	public static BlockTable blockTable;


	@Override
	public void onInitialize() {
		blockTable = new BlockTable();
	}
}
