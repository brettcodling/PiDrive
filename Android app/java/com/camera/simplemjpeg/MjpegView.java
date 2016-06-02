package com.camera.simplemjpeg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {

    //Global declarations
    public static final String TAG = "MJPEG";

    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD = 1;
    public final static int SIZE_BEST_FIT = 4;
    public final static int SIZE_FULLSCREEN = 8;

    SurfaceHolder holder;
    Context saved_context;

    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;

    //Display values
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;

    private boolean suspending = false;

    private Bitmap bmp = null;

    //Mjpeg size
    public int IMG_WIDTH = 640;
    public int IMG_HEIGHT = 480;

    //Initializes the view to display stream
    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private String fps = "";


        //Constructor
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        }

        //Setting size of stream view based on size chosen
        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN)
                return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }

        //Sets surface size
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        //Setting variables for displaying FPS
        private Bitmap makeFpsOverlay(Paint p) {
            Rect b = new Rect();
            p.getTextBounds(fps, 0, fps.length(), b);

            Bitmap bm = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, b.width(), b.height(), p);
            p.setColor(overlayTextColor);
            c.drawText(fps, -b.left, b.bottom - b.top - p.descent(), p);
            return bm;
        }

        //Displaying the stream from MjpegInputStream on the view
        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

            int width;
            int height;
            Paint p = new Paint();
            Bitmap ovl = null;

            //While stream is started display mjpegs
            while (mRun) {

                Rect destRect = null;
                Canvas c = null;

                if (surfaceDone) {
                    try {
                        if (bmp == null) {
                            bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
                        }
                        int ret = mIn.readMjpegFrame(bmp);

                        //Setting error message
                        if (ret == -1) {
                            ((MainActivity) saved_context).setImageError();
                            return;
                        }

                        destRect = destRect(bmp.getWidth(), bmp.getHeight());

                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {

                            c.drawBitmap(bmp, null, destRect, p);

                            //Settings to show FPS
                            if (showFps) {
                                p.setXfermode(mode);
                                if (ovl != null) {

                                    height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                    width = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();

                                    c.drawBitmap(ovl, width, height, null);
                                }
                                p.setXfermode(null);
                                frameCounter++;
                                if ((System.currentTimeMillis() - start) >= 1000) {
                                    fps = String.valueOf(frameCounter) + "fps";
                                    frameCounter = 0;
                                    start = System.currentTimeMillis();
                                    if (ovl != null) ovl.recycle();

                                    ovl = makeFpsOverlay(overlayPaint);
                                }
                            }


                        }

                    } catch (IOException e) {

                    } finally {
                        if (c != null) mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }

    //Initializing varaibles for use in run()
    private void init(Context context) {

        holder = getHolder();
        saved_context = context;
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = MjpegView.POSITION_LOWER_RIGHT;
        displayMode = MjpegView.SIZE_BEST_FIT;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    //Starting the stream
    public void startPlayback() {
        if (mIn != null) {
            //Allowing while loop to start in run()
            mRun = true;
            if (thread == null) {
                //Creating new thread if one doesn't exist
                thread = new MjpegViewThread(holder, saved_context);
            }
            //Starting thread
            thread.start();
        }
    }

    //Resuming the stream after stopping it
    public void resumePlayback() {
        //Checks if the stream is stopped
        if (suspending) {
            //Restarts stream and thread
            if (mIn != null) {
                mRun = true;
                SurfaceHolder holder = getHolder();
                holder.addCallback(this);
                thread = new MjpegViewThread(holder, saved_context);
                thread.start();
                suspending = false;
            }
        }
    }

    //Stopping the stream
    public void stopPlayback() {
        //Checks is stream is running and suspends it
        if (mRun) {
            suspending = true;
        }
        mRun = false;
        //Closing thread
        if (thread != null) {
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
            thread = null;
        }
        //If input stream is open then close it
        if (mIn != null) {
            try {
                mIn.close();
            } catch (IOException e) {
            }
            mIn = null;
        }

    }

    //Frees the memory from the input stream
    public void freeCameraMemory() {
        if (mIn != null) {
            mIn.freeCameraMemory();
        }
    }

    //Constructor
    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //Checks if surface is changed
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
        if (thread != null) {
            thread.setSurfaceSize(w, h);
        }
    }

    //If surface is destroyed it stops the stream
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
    }

    //Constructor
    public MjpegView(Context context) {
        super(context);
        init(context);
    }

    //Surface created
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    //Sets whether to show FPS
    public void showFps(boolean b) {
        showFps = b;
    }

    //Setting the input stream
    public void setSource(MjpegInputStream source) {
        mIn = source;
        if (!suspending) {
            startPlayback();
        } else {
            resumePlayback();
        }
    }

    //Setting values for FPS
    public void setOverlayPaint(Paint p) {
        overlayPaint = p;
    }

    public void setOverlayTextColor(int c) {
        overlayTextColor = c;
    }

    public void setOverlayBackgroundColor(int c) {
        overlayBackgroundColor = c;
    }

    public void setOverlayPosition(int p) {
        ovlPos = p;
    }

    //Setting which display mode to use
    public void setDisplayMode(int s) {
        displayMode = s;
    }

    //Setting resolution of stream
    public void setResolution(int w, int h) {
        IMG_WIDTH = w;
        IMG_HEIGHT = h;
    }

    //Checks is stream is running
    public boolean isStreaming() {
        return mRun;
    }
}
