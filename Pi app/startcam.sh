#!/bin/sh


export LD_LIBRARY_PATH=/home/pi/mjpg-streamer-experimental/


/home/pi/mjpg-streamer-experimental/mjpg_streamer -o "output_http.so -w /home/pi/mjpg-streamer-experimental/www" -i "input_raspicam.so -fps 15 -quality 15" &