package btxds.dekal.relay.mixin;

import btxds.dekal.relay.Config;
import btxds.dekal.relay.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class MixinClientHandshake {

    @Shadow @Final private Minecraft minecraft;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Inject(
            method = "authenticateServer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAuthenticateServer(String digest, CallbackInfoReturnable<Component> cir) {
        Main.LOGGER.info("[Dekal Relay] Intercepted authenticateServer! Digest: {}", digest);

        try {
            String token = this.minecraft.getUser().getAccessToken();
            String strippedUuid = this.minecraft.getUser().getProfileId().toString().replace("-", "");

            String jsonBody = String.format(
                    "{\"accessToken\":\"%s\",\"selectedProfile\":\"%s\",\"serverId\":\"%s\"}",
                    token,
                    strippedUuid,
                    digest
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Config.AUTH_SERVER_URL + "/session/minecraft/join"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 204 || response.statusCode() == 200) {
                Main.LOGGER.info("[Dekal Relay] Session successfully verified with Dekal Transmitter!");
                cir.setReturnValue(null);
            } else {
                Main.LOGGER.error("[Dekal Relay] Auth server rejected join: HTTP {}", response.statusCode());
                cir.setReturnValue(Component.literal("Dekal Auth Error: " + response.body()));
            }

        } catch (Exception e) {
            Main.LOGGER.error("[Dekal Relay] Failed to contact auth server: ", e);
            cir.setReturnValue(Component.literal("Не удалось связаться с сервером авторизации"));
        }
    }

}