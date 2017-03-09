package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.sprites.Immovable;
import net.gvgai.vgdl.sprites.VGDLSprite;

@SpriteInfo( resourceInfo = "img=wall zLevel=10" )
public class Wall extends Immovable {

    @Override
    public VGDLSprite copy( GameMap m ) {
        final Wall w = new Wall();
        setup( w, m );
        return w;
    }

    @Override
    public int getClassId() {
        return 1;
    }

}
