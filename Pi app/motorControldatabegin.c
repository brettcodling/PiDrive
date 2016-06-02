//Set up methods are Adafruit motor hat defaults but converted from Python to C using the WiringPi library


#include <wiringPiI2C.h>

#include <wiringPi.h>

#include <sys/socket.h>

#include <netinet/in.h>

#include <arpa/inet.h>

#include <stdio.h>

#include <stdlib.h>

#include <unistd.h>

#include <sys/types.h>

#include <netdb.h>

#include <errno.h>

#include <string.h>

#include <pthread.h>



//Global declarations

#define word	unsigned short


#define ADAFRUIT_MOTORHAT	0x60



//Setting GPIO pins

#define PWM_M1_PWM		8

#define PWM_M1_IN2		9

#define PWM_M1_IN1		10

#define PWM_M2_PWM		13

#define PWM_M2_IN2		12

#define PWM_M2_IN1		11

#define PWM_M3_PWM		2

#define PWM_M3_IN2		3

#define PWM_M3_IN1		4

#define PWM_M4_PWM		7

#define PWM_M4_IN2		6

#define PWM_M4_IN1		5


#define PWM_FREQUENCY		1600.0

#define PWM_PRESCALE		0xFE


#define PWM_MODE1		0x00

#define PWM_MODE2		0x01

#define PWM_LED0_ON_L		0x06

#define PWM_LED0_ON_H		0x07

#define PWM_LED0_OFF_L		0x08

#define PWM_LED0_OFF_H		0x09


#define PWM_RESTART		0x80

#define PWM_SLEEP		0x10

#define PWM_ALLCALL		0x01

#define PWM_INVRT		0x10

#define PWM_OUTDRV		0x04


#define PWM_ALL_LED_ON_L	0xFA

#define PWM_ALL_LED_ON_H	0xFB

#define PWM_ALL_LED_OFF_L	0xFC

#define PWM_ALL_LED_OFF_H	0xFD


#define MOTOR_FORWARD		1

#define MOTOR_BACK		2

#define MOTOR_BRAKE		3

#define MOTOR_RELEASE		4



//Setting up PWM features

void setAllPWM(word i2c, word on, word off){
	
	wiringPiI2CWriteReg8(i2c, PWM_ALL_LED_ON_L, on & 0xFF);
	
	wiringPiI2CWriteReg8(i2c, PWM_ALL_LED_ON_H, on >> 8);
	
	wiringPiI2CWriteReg8(i2c, PWM_ALL_LED_OFF_L, off & 0xFF);
	
	wiringPiI2CWriteReg8(i2c, PWM_ALL_LED_OFF_H, off >> 8);

}



//Setting up PWM features

void setPWM(word i2c, word pin, word on, word off){
	
	wiringPiI2CWriteReg8(i2c, PWM_LED0_ON_L + 4 * pin, on & 0xFF);
	
	wiringPiI2CWriteReg8(i2c, PWM_LED0_ON_H + 4 * pin, on >> 8);
	
	wiringPiI2CWriteReg8(i2c, PWM_LED0_OFF_L + 4 * pin, off & 0xFF);
	
	wiringPiI2CWriteReg8(i2c, PWM_LED0_OFF_H + 4 * pin, off >> 8);

}



//Setting whch pin to control

void setPin(word i2c, word pin, word value){
	
	if(pin < 0 || pin > 15){
		
		printf("PWM pin must be between 0 and 15 inclusive.  Received '%d'\n", pin);
		
		return;
	
	}

	
	
	//Setting pin to high or low
	
	switch(value){
		
		case 0:

			setPWM(i2c, pin, 0, 4096);

			break;

		case 1:

			setPWM(i2c, pin, 4096, 0);

			break;

		default:

			printf("PWM pin value must be 0 or 1.  Received '%d'\n", pin);

			return;

	}

}



//Controls the motors

void runMotor(word i2c, word motor, word command){

	word in1, in2;


	//Defines which motor to control by setting which pins are input and output

	switch(motor){

		case 1:

			in1 = PWM_M1_IN1;

			in2 = PWM_M1_IN2;

			break;

		case 2:

			in1 = PWM_M2_IN1;

			in2 = PWM_M2_IN2;

			break;

		case 3:

			in1 = PWM_M3_IN1;

			in2 = PWM_M3_IN2;

			break;

		case 4:

			in1 = PWM_M4_IN1;

			in2 = PWM_M4_IN2;

			break;

		default:

			printf("Invalid motor number '%d'\n", motor);

			return;

	}


	//Sets pins to high or low to define controls

	switch(command){

		case MOTOR_FORWARD:

			setPin(i2c, in2, 0);

			setPin(i2c, in1, 1);

			break;

		case MOTOR_BACK:

			setPin(i2c, in1, 0);

			setPin(i2c, in2, 1);

			break;

		case MOTOR_RELEASE:

			setPin(i2c, in1, 0);

			setPin(i2c, in2, 0);

			break;

		case MOTOR_BRAKE:

			setPin(i2c, in1, 1);

			setPin(i2c, in2, 1);

		default:

			printf("Unsupported command '%d'\n", command);

			return;

	}

}



//Sets speed of motors

void setSpeed(word i2c, word motor, word speed){

	if(speed < 0 || speed > 255){

		printf("Speed must be between 0 and 255 inclusive.  Received '%d'\n", speed);

		return;

	}


	//Defines which motor to control speed

	word pwm;

	switch(motor){

		case 1:

			pwm = PWM_M1_PWM;

			break;

		case 2:

			pwm = PWM_M2_PWM;

			break;

		case 3:

			pwm = PWM_M3_PWM;

			break;

		case 4:

			pwm = PWM_M4_PWM;

			break;

		default:

			printf("Unsupported motor '%i'\n", motor);

			break;

	}


	//Sets the speed of the given motor
	setPWM(i2c, pwm, 0, speed * 16);

}



