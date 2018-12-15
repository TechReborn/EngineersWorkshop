package engineers.workshop.common.network;

import engineers.workshop.client.container.ContainerTable;
import engineers.workshop.common.network.data.DataType;
import engineers.workshop.common.table.TileTable;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;

public class DataPacket {

	public BlockPos tablePos;
	public PacketId packetId;
	public CompoundTag compound;
	public DataType dataType;

	public CompoundTag createCompound() {
		CompoundTag tagCompound = new CompoundTag();
		this.compound = tagCompound;
		return tagCompound;
	}

	public void writeData(PacketByteBuf buffer) throws IOException {
		if (tablePos != null) {
			buffer.writeBoolean(true);
			buffer.writeBlockPos(tablePos);
		} else {
			buffer.writeBoolean(false);
			buffer.writeBlockPos(new BlockPos(0, 0, 0));
		}

		if (compound != null) {
			buffer.writeBoolean(true);
			buffer.writeCompoundTag(compound);
		} else {
			buffer.writeBoolean(false);
			buffer.writeCompoundTag(new CompoundTag());
		}

		if (dataType != null) {
			buffer.writeBoolean(true);
			buffer.writeInt(dataType.ordinal());
		} else {
			buffer.writeBoolean(false);
			buffer.writeInt(0);
		}

		buffer.writeInt(packetId.ordinal());
	}

	public void readData(PacketByteBuf buffer) throws IOException {
		if (buffer.readBoolean()) {
			tablePos = buffer.readBlockPos();
		} else {
			buffer.readBlockPos();
			tablePos = null;
		}
		if (buffer.readBoolean()) {
			compound = buffer.readCompoundTag();
		} else {
			buffer.readCompoundTag();
		}

		if (buffer.readBoolean()) {
			dataType = DataType.values()[buffer.readInt()];
		} else {
			buffer.readInt();
		}

		packetId = PacketId.values()[buffer.readInt()];
	}



	public void processData(DataPacket message, PacketContext context) {
		onPacket(message, context.getPlayer(), !context.getPlayer().getEntityWorld().isRemote);
	}

	private void onPacket(DataPacket message, PlayerEntity player,
	                      boolean onServer) {
		PacketId id = message.packetId;
		TileTable table = null;

		if (id.isInInterface()) {
			if (player.container instanceof ContainerTable) {
				table = ((ContainerTable) player.container).getTable();
			}
		}
		if (table == null && message.tablePos != null) {
			BlockPos tablePos = message.tablePos;
			World world = player.world;
			if (!world.isBlockLoaded(tablePos))
				return;
			BlockEntity te = world.getBlockEntity(tablePos);
			if (te instanceof TileTable) {
				table = (TileTable) te;
			}
		}

		if (table != null) {
			if (onServer) {
				table.receiveServerPacket(message, id, player);
			} else {
				table.receiveClientPacket(message, id);
			}
		}
	}
}
