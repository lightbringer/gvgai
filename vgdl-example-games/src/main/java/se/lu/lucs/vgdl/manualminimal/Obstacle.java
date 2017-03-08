package se.lu.lucs.vgdl.manualminimal;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.sprites.VGDLSprite;
import net.gvgai.vgdl.sprites.missile.Missile;

@SpriteInfo( resourceInfo = "img=log zLevel=10" )
public class Obstacle extends Missile {

    public Obstacle() {
        speed = 1;
        setDirection( LeftDirection.get() );
    }

    @Override
    public void collide( GameState state, VGDLSprite... others ) {
        for (final VGDLSprite o : others) {
            if (o instanceof Wall) {
                collide( state, (Wall) o );
                break;
            }
        }
        super.collide( state, others );
    }

    @Override
    public VGDLSprite copy() {
        final Obstacle o = new Obstacle();
        setup( o );
        return o;
    }

    @Override
    public int getClassId() {
        return 5;
    }

    @Override
    public void OnOutOfBounds( GameState map ) {
        kill( map );
    }

    private void collide( GameState state, Wall w ) {
        kill( state );
    }

}
