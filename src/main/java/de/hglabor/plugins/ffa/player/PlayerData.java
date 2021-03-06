package de.hglabor.plugins.ffa.player;

import de.hglabor.plugins.ffa.Main;
import de.hglabor.plugins.kitapi.kit.AbstractKit;
import de.hglabor.plugins.kitapi.kit.KitManager;
import de.hglabor.plugins.kitapi.kit.config.Cooldown;
import de.hglabor.plugins.kitapi.kit.config.KitMetaData;
import de.hglabor.plugins.kitapi.kit.config.KitProperties;
import de.hglabor.plugins.kitapi.kit.config.LastHitInformation;
import de.hglabor.plugins.kitapi.kit.kits.CopyCatKit;
import de.hglabor.plugins.kitapi.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PlayerData extends FFAPlayer {
    private final List<AbstractKit> kits;
    private final Map<AbstractKit, Map<Class<?>, Object>> kitAttributes;
    private final Map<AbstractKit, Cooldown> kitCooldowns;
    private final Map<KitMetaData, KitProperties> kitProperties;
    private final LastHitInformation lastHitInformation;
    private Scoreboard scoreboard;
    private Objective objective;
    private boolean kitsDisabled;

    protected PlayerData(UUID uuid) {
        super(uuid);
        kitAttributes = new HashMap<>();
        kitCooldowns = new HashMap<>();
        kitProperties = new HashMap<>();
        lastHitInformation = new LastHitInformation();
        kits = KitManager.getInstance().emptyKitList();
    }

    @Override
    public List<AbstractKit> getKits() {
        AbstractKit copyCatKit = this.getKitAttribute(CopyCatKit.INSTANCE);
        if (copyCatKit != null) {
            List<AbstractKit> kitList = new ArrayList<>(this.kits);
            kitList.add(copyCatKit);
            return kitList;
        } else {
            return this.kits;
        }
    }

    @Override
    public void setKits(List<AbstractKit> list) {
        this.kits.clear();
        this.kits.addAll(list);
    }

    @Override
    public boolean hasKit(AbstractKit kit) {
        AbstractKit copyCatKit = this.getKitAttribute(CopyCatKit.INSTANCE);
        return copyCatKit != null && copyCatKit.equals(kit) || this.kits.contains(kit);
    }

    @Override
    public boolean areKitsDisabled() {
        return kitsDisabled;
    }

    @Override
    public void setKit(AbstractKit abstractKit, int i) {
        kits.set(i, abstractKit);
    }

    @Override
    public boolean hasKitCooldown(AbstractKit kit) {
        return kitCooldowns.getOrDefault(kit, new Cooldown(false)).hasCooldown();
    }

    @Override
    public LastHitInformation getLastHitInformation() {
        return lastHitInformation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends KitProperties> T getKitProperty(KitMetaData kitMetaData) {
        return (T) kitProperties.getOrDefault(kitMetaData, null);
    }

    @Override
    public <T extends KitProperties> void putKitPropety(KitMetaData kitMetaData, T t) {
        kitProperties.put(kitMetaData, t);
    }

    @Override
    public boolean isValid() {
        return isInArena();
    }

    @Override
    public void disableKits(boolean kitsDisabled) {
        this.kitsDisabled = kitsDisabled;
    }

    @Override
    public void activateKitCooldown(AbstractKit kit, int seconds) {
        if (hasKit(kit) && !kitCooldowns.getOrDefault(kit, new Cooldown(false)).hasCooldown()) {
            kitCooldowns.put(kit, new Cooldown(true, System.currentTimeMillis()));
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> kitCooldowns.put(kit, new Cooldown(false)),// (seconds + additionalKitCooldowns.getOrDefault(kit, 0)) * 20);
                    (seconds) * 20L);
        }
    }

    @Override
    public Cooldown getKitCooldown(AbstractKit abstractKit) {
        return kitCooldowns.getOrDefault(abstractKit, new Cooldown(false));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getKitAttribute(AbstractKit kit, Class<?> clazz) {
        return (T) kitAttributes.getOrDefault(kit,new HashMap<>()).getOrDefault(clazz,null);
    }

    @Override
    public <T> void putKitAttribute(AbstractKit kit, T t, Class<?> clazz) {
        if (!kitAttributes.containsKey(kit)) {
            kitAttributes.put(kit,new HashMap<>());
        }
        kitAttributes.get(kit).put(clazz,t);
    }

    @Override
    public boolean isInCombat() {
        return false;
    }

    @Override
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @Override
    public Objective getObjective() {
        return objective;
    }

    @Override
    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    @Override
    public Locale getLocale() {
        return Utils.getPlayerLocale(uuid);
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void resetKitAttributes() {
        this.kitAttributes.clear();
    }

    public void resetKitCooldowns() {
        this.kitCooldowns.clear();
    }

    public void resetKitProperties() {
        this.kitProperties.clear();
    }
}
