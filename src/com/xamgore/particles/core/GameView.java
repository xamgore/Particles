package com.xamgore.particles.core;

import android.content.Context;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.xamgore.particles.game.ParticleScreen;

/**
 * Полотно. Умеет ловить нажатия и отрисовывать канвас.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    /* Singleton implementation */
    private static GameView _instance = null;

    public static GameView init(Context context) {
        return _instance = new GameView(context);
    }

    public static GameView getInstance() {
        return _instance;
    }

    private GameView(Context context) {
        super(context);
        gameState = new ParticleScreen();

        setFocusableInTouchMode(true);
        getHolder().addCallback(this);
    }


    /* Class implementation */
    public GameScreen gameState;
    private DrawingThread thread;

    private class DrawingThread extends Thread {
        public boolean keepRunning = true;
        private Canvas canvas;

        @Override
        public void run() {
            SurfaceHolder mSurfaceHolder = getHolder();

            while (keepRunning) {
                Fps.startMeasuringDelay();

                /* Game mechanics */
                gameState.onUpdate();

                /* Drawings */
                canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        // Send request to Game class
                        gameState.onDraw(canvas);
                    }
                } finally {
                    if (canvas != null)
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                }

                /* FPS measures */
                try {
                    Thread.sleep(Fps.getDelay());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


    /* Series of event listeners. */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new DrawingThread();
        thread.keepRunning = true;
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.keepRunning = false;
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        gameState.onSurfaceChanged(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gameState.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return gameState.onKeyDownEvent(keyCode, event) ||
                super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return gameState.onKeyUpEvent(keyCode, event) ||
                super.onKeyUp(keyCode, event);
    }

}