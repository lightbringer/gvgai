package net.gvgai.vgdl.game;

public class Wall extends VGDLSprite {
    public final static int VGDL_WALL_ID = -1;

    @Override
    public <T extends VGDLSprite> void collide( T other ) {
        // NOP

    }

    @Override
    public int getClassId() {
        return VGDL_WALL_ID;
    }

}
