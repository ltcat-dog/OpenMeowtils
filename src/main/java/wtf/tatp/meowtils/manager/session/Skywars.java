package wtf.tatp.meowtils.manager.session;

import java.util.function.BooleanSupplier;

public enum Skywars {
    ALL(() -> SessionManager.skywars),
    LOBBY(() -> SessionManager.skywarsLobby),
    GAME(() -> SessionManager.skywarsGame),
    MINI(() -> SessionManager.skywarsGameMini);

    private final BooleanSupplier supplier;

    private Skywars(BooleanSupplier supplier) {
        this.supplier = supplier;
    }

    public boolean isActive() {
        return this.supplier.getAsBoolean();
    }

    public boolean isNotActive() {
        return !this.isActive();
    }
}
