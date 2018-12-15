package engineers.workshop;

import engineers.workshop.common.table.BlockTable;
import engineers.workshop.common.table.TileTable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
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
		Registry.BLOCK.register(new Identifier("ewr", "table"), blockTable);

		blockEntityTable = register("table", BlockEntityType.Builder.create(TileTable::new));
	}

	public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
		BlockEntityType<T> blockEntityType = builder.method_11034(null);
		Registry.register(Registry.BLOCK_ENTITY, "ewr:" + name, blockEntityType);
		return blockEntityType;
	}
}
