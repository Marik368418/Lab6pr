package server;


import managers.SerializationManager;
import server.exceptions.ServerLaunchException;
import server.supervisor.Supervisor;
import transfers.Request;
import transfers.Response;
import transfers.ResponseStatus;


import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port = 8888;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private ServerSocketChannel ssc;
    private Selector selector;
    private boolean isActivate;
    private Supervisor supervisor;
    private RequestHandler requestHandler;
    private int timeout;

    private HashMap<SocketChannel, LinkedList<Response>> responseMap = new HashMap<>();

    public Server(Supervisor supervisor, int port, int timeout, RequestHandler requestHandler) {
        this.supervisor = supervisor;
        this.timeout = timeout;
        this.requestHandler = requestHandler;
    }

    public void open() throws ServerLaunchException {
        try {
            System.out.println("Запуск сервера... ");
            ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress("localhost", port));
            ssc.configureBlocking(false);
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            activateServer();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("Порт " + port + " недоступен!");
            throw new ServerLaunchException("Сервер не смог запуститься!!!");
        } catch (IOException e) {
            System.out.println("Непредвиденная ошибка при использовании порта " + port + "!");
            throw new ServerLaunchException("Сервер не смог запуститься!!!");
        }
    }

    public boolean receiveRequest(SocketChannel channel) throws IOException {
//        executor.submit(() -> {
            Request userRequest = null;
            Response responseToUser = null;

            ByteBuffer bb = ByteBuffer.allocate(10000);
            channel.read(bb);

            try  {
                userRequest = (Request) SerializationManager.deserialize(bb);
                responseToUser = requestHandler.handle(userRequest);

                processResponse(channel, responseToUser);

                System.out.println("Запрос '" + userRequest.getCommandName() + "' успешно обработан");

                return false;
            } catch (ClassNotFoundException e) {
                System.out.println("Ошибка при чтении полученных данных");
            } catch (InvalidClassException | NotSerializableException ex) {
                System.out.println("Ошибка при отправке данных на клиент");
            } catch (IOException e) {
                System.out.println("Разрыв соединения с клиентом");
                throw new IOException();
            }
            return true;
//        });
//        return true;
    }

    private void processResponse(SocketChannel channel, Response response) {
        String body = response.getResponseBody();
        int responseBodyLength = body.getBytes(StandardCharsets.UTF_8).length;
        LinkedList<Response> responses = new LinkedList<>();

        if (responseBodyLength > 5000) {
            int amountOfPackages = responseBodyLength / 4000 + (responseBodyLength % 4000 != 0 ? 1 : 0);
            responses.add(new Response(ResponseStatus.OK, String.valueOf(amountOfPackages)));
            String[] responseParts = divideOnSameParts(body, 4000);

            for (String val : responseParts) {
                Response resp = new Response(ResponseStatus.OK, val);
                responses.add(resp);
            }
        }
        else {
            responses.add(response);
        }
        responseMap.put(channel, responses);
    }

    public static String[] divideOnSameParts(String src, int len) {
        String[] result = new String[(int)Math.ceil((double)src.length()/(double)len)];
        for (int i=0; i<result.length; i++)
            result[i] = src.substring(i*len, Math.min(src.length(), (i+1)*len));
        return result;
    }

    public void run() throws ServerLaunchException, IOException {
        open();
        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                try {
                    if (key.isAcceptable()) {
                        SocketChannel sc = ssc.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                        System.out.println("Соединение с клиентом установлено!");

                    } else if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        receiveRequest(sc);

                    } else if (key.isWritable()) {

                        SocketChannel sc = (SocketChannel) key.channel();
                        if (responseMap.containsKey(sc)) {
                            List<Response> responses = responseMap.get(sc);
                            for (Response response: responses) {
                                sc.write(SerializationManager.serialize(response));
                                Thread.sleep(50);
                            }
                        }
                        responseMap.remove(sc);
                    }
                }
                catch (IOException e) {
                    key.channel().close();
                    System.out.println("Client error");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    iterator.remove();
                }
            }
        }
    }
//            while (isActivate) {
//                try {
//                    isActivate = receiveRequest(connect());
//                } catch (ConnectException e) {
//                    break;
//                }
//            }

//    public void stop() {
//        deactivateServer();
//        try {
//            ssc.close();
//            System.out.println("Работа сервера завершена!");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void activateServer() {
        isActivate = true;
        System.out.println("Сервер запущен!");
    }

    public void deactivateServer() {
        isActivate = false;
        System.out.println("Сервер закрыт!");
    }

    public int getPort() {
        return port;
    }

    public void controlServer(Thread threadToControl) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.nextLine();
            if (!threadToControl.isAlive()) {
                break;
            }
            switch (command) {
                case "save" -> {
                    try {
                        supervisor.getDatabase().saveData();
                    } catch (IOException e) {
                        System.out.println("Ошибка при сохранении коллекции");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                case "exit" -> {
//                    try {
//                        if (!server.isClosed()) {
//                            server.close();
//                        }
//                    } catch (IOException e) {
//
//                    }

                    System.out.println("На выход!");
                    System.exit(0);
                }
                default -> {
                    System.out.println("Неизвестная команда: " + command);
                }
            }
        }
    }
}
