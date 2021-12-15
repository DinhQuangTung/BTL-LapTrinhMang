package Subscriber;

import GlobalVariable.Locations;
import GlobalVariable.Topics;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 */
public class Subscriber implements Locations, Topics {


    static int PORT_NUMBER = 9000;
    static boolean ended = false;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Socket serverAddress = new Socket("127.0.0.1", PORT_NUMBER);

        InputStream inpStream = serverAddress.getInputStream();
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(inpStream));

        OutputStream outStream = serverAddress.getOutputStream();
        DataOutputStream datOutStream = new DataOutputStream(outStream);

        System.out.println("Connected to the server ....");

        String receivedMessage, sendMessage;
        while (true) {
            System.out.print("Input a message to the server:  ");
            //
            sendMessage = scanner.nextLine();
            sendToServer(datOutStream, sendMessage);
            //
            receivedMessage = bufferRead.readLine();
            notiReceived(receivedMessage);
            if (sendMessage.equals("QUIT")) {
                break;
            }
            if (receivedMessage.equals("Invalid command")) {
                return;
            }
            if (sendMessage.equals("TOPIC")) {
                receivedMessage
            }

        }
    }



    private static void notiReceived(String receivedMessage) {
        System.out.println("Received message from server:  " + receivedMessage);
    }

    private static void sendToServer(OutputStream outStream, String message) throws IOException {
        DataOutputStream datOutStream = new DataOutputStream(outStream);
        datOutStream.writeBytes(message + '\n');
    }
    static void splitTopic(String topics){
        String[] string = topics.split("/");
        String topic = string[0];
        String location = string[1];
        String sensor = string[2];

    }
    private static void selectTopic(InputStream inpStream, OutputStream outStream) throws IOException {
        while (true) {
            System.out.print("Input a message to the server:  ");
            //
            sendMessage = scanner.nextLine();
            sendToServer(datOutStream, sendMessage);
            //
            receivedMessage = bufferRead.readLine();
            notiReceived(receivedMessage);
            if (sendMessage.equals("QUIT")) {
                break;
            }
            if (receivedMessage.equals("Invalid command")) {
                return;
            }
            if (sendMessage.equals("TOPIC")) {

            }

        }
    }
    private static void showDataTopic(String topic) {

    }

    private static void getItemTopic(){

    }


}

