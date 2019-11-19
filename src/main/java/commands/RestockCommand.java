package commands;

import events.*;
import model.*;
import model.Tile.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RestockCommand extends Command {
    private final int amount;
    private static final int MAXAMOUNT = Integer.MAX_VALUE / 5;

    public RestockCommand(int amount) {
        this.amount = amount;
    }


    @Override
    public int actionsUsed() {
        return 1;
    }

    @Override
    public List<Event> exec(Map map, Random random, int actionsLeft) {

        if (map.getPlayerTile().getFieldType() == FieldType.HARBOR) {
            // Spieler ist auf Hafen, ammunitionStorage-Nullcheck lt. Mika nicht nötig
            Ship player = map.getClientShip();
            AmmunitionStorage ammunitionStorage = player.getAmmunitionStorage();
            final int totalPrice = amount * Ammunition.PRICE;
            final int ammunitionCapacity = player.getAmmunitionStorage().getCapacity();

            if (amount < 0) {
                // Negative Anzahl => Ganze Munition weg
                ammunitionStorage.clear();

                List<Event> eventList = new ArrayList<>();
                eventList.add(new CommandFailedEvent(this));
                return eventList;
            } else if (amount == 0) {
                // Abfrage für den Cutter https://forum.se.cs.uni-saarland.de/t/493/3
                List<Event> eventList = new ArrayList<>();
                eventList.add(new RestockedEvent(amount));
                return eventList;
            } else if (amount > MAXAMOUNT){ //Spieler will mehr als maximal erlaubt ist kaufen
                player.setGold(0);

                List<Event> eventList = new ArrayList<>();
                eventList.add(new CommandFailedEvent(this));
                return eventList;
            } else if (totalPrice > player.getGold()) {
                // Spieler kauft mehr als er sich leisten kann => Geld weg, keine Munition
                player.setGold(0);

                List<Event> eventList = new ArrayList<>();
                eventList.add(new CommandFailedEvent(this));
                return eventList;
            } else {
                player.setGold(player.getGold() - totalPrice);
                // Falls Storage voll wird ohne dass alle Kugeln eingeladen wurden => diese Munition wird nicht eingeladen
                // Mitzählen wie viele Kugeln eingeladen wurden in `restockedCounter`
                int restockedCounter = 0;
                for (int i = 0; i < ammunitionCapacity && i < amount; i++) {
                    ammunitionStorage.add(new Ammunition());
                    restockedCounter++;
                }

                List<Event> eventList = new ArrayList<>();
                eventList.add(new RestockedEvent(restockedCounter));
                return eventList;
            }
        } else {
            List<Event> eventList = new ArrayList<>();
            eventList.add(new CommandFailedEvent(this));
            return eventList;
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestockCommand that = (RestockCommand) o;
        return amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    @Override
    public String toString() {
        return "RestockCommand";
    }
}
