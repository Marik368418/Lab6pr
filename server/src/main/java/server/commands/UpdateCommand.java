package server.commands;

import exceptions.WrongValuesException;
import model.SpaceMarine;
import server.supervisor.Supervisor;
import transfers.Request;
import transfers.Response;
import transfers.ResponseStatus;

import java.util.Optional;

public class UpdateCommand extends AbstractCommand {
    public UpdateCommand(Supervisor supervisor) {
        super("update", "Обновить значение элемента коллекции, id которого равен заданному", supervisor);
    }

    @Override
    public Response execute(Request request) throws WrongValuesException {
        if (!request.getArgs().matches("-?\\d+")) {
            return new Response(ResponseStatus.ERROR, "id должен быть целочисленным!");
        }
        Optional<SpaceMarine> spaceMarine = supervisor.getCollection().stream()
                .filter(p -> p.getId() == Integer.parseInt(request.getArgs()))
                .findFirst();
        if (supervisor.getCollection().isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста - нечего обновлять!");
        } else if (spaceMarine.isEmpty()) {
            return new Response(ResponseStatus.OK, "Нет элемента с таким id!");
        } else {
            supervisor.getDatabase().updateItem(Integer.parseInt(request.getArgs()), request.getObject());
            return new Response(ResponseStatus.OK, "Элемент с id " + request.getArgs() + " успешно обновлён!");
        }
    }
}
