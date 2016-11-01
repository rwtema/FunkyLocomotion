package com.rwtema.funkylocomotion.items;

import com.rwtema.funkylocomotion.FunkyLocomotion;
import com.rwtema.funkylocomotion.movers.IMover;
import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemWrench extends Item {
	public static final int metaWrenchNormal = 0;
	public static final int metaWrenchEye = 1;
	public static final int metaWrenchHammer = 2;

	public ItemWrench() {
		super();
		this.setMaxStackSize(1);
		this.setRegistryName("funkylocomotion:wrench");
		this.setUnlocalizedName("funkylocomotion:wrench");
		this.setCreativeTab(FunkyLocomotion.creativeTabFrames);
		this.setHasSubtypes(true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
		list.add(new ItemStack(item, 1, 2));
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return true;
	}
//
//	@Override
//	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
//		if (stack.getItemDamage() != metaWrenchHammer)
//			return EnumActionResult.PASS;
//
//
//		if (world.isRemote) {
//			FunkyLocomotion.proxy.sendUsePacket(pos, side, hand, hitX, hitY, hitZ);
//		}
//
//		int face = side.ordinal();
//		int d1 = offsetDir1[face];
//		int d2 = offsetDir2[face];
//
//		IBlockState state = world.getBlockState(pos);
//		Block block = state.getBlock();
//
//		if (block.isAir(state, world, pos))
//			return EnumActionResult.PASS;
//
//		if (block instanceof BlockStickyFrame) {
//			int open = ((BlockStickyFrame) block).index + world.getBlockState(pos).getValue(BlockStickyFrame.META) & 1 << face;
//
//			Block otherBlock;
//			for (int i = -2; i <= 2; i++) {
//				for (int j = -2; j <= 2; j++) {
//					int dx = x + Facing.offsetsXForSide[d1] * i + Facing.offsetsXForSide[d2] * j;
//					int dy = y + Facing.offsetsYForSide[d1] * i + Facing.offsetsYForSide[d2] * j;
//					int dz = z + Facing.offsetsZForSide[d1] * i + Facing.offsetsZForSide[d2] * j;
//
//					int dx2 = dx + Facing.offsetsXForSide[face];
//					int dy2 = dy + Facing.offsetsYForSide[face];
//					int dz2 = dz + Facing.offsetsZForSide[face];
//
//					Block blockingBlock = world.getBlock(dx2, dy2, dz2);
//
//					if (!blockingBlock.isAir(world, dx2, dy2, dz2)) {
//						if (blockingBlock.getCollisionBoundingBoxFromPool(world, dx2, dy2, dz2) != null && block.canCollideCheck(world.getBlockMetadata(dx2, dy2, dz2), false)) {
//							if (block.collisionRayTrace(world, dx2, dy2, dz2,
//									Vec3d.createVectorHelper(
//											dx2 + (face == 4 ? -0.1 : face == 5 ? 1.1 : hitX),
//											dy2 + (face == 0 ? -0.1 : face == 1 ? 1.1 : hitY),
//											dz2 + (face == 2 ? -0.1 : face == 3 ? 1.1 : hitZ)
//									),
//									Vec3d.createVectorHelper(
//											dx2 + (face == 4 ? 1.1 : face == 5 ? -0.1 : hitX),
//											dy2 + (face == 0 ? 1.1 : face == 1 ? -0.1 : hitY),
//											dz2 + (face == 2 ? 1.1 : face == 3 ? -0.1 : hitZ)
//									)
//							) != null) {
//								continue;
//							}
//						}
//					}
//
//					if (i == 0 && j == 0) {
//						block.onBlockActivated(world, dx, dy, dz, player, face, hitX, hitY, hitZ);
//					} else {
//						otherBlock = world.getBlock(dx, dy, dz);
//						if (otherBlock instanceof BlockStickyFrame && ((((BlockStickyFrame) otherBlock).index + world.getBlockMetadata(dx, dy, dz)) & (1 << face)) == open) {
//							otherBlock.onBlockActivated(world, dx, dy, dz, player, face, hitX, hitY, hitZ);
//						}
//					}
//				}
//			}
//
//
//		} else {
//			int meta = world.getBlockMetadata(x, y, z);
//
//			for (int i = -2; i <= 2; i++) {
//				for (int j = -2; j <= 2; j++) {
//					int dx = x + Facing.offsetsXForSide[d1] * i + Facing.offsetsXForSide[d2] * j;
//					int dy = y + Facing.offsetsYForSide[d1] * i + Facing.offsetsYForSide[d2] * j;
//					int dz = z + Facing.offsetsZForSide[d1] * i + Facing.offsetsZForSide[d2] * j;
//
//					Block testBlock = world.getBlock(dx, dy, dz);
//
//					if (testBlock.isAir(world, dx, dy, dz))
//						continue;
//
//					if (i != 0 || j != 0) {
//						if (isInaccessible(world, face ^ 1, hitX, hitY, hitZ, block, dx, dy, dz, testBlock)) {
//							int dx2 = dx + Facing.offsetsXForSide[face];
//							int dy2 = dy + Facing.offsetsYForSide[face];
//							int dz2 = dz + Facing.offsetsZForSide[face];
//
//							Block blockingBlock = world.getBlock(dx2, dy2, dz2);
//
//							if (isInaccessible(world, face, hitX, hitY, hitZ, block, dx2, dy2, dz2, blockingBlock))
//								continue;
//						}
//					}
//
//					if (testBlock == block && world.getBlockMetadata(dx, dy, dz) == meta)
//						block.onBlockActivated(world, dx, dy, dz, player, face, hitX, hitY, hitZ);
//				}
//			}
//		}
//		return true;
//	}
//
//	private boolean isInaccessible(World world, int face, float hitX, float hitY, float hitZ, Block block, int dx2, int dy2, int dz2, Block blockingBlock) {
//		return !blockingBlock.isAir(world, dx2, dy2, dz2) &&
//				blockingBlock.getCollisionBoundingBoxFromPool(world, dx2, dy2, dz2) != null &&
//				block.canCollideCheck(world.getBlockMetadata(dx2, dy2, dz2), false) &&
//				block.collisionRayTrace(world, dx2, dy2, dz2,
//						Vec3d.createVectorHelper(
//								dx2 + (face == 4 ? -0.1 : face == 5 ? 1.1 : hitX),
//								dy2 + (face == 0 ? -0.1 : face == 1 ? 1.1 : hitY),
//								dz2 + (face == 2 ? -0.1 : face == 3 ? 1.1 : hitZ)),
//						Vec3d.createVectorHelper(
//								dx2 + (face == 4 ? 0.1 : face == 5 ? 0.9 : hitX),
//								dy2 + (face == 0 ? 0.1 : face == 1 ? 0.9 : hitY),
//								dz2 + (face == 2 ? 0.1 : face == 3 ? 0.9 : hitZ))
//				) != null;
//	}


	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			TileEntity tileEntity = player.worldObj.getTileEntity(pos);
			if (tileEntity instanceof IMover) {
				MoverEventHandler.registerMover((IMover) tileEntity);
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack.getItemDamage() == metaWrenchEye)
			return "item.funkylocomotion:wrench_eye";
		else
			return super.getUnlocalizedName(itemstack);
	}

	@SubscribeEvent
	public void leftClick(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getEntityPlayer().getHeldItem(event.getHand()) == null || event.getEntityPlayer().getHeldItem(event.getHand()).getItem() != this)
			return;
		if (!event.getWorld().isRemote) {
			TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
			if (tileEntity instanceof IMover) {
				MoverEventHandler.registerMover((IMover) tileEntity);
			}
		}
		event.setCanceled(true);
	}


//	@Override
//	public float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
//		return 0;
//	}
}
