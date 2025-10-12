package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.ApplyPendingChangesCommand;

public interface ApplyPendingChangesUseCase {
    void applyPendingChanges(ApplyPendingChangesCommand command);
}
