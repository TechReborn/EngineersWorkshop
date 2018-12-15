package engineers.workshop;

import engineers.workshop.common.Config;
import engineers.workshop.common.items.ItemUpgrade;
import engineers.workshop.common.network.DataPacket;
import engineers.workshop.common.table.BlockTable;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;



public class EngineersWorkshop implements ModInitializer {

	public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

	public static BlockTable blockTable;
	public static Item itemUpgrade;


	@Override
	public void onInitialize() {
		blockTable = new BlockTable();
		itemUpgrade = new ItemUpgrade();
	}
}
