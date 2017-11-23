package engineers.workshop.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
	}

	public EntityPlayer getPlayer() {
		throw new RuntimeException("Not supported on the server");
	}

}
