package framesapi;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockPos {
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

        if (x != blockPos.x) return false;
        if (y != blockPos.y) return false;
        if (z != blockPos.z) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public BlockPos(TileEntity tile) {
        this(tile.xCoord, tile.yCoord, tile.zCoord);

    }


    public BlockPos advance(ForgeDirection dir) {
        return new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
    }
}
