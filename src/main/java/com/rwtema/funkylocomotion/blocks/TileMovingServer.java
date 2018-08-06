package com.rwtema.funkylocomotion.blocks;

import com.rwtema.funkylocomotion.movers.MoverEventHandler;
import com.rwtema.funkylocomotion.movers.MovingTileRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

public class TileMovingServer extends TileMovingBase {

	public WeakReference<EntityPlayer> activatingPlayer = null;
	public EnumFacing activatingSide = null;
	public EnumHand activatingHand = null;
	public float activatingHitX, activatingHitY, activatingHitZ;


	public TileMovingServer() {
		super(Side.SERVER);
	}

	@Override
	@Nonnull
	public NBTTagCompound getUpdateTag() {
		if (this.desc == null)
			return super.getUpdateTag();

		this.desc.setInteger("Time", this.time);
		this.desc.setInteger("MaxTime", this.maxTime);
		this.desc.setByte("Dir", (byte) this.dir);

		if (this.block != null)
			this.desc.setTag("BlockTag", this.block);
		if (this.collisions.length > 0)
			this.desc.setTag("Collisions", TagsAxis(this.collisions));
		if (this.lightLevel != 0)
			this.desc.setByte("Light", (byte) this.lightLevel);
		if (this.lightOpacity != 0)
			this.desc.setShort("Opacity", (short) this.lightOpacity);

		return this.desc;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		MovingTileRegistry.deregister(this);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		MovingTileRegistry.deregister(this);
	}

	@Override
	public void validate() {
		super.validate();
		MovingTileRegistry.register(this);
	}

	@Override
	public void update() {
		if (time < maxTime) {
			super.update();
			this.getWorld().markChunkDirty(pos, this);
		} else {
			MoverEventHandler.registerFinisher();
//			MoveManager.finishMoving();
		}
	}

	public void cacheActivate(EntityPlayer player, EnumFacing side, EnumHand hand, float hitX, float hitY, float hitZ) {
		if (this.activatingPlayer == null || this.activatingPlayer.get() == null) {
			activatingPlayer = new WeakReference<>(player);
			activatingSide = side;
			activatingHand = hand;
			activatingHitX = hitX;
			activatingHitY = hitY;
			activatingHitZ = hitZ;
		}
	}
}
