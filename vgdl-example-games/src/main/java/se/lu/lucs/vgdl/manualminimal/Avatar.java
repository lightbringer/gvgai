package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

@SpriteInfo( resourceInfo = "img=frog zLevel=14" )
public class Avatar extends MovingAvatar {

    @Override
    public void collide( GameState state, VGDLSprite... others ) {
        for (final VGDLSprite o : others) {
            if (o instanceof Wall) {
                collide( state, (Wall) o );
                break;
            }
            else if (o instanceof Goal) {
                collide( state, (Goal) o );
            }
        }
        super.collide( state, others );
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

    private void collide( GameState state, Goal g ) {
        state.setScore( 1 );
        kill( state );
    }

    private void collide( GameState state, Wall w ) {
        final Object reverseDirection = reverseDirection();
        move( state.getLevel(), reverseDirection );
    }

}
