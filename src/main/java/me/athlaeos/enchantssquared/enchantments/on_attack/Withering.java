package me.athlaeos.enchantssquared.enchantments.on_attack;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.animations.Animation;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.*;
import me.athlaeos.enchantssquared.managers.AnimationRegistry;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.enchantssquared.utility.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;

public class Withering extends CustomEnchant implements TriggerOnAttackEnchantment {

    private final double chanceBase;
    private final double chanceLv;
    private final int durationBase;
    private final int durationLv;
    private final int amplifierBase;
    private final int amplifierLv;
    private final String particleAnimation;
    private final Sound sound;
    private final YamlConfiguration config;
    private final Collection<String> incompatibleVanillaEnchantments;
    private final Collection<String> incompatibleCustomEnchantments;

    public Withering(int id, String type) {
        super(id, type);
        this.config = ConfigManager.getInstance().getConfig("config.yml").get();
        this.chanceBase = config.getDouble("enchantment_configuration.withering.apply_chance");
        this.chanceLv = config.getDouble("enchantment_configuration.withering.apply_chance_lv");
        this.amplifierBase = config.getInt("enchantment_configuration.withering.amplifier");
        this.amplifierLv = config.getInt("enchantment_configuration.withering.amplifier_lv");
        this.durationBase = config.getInt("enchantment_configuration.withering.duration");
        this.durationLv = config.getInt("enchantment_configuration.withering.duration_lv");
        this.naturallyCompatibleWith = new HashSet<>(config.getStringList("enchantment_configuration.withering.compatible_with"));
        this.incompatibleVanillaEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.withering.incompatible_vanilla_enchantments"));
        this.incompatibleCustomEnchantments = new HashSet<>(config.getStringList("enchantment_configuration.withering.incompatible_custom_enchantments"));
        this.sound = Utils.soundFromString(config.getString("enchantment_configuration.withering.sound"), Sound.ENTITY_WITHER_SKELETON_DEATH);
        this.particleAnimation = config.getString("enchantment_configuration.withering.animation");

        this.icon = ItemUtils.getItemStackFromConfig(config, "enchantment_configuration.withering.icon", new ItemStack(Material.WITHER_SKELETON_SKULL));
    }

    private final LevelService mainHandLevels = new LevelsFromMainHandAndEquipment(this);
    private final LevelService offHandLevels = new LevelsFromOffHandAndEquipment(this);
    @Override
    public LevelService getLevelService(boolean offHand, LivingEntity entity) {
        return offHand ? offHandLevels : mainHandLevels;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker) {
        LivingEntity victim = (LivingEntity) e.getEntity();
        if (shouldEnchantmentCancel(level, realAttacker, victim.getLocation())) return;

        double chance = chanceBase + ((level - 1) * chanceLv);
        if (Utils.getRandom().nextDouble() < chance){
            int duration = durationBase + ((level - 1) * durationLv);
            int amplifier = amplifierBase + ((level - 1) * amplifierLv);

            EntityUtils.applyPotionEffectIfStronger(victim,
                    new PotionEffect(PotionEffectType.WITHER, duration, amplifier, true, false, true)
            );

            if (sound != null) victim.getWorld().playSound(victim, sound, 0.5f, 1f);
            Animation animation = AnimationRegistry.get(particleAnimation);
            if (animation != null) animation.play(victim.getEyeLocation());
        }
    }

    @Override
    public String getDisplayEnchantment() {
        return config.getString("enchantment_configuration.withering.enchant_name", getType())
                .replace(" %lv_roman%", "")
                .replace(" %lv_number%", "");
    }

    @Override
    public String getDescription() {
        return config.getString("enchantment_configuration.withering.description");
    }

    @Override
    public boolean isEnabled() {
        return config.getBoolean("enchantment_configuration.withering.enabled");
    }

    @Override
    public String getRequiredPermission() {
        return "es.enchant.withering";
    }

    @Override
    public boolean conflictsWithEnchantment(String enchantment) {
        return incompatibleCustomEnchantments.contains(enchantment) || incompatibleVanillaEnchantments.contains(enchantment);
    }

    private final Collection<String> naturallyCompatibleWith;
    @Override
    public boolean isNaturallyCompatible(Material material) {
        return MaterialClassType.isMatchingClass(material, naturallyCompatibleWith);
    }

    @Override
    public boolean isFunctionallyCompatible(Material material) {
        return true;
    }

    @Override
    public int getWeight() {
        return config.getInt("enchantment_configuration.withering.weight");
    }

    @Override
    public int getMaxLevel() {
        return config.getInt("enchantment_configuration.withering.max_level");
    }

    @Override
    public int getMaxTableLevel() {
        return config.getInt("enchantment_configuration.withering.max_level_table");
    }

    @Override
    public boolean isTreasure() {
        return config.getBoolean("enchantment_configuration.withering.is_treasure");
    }

    @Override
    public boolean isBookOnly() {
        return config.getBoolean("enchantment_configuration.withering.book_only");
    }

    @Override
    public boolean isTradingEnabled() {
        return config.getBoolean("enchantment_configuration.withering.trade_enabled");
    }

    @Override
    public int getTradingMinBasePrice() {
        return config.getInt("enchantment_configuration.withering.trade_cost_base_lower");
    }

    @Override
    public int getTradingMaxBasePrice() {
        return config.getInt("enchantment_configuration.withering.trade_cost_base_upper");
    }

    @Override
    public int getTradingMinLeveledPrice() {
        return config.getInt("enchantment_configuration.withering.trade_cost_lv_lower");
    }

    @Override
    public int getTradingMaxLeveledPrice() {
        return config.getInt("enchantment_configuration.withering.trade_cost_base_upper");
    }

    private final ItemStack icon;
    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public String getWorldGuardFlagName() {
        return "es-deny-withering";
    }
}
