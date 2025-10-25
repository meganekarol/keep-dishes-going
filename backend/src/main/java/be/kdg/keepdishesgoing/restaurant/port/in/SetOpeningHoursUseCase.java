package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.SetOpeningHoursCommand;

public interface SetOpeningHoursUseCase {
    void setOpeningHours(SetOpeningHoursCommand command);
}
