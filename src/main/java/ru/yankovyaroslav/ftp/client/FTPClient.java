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

    private String file;

    private ByteArrayOutputStream fileBuffer;

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
                resetState();
                throw new FtpServerConnectionException("Ошибка при соединении с FTP сервером");
            }

            authenticate(username, password);

        } catch (Exception e) {
            redirectToConnectionMenu();
        }
    }

    public boolean uploadFile(String filePath) throws IOException {
        if (!isConnected) {
            System.out.println("WARNING! Клиент не подключен к серверу");
        }
        if (filePath != null && !filePath.isEmpty()) {
            if (serverMode == ServerMode.PASSIVE) {
                activatePassiveMode();
            }
            sendRequest("STOR " + filePath);
            String serverResponse = serverReader.readLine();
            if (serverResponse.startsWith("150")) {
                InputStream inputStream = Files.newInputStream(Paths.get(filePath));
                OutputStream outputStream = transferSocket.getOutputStream();

                byte[] buffer = new byte[8192];
                int byteElement;
                while ((byteElement = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteElement);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
                String response = serverReader.readLine();
                if (response.startsWith("226")) {
                    file = filePath;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean downloadFile(String fileName) {
        if (serverMode == ServerMode.PASSIVE) {
            activatePassiveMode();
        }
        try {
            sendRequest("RETR " + fileName);
            String serverResponse = serverReader.readLine();
            if (serverResponse.startsWith("1")) {
                InputStream inputStream = transferSocket.getInputStream();
                fileBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int byteElement;
                while ((byteElement = inputStream.read(buffer)) != -1) {
                    fileBuffer.write(buffer, 0, byteElement);
                }
                inputStream.close();
                serverResponse = serverReader.readLine();
                if (serverResponse.startsWith("2")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateFileOnServer(ByteArrayOutputStream outputStream) {
        if (serverMode == ServerMode.PASSIVE) {
            activatePassiveMode();
        }
        try {
            if (file != null && !file.isEmpty()) {
                sendRequest("STOR " + file);
                String serverResponse = serverReader.readLine();
            }
            OutputStream socketOutputStream = transferSocket.getOutputStream();
            socketOutputStream.write(outputStream.toByteArray());
            socketOutputStream.flush();
            socketOutputStream.close();
            serverReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
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

            System.out.println("\nSUCCESSFUL !!! Пассивный режим успешно включен! Адресс: " + serverHost + ". Порт " + serverPort);
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
        FtpServerConnectionMenu ftpServerConnectionMenu = new FtpServerConnectionMenu();
        ftpServerConnectionMenu.showError("Ошибка при соединении с FTP сервером");
    }

    private void authenticate(String username, String password) throws IOException {
        sendRequest("USER " + username);
        String serverResponse = serverReader.readLine();
        if (serverResponse.startsWith("331")) {
            sendRequest("PASS " + password);
            serverResponse = serverReader.readLine();
            if (serverResponse.startsWith("230")) {
                isConnected = true;
            } else {
                resetState();
                throw new FtpServerConnectionException("Ошибка при соединении с FTP сервером");
            }
        } else {
            resetState();
            throw new FtpServerConnectionException("Ошибка при соединении с FTP сервером");
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

    private void closeResources() {
        closeQuietly(connectionSocket);
        closeQuietly(transferSocket);
        closeQuietly(serverReader);
        closeQuietly(serverWriter);
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetState() {
        closeResources();
        host = null;
        username = null;
        password = null;
        serverHost = null;
        serverPort = 0;
        serverMode = null;
        isConnected = false;
        connectionSocket = null;
        transferSocket = null;
        serverReader = null;
        serverWriter = null;
        file = null;
        fileBuffer = null;
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

    public ByteArrayOutputStream getFileBuffer() {
        return fileBuffer;
    }

    public void setFileBuffer(ByteArrayOutputStream fileBuffer) {
        this.fileBuffer = fileBuffer;
    }

    public String getFile() {
        return file;
    }

    public ServerMode getServerMode() {
        return serverMode;
    }

    public void setServerMode(ServerMode serverMode) {
        this.serverMode = serverMode;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public Socket getConnectionSocket() {
        return connectionSocket;
    }

    public void setConnectionSocket(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public Socket getTransferSocket() {
        return transferSocket;
    }

    public void setTransferSocket(Socket transferSocket) {
        this.transferSocket = transferSocket;
    }

    public BufferedReader getServerReader() {
        return serverReader;
    }

    public void setServerReader(BufferedReader serverReader) {
        this.serverReader = serverReader;
    }

    public BufferedWriter getServerWriter() {
        return serverWriter;
    }

    public void setServerWriter(BufferedWriter serverWriter) {
        this.serverWriter = serverWriter;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
