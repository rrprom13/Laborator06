package ro.pub.cs.systems.eim.lab06.singlethreadedserver.network;

import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.lab06.singlethreadedserver.general.Constants;
import ro.pub.cs.systems.eim.lab06.singlethreadedserver.general.Utilities;

public class CommunicationThread extends Thread {

    private Socket socket;
    private EditText serverTextEditText;

    public CommunicationThread(Socket socket, EditText serverTextEditText) {
        this.socket = socket;
        this.serverTextEditText = serverTextEditText;
        start();
    }

    @Override
    public void run() {
        Log.v(Constants.TAG, "Connection opened with " + socket.getInetAddress() + ":" + socket.getLocalPort());
        // TODO exercise 5c
        // simulate the fact the communication routine between the server and the client takes 3 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException interruptedException) {
            Log.e(Constants.TAG, interruptedException.getMessage());
            if (Constants.DEBUG) {
                interruptedException.printStackTrace();
            }
        }

        PrintWriter printWriter = null;
        try {
            printWriter = Utilities.getWriter(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        printWriter.println(serverTextEditText.getText().toString());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.v(Constants.TAG, "Connection closed");
    }

}
