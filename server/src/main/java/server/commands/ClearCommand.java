package server.commands;


import exceptions.WrongArgumentException;
import server.supervisor.Supervisor;
import transfers.Request;
import transfers.Response;
import transfers.ResponseStatus;

public class ClearCommand extends AbstractCommand {
    public ClearCommand(Supervisor supervisor) {
        super("clear", "Очистить коллекцию", supervisor);
    }

    @Override
    public Response execute(Request request) throws WrongArgumentException {
        if (!request.getArgs().isBlank()) throw new WrongArgumentException("");
        supervisor.getDatabase().clearData();
        return new Response(ResponseStatus.OK, "Коллекция очищена!");
    }
}
