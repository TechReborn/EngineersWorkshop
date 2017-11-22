package engineers.workshop.proxy;

import engineers.workshop.EngineersWorkshop;
import engineers.workshop.client.GuiHandler;
import engineers.workshop.common.loaders.ConfigLoader;
import engineers.workshop.common.loaders.RecipeLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

	public Side getSide() {
		return Side.SERVER;
	}

	public void preInit(FMLPreInitializationEvent event) {

		RecipeLoader.loadRecipes();
		ConfigLoader.loadConfig(event.getSuggestedConfigurationFile());
	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(EngineersWorkshop.instance, new GuiHandler());

	}

	public void postInit(FMLPostInitializationEvent event) {}

	public EntityPlayer getPlayer() {
		throw new RuntimeException("Not supported on the server");
	}

}
