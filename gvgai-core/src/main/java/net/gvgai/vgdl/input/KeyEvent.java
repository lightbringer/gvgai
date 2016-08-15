package net.gvgai.vgdl.input;

public class KeyEvent {
    public final boolean isPressed;
    public final int keyCode;

    public KeyEvent( boolean isPressed, int keyCode ) {
        super();
        this.isPressed = isPressed;
        this.keyCode = keyCode;
    }

}
