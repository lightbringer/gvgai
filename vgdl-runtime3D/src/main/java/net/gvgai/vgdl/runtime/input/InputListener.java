package net.gvgai.vgdl.runtime.input;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import net.gvgai.vgdl.game.GameState3D;

public class InputListener implements AnalogListener {
    private final static float SPEED = .01f;
    private final static float ROTATION_SPEED = .1f;
    private final BetterCharacterControl character;

    private final GameState3D state;
    private final BulletAppState bulletAppState;

    private boolean keyPressed;

    private boolean enabled;

    public InputListener( GameState3D state ) {
        this.state = state;

        character = new BetterCharacterControl( .42f, .9f, 0.1f );

        final Spatial avatarNode = state.getLevel().getNode( state.getAvatar() );
        avatarNode.addControl( character );

        bulletAppState = state.getLevel().getPhysicsState();
        bulletAppState.getPhysicsSpace().add( character );

        enabled = true;
    }

    public boolean checkAndClearKeyPressed() {
        final boolean b = keyPressed;
        keyPressed = false;
        return b;

    }

    @Override
    public void onAnalog( String name, float value, float tpf ) {
        if (!enabled) {
            return;
        }

        switch (name) {
            case "Forward":
                character.setWalkDirection( character.getViewDirection().add( Vector3f.UNIT_Z.mult( SPEED ) ) );
                break;
            case "Backward":
                character.setWalkDirection( character.getViewDirection().add( Vector3f.UNIT_Z.mult( SPEED ) ).negateLocal() );
                break;
            case "Left":
                character.setViewDirection( new Quaternion().fromAngleAxis( FastMath.DEG_TO_RAD * ROTATION_SPEED, Vector3f.UNIT_Y )
                                .multLocal( character.getViewDirection() ) );
                break;
            case "Right":
                character.setViewDirection( new Quaternion().fromAngleAxis( -FastMath.DEG_TO_RAD * ROTATION_SPEED, Vector3f.UNIT_Y )
                                .multLocal( character.getViewDirection() ) );
                break;
            default:
                break;
        }
        state.getAvatar().setDirection( character.getViewDirection() );
        state.getAvatar().setPosition( state.getLevel().getNode( state.getAvatar() ).getLocalTranslation() );
        keyPressed = true;
    }

    public void setEnabled( boolean b ) {
        enabled = b;

    }

}
