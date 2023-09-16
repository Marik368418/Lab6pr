package server.supervisor;


import exceptions.RecursionScriptException;
import exceptions.WrongArgumentException;
import exceptions.WrongCommandException;
import exceptions.WrongValuesException;
import model.SpaceMarine;
import server.LinkedListCollection;
import server.commands.*;
import server.reader.DataReader;
import transfers.Request;
import transfers.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Supervisor implements Supervising {
    private static Scanner scanner;
    private static List<Path> stack = new LinkedList<>();
    private final LinkedListCollection database;
    private final HashMap<String, Command> commandMap;
    private boolean active;

    public Supervisor(DataReader reader) throws Exception {
        this.database = new LinkedListCollection(reader);
        scanner = new Scanner(System.in);
        this.commandMap = new HashMap<>();
        commandMap.put("add", new AddCommand(this));
        commandMap.put("add_if_max", new AddIfMaxCommand(this));
        commandMap.put("add_if_min", new AddIfMinCommand(this));
        commandMap.put("average_of_heart_count", new AverageOfHeartCountCommand(this));
        commandMap.put("clear", new ClearCommand(this));
        commandMap.put("execute_script", new ExecuteScriptCommand(this));
        commandMap.put("exit", new ExitCommand(this));
        commandMap.put("group_counting_by_name", new GroupCountingByNameCommand(this));
        commandMap.put("help", new HelpCommand(this));
        commandMap.put("info", new InfoCommand(this));
        commandMap.put("min_by_id", new MinByIdCommand(this));
        commandMap.put("remove_by_id", new RemoveByIdCommand(this));
        commandMap.put("remove_first", new RemoveFirstCommand(this));
        commandMap.put("show", new ShowCommand(this));
        commandMap.put("update", new UpdateCommand(this));
    }

    public static Scanner getScanner() {
        return scanner;
    }

    public static void setScanner(Scanner scan) {
        scanner = scan;
    }

    public LinkedList<SpaceMarine> getCollection() {
        return database.getData();
    }

    public LinkedListCollection getDatabase() {
        return database;
    }

    //@Override
    public void run() throws Exception {
        this.active = true;
        System.out.println("Добро пожаловать, Ярослав Кулинич!");
        while (active) {
            //waitCommand();
        }
    }

    public HashMap<String, Command> getHashMapCommands() {
        return commandMap;
    }

    public Command getCommandByName(String name) {
        return commandMap.get(name);
    }

    @Override
    public void stop() {
        this.active = false;
    }

    /*@Override
    public void waitCommand() throws Exception {
        System.out.print(">>> ");
        try {
            if (!scanner.hasNext()) System.exit(0);
            String userCommand = scanner.nextLine().trim();
            //this.start(userCommand.split(" ", 2));
        } catch (WrongCommandException e) {
            System.out.println(e.getMessage());
        } catch (WrongArgumentException e) {
            System.out.println(e.getMessage());
        }
    }*/

    public Response start(Request request) throws WrongCommandException, WrongValuesException, WrongArgumentException {
        Command command = commandMap.get(request.getCommandName());
        System.out.println(command);
        if (command == null) throw new WrongCommandException();
        Response response = command.execute(request);
        return response;
    }

    /*public void executeScript(String fileName) {
        stack.add(Paths.get(fileName));
        Scanner tmpScanner = getScanner();
        try (Scanner scrScanner = new Scanner(new File(fileName))) {
            if (!scrScanner.hasNext()) throw new NoSuchElementException();
            setScanner(scrScanner);
            do {
                String userCmd = scrScanner.nextLine().trim();
                while (scrScanner.hasNextLine() && userCmd.split(" ", 2)[0].isEmpty()) {
                    userCmd = scrScanner.nextLine().trim();
                }
                if (userCmd.split(" ", 2)[0].equals("execute_script")) {
                    if (stack.contains(Paths.get(userCmd.split(" ", 2)[1].trim()))) {
                        throw new RecursionScriptException("Нельзя вывести скрипт рекурсивно");
                    }
                }
                stack.clear();
                //start(userCmd.split(" ", 2));
            } while (scrScanner.hasNextLine());
            setScanner(tmpScanner);
        } catch (WrongArgumentException e) {
            System.out.println(e.getMessage());
            setScanner(tmpScanner);
        } catch (FileNotFoundException e) {
            System.out.println();
            setScanner(tmpScanner);
        } catch (NoSuchElementException e) {
            System.out.println();
            setScanner(tmpScanner);
        } catch (WrongCommandException e) {
            System.out.println(e.getMessage());
            setScanner(tmpScanner);
        } catch (RecursionScriptException e) {
            System.out.println(e.getMessage());
            setScanner(tmpScanner);
        } catch (Exception e) {
            System.out.println("Непредвиденная ошибка");
            setScanner(tmpScanner);
        }
    */
}