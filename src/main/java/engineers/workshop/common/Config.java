package engineers.workshop.common;


public final class Config {



	public static class TWEAKS {
		public static int FUEL_DELAY;


		@Override
		public void load() {
			FUEL_DELAY = config.getInt("Fuel Delay", category, 15, 0, Integer.MAX_VALUE,
				"Sets the amount of ticks between each time the worktable consumes a fuel resource");
		}
	}

	public static class POWER  {
		public static boolean RF_SUPPORT;

		@Override
		public void load() {
			RF_SUPPORT = config.getBoolean("RF Support", "Power", true,
				"Should RF upgrades be allowed?");
		}
	}

	public static class MACHINES {

		public static String[] CRAFTER_BLOCKS, FURNACE_BLOCKS, CRUSHER_BLOCKS, ALLOY_BLOCKS, STORAGE_BLOCKS;


		@Override
		public void load() {

			CRAFTER_BLOCKS = config.getStringList("Crafter Blocks", "Machines", new String[] { "minecraft:crafting_table" }, "What blocks should the table accept for crafters.");
			FURNACE_BLOCKS = config.getStringList("Furnace Blocks", "Machines", new String[] { "minecraft:furnace" }, "What blocks should the table accept for furances.");
			CRUSHER_BLOCKS = config.getStringList("Crusher Blocks", "Machines", new String[] { "enderio:blockSagMill" }, "What blocks should the table accept for crushers.");
			ALLOY_BLOCKS = config.getStringList("Alloy Blocks", "Machines", new String[] { "enderio:blockAlloySmelter" }, "What blocks should the table accept for alloy smelters.");
			STORAGE_BLOCKS = config.getStringList("Storage Blocks", "Machines", new String[] { "minecraft:chest" }, "What blocks should the table accept for storage.");
		}
	}
}
