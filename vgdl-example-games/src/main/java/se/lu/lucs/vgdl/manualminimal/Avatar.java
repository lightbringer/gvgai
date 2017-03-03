package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

@SpriteInfo( resourceInfo = "img=frog zLevel=14" )
public class Avatar extends MovingAvatar {

    @Override
    public void collide( VGDLSprite... others ) {
        for (final VGDLSprite o : others) {
            if (o instanceof Wall) {
                collide( (Wall) o );
                break;
            }
            else if (o instanceof Goal) {
                collide( (Goal) o );
            }
        }
        super.collide( others );
    }

    @Override
    public VGDLSprite copy() {
        final Avatar a = new Avatar();
        setup( a );
        return a;
    }

    @Override
    public int getClassId() {
        return 0;
    }

    private void collide( Goal g ) {
        state.setScore( 1 );
        kill();
    }

    private void collide( Wall w ) {
        final Object reverseDirection = reverseDirection();
        move( reverseDirection );
    }

}
