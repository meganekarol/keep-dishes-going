package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.MarkOrderReadyCommand;

public interface MarkOrderReadyUseCase {
    void markOrderReady(MarkOrderReadyCommand command);
}
