package com.rwtema.funkylocomotion.eventhandler;

import com.rwtema.funkylocomotion.Proxy;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientTimer {
	@SubscribeEvent
	public void getTimer(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START)
			Proxy.renderTimeOffset = event.renderTickTime % 1;
	}

}
