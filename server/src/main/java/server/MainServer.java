package server;


import server.reader.FileReader;
import server.supervisor.Supervisor;

import java.io.FileWriter;

public class MainServer {


    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Передайте единственное значение аргументов - название файла");
            System.exit(0);
        }
        try {
//            int id = 0;
//            FileWriter writer = new FileWriter(args[0]);
//            writer.write( "\"id\",\"name\",\"coordinates_x\",\"coordinates_y\",\"creation_date\",\"health,heart_count\",\"achievements\",\"category\",\"chapter_name\",\"chapter_marines_count\"\n");
//            for (int i = 0; i < 4900; i++){
//                writer.write("\"" + String.valueOf(id) + "\"" + ",\"Mark\",\"273.0\",\"228.0\",\"2023-09-14T18:42:47.5005838+03:00[Europe/Moscow]\",\"1\",\"1\",\"5\",\"ASSAULT\",\"Konb\",\"4\"\n");
//                id++;
//            }
            var reader = new FileReader(args[0]);
            var supervisor = new Supervisor(reader);
            var requestHandler = new RequestHandler(supervisor);
            var server = new Server(supervisor, 8888, 100000, requestHandler);
            Thread mainThread = Thread.currentThread();
            Thread controllingServerThread = new Thread(() -> server.controlServer(mainThread));
            controllingServerThread.start();
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("_");
        }
    }
}