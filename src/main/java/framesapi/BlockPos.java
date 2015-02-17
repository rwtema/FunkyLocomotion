package framesapi;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockPos {
    public int x, y, z;


    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockPos blockPos = (BlockPos) o;

        return x == blockPos.x && y == blockPos.y && z == blockPos.z;

    }

    @Override
    public int hashCode() {
        return 256 * x + 7936 * z + y;
    }

    public BlockPos(TileEntity tile) {
        this(tile.xCoord, tile.yCoord, tile.zCoord);
    }


    public BlockPos advance(ForgeDirection dir) {
        return new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
    }
}
