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

    private static int randomData(String topic) {
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

    private static String randomLocation() {
        int min = 0;
        int max = 3;
        Random rand = new Random();
        int index = rand.nextInt(max - min + 1) + min;

        if(index == 0) return KITCHEN;
        if(index == 1) return LIVING_ROOM;
        if(index == 2) return BATHROOM;

        return BEDROOM;
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    private static String setDataForTopic(String topic) {
        JSONObject data = new JSONObject();
        String sensor = topic + "_SENSOR";
        data.put("topic", topic);
        data.put("location", randomLocation());
        data.put("sensor", sensor);
        data.put("value", randomData(topic));
        data.put("time", getCurrentDateTime());

        return data.toJSONString();
    }

    private static void notiReceived(String receivedMessage) {
        System.out.println("Broker: " + receivedMessage);
    }

    private static void sendToServer(OutputStream outStream, String message) throws IOException {
        DataOutputStream datOutStream = new DataOutputStream(outStream);
        datOutStream.writeBytes(message + '\n');
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

            Scanner scanner = new Scanner(System.in);

            InputStream inpStream = clientSocket.getInputStream();
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(inpStream));
            OutputStream outStream = clientSocket.getOutputStream();
            DataOutputStream datOutStream = new DataOutputStream(outStream);

            // show topics list
            String listTopic = "Topics: \n" + ROOM_TEMP + '\n' + TV_SWITCH + '\n' + LIGHT_SWITCH + '\n' + VOLUME_RANGE + '\n' + AC_TEMP;
            System.out.println(listTopic);

            while (true) {
                if (state.equals(SELECT_TOPIC)) {
                    System.out.print("Select a topic to push: ");
                    selectedTopic = scanner.nextLine();

                    if (selectedTopic.equals(ROOM_TEMP) || selectedTopic.equals(TV_SWITCH) || selectedTopic.equals(LIGHT_SWITCH) || selectedTopic.equals(VOLUME_RANGE) || selectedTopic.equals(AC_TEMP)) {
                        state = WAITING;
                    } else {
                        System.out.println("Invalid topic!");
                        state = SELECT_TOPIC;
                    }
                } else if (state.equals(WAITING)) {
                    stringToServer = "PUBLISHER HELLO";
                    sendToServer(outStream, stringToServer);
                    System.out.println("Publisher (QUIT for exit): " + stringToServer);
                    receivedFromServer = bufferRead.readLine();

                    if (receivedFromServer.equals("200 HELLO PUBLISHER")) {
                        notiReceived(receivedFromServer);
                        state = CONNECTED;
                    } else {
                        notiReceived("Hello error");
                        state = WAITING;
                    }
                } else if (state.equals(CONNECTED)) {
                    stringToServer = "SEND";
                    sendToServer(outStream, stringToServer);
                    System.out.println("Publisher (QUIT for exit): " + stringToServer);
                    receivedFromServer = bufferRead.readLine();

                    if (receivedFromServer.equals("210 SEND OK")) {
                        notiReceived(receivedFromServer);
                        state = SEND_DATA;
                    } else {
                        notiReceived("Send error");
                        state = CONNECTED;
                    }
                } else if (state.equals(SEND_DATA)) {
                    stringToServer = setDataForTopic(selectedTopic);
                    sendToServer(outStream, stringToServer);
                } else {
                    System.out.println("------------Invalid state-----------");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
