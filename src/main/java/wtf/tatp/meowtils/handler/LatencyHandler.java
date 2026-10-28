package wtf.tatp.meowtils.handler;

import java.net.InetAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;

public class LatencyHandler {
    private static long lastPacket;

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        lastPacket = System.currentTimeMillis();
    }

    public static long getLastPacket() {
        return lastPacket;
    }

    public static EnumChatFormatting getLatencyColor(int ms) {
        if (ms < 40) {
            return EnumChatFormatting.DARK_GREEN;
        } else if (ms < 100) {
            return EnumChatFormatting.GREEN;
        } else if (ms < 150) {
            return EnumChatFormatting.YELLOW;
        } else if (ms < 200) {
            return EnumChatFormatting.GOLD;
        } else {
            return ms < 300 ? EnumChatFormatting.RED : EnumChatFormatting.DARK_RED;
        }
    }

    public static void ping(ServerData server, LatencyHandler.PingCallback callback) {
        new Thread(
                () -> {
                    try {
                        ServerAddress address = ServerAddress.fromString(server.serverIP);
                        final NetworkManager networkManager = NetworkManager.createNetworkManagerAndConnect(
                            InetAddress.getByName(address.getIP()), address.getPort(), false
                        );
                        networkManager.setNetHandler(new INetHandlerStatusClient() {
                            private long startTime;

                            public void handleServerInfo(S00PacketServerInfo packetIn) {
                                this.startTime = Minecraft.getSystemTime();
                                networkManager.sendPacket(new C01PacketPing(this.startTime));
                            }

                            public void handlePong(S01PacketPong packetIn) {
                                int latency = (int)(Minecraft.getSystemTime() - this.startTime);
                                callback.done(latency);
                                networkManager.closeChannel(new ChatComponentText(""));
                            }

                            public void onDisconnect(IChatComponent reason) {
                                callback.done(-1);
                            }
                        });
                        networkManager.sendPacket(new C00Handshake(47, address.getIP(), address.getPort(), EnumConnectionState.STATUS));
                        networkManager.sendPacket(new C00PacketServerQuery());
                    } catch (Exception var4) {
                        callback.done(-1);
                        Meowtils.error("Unable to ping server: " + var4);
                    }
                },
                "Meowtils-Ping"
            )
            .start();
    }

    public interface PingCallback {
        void done(int var1);
    }
}
