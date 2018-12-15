package engineers.workshop;

import engineers.workshop.common.network.PacketHandler;
import engineers.workshop.common.table.BlockTable;
import engineers.workshop.common.table.TileTable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.block.BlockItem;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EngineersWorkshop implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger();
	public static BlockTable blockTable;
	public static BlockEntityType<TileTable> blockEntityTable;


	@Override
	public void onInitialize() {
		blockTable = new BlockTable(FabricBlockSettings.of(Material.ANVIL).build());
		registerBlock("table", blockTable, ItemGroup.MISC);

		blockEntityTable = register("table", BlockEntityType.Builder.create(TileTable::new));

		PacketHandler.init();
	}

	public static Block registerBlock(String name, Block block, ItemGroup tab) {
		Registry.register(Registry.BLOCK, "engineersworkshop:" + name, block);
		BlockItem item = new BlockItem(block, new Item.Settings().itemGroup(tab));
		item.registerBlockItemMap(Item.BLOCK_ITEM_MAP, item);
		registerItem(name, item);
		return block;
	}

	public static Item registerItem(String name, Item item) {
		Registry.register(Registry.ITEM, "engineersworkshop:" + name, item);
		return item;
	}

	public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
		BlockEntityType<T> blockEntityType = builder.method_11034(null);
		Registry.register(Registry.BLOCK_ENTITY, "engineersworkshop:" + name, blockEntityType);
		return blockEntityType;
	}
}
