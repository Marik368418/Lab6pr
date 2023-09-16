package server.commands;


import exceptions.WrongValuesException;
import server.supervisor.Supervisor;
import transfers.Request;
import transfers.Response;
import transfers.ResponseStatus;

public class RemoveByIdCommand extends AbstractCommand {
    public RemoveByIdCommand(Supervisor supervisor) {
        super("remove", "Удалить элемент из коллекции по его id", supervisor);
    }

    @Override
    public Response execute(Request request) throws WrongValuesException {
        if (!request.getArgs().matches("-?\\d+")) {
            return new Response(ResponseStatus.ERROR, "id должен быть целочисленным!");
        }
        if (supervisor.getCollection().isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста - нельзя ничего удалить!");
        } else {
            supervisor.getDatabase().removeByIdItem(Integer.parseInt(request.getArgs()));
            return new Response(ResponseStatus.OK, "Элемент коллекции с id " + request.getArgs() + " успешно удалён!");
        }
    }
}
