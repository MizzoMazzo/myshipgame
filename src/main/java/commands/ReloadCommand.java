package commands;

import events.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ReloadCommand extends Command {

    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {
        Ship player = map.getClientShip();
        AmmunitionStorage ammunitionStorage = player.getAmmunitionStorage();

        if (ammunitionStorage == null || ammunitionStorage.isEmpty()) {
            // Spieler hat keine Munition zum Nachladen
            // Bereits geladen? => Don't care https://forum.se.cs.uni-saarland.de/t/448/10
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            return eventList;
        } else {
            if (player.isLoaded()) {
                // Spieler hat bereits geladen und hat Munition => eine Kugel futsch https://forum.se.cs.uni-saarland.de/t/448/4
                ammunitionStorage.remove();

                List<Event> eventList = new ArrayList<>();
                eventList.add(new CommandFailedEvent(this));
                return eventList;
            } else {
                // Alles normal
                ammunitionStorage.remove();
                player.setLoaded(true);

                List<Event> eventList = new ArrayList<>();
                eventList.add(new ReloadedEvent());
                return eventList;
            }
        }
    }

    @Override
    public boolean requiresGameStarted() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public String toString() {
        return "ReloadCommand";
    }
}