//Intializes the motor hat

word init(){

	//Setup I2C

	word i2c = wiringPiI2CSetup(ADAFRUIT_MOTORHAT);


	//Setup PWM

	setAllPWM(i2c, 0, 0);

	wiringPiI2CWriteReg8(i2c, PWM_MODE2, PWM_OUTDRV);

	wiringPiI2CWriteReg8(i2c, PWM_MODE1, PWM_ALLCALL);

	delay(5);

	word mode1 = wiringPiI2CReadReg8(i2c, PWM_MODE1) & ~PWM_SLEEP;

	wiringPiI2CWriteReg8(i2c, PWM_MODE1, mode1);

	delay(5);


	//Set PWM frequency

	word prescale = (int)(25000000.0 / 4096.0 / PWM_FREQUENCY - 1.0);

	word oldmode = wiringPiI2CReadReg8(i2c, PWM_MODE1);

	word newmode = oldmode & (0x7F | 0x10);

	wiringPiI2CWriteReg8(i2c, PWM_MODE1, newmode);

	wiringPiI2CWriteReg8(i2c, PWM_PRESCALE, prescale);

	wiringPiI2CWriteReg8(i2c, PWM_MODE1, oldmode);

	delay(5);

	wiringPiI2CWriteReg8(i2c, PWM_MODE1, oldmode | 0x80);


	return i2c;

}



//Processes the data received from android

void processData(word i2c, int data){

	word motor;



	//Controls motor based on data received from android

	switch(data){

		case 1:

			motor = 1;

			runMotor(i2c, motor, MOTOR_FORWARD);

			break;

		case 2:

			motor = 1;

			//TURN OFF MOTOR

			runMotor(i2c, motor, MOTOR_RELEASE);

			break;

		case 3:

			motor = 1;

			runMotor(i2c, motor, MOTOR_BACK);

			break;

		case 4:

			motor = 4;

			runMotor(i2c, motor, MOTOR_FORWARD);

			break;

		case 5:

			motor = 4;

			//TURN OFF MOTOR

			runMotor(i2c, motor, MOTOR_RELEASE);

			break;

		case 6:

			motor = 4;

			runMotor(i2c, motor, MOTOR_BACK);

			break;


			//Sets speed of forward and backward motion

		case 7:

			motor = 1;

			//FAST SPEED

			setSpeed(i2c, motor, 250);

			break;

		case 9:

			motor = 1;

			//MEDIM SPEED

			setSpeed(i2c, motor, 200);

			break;

		case 0:

			motor = 1;

			//SLOW SPEED

			setSpeed(i2c ,motor, 150);

			break;

		//Releases motors if invalid data is received

		default:

			runMotor(i2c, 1, MOTOR_RELEASE);

			runMotor(i2c, 4, MOTOR_RELEASE);

			break;

	}

}



//Main function which is run when app starts

int main(){

	printf("starting\n");

	//Setting up library

	wiringPiSetup();

	//Initializing motor hat

	word i2c = init();

	//Sets the steering speed to fast
	setSpeed(i2c, 4, 250);
	word condition = 1;


	//Declaring sockets

	int server_sock_fd = 0, client_sock_fd = 0;

	struct sockaddr_in server_address;

	int iSetOption = 1;


	//Creating main socket wth ability to reuse port address

	server_sock_fd = socket(AF_INET, SOCK_STREAM, 0);

	setsockopt(server_sock_fd, SOL_SOCKET, SO_REUSEADDR, (char*)&iSetOption, sizeof(iSetOption));

	if (server_sock_fd == -1) {

		perror("socket");

		return EXIT_FAILURE;

	}



	//Setting server address to all 0's so that it is clean

	memset(&server_address, '0', sizeof(server_address));

	//Setting server address to needed values such as port number
	server_address.sin_family = AF_INET;

	server_address.sin_addr.s_addr = htonl(INADDR_ANY);

	server_address.sin_port = htons(51717);


	printf("socket made\n");


	//Binding socket to port

	if (-1 == bind(server_sock_fd, (struct sockaddr*)&server_address, sizeof(server_address))) {

		perror("bind");
		return EXIT_FAILURE;

	}


	printf("socket bind\n");



	//Listening for connecton from android

	if (-1 == listen(server_sock_fd, 1)) {

	perror("listen");

	printf("listen error\n");

	return EXIT_FAILURE;

	}


	//Declaring varibale to store recived data
	char data;


	//Label for goto statement
	reconnect:


	//Setting forward/backward speed to slow to match android default

	setSpeed(i2c, 2, 150);

	//Accepting connection from android

	client_sock_fd = accept(server_sock_fd, (struct sockaddr*)NULL, NULL);


	printf("connected\n");


	//Entering loop to receive and process data from android

	while(condition == 1) {

		//Declaring variables to handle connecton loss

		int error = 0;

		socklen_t len = sizeof(error);
		int retval = getsockopt(client_sock_fd, SOL_SOCKET, SO_ERROR, &error, &len);

		//If connection is lost redirect to listen for a new connection
		if (retval != 0 || error != 0) {

			goto reconnect;
		}

		//Receive data from android

		recv(client_sock_fd, &data, 1, 0);



		//Parse data from android to integer

		int dataPass = data - '0';


		//Process data from android

		processData(i2c, dataPass);



		printf("%c\n", data);

	
			
		//If android app is closed, redrect to listen for new connection and release motors

		if (dataPass == 8) {

			close(client_sock_fd);

			runMotor(i2c, 1, MOTOR_RELEASE);

			runMotor(i2c, 4, MOTOR_RELEASE);

			goto reconnect;

		}

	}
	
	return 0;

}

