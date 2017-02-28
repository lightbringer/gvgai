package net.gvgai.vgdl.runtime.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.input.Controller;

public class EventKeyHandler extends KeyAdapter implements Controller {

    private final static int UP = 1;
    private final static int DOWN = 2;
    private final static int LEFT = 4;
    private final static int RIGHT = 8;
    private int keyMap;

//    private final Object mutex = new Object();

    @Override
    public Action act( GameState s, double seconds ) {
//        try {
//            //TODO figure out if key_handler=pulse actually means that the simulation waits for keypresses
//            synchronized (mutex) {
//                mutex.wait();
//            }
//
//        }
//        catch (final InterruptedException e) {
//            throw new RuntimeException( e );
//        }

        switch (keyMap) {
            case UP:
                keyMap = 0;
                return Action.ACTION_UP;
            case DOWN:
                keyMap = 0;
                return Action.ACTION_DOWN;
            case LEFT:
                keyMap = 0;
                return Action.ACTION_LEFT;
            case RIGHT:
                keyMap = 0;
                return Action.ACTION_RIGHT;
            default:
                keyMap = 0;
                return Action.ACTION_NIL;
        }
    }

    private void handleKeyEvent( KeyEvent e, boolean pressed ) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (pressed) {
                    keyMap |= UP;
                }
                else {
                    keyMap &= ~UP;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (pressed) {
                    keyMap |= DOWN;
                }
                else {
                    keyMap &= ~DOWN;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (pressed) {
                    keyMap |= LEFT;
                }
                else {
                    keyMap &= ~LEFT;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (pressed) {
                    keyMap |= RIGHT;
                }
                else {
                    keyMap &= ~RIGHT;
                }
                break;
            default:
                break;
        }
//        synchronized (mutex) {
//            mutex.notify();
//        }

    }

    @Override
    public void keyPressed( KeyEvent e ) {
        handleKeyEvent( e, true );

    }

    @Override
    public void keyReleased( KeyEvent e ) {
        handleKeyEvent( e, false );

    }

    @Override
    public void keyTyped( KeyEvent e ) {
        handleKeyEvent( e, true );
    }

}
