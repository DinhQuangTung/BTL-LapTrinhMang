package Subscriber;

import GlobalVariable.Locations;
import GlobalVariable.Topics;
import org.json.simple.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONArray;

import java.util.concurrent.TimeUnit;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Subscriber implements Locations, Topics {

    static int PORT_NUMBER = 9000;

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Socket serverAddress = new Socket("127.0.0.1", PORT_NUMBER);

        String BEGIN = "BEGIN";
        String CONNECTED = "CONNECTED";
        String RECEIVE_DATA = "RECEIVE_DATA";
        int delayReceive = 1;

        String state = BEGIN;
        String savedTopic = "";
        String savedTopicData = "";

        InputStream inpStream = serverAddress.getInputStream();
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(inpStream));

        OutputStream outStream = serverAddress.getOutputStream();
        DataOutputStream datOutStream = new DataOutputStream(outStream);

        System.out.println("Connected to the server ....");

        String receivedMessage, sendMessage;
        while (true) {
            if (state.equals(BEGIN)) {
                System.out.print("Input a message to the server:  ");
                sendMessage = scanner.nextLine();
                sendToServer(datOutStream, sendMessage);
                receivedMessage = bufferRead.readLine();
                notiReceived(receivedMessage);

                if (receivedMessage.equals("200 HELLO SUBSCRIBER")) {
                    state = CONNECTED;
                } else {
                    System.out.println("Invalid command!");
                    state = BEGIN;
                }
            } else if (state.equals(CONNECTED)) {
                System.out.print("Input a message to the server:  ");
                sendMessage = scanner.nextLine();
                savedTopic = sendMessage;
                sendToServer(datOutStream, sendMessage);
                receivedMessage = bufferRead.readLine();
                notiReceived(receivedMessage);

                if (receivedMessage.equals("210 TOPIC OK")) {
                    state = RECEIVE_DATA;
                } else {
                    state = CONNECTED;
                }
            } else if (state.equals(RECEIVE_DATA)) {
                sendToServer(datOutStream, savedTopic);
                receivedMessage = bufferRead.readLine();
                notiReceived(receivedMessage);
                receivedMessage = bufferRead.readLine();
                if (!receivedMessage.equals(savedTopicData)) {
                    notiReceived(receivedMessage);
                    savedTopicData = receivedMessage;
                }

                TimeUnit.SECONDS.sleep(delayReceive);
            } else {
                System.out.println("Invalid state");
            }

//            System.out.print("Input a message to the server:  ");
//            //
//            sendMessage = scanner.nextLine();
//            sendToServer(datOutStream, sendMessage);
//            //
//            receivedMessage = bufferRead.readLine();
//            notiReceived(receivedMessage);
//            if (sendMessage.equals("QUIT")) {
//                break;
//            }
//            if (isJSONValid(receivedMessage)) {
//                JsonDecode(receivedMessage);
//            }

        }
    }


    private static void notiReceived(String receivedMessage) {
        System.out.println("Received message from server:  " + receivedMessage);
    }

    private static void sendToServer(OutputStream outStream, String message) throws IOException {
        DataOutputStream datOutStream = new DataOutputStream(outStream);
        datOutStream.writeBytes(message + '\n');
    }

//    public static boolean isJSONValid(String test) {
//        try {
//            new JSONObject(test);
//        } catch (JSONException ex) {
//            // edited, to include @Arthur's comment
//            // e.g. in case JSONArray is valid as well...
//            try {
//                new JSONArray(test);
//            } catch (JSONException ex1) {
//                return false;
//            }
//        }
//        return true;
//    }

    public static boolean isJSONValid(String test) {
        try {
            Object obj = new JSONParser().parse(test);

            return (obj instanceof JSONObject || obj instanceof JSONArray);
        } catch (ParseException e) {
            return false;
        }
    }

    public static void JsonDecode(String jsonStr) {
        Object obj = JSONValue.parse(jsonStr);
        JSONObject jsonObject = (JSONObject) obj;

        String topic = (String) jsonObject.get("topic");
        String location = (String) jsonObject.get("location");
        String sensor = (String) jsonObject.get("sensor");
        String value = (String) jsonObject.get("value");
        String time = (String) jsonObject.get("time");

        System.out.println("topic: " + topic);
        System.out.println("location: " + location);
        System.out.println("sensor: " + sensor);
        System.out.println("value: " + value);
        System.out.println("time: " + time);
    }

//    static String setTopic(String topics, String location, String sensor ){
//        return topics + "/" + location + "/" + sensor;
//    }
//
//    private static void showDataTopic(String topic) {
//
//    }
//    static void splitTopic(String topics){
//        String[] string = topics.split("/");
//        String topic = string[0];
//        String location = string[1];
//        String sensor = string[2];
//
//    }


}

