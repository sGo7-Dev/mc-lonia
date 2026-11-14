package game;

public enum BlockFace {
    TOP(0, 1, 0, 0.0f, 1.0f, 0.0f),
    BOTTOM(0, -1, 0, 0.0f, -1.0f, 0.0f),
    NORTH(0, 0, 1, 0.0f, 0.0f, 1.0f),
    SOUTH(0, 0, -1, 0.0f, 0.0f, -1.0f),
    EAST(1, 0, 0, 1.0f, 0.0f, 0.0f),
    WEST(-1, 0, 0, -1.0f, 0.0f, 0.0f);

    public final int dx;
    public final int dy;
    public final int dz;
    public final float nx;
    public final float ny;
    public final float nz;

    BlockFace(int dx, int dy, int dz, float nx, float ny, float nz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }
}
