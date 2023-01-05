package me.fodded.duels.utils;

import org.bukkit.Location;
import org.bukkit.World;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;

public class BukkitUtils {
    public static String serializeLocation(Location unserialized) {
        String serialized = unserialized.getWorld().getName() + "; " + unserialized.getX() + "; " + unserialized.getY() + "; " + unserialized.getZ() + "; " + unserialized.getYaw()
                + "; " + unserialized.getPitch();
        return serialized;
    }

    public static Location deserializeLocation(String serialized, World world) {
        String[] divPoints = serialized.split("; ");
        Location deserialized = new Location(world, parseDouble(divPoints[1]), parseDouble(divPoints[2]), parseDouble(divPoints[3]));
        deserialized.setYaw(parseFloat(divPoints[4]));
        deserialized.setPitch(parseFloat(divPoints[5]));
        return deserialized;
    }
}
