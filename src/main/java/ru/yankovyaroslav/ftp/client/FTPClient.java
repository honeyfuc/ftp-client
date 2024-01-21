package ru.yankovyaroslav.ftp.client;

import ru.yankovyaroslav.ftp.domain.ServerMode;
import ru.yankovyaroslav.ftp.domain.exception.FtpServerConnectionException;
import ru.yankovyaroslav.ftp.menu.impl.FtpServerConnectionMenu;
import ru.yankovyaroslav.ftp.menu.impl.ServerInteractionMenu;

import java.io.*;
import java.net.Socket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FTPClient {

    private String host;
    private String username;
    private String password;
    private String serverHost;
    private int serverPort;

    private ServerMode serverMode;
    private boolean isConnected;

    private Socket connectionSocket;

    private Socket transferSocket;

    private BufferedReader serverReader;

    private BufferedWriter serverWriter;

    private final Integer PORT = 21;
    private static FTPClient instance;

    private FTPClient() {

    }

    public static FTPClient getInstance() {
        if (instance == null) {
            instance = new FTPClient();
        }
        return instance;
    }

    public void connect() {
        if (connectionSocket != null) {
            showServerInteractionMenu();
        }

        try {
            connectionSocket = new Socket(host, PORT);
            serverReader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            serverWriter = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
            String serverResponse = serverReader.readLine();
            if (!serverResponse.startsWith("220 ")) {
                throw new FtpServerConnectionException("Ошибка при соединении с FTP сервером");
            }

            authenticate(username, password);

        } catch (Exception e) {
            redirectToConnectionMenu();
        }
    }

    public void uploadFile(String filePath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath));
             BufferedInputStream input = new BufferedInputStream(inputStream)) {


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void activatePassiveMode() {
        sendRequest("PASV");
        String serverResponse;
        try {
            serverResponse = serverReader.readLine();

            if (!serverResponse.startsWith("227")) {
                throw new FtpServerConnectionException("Не удалось подключить пассивный режим.");
            }

            String[] serverResponseInfo = getServerResponseInfo(serverResponse);

            serverHost = getServerHost(serverResponseInfo);
            serverPort = getServerPort(serverResponseInfo);

            transferSocket = new Socket(serverHost, serverPort);
            serverMode = ServerMode.PASSIVE;

            System.out.println("Пассивный режим успешно включен! Адресс: " + serverHost + ". Порт " + serverPort);

        } catch (FtpServerConnectionException e) {
            System.out.println("Возникла ошибка при включении Пассивного режима работы");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getServerResponseInfo(String serverResponse) {
        int openParenthesisIndex = serverResponse.indexOf('(');
        int closingParenthesisIndex = serverResponse.indexOf(')', openParenthesisIndex + 1);

        return serverResponse
                .substring(openParenthesisIndex + 1, closingParenthesisIndex)
                .split(",");
    }

    private String getServerHost(String[] serverResponseInfo) {
        return serverResponseInfo[0] + "." + serverResponseInfo[1] + "." + serverResponseInfo[2] + "." + serverResponseInfo[3];
    }

    private int getServerPort(String[] serverResponseInfo) {
        return Integer.parseInt(serverResponseInfo[4]) * 256 + Integer.parseInt(serverResponseInfo[5]);
    }

    private static void showServerInteractionMenu() {
        System.out.println("\nСоединение с FTP сервером уже установленно.\n");
        ServerInteractionMenu serverInteractionMenu
                = new ServerInteractionMenu();
        serverInteractionMenu.showMenu();
    }

    private static void redirectToConnectionMenu() {
        System.out.println("Ошибка при соединении с FTP сервером");
        FtpServerConnectionMenu ftpServerConnectionMenu = new FtpServerConnectionMenu();
        ftpServerConnectionMenu.showMenu();
    }

    private void authenticate(String username, String password) throws IOException {
        sendRequest("USER " + username);
        String serverResponse = serverReader.readLine();
        if (serverResponse.startsWith("331")) {
            sendRequest("PASS " + password);
            serverResponse = serverReader.readLine();
            if (serverResponse.startsWith("230")) {
                isConnected = true;
            }
        }
    }

    private void sendRequest(String request) {
        try {
            serverWriter.write(request + "\r\n");
            serverWriter.flush();
        } catch (IOException e) {
            try {
                connectionSocket.close();
                serverWriter.close();
                serverReader.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return PORT;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

}