package ro.pub.cs.systems.eim.lab06.pheasantgame.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Constants;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Utilities;

public class ClientCommunicationThread extends Thread {

    private Socket socket = null;

    private String mostRecentWordSent = new String();
    private String mostRecentValidPrefix = new String();

    private Context context;
    private Handler handler;
    private EditText wordEditText;
    private Button sendButton;
    private TextView clientHistoryTextView;

    public ClientCommunicationThread(Socket socket, Context context, Handler handler, EditText wordEditText, Button sendButton, TextView clientHistoryTextView) {
        this.socket = socket;
        this.context = context;
        this.handler = handler;
        this.wordEditText = wordEditText;
        this.sendButton = sendButton;
        this.clientHistoryTextView = clientHistoryTextView;
        if (socket == null) {
            try {
                socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
            } catch (UnknownHostException unknownHostException) {
                Log.e(Constants.TAG, "An exception has occurred: " + unknownHostException.getMessage());
                if (Constants.DEBUG) {
                    unknownHostException.printStackTrace();
                }
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
        Log.d(Constants.TAG, "[CLIENT] Created communication thread with: " + socket.getInetAddress() + ":" + socket.getLocalPort());
    }

    public void run() {
        try {
            BufferedReader responseReader = Utilities.getReader(socket);
            PrintWriter requestPrintWriter = Utilities.getWriter(socket);
            // TODO exercise 7b
            String wordSent;
            do {
                wordSent = wordEditText.getText().toString();
            } while (wordSent.length() <= 2);
            mostRecentWordSent = wordSent;

            requestPrintWriter.println(mostRecentWordSent);
            String history = clientHistoryTextView.getText().toString();
            history += "Client sent " + mostRecentWordSent + " to server.\n";
            final String toWrite = history;

            handler.post(new Runnable() {
                public void run() {
                    clientHistoryTextView.setText(toWrite);
                }
            });

            if (mostRecentWordSent.equals(Constants.END_GAME)) {
                wordEditText.setEnabled(false);
                sendButton.setEnabled(false);
                return;
            }

            String receivedWord;
            do {
                receivedWord = responseReader.readLine();
            } while (receivedWord == null);

            history = clientHistoryTextView.getText().toString();
            history += "Client received " + receivedWord + " from server.\n";
            final String toWrite2 = history;

            handler.post(new Runnable() {
                public void run() {
                    clientHistoryTextView.setText(toWrite2);
                }
            });

            if (receivedWord.equals(Constants.END_GAME)) {
                wordEditText.setEnabled(false);
                sendButton.setEnabled(false);
                return;
            }
            if (!receivedWord.equals(mostRecentWordSent)) {
                mostRecentValidPrefix = receivedWord.substring(receivedWord.length() - 2, receivedWord.length());
            }

            final String finalPrefix = mostRecentValidPrefix;
            handler.post(new Runnable() {
                public void run() {
                    wordEditText.setText(finalPrefix);
                }
            });

        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}