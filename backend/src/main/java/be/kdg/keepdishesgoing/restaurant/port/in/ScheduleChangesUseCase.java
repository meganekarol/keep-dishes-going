package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.ScheduleDishChangesCommand;

public interface ScheduleChangesUseCase {
    void schedulePendingChanges(ScheduleDishChangesCommand command);
}
