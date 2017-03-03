package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.sprites.Immovable;
import net.gvgai.vgdl.sprites.VGDLSprite;

@SpriteInfo( resourceInfo = "img=coin zLevel=11" )
public class Goal extends Immovable {

    @Override
    public VGDLSprite copy() {
        return new Goal();
    }

    @Override
    public int getClassId() {
        return 2;
    }

}
