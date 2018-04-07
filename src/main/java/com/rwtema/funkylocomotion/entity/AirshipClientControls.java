package com.rwtema.funkylocomotion.entity;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class AirshipClientControls {

	public static final IKeyConflictContext AIRSHIP_CONTEXT = new IKeyConflictContext() {
		@Override
		public boolean isActive() {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (player == null) return false;
			Entity ridingEntity = player.getRidingEntity();
			return ridingEntity instanceof EntityAirShip && ridingEntity.getControllingPassenger() == player;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return this == other;
		}
	};
	public static final double RISE_SPEED = 1 / 10.0;
	public static final double TURN_SPEED = 4.0;
	public static final double FORWARD_SPEED = 1 / 20.0;
	private static final List<KeyBinding> airship_binds = Lists.newArrayList();
	private static KeyBinding roll_left = createKey("roll_left", Keyboard.KEY_Q);
	private static KeyBinding roll_right = createKey("roll_right", Keyboard.KEY_E);
	private static KeyBinding turn_left = createKey("turn_left", Keyboard.KEY_A);
	private static KeyBinding turn_right = createKey("turn_right", Keyboard.KEY_D);
	private static KeyBinding thrust = createKey("thrust", Keyboard.KEY_W);
	private static KeyBinding brake = createKey("reverse_thrust", Keyboard.KEY_S);
	private static KeyBinding rise = createKey("rise", Keyboard.KEY_SPACE);
	private static KeyBinding fall = createKey("fall", Keyboard.KEY_C);
	public static final AirshipClientControls BALLOON = new AirshipClientControls() {
		@Override
		public void control(EntityAirShip entityAirShip) {
			if (rise.isKeyDown()) {
				entityAirShip.velocity = entityAirShip.velocity.addVector(0, RISE_SPEED, 0);
			}
			if (fall.isKeyDown()) {
				entityAirShip.velocity = entityAirShip.velocity.addVector(0, -RISE_SPEED, 0);
			}
			if (turn_left.isKeyDown()) {
				entityAirShip.rotation = entityAirShip.rotation.addVector(TURN_SPEED, 0, 0);
			}
			if (turn_right.isKeyDown()) {
				entityAirShip.rotation = entityAirShip.rotation.addVector(-TURN_SPEED, 0, 0);
			}

			if (thrust.isKeyDown()) {
				double x = -entityAirShip.rotation.x;
				entityAirShip.velocity = entityAirShip.velocity.addVector(
						Math.cos(x* EntityAirShip.PI_DIV) * FORWARD_SPEED, 0, Math.sin(x* EntityAirShip.PI_DIV) * FORWARD_SPEED
				);
			}

			if (brake.isKeyDown()) {
				double x = -entityAirShip.rotation.x;
//				double m = Math.min(entityAirShip.velocity.distanceTo(Vec3d.ZERO), FORWARD_SPEED);
				double m= FORWARD_SPEED;
				entityAirShip.velocity = entityAirShip.velocity.addVector(
						-Math.cos(x * EntityAirShip.PI_DIV) * m, 0, -Math.sin(x * EntityAirShip.PI_DIV) * m
				);
			}
		}
	};

	static {
		MinecraftForge.EVENT_BUS.register(AirshipClientControls.class);
	}

	@Nonnull
	private static KeyBinding createKey(String description, int keyL) {
		KeyBinding keyBinding = new KeyBinding("floco.key." + description, keyL, "funkylocomotion.keys.airship");
		keyBinding.setKeyConflictContext(AIRSHIP_CONTEXT);
		airship_binds.add(keyBinding);
		return keyBinding;
	}

	public static void init() {
		for (KeyBinding airship_bind : airship_binds) {
			ClientRegistry.registerKeyBinding(airship_bind);
		}
	}

	public abstract void control(EntityAirShip entityAirShip);

	@SubscribeEvent
	public void cancelMovement(InputEvent.KeyInputEvent event) {
		if (AIRSHIP_CONTEXT.isActive()) {
			for (KeyBinding binding : Minecraft.getMinecraft().gameSettings.keyBindings) {
				if (binding.getKeyConflictContext() == AIRSHIP_CONTEXT || !binding.getKeyConflictContext().isActive() || binding.getKeyConflictContext() == KeyConflictContext.UNIVERSAL) {
					continue;
				}

//				if (binds.contains(binding)) {
//					continue;
//				}
				for (KeyBinding possibleBinding : airship_binds) {
					if (possibleBinding.getKeyCode() == binding.getKeyCode()) {
						if (getPressed(binding)) {
							setPressed(binding, false);
							setPressed(possibleBinding, true);
						}

						int pressTime = getPressTime(binding);
						setPressTime(binding, 0);
						setPressTime(possibleBinding, Math.max(getPressTime(possibleBinding), pressTime));
						break;
					}
				}
			}
		}
	}

	private void setPressTime(KeyBinding binding, int value) {
		ObfuscationReflectionHelper.setPrivateValue(KeyBinding.class, binding, value, "pressTime");
	}

	private void setPressed(KeyBinding binding, boolean value) {
		ObfuscationReflectionHelper.setPrivateValue(KeyBinding.class, binding, value, "pressed");
	}

	private Integer getPressTime(KeyBinding binding) {
		return ObfuscationReflectionHelper.getPrivateValue(KeyBinding.class, binding, "pressTime");
	}

	private Boolean getPressed(KeyBinding binding) {
		return ObfuscationReflectionHelper.getPrivateValue(KeyBinding.class, binding, "pressed");
	}
}
