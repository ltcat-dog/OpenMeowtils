package wtf.tatp.meowtils.manager.session;

import java.util.function.BooleanSupplier;

public enum MegaWalls {
    ALL(() -> SessionManager.megaWalls),
    LOBBY(() -> SessionManager.megaWallsLobby),
    GAME(() -> SessionManager.megaWallsLobby);

    private final BooleanSupplier supplier;

    private MegaWalls(BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean isActive() {
        return this.supplier.getAsBoolean();
    }

    public boolean isNotActive() {
        return !this.isActive();
    }
}
