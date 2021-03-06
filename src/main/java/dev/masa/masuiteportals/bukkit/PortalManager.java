package dev.masa.masuiteportals.bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import dev.masa.masuitecore.core.adapters.BukkitAdapter;
import dev.masa.masuitecore.core.channels.BukkitPluginChannel;
import dev.masa.masuiteportals.core.models.Portal;
import org.bukkit.*;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;

public class PortalManager {

    private MaSuitePortals plugin;

    public HashMap<String, Portal> portals = new HashMap<>();

    public PortalManager(MaSuitePortals plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads portals from list
     */
    public void loadPortals() {
        portals.values().forEach(this::fillPortal);
    }

    /**
     * Get all worlds used by portals
     *
     * @return a list of portals
     */
    public List<World> getWorlds() {
        return portals.values().stream().map(portal -> Bukkit.getWorld(portal.getMinLocation().getWorld())).collect(Collectors.toList());
    }

    /**
     * Get portals from specific world
     *
     * @param world world to use
     * @return returns a list of portals
     */
    public List<Portal> getPortalsFromWorld(World world) {
        return portals.values().stream().filter(portal -> Bukkit.getWorld(portal.getMinLocation().getWorld()) == world).collect(Collectors.toList());
    }

    /**
     * Removes portal
     *
     * @param portal specific portal from list
     */
    public void removePortal(Portal portal) {
        this.clearPortal(portal);
        portals.remove(portal.getName());
    }

    /**
     * Add portal
     *
     * @param portal portal to add
     */
    public void addPortal(Portal portal) {
        portals.put(portal.getName(), portal);
    }

    /**
     * Get portal by name
     *
     * @param name name of portal
     * @return portal or null
     */
    public Portal getPortal(String name) {
        return this.portals.get(name);
    }

    /**
     * Fill a portal
     *
     * @param portal portal to fill
     */
    public void fillPortal(Portal portal) {
        Location minLoc = BukkitAdapter.adapt(portal.getMinLocation());
        Location maxLoc = BukkitAdapter.adapt(portal.getMaxLocation());
        PortalRegion pr = new PortalRegion(minLoc, maxLoc);

        pr.blockList().forEach(block -> {
            if (block.getBlockData().getMaterial().equals(Material.AIR)) {
                // Portal manipulation if type is nether_portal
                if (portal.getFillType().toLowerCase().contains("nether_portal")) {
                    block.setType(Material.NETHER_PORTAL);
                    Orientable orientable = (Orientable) block.getBlockData();
                    switch (portal.getFillType().toLowerCase()) {
                        case ("nether_portal_north"):
                        case ("nether_portal_south"):
                            orientable.setAxis(Axis.X);
                            break;
                        case ("nether_portal_east"):
                        case ("nether_portal_west"):
                            orientable.setAxis(Axis.Z);
                            break;
                    }
                    block.setBlockData(orientable);
                } else {
                    block.setType(Material.valueOf(portal.getFillType().toUpperCase()));
                    if (portal.getFillType().equals("water")) {
                        Levelled levelledData = (Levelled) block.getState().getBlockData();
                        levelledData.setLevel(6);
                        block.getState().setBlockData(levelledData);
                    }
                }


            }
        });
    }

    /**
     * Clear portal
     *
     * @param portal portal to clear
     */
    public void clearPortal(Portal portal) {
        Location minLoc = BukkitAdapter.adapt(portal.getMinLocation());
        Location maxLoc = BukkitAdapter.adapt(portal.getMaxLocation());
        PortalRegion pr = new PortalRegion(minLoc, maxLoc);

        pr.blockList().forEach(block -> {
            if (block.getType().toString().toLowerCase().equals(portal.getFillType())) {
                block.setType(Material.AIR);
            }
        });
    }

    /**
     * Send player to portal's destination
     *
     * @param player player to send
     * @param portal portal to use
     */
    public void sendPlayer(Player player, Portal portal) {
        if (portal.getType().equals("server")) {
            new BukkitPluginChannel(plugin, player, "ConnectOther", player.getName(), portal.getDestination()).send();
        } else if (portal.getType().equals("warp")) {
            new BukkitPluginChannel(plugin, player, "Warp", player.getName(), portal.getDestination(), true, true, true, true).send();
        } else if (portal.getType().equals("host_transfer")) {
            player.kickPlayer("host_transfer=" + portal.getDestination());
        }
    }
}
