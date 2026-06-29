package net.filipes.rituals.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.custom.LightningExplosionEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.*;

public enum ShadeshatterPowerup {

    DAMAGE_BOOST {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.ATTACK_DAMAGE, id("damage_boost"), 0.25, ADD_MULTIPLIED_TOTAL); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.ATTACK_DAMAGE, id("damage_boost")); }
        @Override public String displayName()        { return "§cDamage Boost §7(+25% damage)"; }
    },
    ATTACK_SPEED {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.ATTACK_SPEED, id("attack_speed"), 0.3, ADD_MULTIPLIED_TOTAL); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.ATTACK_SPEED, id("attack_speed")); }
        @Override public String displayName()        { return "§eAttack Speed §7(+30%)"; }
    },
    REACH_INCREASE {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.ENTITY_INTERACTION_RANGE, id("reach"), 1.5, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.ENTITY_INTERACTION_RANGE, id("reach")); }
        @Override public String displayName()        { return "§bReach §7(+1.5 blocks)"; }
    },
    MAX_HEALTH {
        @Override public void apply(ServerPlayer p) {
            addAttr(p, Attributes.MAX_HEALTH, id("max_health"), 4.0, ADD_VALUE);
            p.heal(4.0f);
        }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.MAX_HEALTH, id("max_health")); }
        @Override public String displayName()        { return "§aMax Health §7(+2 hearts)"; }
    },
    MOVEMENT_SPEED {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.MOVEMENT_SPEED, id("movement_speed"), 0.15, ADD_MULTIPLIED_TOTAL); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.MOVEMENT_SPEED, id("movement_speed")); }
        @Override public String displayName()        { return "§aMovement Speed §7(+15%)"; }
    },
    JUMP_BOOST {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.JUMP_STRENGTH, id("jump_boost"), 0.2, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.JUMP_STRENGTH, id("jump_boost")); }
        @Override public String displayName()        { return "§aJump Boost §7(+0.2 height)"; }
    },
    KNOCKBACK_STRENGTH {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.ATTACK_KNOCKBACK, id("knockback_str"), 1.5, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.ATTACK_KNOCKBACK, id("knockback_str")); }
        @Override public String displayName()        { return "§6Knockback §7(+1.5 knockback)"; }
    },
    KNOCKBACK_RESISTANCE {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.KNOCKBACK_RESISTANCE, id("knockback_res"), 0.3, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.KNOCKBACK_RESISTANCE, id("knockback_res")); }
        @Override public String displayName()        { return "§7Knockback Resistance §7(+30%)"; }
    },
    ARMOR_BOOST {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.ARMOR, id("armor"), 4.0, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.ARMOR, id("armor")); }
        @Override public String displayName()        { return "§7Armor §7(+4)"; }
    },
    ARMOR_TOUGHNESS {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.ARMOR_TOUGHNESS, id("armor_toughness"), 2.0, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.ARMOR_TOUGHNESS, id("armor_toughness")); }
        @Override public String displayName()        { return "§7Armor Toughness §7(+2)"; }
    },
    STEP_HEIGHT {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.STEP_HEIGHT, id("step_height"), 0.5, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.STEP_HEIGHT, id("step_height")); }
        @Override public String displayName()        { return "§aStep Height §7(walk up half-blocks)"; }
    },

    FIRE_RESISTANCE {
        @Override public void apply(ServerPlayer p)  { p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false)); }
        @Override public void remove(ServerPlayer p) { p.removeEffect(MobEffects.FIRE_RESISTANCE); }
        @Override public String displayName()        { return "§6Fire Resistance"; }
    },
    SLOW_FALLING {
        @Override public void apply(ServerPlayer p)  { p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false)); }
        @Override public void remove(ServerPlayer p) { p.removeEffect(MobEffects.SLOW_FALLING); }
        @Override public String displayName()        { return "§fSlow Falling"; }
    },
    REGENERATION {
        @Override public void apply(ServerPlayer p)  { p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, false)); }
        @Override public void remove(ServerPlayer p) { p.removeEffect(MobEffects.REGENERATION); }
        @Override public String displayName()        { return "§aRegeneration II"; }
    },
    ABSORPTION {
        @Override public void apply(ServerPlayer p)  { p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1, false, false)); }
        @Override public void remove(ServerPlayer p) { p.removeEffect(MobEffects.ABSORPTION); }
        @Override public String displayName()        { return "§6Absorption §7(4 hearts)"; }
    },

    LIFE_STEAL {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            a.heal(dmg * 0.15f);
        }
        @Override public String displayName() { return "§cLife Steal §7(15% of damage as healing)"; }
    },
    EXECUTE {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            if (t.getHealth() / t.getMaxHealth() < 0.25f) {
                t.hurtServer(lvl, src, dmg * 0.6f);
            }
        }
        @Override public String displayName() { return "§4Execute §7(+60% below 25% HP)"; }
    },
    FIRST_STRIKE {
        @Override public void apply(ServerPlayer p)  { ShadeshatterPowerupTracker.resetFirstStrike(p.getUUID()); }
        @Override public void remove(ServerPlayer p) { ShadeshatterPowerupTracker.clearFirstStrike(p.getUUID()); }
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            if (ShadeshatterPowerupTracker.consumeFirstStrike(a.getUUID())) {
                t.hurtServer(lvl, src, 10.0f);
            }
        }
        @Override public String displayName() { return "§eFirst Strike §7(+10 damage, first hit only)"; }
    },
    BACKSTAB {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            // Negative dot product = attacker is behind the target's facing direction
            net.minecraft.world.phys.Vec3 toAttacker = a.position().subtract(t.position()).normalize();
            if (t.getLookAngle().dot(toAttacker) < -0.3) {
                t.hurtServer(lvl, src, dmg * 0.5f);
            }
        }
        @Override public String displayName() { return "§8Backstab §7(+50% damage from behind)"; }
    },
    IGNITE {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            t.igniteForTicks(4);
        }
        @Override public String displayName() { return "§6Ignite §7(enemies burn for 4s)"; }
    },
    POISON {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, true));
        }
        @Override public String displayName() { return "§2Poison §7(Poison I on hit)"; }
    },
    WITHER {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            t.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, true));
        }
        @Override public String displayName() { return "§8Wither §7(Wither I on hit)"; }
    },
    BONUS_VS_UNDEAD {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            if (t.getType().builtInRegistryHolder().is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
                t.hurtServer(lvl, src, dmg * 0.4f);
            }
        }
        @Override public String displayName() { return "§fUndead Bane §7(+40% vs undead)"; }
    },
    BONUS_VS_HOSTILE {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            if (t instanceof Monster) {
                t.hurtServer(lvl, src, dmg * 0.25f);
            }
        }
        @Override public String displayName() { return "§cMob Slayer §7(+25% vs hostile mobs)"; }
    },
    EXPLOSION_ON_KILL {
        @Override public void apply(ServerPlayer p)  { addAttr(p, Attributes.MAX_HEALTH, id("expl_kill_hp"), -4.0, ADD_VALUE); }
        @Override public void remove(ServerPlayer p) { removeAttr(p, Attributes.MAX_HEALTH, id("expl_kill_hp")); }
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            if (killed) {
                LightningExplosionEntity explosion = new LightningExplosionEntity(ModEntities.LIGHTNING_EXPLOSION, lvl);
                explosion.setPos(t.getX(), t.getY() + 0.5, t.getZ());
                explosion.setEntityScale(1.5f);
                lvl.addFreshEntity(explosion);
            }
        }
        @Override public String displayName() { return "§6Volatile §7(explosion on kill, §4-2 hearts§7)"; }
    },

    GLASS_CANNON {
        @Override public void apply(ServerPlayer p) {
            addAttr(p, Attributes.ATTACK_DAMAGE, id("glass_dmg"),   0.5,  ADD_MULTIPLIED_TOTAL);
            addAttr(p, Attributes.ARMOR,         id("glass_armor"), -6.0, ADD_VALUE);
        }
        @Override public void remove(ServerPlayer p) {
            removeAttr(p, Attributes.ATTACK_DAMAGE, id("glass_dmg"));
            removeAttr(p, Attributes.ARMOR,         id("glass_armor"));
        }
        @Override public String displayName() { return "§cGlass Cannon §7(+50% dmg, §4-6 armor§7)"; }
    },
    BERSERKER {
        @Override public void apply(ServerPlayer p) {
            addAttr(p, Attributes.ATTACK_DAMAGE, id("berserk_dmg"), 0.4,  ADD_MULTIPLIED_TOTAL);
            addAttr(p, Attributes.MAX_HEALTH,    id("berserk_hp"),  -4.0, ADD_VALUE);
        }
        @Override public void remove(ServerPlayer p) {
            removeAttr(p, Attributes.ATTACK_DAMAGE, id("berserk_dmg"));
            removeAttr(p, Attributes.MAX_HEALTH,    id("berserk_hp"));
        }
        @Override public String displayName() { return "§4Berserker §7(+40% dmg, §4-2 hearts§7)"; }
    },
    ABILITY_HASTE {
        @Override public void apply(ServerPlayer p) {
            ServerPlayNetworking.send(p, new ShadeshatterHastePacket(2.0f));
        }
        @Override public void remove(ServerPlayer p) {
            ServerPlayNetworking.send(p, new ShadeshatterHastePacket(1.0f));
        }
        @Override public String displayName() { return "§bAbility Haste §7(cooldowns 2× faster)"; }
    },

    EXTENDED_MORPH {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {}
        @Override public float durationMultiplier()  { return 1.75f; }
        @Override public String displayName()        { return "§5Extended Morph §7(+75% morph duration)"; }
    },

    MORPH_RECHARGE {
        @Override public void apply(ServerPlayer p)  {}
        @Override public void remove(ServerPlayer p) {} // recharge logic is in ShadeshatterMorphHandler.restoreMorph
        @Override public String displayName()        { return "§dMorph Recharge §7(next morph cooldown halved)"; }
    },

    RAPID_RESET {
        @Override public void apply(ServerPlayer p)  { ShadeshatterPowerupTracker.setResetAvailable(p.getUUID(), true); }
        @Override public void remove(ServerPlayer p) { ShadeshatterPowerupTracker.setResetAvailable(p.getUUID(), false); }
        @Override public void onHit(ServerPlayer a, LivingEntity t, DamageSource src, float dmg, boolean killed, ServerLevel lvl) {
            if (killed && ShadeshatterPowerupTracker.consumeReset(a.getUUID())) {
                ServerPlayNetworking.send(a, new ShadeshatterAbilityResetPacket());
            }
        }
        @Override public String displayName() { return "§eRapid Reset §7(kill → instant ability reset)"; }
    },

    WEIGHTY_STRIKES {
        @Override public void apply(ServerPlayer p) {
            addAttr(p, Attributes.ATTACK_DAMAGE, id("weighty_dmg"), 0.35, ADD_MULTIPLIED_TOTAL);
            ServerPlayNetworking.send(p, new ShadeshatterHastePacket(0.5f)); // cooldowns 2× slower
        }
        @Override public void remove(ServerPlayer p) {
            removeAttr(p, Attributes.ATTACK_DAMAGE, id("weighty_dmg"));
            ServerPlayNetworking.send(p, new ShadeshatterHastePacket(1.0f));
        }
        @Override public String displayName() { return "§6Weighty Strikes §7(+35% dmg, §4half cooldown speed§7)"; }
    };

    public abstract void apply(ServerPlayer player);
    public abstract void remove(ServerPlayer player);
    public abstract String displayName();

    public void onHit(ServerPlayer attacker, LivingEntity target, DamageSource source,
                      float damageTaken, boolean killed, ServerLevel level) {}
    public float durationMultiplier() { return 1.0f; }


    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("rituals", "shadeshatter_" + path);
    }

    private static void addAttr(ServerPlayer p,
                                net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                                Identifier rid, double amount, Operation op) {
        var inst = p.getAttribute(attr);
        if (inst != null) inst.addOrUpdateTransientModifier(new AttributeModifier(rid, amount, op));
    }

    static void removeAttr(ServerPlayer p,
                           net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                           Identifier rid) {
        var inst = p.getAttribute(attr);
        if (inst != null) inst.removeModifier(rid);
    }
}