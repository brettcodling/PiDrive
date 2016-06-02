package com.camera.simplemjpeg;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import android.util.Log;

public class Connection
        implements Runnable {

    //Global declarations
    private Socket socket;
    public String ServerIP = "";
    private static final int ServerPort = 51717;
    private boolean checked;
    private boolean result;
    int count = 0;

    //Method to run when connection is attempted
    @Override
    public void run() {
        count++;
        try {
            //Creating new socket using Pi's IP address and listening port
            socket = new Socket(ServerIP, ServerPort);
            Log.d("result", "Connected!");
            setResult(true);
        } catch (Exception e) {
            Log.d("error", "Connection not established... Retrying...");
            setResult(false);
        }
    }

    //Sends char to the Pi for processing
    public void send(char c) {

        //Checking if checkbox is checked for cruise control
        if (c == 'a') {
            checked = true;
        } else if (c == 'b') {
            checked = false;
            c = '2';
        } else if (c == 'c') {
            checked = false;
        }
        if (!(c == '2' && checked)) {
            try {
                //Creating output stream to send data
                OutputStream os = socket.getOutputStream();
                //Making sure invalid characters arent sent
                if (c != 'a' && c != 'c') {
                    //Sending data
                    os.write(c);
                }
                Log.d("result", c + " sent");
                //Flushing the stream
                os.flush();
                //Closes the open output stream
                if (c == '8') {
                    os.close();
                    close();
                }
            } catch (UnknownHostException e) {
                System.out.print(e.toString());
            } catch (IOException e) {
                System.out.print(e.toString());
            } catch (Exception e) {
                System.out.print(e.toString());
            }
        }
    }

    //Closes the open socket
    public void close() {
        try {
            socket.close();
            setResult(false);
        } catch (IOException e) {
            System.out.print(e.toString());
            Log.d("error", "close not working");
        }
    }

    //Gets the result of connection
    public boolean getResult() {
            return result;
    }

    //Sets the result of connection
    public void setResult(boolean value) {
        result = value;
    }

    //Gets IP address that the socket is connected to
    public String getIP() {
        return ServerIP;
        //return socket.getRemoteSocketAddress().toString();
    }

    //Sets the IP address using individual values sent from log in or settings
    public void setIP(int ip_1, int ip_2, int ip_3, int ip_4) {
        StringBuilder sb = new StringBuilder();
        String s_dot = ".";
        sb.append(ip_1);
        sb.append(s_dot);
        sb.append(ip_2);
        sb.append(s_dot);
        sb.append(ip_3);
        sb.append(s_dot);
        sb.append(ip_4);
        ServerIP = new String(sb);
    }
}