package commands;

import model.Ship.ActorType;
import model.Tile.Direction;

public class OwnCommandFactory implements CommandFactory<Command> {
    @Override
    public Command createRegister(String name, ActorType actorType) {
        return new RegisterCommand(name, actorType);
    }

    @Override
    public Command createMove(Direction direction) {
        return new MoveCommand(direction);
    }

    @Override
    public Command createEndTurn() {
        return new EndTurnCommand();
    }

    @Override
    public Command createRepair() {
        return new RepairCommand();
    }

    @Override
    public Command createFire(Direction direction) {
        return new FireCommand(direction);
    }

    @Override
    public Command createPickup() {
        return new PickupCommand();
    }

    @Override
    public Command createSell() {
        return new SellCommand();
    }

    @Override
    public Command createDrop(int treasureId) {
        return new DropCommand(treasureId);
    }

    @Override
    public Command createReload() {
        return new ReloadCommand();
    }

    @Override
    public Command createRestock(int amount) {
        return new RestockCommand(amount);
    }

    @Override
    public Command createLeave() {
        return new LeaveCommand();
    }
}
