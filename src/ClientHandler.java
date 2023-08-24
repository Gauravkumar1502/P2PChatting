import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable{
//    set of clientHandlers
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = bufferedReader.readLine();
            clientHandlers.forEach(clent -> {
                while (clent.username.equals(this.username)){
                    try{
                        bufferedWriter.write("Username already exists");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        this.username = bufferedReader.readLine();
                    }catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            });
            bufferedWriter.write("!exit: to exit");
            bufferedWriter.newLine();
            bufferedWriter.write("ls: to list all connected clients");
            bufferedWriter.newLine();
            bufferedWriter.write("@username [message]: to send message to specific client");
            bufferedWriter.newLine();
            bufferedWriter.write("[message]: to send message to all clients");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            clientHandlers.add(this);

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String message;
        try{
            while (socket.isConnected()) {
                message = bufferedReader.readLine();
                if (message.startsWith("!exit")) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                } else if (message.equals("ls")) {
                    String list = clientHandlers.stream()
                            .filter(clientHandler -> !clientHandler.username.equals(this.username))
                            .map(clientHandler -> clientHandler.username + "\n")
                            .collect(Collectors.joining());
                    bufferedWriter.write(list);
                    bufferedWriter.flush();
                } else if (message.startsWith("@")) {
                    String[] split = message.split(" ", 2);
                    if (split.length < 2) {
                        bufferedWriter.write("Usage: @username [message]");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        continue;
                    }
                    String recipientName = split[0].substring(1);
                    String messageToRecipient = split[1];
                    ClientHandler recipient = clientHandlers.stream()
                            .filter(clientHandler -> clientHandler.username.equals(recipientName))
                            .findFirst()
                            .orElse(null);
                    if (recipient != null) {
                        recipient.bufferedWriter.write(this.username + ": " + messageToRecipient);
                        recipient.bufferedWriter.newLine();
                        recipient.bufferedWriter.flush();
                    } else {
                        bufferedWriter.write("User " + recipientName + " not found");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                } else
                    sentMessageToAllClients(this.username + ": " + message);
            }
        }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void sentMessageToAllClients(String message){
        if (clientHandlers.size() == 1){
            try{
                bufferedWriter.write("No other clients connected");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }else {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (!clientHandler.username.equals(this.username)) {
                        clientHandler.bufferedWriter.write(message);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
                }
            }
        }
    }
    public void removeClientHandler(){
        clientHandlers.remove(this);
        sentMessageToAllClients("Server: " + username + " disconnected");
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(socket != null){
                socket.close();
            }
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}