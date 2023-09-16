package server.commands;


import server.supervisor.Supervisor;
import transfers.Request;
import transfers.Response;
import transfers.ResponseStatus;

public class RemoveFirstCommand extends AbstractCommand {
    public RemoveFirstCommand(Supervisor supervisor) {
        super("remove_first", "Удалить первый элемент из коллекции", supervisor);
    }

    @Override
    public Response execute(Request request) {
        if (supervisor.getCollection().isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста - нельзя ничего удалить!");
        } else {
            supervisor.getDatabase().removeFirstItem();
            return new Response(ResponseStatus.OK, "Первый элемент коллекции успешно удалён!");
        }
    }
}
