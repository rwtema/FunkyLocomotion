package com.rwtema.funkylocomotion.helper;

import com.google.common.base.Supplier;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collector;

public class NBTHelper {

	public final static NBTConvertor<Vec3d, NBTTagCompound> VEC3D = new NBTConvertorInstance<>(
			vec3i -> NBTHelper.builder().setDouble("x", vec3i.x).setDouble("y", vec3i.y).setDouble("z", vec3i.z).build(),
			nbt -> new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"))
	);



	public final static NBTConvertor<BlockPos, NBTTagCompound> BLOCK_POS = new NBTConvertorInstance<>(
			vec3i -> NBTHelper.builder().setInteger("x", vec3i.getX()).setInteger("y", vec3i.getY()).setInteger("z", vec3i.getZ()).build(),
			nbt -> new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"))
	);

	public final static NBTConvertor<IBlockState, NBTTagCompound> IBLOCKSTATE = new NBTConvertorInstance<IBlockState, NBTTagCompound>(
			state -> builder().setString("block", Block.REGISTRY.getNameForObject(state.getBlock()).toString()).setInteger("meta", state.getBlock().getMetaFromState(state)).build(),
			nbt -> Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("block"))).getStateFromMeta(nbt.getInteger("meta"))
	);

	public final static NBTConvertor<AxisAlignedBB, NBTTagCompound> AXISALIGNEDBB = new NBTConvertorInstance<>(
			axisAlignedBB -> axisAlignedBB == Block.FULL_BLOCK_AABB
					? builder().setString("type", "full_block").build()
					: builder().setDouble("x0", axisAlignedBB.minX).setDouble("y0", axisAlignedBB.minY).setDouble("z0", axisAlignedBB.minZ).setDouble("x1", axisAlignedBB.maxX).setDouble("y1", axisAlignedBB.maxY).setDouble("z1", axisAlignedBB.maxZ).build(),
			tagCompound -> {
				switch (tagCompound.getString("type")) {
					case "full_block":
						return Block.FULL_BLOCK_AABB;
					default:
						return new AxisAlignedBB(tagCompound.getDouble("x0"), tagCompound.getDouble("y0"), tagCompound.getDouble("z0"), tagCompound.getDouble("x1"), tagCompound.getDouble("y1"), tagCompound.getDouble("z1"));
				}
			});

	public static <T extends NBTBase> Collector<T, ?, NBTTagList> toNBTTagList() {
		return Collector.of((Supplier<NBTTagList>) NBTTagList::new,
				NBTTagList::appendTag,
				(nbtBases, nbtBases2) -> {
					for (int i = 0; i < nbtBases2.tagCount(); i++) {
						nbtBases.appendTag(nbtBases2.get(i));
					}
					return nbtBases;
				}
		);
	}


	@Nullable
	public static <T extends NBTBase> List<T> wrapList(@Nonnull NBTTagList list) {
		return new AbstractList<T>() {
			@Override
			public int size() {
				return list.tagCount();
			}

			@Nullable
			@Override
			public T get(int index) {
				NBTBase nbtBase = list.get(index);
				if (nbtBase.getId() != list.getTagType()) return null;
				return (T) nbtBase;
			}
		};
	}

	@Nonnull
	public static NBTChainBuilder builder() {
		return new NBTChainBuilder(new NBTTagCompound());
	}

	@Nonnull
	public static GameProfile profileFromNBT(@Nonnull NBTTagCompound tag) {
		return new GameProfile(new UUID(tag.getLong("upper"), tag.getLong("lower")), tag.getString("name"));
	}

	public static NBTTagCompound profileToNBT(@Nonnull GameProfile profile) {
		return builder()
				.setLong("lower", profile.getId().getLeastSignificantBits())
				.setLong("upper", profile.getId().getMostSignificantBits())
				.setString("name", profile.getName())
				.build();


	}

	public static <NBT extends NBTBase, T extends INBTSerializable<NBT>> Function<NBT, T> deserializer(@Nonnull java.util.function.Supplier<T> blank) {
		return tag -> {
			T t = blank.get();
			t.deserializeNBT(tag);
			return t;
		};
	}

	public static <T, K extends NBTBase> NBTTagList serializeArray(T[] arr, NBTConvertor<T, K> convertor) {
		NBTTagList list = new NBTTagList();
		for (T t : arr) {
			list.appendTag(convertor.toNBT(t));
		}
		return list;
	}

	public static <T, K extends NBTBase> T[] deserializeArray(NBTTagList list, NBTConvertor<T, K> convertor, IntFunction<T[]> generator) {
		int n = list.tagCount();
		T[] arr = generator.apply(n);
		for (int i = 0; i < n; i++) {
			@SuppressWarnings("unchecked")
			K k = (K) list.get(i);
			arr[i] = convertor.fromNBT(k);
		}
		return arr;
	}

	public interface NBTConvertor<T, K extends NBTBase> {
		K toNBT(T t);

		T fromNBT(K k);
	}

	private static class NBTConvertorInstance<T, K extends NBTBase> implements NBTConvertor<T, K> {
		final Function<T, K> toNBT;
		final Function<K, T> fromNBT;

		private NBTConvertorInstance(Function<T, K> toNBT, Function<K, T> fromNBT) {
			this.toNBT = toNBT;
			this.fromNBT = fromNBT;
		}

		@Override
		public K toNBT(T t) {
			return toNBT.apply(t);
		}

		@Override
		public T fromNBT(K k) {
			return fromNBT.apply(k);
		}
	}

	public static class NBTChainBuilder {
		final NBTTagCompound tag;

		public NBTChainBuilder() {
			this(new NBTTagCompound());
		}


		public NBTChainBuilder(NBTTagCompound tag) {
			this.tag = tag;
		}

		public NBTTagCompound build() {
			return tag;
		}

		@Nonnull
		public NBTChainBuilder setTag(@Nonnull String key, @Nonnull NBTBase value) {
			tag.setTag(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setByte(@Nonnull String key, byte value) {
			tag.setByte(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setShort(@Nonnull String key, short value) {
			tag.setShort(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setInteger(@Nonnull String key, int value) {
			tag.setInteger(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setLong(@Nonnull String key, long value) {
			tag.setLong(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setUniqueId(String key, @Nonnull UUID value) {
			tag.setUniqueId(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setFloat(@Nonnull String key, float value) {
			tag.setFloat(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setDouble(@Nonnull String key, double value) {
			tag.setDouble(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setString(@Nonnull String key, @Nonnull String value) {
			tag.setString(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setByteArray(@Nonnull String key, @Nonnull byte[] value) {
			tag.setByteArray(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setIntArray(@Nonnull String key, @Nonnull int[] value) {
			tag.setIntArray(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder setBoolean(@Nonnull String key, boolean value) {
			tag.setBoolean(key, value);
			return this;
		}

		@Nonnull
		public NBTChainBuilder merge(@Nonnull NBTTagCompound other) {
			tag.merge(other);
			return this;
		}

		@Nonnull
		public <T, K extends NBTBase> NBTChainBuilder set(String key, T value, NBTConvertor<T, K> convertor) {
			return setTag(key, convertor.toNBT(value));
		}
	}

}
