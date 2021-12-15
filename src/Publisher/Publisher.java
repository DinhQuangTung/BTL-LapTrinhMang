package Publisher;

import GlobalVariable.*;

import java.io.*;
import java.util.*;
import java.net.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.text.SimpleDateFormat;

public class Publisher implements Locations, Topics {
    static int PORT_NUMBER = 9000;

    static int randomData(String topic) {
        Random rand = new Random();
        int min;
        int max;

        if(topic.equals(ROOM_TEMP) || topic.equals(AC_TEMP)) {
            min = 16;
            max = 35;
        } else if (topic.equals(TV_SWITCH) || topic.equals(LIGHT_SWITCH)) {
            min = 0;
            max = 1;
        } else {
            // VOLUME_RANGE
            min = 0;
            max = 100;
        }

        return rand.nextInt(max - min + 1) + min;
    }

    static String randomLocation() {
        int min = 0;
        int max = 3;
        Random rand = new Random();
        int index = rand.nextInt(max - min + 1) + min;

        if(index == 0) return KITCHEN;
        if(index == 1) return LIVING_ROOM;
        if(index == 2) return BATHROOM;

        return BEDROOM;
    }

    static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    static String setDataForTopic(String topic) {
        JSONObject data = new JSONObject();
        String sensor = topic + "_SENSOR";
        data.put("topic", topic);
        data.put("location", randomLocation());
        data.put("sensor", sensor);
        data.put("value", randomData(topic));
        data.put("time", getCurrentDateTime());

        return data.toJSONString();
    }

    public static void main(String[] args) throws IOException {
        String stringToServer;
        String receivedFromServer;
        String selectedTopic = null;

        String SELECT_TOPIC = "SELECT_TOPIC";
        String WAITING = "WAITING";
        String CONNECTED = "CONNECTED";
        String SEND_DATA = "SEND_DATA";

        String state = SELECT_TOPIC;

        try {
            Socket clientSocket = new Socket("127.0.0.1", PORT_NUMBER);

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

            // show topics list
            String listTopic = "Topics: \n" + ROOM_TEMP + '\n' + TV_SWITCH + '\n' + LIGHT_SWITCH + '\n' + VOLUME_RANGE + '\n' + AC_TEMP;
            System.out.println(listTopic);


            while (true) {
                if (state.equals(SELECT_TOPIC)) {
                    System.out.print("Select a topic to push: ");
                    selectedTopic = inFromUser.readLine();

                    if (selectedTopic.equals(ROOM_TEMP) || selectedTopic.equals(TV_SWITCH) || selectedTopic.equals(LIGHT_SWITCH) || selectedTopic.equals(VOLUME_RANGE) || selectedTopic.equals(AC_TEMP)) {
                        state = WAITING;
                    } else {
                        System.out.println("Invalid topic!");
                        state = SELECT_TOPIC;
                    }
                } else if (state.equals(WAITING)) {
                    stringToServer = "PUBLISHER HELLO";
                    outToServer.writeBytes(stringToServer + '\n');
                    System.out.println("Publisher (QUIT for exit): " + stringToServer);
                    receivedFromServer = inFromServer.readLine();

                    if (receivedFromServer.equals("200 HELLO PUBLISHER")) {
                        System.out.println("Broker: 200 HELLO PUBLISHER");
                        state = CONNECTED;
                    } else {
                        System.out.println("Broker: Hello error");
                        state = WAITING;
                    }
                } else if (state.equals(CONNECTED)) {
                    stringToServer = "SEND";
                    outToServer.writeBytes(stringToServer + '\n');
                    System.out.println("Publisher (QUIT for exit): " + stringToServer);
                    receivedFromServer = inFromServer.readLine();

                    if (receivedFromServer.equals("210 SEND OK")) {
                        System.out.println("Broker: 210 SEND OK");
                        state = SEND_DATA;
                    } else {
                        System.out.println("Broker: Send error");
                        state = CONNECTED;
                    }
                } else if (state.equals(SEND_DATA)) {
                    stringToServer = setDataForTopic(selectedTopic);
                    outToServer.writeBytes(stringToServer + '\n');
                } else {
                    System.out.println("Invalid state");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
