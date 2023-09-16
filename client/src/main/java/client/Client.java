package client;

import exceptions.ConnectionException;
import exceptions.WrongValuesException;
import managers.SerializationManager;
import transfers.Request;
import transfers.Response;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client {
    private final String host;
    private final int port;
    private final int reconnectionTimeout;
    private final int maxReconnectionAttempts;
    private final Manager manager;
    private int reconnectionAttempts;
    private Socket socket;
    private OutputStream serverWriter;
    private InputStream serverReader;

    public Client(String host, int port, int reconnectionTimeout, int maxReconnectionAttempts, Manager manager) {
        this.host = host;
        this.port = port;
        this.reconnectionTimeout = reconnectionTimeout;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.manager = manager;
    }

    public void run() {
        try {
            boolean processingStatus = true;
            while (processingStatus) {
                try {
                    connect();
                    processingStatus = processRequestToServer();
                } catch (ConnectionException e) {
                    if (reconnectionAttempts >= maxReconnectionAttempts) {
                        System.out.println("Превышено количество попыток подключения");
                        break;
                    }
                    try {
                        Thread.sleep(reconnectionTimeout);
                    } catch (IllegalArgumentException timeoutEx) {
                        System.out.println("Время ожидания подключения '" + reconnectionTimeout +
                                "' находится за пределом допустимого значения");
                        System.out.println("Производиться повторное подключение");
                    } catch (Exception timeoutEx) {
                        System.out.println("Произошла ошибка при попытке ожидания подключения");
                        System.out.println("Производиться повторное подключение");
                    }
                }
                reconnectionAttempts++;
            }
            if (socket != null) socket.close();
            System.out.println("Работа клиента успешно завершена");
        } catch (WrongValuesException e) {
            System.out.println("Клиент не может быть запущен");
        } catch (IOException e) {
            System.out.println("Произошла ошибка при попытке завершить соединение с сервером");
        }
    }

    private void connect() throws ConnectionException, WrongValuesException {
        try {
            if (reconnectionAttempts >= 1) System.out.println("Повторное соединение с сервером...");
            socket = new Socket(host, port);
            System.out.println("Соединение с сервером успешно установлено.");
            System.out.println("Ожидание разрешения на обмен данными.");

            serverWriter = socket.getOutputStream();
            serverReader = socket.getInputStream();

            System.out.println("Разрешение на обмен данными получено.");
            System.out.println("Добро пожаловать!");
        } catch (IllegalArgumentException e) {
            System.out.println("Адрес сервера введен некорректно");
            throw new WrongValuesException("");
        } catch (IOException e) {
            System.out.println("Произошла ошибка соединения с сервером.");
            throw new ConnectionException();
        }

    }

    private boolean processRequestToServer() {
        Request requestToServer = null;
        Response serverResponse = null;
        do {
            try {
                requestToServer = serverResponse != null ?
                        manager.handle(serverResponse.getStatus()) :
                        manager.handle(null);

                if (requestToServer.isEmpty()) continue;
                serverWriter.write(SerializationManager.serialize(requestToServer).array());
                if (serverReader == null) serverReader = new ObjectInputStream(socket.getInputStream());

                ByteBuffer bb = ByteBuffer.allocate(10000);
                serverReader.read(bb.array());

                serverResponse = (Response) SerializationManager.deserialize(bb);
                int responseLen = 0;

                try {
                    responseLen = Integer.parseInt(serverResponse.getResponseBody());
                }
                catch (NumberFormatException ignored){};

                if (responseLen == 0) {
                    System.out.println(serverResponse.getResponseBody());
                }
                else {
                    StringBuilder generalRes = new StringBuilder();
                    while (responseLen != 0) {
                        ByteBuffer bb2 = ByteBuffer.allocate(10000);
                        serverReader.read(bb2.array());
                        serverResponse = (Response) SerializationManager.deserialize(bb2);
                        generalRes.append(serverResponse.getResponseBody());

                        responseLen--;
                    }
                    System.out.println(generalRes);
                }

            } catch (InvalidClassException | NotSerializableException e) {
                System.out.println("Произошла ошибка при отправке данных на сервер");
            } catch (ClassNotFoundException e) {
                System.out.println("Произошла ошибка при чтении полученных данных");
            } catch (IOException e) {
                System.out.println("Соединение с сервером разорвано");
                try {
                    reconnectionAttempts++;
                    connect();
                } catch (ConnectionException | WrongValuesException reconnectionEx) {
                    if (requestToServer.getCommandName().equals("exit"))
                        System.out.println("Команда не зарегистрирована на сервере");
                    else System.out.println("Попробуйте повторить команду позже");
                }
            }
        } while (!requestToServer.getCommandName().equals("exit"));
        return false;
    }
}