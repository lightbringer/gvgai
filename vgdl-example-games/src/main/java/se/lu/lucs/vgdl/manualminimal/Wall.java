package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.sprites.Immovable;
import net.gvgai.vgdl.sprites.VGDLSprite;

@SpriteInfo( resourceInfo = "img=wall zLevel=10" )
public class Wall extends Immovable {

    @Override
    public VGDLSprite copy() {
        return new Wall();
    }

    @Override
    public int getClassId() {
        return 1;
    }

}
