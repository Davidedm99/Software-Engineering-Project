package it.polimi.ingsw.network.client;

import com.google.gson.Gson;

import it.polimi.ingsw.network.Utilities;
import it.polimi.ingsw.network.messages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    String hostName = "127.0.0.1";
    PrintWriter out;
    Scanner in;
    BufferedReader stdIn;
    Socket echoSocket;
    Gson gson;
    ClientVisitorHandler clientHandlerVisitor = new ClientVisitorHandler();

    public void run(int serverPortNumber) {
        gson = Utilities.initializeGsonMessage();

        try {
            echoSocket = new Socket(hostName, serverPortNumber);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new Scanner(echoSocket.getInputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e){
            System.out.println("Server not available");
            return;
        }
        String jsonString;
        while (!echoSocket.isClosed()) {
            try {
                jsonString = in.nextLine();
            } catch (NoSuchElementException e) {
                System.out.println("Error, disconnecting . . .");
                in.close();
                out.close();
                break;
            }
            try {
                handleMessage(gson.fromJson(jsonString, Message.class));
            } catch (IOException e) {
                System.out.println("Error, disconnecting . . .");
                in.close();
                out.close();
                break;
            }
        }
    }

    public Scanner getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public Socket getEchoSocket() {
        return echoSocket;
    }

    public BufferedReader getStdIn() {
        return stdIn;
    }

    public Gson getGson() {
        return gson;
    }

    public void handleMessage(Message m) throws IOException {
        ClientMessage clientMessage = (ClientMessage) m;
        clientMessage.accept(clientHandlerVisitor,this);
    }

    public static void main(String[] args) throws IOException {
        Integer serverPortNumber = Utilities.loadServerPortNumber();
        new Client().run(serverPortNumber);
    }
}
