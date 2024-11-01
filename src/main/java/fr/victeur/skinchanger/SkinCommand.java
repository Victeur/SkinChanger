package fr.victeur.skinchanger;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;

public class SkinCommand implements CommandExecutor {

    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigner=false";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof final Player player)) {
            sender.sendMessage("§cOnly players can run this command");
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        final String targetSkin = args[0];
        final PlayerProfile playerProfile = player.getPlayerProfile();
        playerProfile.setProperties(getTextureProperty(targetSkin));
        player.setPlayerProfile(playerProfile);

        player.sendMessage("§cYour skin has been changed");

        return true;
    }

    private Collection<ProfileProperty> getTextureProperty(String targetSkin) {
        final String profileResponse = makeRequest(PROFILE_URL + targetSkin);
        final JsonObject profileObject = JsonParser.parseString(profileResponse).getAsJsonObject();

        if (!profileObject.has("id")) {
            throw new RuntimeException("Profile not found for " + targetSkin);
        }

        final String uuid = profileObject.get("id").getAsString();
        final String skinResponse = makeRequest(SKIN_URL.formatted(uuid));
        final JsonObject skinObject = JsonParser.parseString(skinResponse).getAsJsonObject();

        if (!skinObject.has("properties") || skinObject.getAsJsonArray("properties").size() == 0) {
            throw new RuntimeException("Skin properties not found for UUID " + uuid);
        }

        final JsonObject skinProperty = skinObject.getAsJsonArray("properties").get(0).getAsJsonObject();
        final String value = skinProperty.get("value").getAsString();

        System.out.println("Profile Property: " + value);
        return List.of(new ProfileProperty("textures", value));
    }


    private String makeRequest(String url) {
        try {
            final HttpClient httpClient = HttpClient.newBuilder().build();
            final HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build();
            final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
