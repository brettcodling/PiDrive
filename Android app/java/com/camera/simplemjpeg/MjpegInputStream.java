package com.camera.simplemjpeg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class MjpegInputStream extends DataInputStream {

    //Global declarations
    private final byte[] START_OF_INPUT = {(byte) 0xFF, (byte) 0xD8};
    private final byte[] END_OF_FILE = {(byte) 0xFF, (byte) 0xD9};
    private final String CONTENT_LENGTH = "Content-Length";
    private final static int HEADER_MAX_LENGTH = 100;
    private final static int FRAME_MAX_LENGTH = 200000;
    private int mContentLength = -1;
    byte[] header = null;
    byte[] frameData = null;
    int headerLen = -1;
    int headerLenPrev = -1;

    int skip = 1;
    int count = 0;

    //loading library ImageProc
    static {
        System.loadLibrary("ImageProc");
    }

    //Setting pixels to relevant images
    public native int pixeltobmp(byte[] jp, int l, Bitmap bmp);

    //Free memory from input stream
    public native void freeCameraMemory();

    //Reads the data being sent from the URL
    public static MjpegInputStream read(String surl) {
        try {
            URL url = new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            return new MjpegInputStream(urlConnection.getInputStream());
        } catch (Exception e) {
        }

        return null;
    }

    //Constructor
    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

    //Sets value for last bit of data received
    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence)
            throws IOException {

        int seqIndex = 0;
        byte c;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if (c == sequence[seqIndex]) {
                seqIndex++;
                if (seqIndex == sequence.length) {

                    return i + 1;
                }
            } else seqIndex = 0;
        }


        return -1;
    }

    //Gets the first image from the Pi
    private int getStartOfSequence(DataInputStream in, byte[] sequence)
            throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return (end < 0) ? (-1) : (end - sequence.length);
    }

    //Sets value for last bit of data received using set variables
    private int getEndOfSeqeunceSimplified(DataInputStream in, byte[] sequence)
            throws IOException {
        int startPos = mContentLength / 2;
        int endPos = 3 * mContentLength / 2;

        skipBytes(headerLen + startPos);


        int seqIndex = 0;
        byte c;
        for (int i = 0; i < endPos - startPos; i++) {
            c = (byte) in.readUnsignedByte();
            if (c == sequence[seqIndex]) {
                seqIndex++;
                if (seqIndex == sequence.length) {

                    return headerLen + startPos + i + 1;
                }
            } else seqIndex = 0;
        }


        return -1;
    }

    //Parses the length of the stream to an integer
    private int parseContentLength(byte[] headerBytes)
            throws IOException, NumberFormatException, IllegalArgumentException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);
        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }

    //Called by the MjpegView to read each frame from the input stream
    public Bitmap readMjpegFrame() throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen;
        try {
            headerLen = getStartOfSequence(this, START_OF_INPUT);
        } catch (IOException e) {
            reset();
            return null;
        }
        reset();

        if (header == null || headerLen != headerLenPrev) {
            header = new byte[headerLen];
        }
        headerLenPrev = headerLen;
        readFully(header);

        int ContentLengthNew = -1;
        try {
            //Sets length of stream
            ContentLengthNew = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            ContentLengthNew = getEndOfSeqeunceSimplified(this, END_OF_FILE);

            if (ContentLengthNew < 0) {
                reset();
                ContentLengthNew = getEndOfSeqeunce(this, END_OF_FILE);
            }
        } catch (IllegalArgumentException e) {
            ContentLengthNew = getEndOfSeqeunceSimplified(this, END_OF_FILE);

            if (ContentLengthNew < 0) {
                reset();
                ContentLengthNew = getEndOfSeqeunce(this, END_OF_FILE);
            }
        } catch (IOException e) {
            reset();
            return null;
        }
        mContentLength = ContentLengthNew;
        reset();

        if (frameData == null) {
            frameData = new byte[FRAME_MAX_LENGTH];
        }
        if (mContentLength + HEADER_MAX_LENGTH > FRAME_MAX_LENGTH) {
            frameData = new byte[mContentLength + HEADER_MAX_LENGTH];
        }

        skipBytes(headerLen);

        readFully(frameData, 0, mContentLength);

        if (count++ % skip == 0) {
            //Returns stream as an image to MjpegView
            return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData, 0, mContentLength));
        } else {
            return null;
        }
    }

    //Called by the MjpegView to read each frame from the input stream
    public int readMjpegFrame(Bitmap bmp) throws IOException {
        mark(FRAME_MAX_LENGTH);
        int headerLen;
        try {
            headerLen = getStartOfSequence(this, START_OF_INPUT);
        } catch (IOException e) {
            reset();
            return -1;
        }
        reset();

        if (header == null || headerLen != headerLenPrev) {
            header = new byte[headerLen];
        }
        headerLenPrev = headerLen;
        readFully(header);

        int ContentLengthNew = -1;
        try {
            //Sets length of stream
            ContentLengthNew = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            ContentLengthNew = getEndOfSeqeunceSimplified(this, END_OF_FILE);

            if (ContentLengthNew < 0) {
                reset();
                ContentLengthNew = getEndOfSeqeunce(this, END_OF_FILE);
            }
        } catch (IllegalArgumentException e) {
            ContentLengthNew = getEndOfSeqeunceSimplified(this, END_OF_FILE);

            if (ContentLengthNew < 0) {
                reset();
                ContentLengthNew = getEndOfSeqeunce(this, END_OF_FILE);
            }
        } catch (IOException e) {
            reset();
            return -1;
        }
        mContentLength = ContentLengthNew;
        reset();

        if (frameData == null) {
            frameData = new byte[FRAME_MAX_LENGTH];
        }
        if (mContentLength + HEADER_MAX_LENGTH > FRAME_MAX_LENGTH) {
            frameData = new byte[mContentLength + HEADER_MAX_LENGTH];
        }

        skipBytes(headerLen);

        readFully(frameData, 0, mContentLength);

        if (count++ % skip == 0) {
            //Returns the relevant pixel to the image
            return pixeltobmp(frameData, mContentLength, bmp);
        } else {
            return 0;
        }
    }

    //Sets frame skip
    public void setSkip(int s) {
        skip = s;
    }
}
