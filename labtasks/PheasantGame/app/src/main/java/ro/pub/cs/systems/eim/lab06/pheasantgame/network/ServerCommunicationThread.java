package ro.pub.cs.systems.eim.lab06.pheasantgame.network;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import android.os.Handler;

import java.util.List;
import java.util.Random;

import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Constants;
import ro.pub.cs.systems.eim.lab06.pheasantgame.general.Utilities;

public class ServerCommunicationThread extends Thread {

    private Socket socket;
    private TextView serverHistoryTextView;

    private Random random = new Random();

    private String expectedWordPrefix;

    private Context context;
    private Handler handler;

    public ServerCommunicationThread(Socket socket, TextView serverHistoryTextView, Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        if (socket != null) {
            this.socket = socket;
            Log.d(Constants.TAG, "[SERVER] Created communication thread with: " + socket.getInetAddress() + ":" + socket.getLocalPort());
        }
        this.serverHistoryTextView = serverHistoryTextView;
    }

    public void run() {
        try {
            if (socket == null) {
                return;
            }
            boolean isRunning = true;
            BufferedReader requestReader = Utilities.getReader(socket);
            PrintWriter responsePrintWriter = Utilities.getWriter(socket);

            while (true) {
                String line;
                do {
                    line = requestReader.readLine();
                } while (line == null);
                String history = serverHistoryTextView.getText().toString();
                history += "Server received " + line + " from client.\n";
                final String toWrite = history;

                handler.post(new Runnable() {
                    public void run() {
                        serverHistoryTextView.setText(toWrite);
                    }
                });

                if (line.equals(Constants.END_GAME)) {
                    break;
                }

                boolean valid = Utilities.wordValidation(line);
                String sentWord;
                if (!valid) {
                    sentWord = line;
                } else {
                    if (expectedWordPrefix == null || expectedWordPrefix.equals(line.substring(0, 2))) {
                        String prefix = line.substring(line.length() - 2, line.length());
                        List<String> possibleWords = Utilities.getWordListStartingWith(prefix);
                        if (possibleWords == null || possibleWords.size() == 0) {
                            sentWord = Constants.END_GAME;
                        } else {
                            int index = random.nextInt(possibleWords.size());
                            sentWord = possibleWords.get(index);
                            expectedWordPrefix = sentWord.substring(sentWord.length() - 2, sentWord.length());
                        }
                    } else {
                        sentWord = line;
                    }

                }
                // TODO exercise 7a
                responsePrintWriter.println(sentWord);
                history = serverHistoryTextView.getText().toString();
                history += "Server sent " + sentWord + " to client.\n";

                final String toWrite2 = history;

                handler.post(new Runnable() {
                    public void run() {
                        serverHistoryTextView.setText(toWrite2);
                    }
                });

                if (sentWord.equals(Constants.END_GAME)) {
                    break;
                }
            }
            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }
}
