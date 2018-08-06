package com.rwtema.funkylocomotion.movers;

import com.rwtema.funkylocomotion.helper.WeakSet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class MoverEventHandler {
	public static final WeakSet<IMover> movers = new WeakSet<>();
	public static boolean shouldTick = false;


	private MoverEventHandler() {
	}

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new MoverEventHandler());
	}

	public static void registerFinisher() {
		shouldTick = true;
	}

	public static void registerMover(IMover mover) {
		movers.add(mover);
	}

	@SubscribeEvent
	public void onPostWorldTick2(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER && !movers.isEmpty()) {
			IMover[] iMovers = movers.toArray(new IMover[0]);
			movers.clear();

			for (IMover mover : iMovers) {
				if (mover.stillExists()) {
					mover.startMoving();
				}
			}

			movers.clear();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPostWorldTick(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER && shouldTick) {
			MoveManager.finishMoving();
		}
	}
}
