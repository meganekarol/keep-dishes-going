package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.ScheduleDishChangesCommand;

/** Lets an owner book pending changes to go live later (US-07). */
public interface ScheduleChangesUseCase {
    /**
     * Books the pending changes for a chosen time.
     *
     * @param command which dishes to publish and when
     */
    void schedulePendingChanges(ScheduleDishChangesCommand command);

}
