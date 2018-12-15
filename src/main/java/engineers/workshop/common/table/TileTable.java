package engineers.workshop.common.table;

import engineers.workshop.EngineersWorkshop;
import engineers.workshop.client.container.slot.SlotBase;
import engineers.workshop.client.container.slot.SlotFuel;
import engineers.workshop.client.menu.GuiMenu;
import engineers.workshop.client.menu.GuiMenuItem;
import engineers.workshop.client.page.Page;
import engineers.workshop.client.page.PageMain;
import engineers.workshop.client.page.PageTransfer;
import engineers.workshop.client.page.PageUpgrades;
import engineers.workshop.client.page.setting.Setting;
import engineers.workshop.client.page.setting.Side;
import engineers.workshop.client.page.setting.Transfer;
import engineers.workshop.common.items.Upgrade;
import engineers.workshop.common.network.*;
import engineers.workshop.common.network.data.DataType;
import engineers.workshop.common.unit.Unit;
import engineers.workshop.common.unit.UnitCraft;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TileTable extends BlockEntity implements Inventory, SidedInventory, Tickable {

	private static final int MOVE_DELAY = 20;
	private static final int SLOT_DELAY = 10;
	private static final String NBT_ITEMS = "item";
	private static final String NBT_UNITS = "units";
	private static final String NBT_SETTINGS = "settings";
	private static final String NBT_SIDES = "sides";
	private static final String NBT_INPUT = "input";
	private static final String NBT_OUTPUT = "output";
	private static final String NBT_SLOT = "slot";
	private static final String NBT_POWER = "fuel";
	private static final String NBT_MAX_POWER = "max_power";
	private static final int COMPOUND_ID = 10;
	private static final IBitCount GRID_ID_BITS = new LengthCount(4);
	public int maxFuel = 8000;
	private List<Page> pages;
	private Page selectedPage;
	private List<SlotBase> slots;
	private DefaultedList<ItemStack> items;
	private GuiMenu menu;
	private int fuel;
	private SlotFuel fuelSlot;
	private List<PlayerEntity> players = new ArrayList<>();
	private int fuelTick = 0;
	private int moveTick = 0;
	private boolean lit;
	private boolean lastLit;
	private int slotTick = 0;
	private boolean firstUpdate = true;
	private int tickCount = 0;
	private int[][] sideSlots = new int[6][];

	public TileTable() {
		super();
		pages = new ArrayList<>();
		pages.add(new PageMain(this, "main"));
		pages.add(new PageTransfer(this, "transfer"));
		pages.add(new PageUpgrades(this, "upgrade"));
		// pages.add(new PageSecurity(this, "security"));

		slots = new ArrayList<>();
		int id = 0;
		addSlot(fuelSlot = new SlotFuel(this, null, id++, 226, 226));
		for (Page page : pages) {
			id = page.createSlots(id);
		}
		items = DefaultedList.create(slots.size(), ItemStack.EMPTY);
		setSelectedPage(pages.get(0));
		onUpgradeChange();
	}

	public int getFuel() {
		return fuel;
	}

	public void setFuel(int fuel) {
		this.fuel = fuel;
	}

	public void setCapacity(int newCap) {
		this.maxFuel = newCap;
	}

	public List<SlotBase> getSlots() {
		return slots;
	}

	public List<Page> getPages() {
		return pages;
	}

	public Page getSelectedPage() {
		return selectedPage;
	}

	public void setSelectedPage(Page selectedPage) {
		this.selectedPage = selectedPage;
	}

	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public int getInvSize() {
		return items.size();
	}

	@Override
	public boolean isInvEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public PageMain getMainPage() {
		return (PageMain) pages.get(0);
	}

	public PageTransfer getTransferPage() {
		return (PageTransfer) pages.get(1);
	}

	public PageUpgrades getUpgradePage() {
		return (PageUpgrades) pages.get(2);
	}

	@Override
	public ItemStack getInvStack(int id) {
		return items.get(id);
	}

	@Override
	public ItemStack takeInvStack(int id, int count) {
		ItemStack item = getInvStack(id);
		if (!item.isEmpty()) {
			if (item.getAmount() <= count) {
				setInvStack(id, ItemStack.EMPTY);
				return item;
			}
			return item.split(count);
		} else {
			return ItemStack.EMPTY;
		}
	}

	public ItemStack getStackInSlotOnClosing(int id) {
		if (slots.get(id).shouldDropOnClosing()) {
			ItemStack item = getInvStack(id);
			setInvStack(id, ItemStack.EMPTY);
			return item;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInvStack(int id,
		                                     ItemStack item) {
		items.set(id, item);
	}

	@Override
	public int getInvMaxStackAmount() {
		return 64;
	}

	@Override
	public boolean canPlayerUseInv(PlayerEntity player) {
		return player.distanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
	}

	public void addSlot(SlotBase slot) {
		slots.add(slot);
	}

	public List<PlayerEntity> getOpenPlayers() {
		return players;
	}

	public void addPlayer(PlayerEntity player) {
		EngineersWorkshop.LOGGER.debug("Trying to add player %s", player.getName());
		if (!players.contains(player)) {
			players.add(player);
			sendAllDataToPlayer(player);
		} else {
			EngineersWorkshop.LOGGER.error("Trying to add a listening player: " + player.getName());
		}
	}

	public void removePlayer(PlayerEntity player) {
		EngineersWorkshop.LOGGER.debug("Trying to remove player %s", player.getName());
		if (!players.remove(player)) {
			EngineersWorkshop.LOGGER.error("Trying to remove non-listening player: " + player.getName());
		}
	}

	private void sendAllDataToPlayer(PlayerEntity player) {
		DataPacket packet = PacketHandler.getPacket(this, PacketId.ALL);
		for (DataType dataType : DataType.values()) {
			if (dataType != null && this != null && packet != null)
				dataType.save(this, packet.createCompound(), -1);
		}
		PacketHandler.sendToPlayer(packet, player);
	}

	private void sendDataToPlayer(DataType type, PlayerEntity player) {
		DataPacket packet = PacketHandler.getPacket(this, PacketId.RENDER_UPDATE);
		type.save(this, packet.createCompound(), -1);
		PacketHandler.sendToPlayer(packet, player);
	}

	public void sendDataToAllPlayers(DataType dataType, List<PlayerEntity> players) {
		sendDataToAllPlayers(dataType, 0, players);
	}

	public void sendDataToAllPlayers(DataType dataType, int id, List<PlayerEntity> players) {
		sendToAllPlayers(getWriterForType(dataType, id), players);
	}

	private void sendDataToAllPlayersExcept(DataType dataType, int id, PlayerEntity ignored,
	                                        List<PlayerEntity> players) {
		sendToAllPlayersExcept(getWriterForType(dataType, id), ignored, players);
	}

	private void sendToAllPlayers(DataPacket dw, List<PlayerEntity> players) {
		sendToAllPlayersExcept(dw, null, players);
	}

	private void sendToAllPlayersExcept(DataPacket dw, PlayerEntity ignored, List<PlayerEntity> players) {
		//list is copied to prevent very rare CME's
		new ArrayList<PlayerEntity>().stream().filter(player -> !player.equals(ignored))
			.forEach(player -> PacketHandler.sendToPlayer(dw, player));
	}

	public void updateServer(DataType dataType) {
		updateServer(dataType, 0);
	}

	public void updateServer(DataType dataType, int id) {
		PacketHandler.sendToServer(getWriterForType(dataType, id));
	}

	private DataPacket getWriterForType(DataType dataType, int id) {
		DataPacket packet = PacketHandler.getPacket(this, PacketId.TYPE);
		packet.dataType = dataType;
		dataType.save(this, packet.createCompound(), id);
		return packet;
	}

	public void receiveServerPacket(DataPacket dr, PacketId id, PlayerEntity player) {
		switch (id) {
			case TYPE:
				DataType dataType = dr.dataType;
				int index = dataType.load(this, dr.compound, false);
				if (index != -1 && dataType.shouldBounce(this)) {
					sendDataToAllPlayersExcept(dataType, index, dataType.shouldBounceToAll(this) ? null : player, players);
				}
				if (dataType == DataType.SIDE_ENABLED) {
					onSideChange();
				}
				markDirty();
				break;
			case CLOSE:
				removePlayer(player);
				break;
			case RE_OPEN:
				addPlayer(player);
				break;
			case CLEAR:
				clearGrid(player, dr.compound.getInt("clear"));
				break;
			case ALL:
				break;
			case UPGRADE_CHANGE:
				onUpgradeChange();
				break;
			default:
				break;
		}
	}

	public void receiveClientPacket(DataPacket dr, PacketId id) {
		switch (id) {
			case ALL:
				for (DataType dataType : DataType.values()) {
					dataType.load(this, dr.compound, true);
				}
				onUpgradeChange();
				break;
			case TYPE:
				DataType dataType = dr.dataType;
				dataType.load(this, dr.compound, false);
				if (dataType == DataType.SIDE_ENABLED) {
					onSideChange();
				}
				break;
			case UPGRADE_CHANGE:
				onUpgradeChange();
				break;
			default:
				break;
		}
	}

	@Override
	public void tick() {
		tickCount++;
		pages.forEach(Page::onUpdate);

		if (firstUpdate) {
			onUpgradeChangeDistribute();
			onSideChange();
			onUpgradeChange();
			firstUpdate = false;
		}

		if (!world.isRemote && ++moveTick >= MOVE_DELAY) {
			moveTick = 0;
			if (getUpgradePage().hasGlobalUpgrade(Upgrade.AUTO_TRANSFER)) {
				int transferSize = (int) Math.pow(2, getUpgradePage().getGlobalUpgradeCount(Upgrade.TRANSFER));
				for (Setting setting : getTransferPage().getSettings()) {
					for (Side side : setting.getSides()) {
						transfer(setting, side, side.getInput(), transferSize);
						transfer(setting, side, side.getOutput(), transferSize);
					}
				}
			}
		}

		if (!world.isRemote && ++slotTick >= SLOT_DELAY) {
			slotTick = 0;
			slots.stream().filter(SlotBase::isEnabled).forEach(SlotBase::updateServer);
		}

		if (!world.isRemote) {
			if (tickCount % 20 == 0) {
				int x1 = getPos().getX() - 16;
				int x2 = getPos().getX() + 16;
				int z1 = getPos().getY() - 16;
				int z2 = getPos().getY() + 16;
				BoundingBox aabb = new BoundingBox(x1, 0, z1, x2, 255, z2);
				List<PlayerEntity> updatePlayers = world.getEntities(PlayerEntity.class, aabb, (Predicate<Entity>) entity -> true);
				updatePlayers.removeAll(players);
			}
			updateFuel();
		}
	}

	private void transfer(Setting setting, Side side, Transfer transfer, int transferSize) {
		if (transfer.isEnabled() && transfer.isAuto()) {
			Direction direction = side.getDirection();
			BlockPos nPos = pos.offset(direction);
			BlockEntity te = world.getBlockEntity(nPos);
			if (te instanceof Inventory) {
				Inventory inventory = (Inventory) te;
				/*
				 * if (te instanceof BlockEntityChest) { // inventory =
				 * Blocks.CHEST.func_149951_m(te.getWorld(), //
				 * te.getPos().getX(), te.getPos().getY(), //
				 * te.getPos().getX()); if (inventory == null) { return; } }
				 * else { inventory = (Inventory) te; }
				 */

				List<SlotBase> transferSlots = setting.getSlots();
				if (transferSlots == null) {
					return;
				}
				int[] slots1 = new int[transferSlots.size()];
				for (int i = 0; i < transferSlots.size(); i++) {
					slots1[i] = transferSlots.get(i).id;
				}
				int[] slots2;
				Direction directionReversed = direction.getOpposite();
				if (inventory instanceof SidedInventory) {
					slots2 = ((SidedInventory) inventory).getInvAvailableSlots(directionReversed);
				} else {
					slots2 = new int[inventory.getInvSize()];
					for (int i = 0; i < slots2.length; i++) {
						slots2[i] = i;
					}
				}
				if (slots2 == null || slots2.length == 0) {
					return;
				}

				if (transfer.isInput()) {

					transfer(inventory, this, slots2, slots1, directionReversed, direction, transferSize);
				} else {
					transfer(this, inventory, slots1, slots2, direction, directionReversed, transferSize);
				}
			}
		}
	}

	private void transfer(Inventory from, Inventory to, int[] fromSlots, int[] toSlots, Direction fromSide,
	                      Direction toSide, int maxTransfer) {
		int oldTransfer = maxTransfer;

		try {
			SidedInventory fromSided = fromSide.ordinal() != -1 && from instanceof SidedInventory
			                            ? (SidedInventory) from : null;
			SidedInventory toSided = toSide.ordinal() != -1 && to instanceof SidedInventory ? (SidedInventory) to
			                                                                                  : null;

			for (int fromSlot : fromSlots) {
				ItemStack fromItem = from.getInvStack(fromSlot);
				if (!fromItem.isEmpty() && fromItem.getAmount() > 0) {
					if (fromSided == null || fromSided.canExtractInvStack(fromSlot, fromItem, fromSide)) {
						if (fromItem.canStack()) {
							for (int toSlot : toSlots) {
								ItemStack toItem = to.getInvStack(toSlot);
								if (!toItem.isEmpty() && toItem.getAmount() > 0) {
									if (toSided == null || toSided.canInsertInvStack(toSlot, fromItem, toSide)) {
										if (fromItem.isEqualIgnoreTags(toItem)
											&& ItemStack.areTagsEqual(toItem, fromItem)) {
											int maxSize = Math.min(toItem.getMaxAmount(),
												to.getInvMaxStackAmount());
											int maxMove = Math.min(maxSize - toItem.getAmount(),
												Math.min(maxTransfer, fromItem.getAmount()));
											toItem.addAmount(maxMove);
											maxTransfer -= maxMove;
											fromItem.subtractAmount(maxMove);

											if (maxTransfer == 0) {
												return;
											} else if (fromItem.isEmpty()) {
												break;
											}
										}
									}
								}
							}
						}
						if (fromItem.getAmount() > 0) {
							for (int toSlot : toSlots) {
								ItemStack toItem = to.getInvStack(toSlot);
								if (toItem.isEmpty() && to.isValidInvStack(toSlot, fromItem)) {
									if (toSided == null || toSided.canInsertInvStack(toSlot, fromItem, toSide)) {
										toItem = fromItem.copy();
										toItem.setAmount(Math.min(maxTransfer, fromItem.getAmount()));
										to.setInvStack(toSlot, toItem);
										maxTransfer -= toItem.getAmount();
										fromItem.subtractAmount(toItem.getAmount());

										if (maxTransfer == 0) {
											return;
										} else if (fromItem.isEmpty()) {
											break;
										}
									}
								}
							}
						}
					}
				}

			}
		} finally {
			if (oldTransfer != maxTransfer) {
				to.markDirty();
				from.markDirty();
			}
		}
	}

	private void updateFuel() {
		if (lastLit != lit) {
			lastLit = lit;
			sendDataToAllPlayers(DataType.LIT, players);
		}

		ItemStack fuel = fuelSlot.getStack();
		if (!fuel.isEmpty() && fuelSlot.canAcceptItem(fuel)) {
			int fuelLevel = FurnaceBlockEntity.getBurnTimeMap().get(fuel.getItem());
			if (fuelLevel > 0 && fuelLevel + this.fuel <= maxFuel) {
				this.fuel += fuelLevel;
				if (fuel.getItem().hasContainerItem(fuel)) {
					fuelSlot.setStack(fuel.getItem().getContainerItem(fuel).copy());
				} else {
					takeInvStack(fuelSlot.id, 1);
				}
			}
		}

		if (this.fuel > maxFuel)
			this.fuel = maxFuel;
	}

	public void onUpgradeChangeDistribute() {
		if (!world.isRemote) {
			onUpgradeChange();
			world.notifyNeighborsOfStateChange(pos, EngineersWorkshop.blockTable, true);
			sendToAllPlayers(PacketHandler.getPacket(this, PacketId.UPGRADE_CHANGE), players);
		} else {
			getUpgradePage().onUpgradeChange();
		}
	}

	public void onUpgradeChange() {
		reloadTransferSides();
		getUpgradePage().onUpgradeChange();
		getMainPage().getCraftingList().forEach(UnitCraft::onUpgradeChange);
	}

	public void onSideChange() {
		reloadTransferSides();
	}

	private void reloadTransferSides() {
		for (int i = 0; i < sideSlots.length; i++) {
			for (SlotBase slot : slots) {
				slot.resetValidity(i);
			}

			List<SlotBase> slotsForSide = new ArrayList<>();

			for (Setting setting : getTransferPage().getSettings()) {
				Transfer input = setting.getSides().get(i).getInput();
				Transfer output = setting.getSides().get(i).getOutput();

				if (input.isEnabled() || output.isEnabled()) {
					List<SlotBase> unitSlots = setting.getSlots();
					if (unitSlots != null) {
						slotsForSide.addAll(unitSlots);
						for (SlotBase unitSlot : unitSlots) {
							boolean isSlotInput = input.isEnabled() && unitSlot.canAcceptItems();
							boolean isSlotOutput = output.isEnabled() && unitSlot.canSupplyItems();

							unitSlot.setValidity(i, isSlotInput ? input : null, isSlotOutput ? output : null);
						}
					}
				}
			}
			sideSlots[i] = getSlotIndexArray(slotsForSide);
		}
	}

	private int[] getSlotIndexArray(List<SlotBase> slots) {
		int[] result = new int[slots.size()];
		for (int j = 0; j < slots.size(); j++) {
			result[j] = slots.get(j).id;
		}
		return result;
	}

	@Override
	public boolean isValidInvStack(int id, ItemStack item) {
		return slots.get(id).canAcceptItem(item);
	}

	@Override
	public boolean canInsertInvStack(int slot, ItemStack item, Direction side) {
		return isValidInvStack(slot, item) && slots.get(slot).canAcceptItem(item)
			&& slots.get(slot).isInputValid(side.ordinal(), item);
	}

	@Override
	public boolean canExtractInvStack(int slot, ItemStack item, Direction side) {
		return slots.get(slot).isOutputValid(side.ordinal(), item);
	}

	public GuiMenu getMenu() {
		return menu;
	}

	public void setMenu(GuiMenuItem menu) {
		this.menu = menu;
	}

	public boolean isLit() {
		return lit;
	}

	public void setLit(boolean lit) {
		this.lit = lit;
	}

	@Override
	public CompoundTag getUpdateTag() {
		return writeToNBT(new CompoundTag());
	}

	@Override
	public SPacketUpdateBlockEntity getUpdatePacket() {
		CompoundTag nbt = new CompoundTag();
		this.writeToNBT(nbt);
		return new SPacketUpdateBlockEntity(getPos(), 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateBlockEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
	}

	@Override
	public CompoundTag toTag(CompoundTag compound) {
		compound.putInt(NBT_POWER, fuel);
		compound.putInt(NBT_MAX_POWER, maxFuel);

		ListTag itemList = new ListTag();
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isEmpty()) {
				CompoundTag slotTag = items.get(i).toTag(new CompoundTag());
				slotTag.putInt(NBT_SLOT, i);
				itemList.add(slotTag);
			}
		}
		compound.put(NBT_ITEMS, itemList);

		ListTag unitList = new ListTag();
		for (Unit unit : getMainPage().getUnits()) {
			unitList.add(unit.writeToNBT(new CompoundTag()));
		}
		compound.put(NBT_UNITS, unitList);

		ListTag settingList = new ListTag();
		for (Setting setting : getTransferPage().getSettings()) {
			CompoundTag settingCompound = new CompoundTag();

			ListTag sideList = new ListTag();
			for (Side side : setting.getSides()) {
				CompoundTag sideCompound = new CompoundTag();
				CompoundTag inputCompound = new CompoundTag();
				CompoundTag outputCompound = new CompoundTag();

				side.getInput().writeToNBT(inputCompound);
				side.getOutput().writeToNBT(outputCompound);

				sideCompound.put(NBT_INPUT, inputCompound);
				sideCompound.put(NBT_OUTPUT, outputCompound);
				sideList.add(sideCompound);
			}
			settingCompound.put(NBT_SIDES, sideList);
			settingList.add(settingCompound);
		}
		compound.put(NBT_SETTINGS, settingList);

		return super.toTag(compound);
	}

	@Override
	public void fromTag(CompoundTag compound) {
		super.fromTag(compound);
		fuel = compound.getInt(NBT_POWER);
		maxFuel = compound.getInt(NBT_MAX_POWER);

		items = DefaultedList.create(getInvSize(), ItemStack.EMPTY);

		ListTag itemList = compound.getList(NBT_ITEMS, COMPOUND_ID);
		for (int i = 0; i < itemList.size(); i++) {
			CompoundTag slotCompound = itemList.getCompoundTag(i);
			int id = slotCompound.getInt(NBT_SLOT);
			if (id < 0) {
				id += 256;
			}
			if (id >= 0 && id < items.size()) {
				items.set(id, ItemStack.fromTag(slotCompound));
			}
		}

		ListTag unitList = compound.getList(NBT_UNITS, COMPOUND_ID);
		List<Unit> units = getMainPage().getUnits();
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			CompoundTag unitCompound = unitList.getCompoundTag(i);
			unit.readFromNBT(unitCompound);
		}

		ListTag settingList = compound.getList(NBT_SETTINGS, COMPOUND_ID);
		List<Setting> settings = getTransferPage().getSettings();

		for (int i = 0; i < settings.size(); i++) {
			Setting setting = settings.get(i);
			CompoundTag settingCompound = settingList.getCompoundTag(i);
			ListTag sideList = settingCompound.getList(NBT_SIDES, COMPOUND_ID);
			List<Side> sides = setting.getSides();
			for (int j = 0; j < sides.size(); j++) {
				Side side = sides.get(j);
				CompoundTag sideCompound = sideList.getCompoundTag(j);
				CompoundTag inputCompound = sideCompound.getCompound(NBT_INPUT);
				CompoundTag outputCompound = sideCompound.getCompound(NBT_OUTPUT);

				side.getInput().readFromNBT(inputCompound);
				side.getOutput().readFromNBT(outputCompound);
			}
		}

	}

	public void spitOutItem(ItemStack item) {

		float offsetX, offsetY, offsetZ;
		offsetX = offsetY = offsetZ = world.random.nextFloat() * 0.8F + 1.0F;

		ItemEntity entityItem = new ItemEntity(world, pos.getX() + offsetX, pos.getY() + offsetY,
			pos.getZ() + offsetZ, item.copy());
		entityItem.velocityX = world.random.nextGaussian() * 0.05F;
		entityItem.velocityY = world.random.nextGaussian() * 0.05F + 0.2F;
		entityItem.velocityZ = world.random.nextGaussian() * 0.05F;

		world.spawnEntity(entityItem);
	}

	public void clearGridSend(int id) {
		DataPacket dw = PacketHandler.getPacket(this, PacketId.CLEAR);
		dw.createCompound().putInt("clear", id);
		PacketHandler.sendToServer(dw);
	}

	private void clearGrid(PlayerEntity player, int id) {

		UnitCraft crafting = getMainPage().getCraftingList().get(id);
		if (crafting.isEnabled()) {
			int[] from = new int[9];
			for (int i = 0; i < from.length; i++) {
				from[i] = crafting.getGridId() + i;
			}
			int[] to = new int[player.inventory.main.size()];
			for (int i = 0; i < to.length; i++) {
				to[i] = i;
			}

			for (int i = 0; i < 9; i++) {
				ItemStack fromCrafting = crafting.getSlots().get(i).getStack();
				if (!fromCrafting.isEmpty()) {
					player.inventory.insertStack(fromCrafting);
				}
			}

			// transfer(this, player.inventory, from, to, EnumFacing.UP,
			// EnumFacing.UP, Integer.MAX_VALUE);
		}
	}

	@Override
	public TextComponent getName() {
		return new StringTextComponent("Production Table");
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int[] getInvAvailableSlots(Direction side) {
		return sideSlots[side.ordinal()];
	}

	@Override
	public ItemStack removeInvStack(int index) {
		return slots.get(index).getStack().copy();
	}

	@Override
	public void clearInv() {

	}
}
