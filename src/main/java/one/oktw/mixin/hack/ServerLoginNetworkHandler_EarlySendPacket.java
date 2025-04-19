package one.oktw.mixin.hack;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import one.oktw.VelocityLib;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandler_EarlySendPacket {

    @Shadow
    @Final
    public ClientConnection connection;

    @Inject(method = "onHello", at = @At(value = "HEAD"), cancellable = true)
    private void skipKeyPacket(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if (packet.getProfile().isComplete()) return; // Already receive profile form velocity.

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(ThreadLocalRandom.current().nextInt());
        buf.writeIdentifier(VelocityLib.PLAYER_INFO_CHANNEL);
        LoginQueryRequestS2CPacket s2cPacket = new LoginQueryRequestS2CPacket();
        try {
            s2cPacket.read(buf);
            connection.send(s2cPacket);
        } catch (IOException e) {
            LogManager.getLogger().error("Packet read failed.", e);
            return;
        }
        ci.cancel();
    }
}
