package Broker;

import java.net.*;
import java.io.*;
import GlobalVariable.*;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ConnectThread extends Thread {
    private DataInputStream in;
    private DataOutputStream out;

    Socket clientSocket;

    ConnectThread(Socket nSocket) {
        clientSocket = nSocket ;
    }
    public void stopConnect () throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public boolean checkData(String str) {
        return (str.indexOf("topic") != -1) && (str.indexOf("location") != -1) && (str.indexOf("sensor") != -1) && (str.indexOf("value") != -1) && (str.indexOf("time") != -1);
    }

    public void addDataToJson (String data) {
        String topic = "" ;
        String location = "" ;
        String sensor = "";
        String value  = "";
        String time = "";
        int i ;

        i = data.indexOf("topic") + 8 ;
        while ((i < data.length()) && (data.charAt(i + 1) != ',')) {
            topic = topic +  data.charAt(i) ;
            ++i;
        }

        i = data.indexOf("location") + 11 ;
        while ((i < data.length()) && (data.charAt(i + 1) != ',')) {
            location = location +  data.charAt(i) ;
            ++i;
        }

        i = data.indexOf("sensor") + 9 ;
        while ((i < data.length()) && (data.charAt(i + 1) != ',')) {
            sensor = sensor +  data.charAt(i) ;
            ++i;
        }

        i = data.indexOf("value") + 8 ;
        while ((i < data.length()) && (data.charAt(i + 1) != '}')) {
            value = value +  data.charAt(i) ;
            ++i;
        }

        i = data.indexOf("time") + 7 ;
        while ((i < data.length()) && (data.charAt(i + 1) != ',')) {
            time = time +  data.charAt(i) ;
            ++i;
        }

        //add data to json object
        JSONObject obj = new JSONObject();
        obj.put("topic", topic);
        obj.put("location", location);
        obj.put("sensor" , sensor) ;
        obj.put("value" , value) ;
        obj.put("time", time) ;

        JSONParser jsonParser = new JSONParser();
        JSONArray objArr = new JSONArray() ;
        try (FileReader reader = new FileReader("./src/Broker/topic_data.json"))
        {
            //Read JSON file
            Object objFile = jsonParser.parse(reader);
            objArr = (JSONArray) objFile;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int isAdd = 0 ;
        for (int index = 0 ; index < objArr.size() ; ++index) {
            JSONObject tmpObj = (JSONObject) objArr.get(index) ;
            String nTopic = (String) tmpObj.get("topic") ;
            String nLocation = (String) tmpObj.get("location") ;
            String nSensor = (String) tmpObj.get("sensor") ;

            if (nTopic.equals(topic) && nLocation.equals(location) && nSensor.equals(sensor)) {
                tmpObj.put("value", value) ;
                tmpObj.put("time",time) ;
                isAdd = 1 ;
                break;
            }
        }

        if (isAdd != 1) {
            objArr.add(obj) ;
        }

        try (FileWriter file = new FileWriter("./src/Broker/topic_data.json")) {
            //We can write any JSONArray or JSONObject instance to the file
            file.write(objArr.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String requestData (String request) {
        String [] words = request.split("-") ;
        String topic = words[0] ;
        String location = words[1] ;
        String sensor = words[2] ;
        String data = "" ;

        JSONArray objArr = new JSONArray() ;
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("./src/Broker/topic_data.json"))
        {
            //Read JSON file
            Object objFile = jsonParser.parse(reader);
            objArr = (JSONArray) objFile;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int index = 0 ; index < objArr.size() ; ++index) {
            JSONObject tmpObj = (JSONObject) objArr.get(index) ;
            String nTopic = (String) tmpObj.get("topic") ;
            String nLocation = (String) tmpObj.get("location") ;
            String nSensor = (String) tmpObj.get("sensor") ;

            if (nTopic.equals(topic) && nLocation.equals(location) && nSensor.equals(sensor)) {
                data = (String) tmpObj.toJSONString() ;
                break;
            }
        }
        return data ;
    }
    public void run() {
        try {
//            out = new DataOutputStream(clientSocket.getOutputStream());
//            in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String receive = in.readLine();
                if (receive.equals("SUBSCRIBER HELLO")) {
                    out.writeBytes("200 HELLO SUBSCRIBER" + '\n');
                    receive = in.readLine() ;
                    String data = requestData(receive) ;
                    out.writeBytes(data + '\n');
                } else if (receive.equals("PUBLISHER HELLO")) {
                    out.writeBytes("200 HELLO PUBLISHER" + '\n');
                } else if (receive.equals("SEND")) {
                    out.writeBytes("210 SEND OK" + '\n');
//                    receive = in.readLine();
//                    if (!checkData(receive)) {
//                        out.writeBytes("404 DATA ERROR" + '\n');
//                    } else {
//                        out.writeBytes("220 DATA OK" + '\n');
//                        String data = receive ;
//                        addDataToJson(data) ;
//                    }
                }
                else if (checkData(receive)) {
                    out.writeBytes("220 DATA OK" + '\n') ;
                    addDataToJson(receive) ;
                }
                else {
                    out.writeBytes("Wrong Command" + '\n');
                }
            }
        }
        catch (Exception E) {
            System.out.println(E);
        } finally {
        }
    }
}

public class Broker implements Locations, Topics {
    private ServerSocket serverSocket;

    public void getServer(int port) throws IOException {
        serverSocket = new ServerSocket(port) ;
        while (true) {
            System.out.println("Waiting for client....") ;
            Socket clientSocket = serverSocket.accept() ;

            if (clientSocket.isConnected()) {
                System.out.println("Client connected!") ;
            }

            Thread thread = new ConnectThread(clientSocket);
            thread.start();
        }
    }
    public static void main(String[] args) throws Exception {
        Broker broker = new Broker();
        broker.getServer(9000);
    }
}