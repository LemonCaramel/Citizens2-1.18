package net.citizensnpcs.npc.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftShulker;
import org.bukkit.entity.Shulker;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.EntityAIBodyControl;
import net.minecraft.server.v1_9_R1.EntityShulker;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.SoundEffect;
import net.minecraft.server.v1_9_R1.World;

public class ShulkerController extends MobEntityController {
    public ShulkerController() {
        super(EntityShulkerNPC.class);
    }

    @Override
    public Shulker getBukkitEntity() {
        return (Shulker) super.getBukkitEntity();
    }

    public static class EntityShulkerNPC extends EntityShulker implements NPCHolder {
        private final CitizensNPC npc;

        public EntityShulkerNPC(World world) {
            this(world, null);
        }

        public EntityShulkerNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        protected SoundEffect bR() {
            return npc == null || !npc.data().has(NPC.HURT_SOUND_METADATA) ? super.bR()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.HURT_SOUND_METADATA, SoundEffect.a.b(super.bR()).toString())));
        }

        @Override
        protected SoundEffect bS() {
            return npc == null || !npc.data().has(NPC.DEATH_SOUND_METADATA) ? super.bS()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.DEATH_SOUND_METADATA, SoundEffect.a.b(super.bR()).toString())));
        }

        @Override
        public void collide(net.minecraft.server.v1_9_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            }
        }

        @Override
        public void enderTeleportTo(double d0, double d1, double d2) {
            if (npc == null)
                super.enderTeleportTo(d0, d1, d2);
            NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                super.enderTeleportTo(d0, d1, d2);
            }
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public void g(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.g(f, f1);
            } else {
                NMS.flyingMoveLogic(this, f, f1);
            }
        }

        @Override
        protected SoundEffect G() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.G()
                    : SoundEffect.a.get(new MinecraftKey(
                            npc.data().get(NPC.AMBIENT_SOUND_METADATA, SoundEffect.a.b(super.G()).toString())));
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new ShulkerNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isLeashed() {
            if (npc == null)
                return super.isLeashed();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.isLeashed();
            if (super.isLeashed()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        protected void L() {
            if (npc == null) {
                super.L();
            }
        }

        @Override
        public void m() {
            if (npc != null) {
                npc.update();
            } else {
                super.m();
            }
        }

        @Override
        public void n() {
            if (npc == null) {
                super.n();
            }
        }

        @Override
        public boolean n_() {
            if (npc == null || !npc.isFlyable()) {
                return super.n_();
            } else {
                return false;
            }
        }

        @Override
        protected EntityAIBodyControl s() {
            return new EntityAIBodyControl(this);
        }

        @Override
        public void setSize(float f, float f1) {
            if (npc == null) {
                super.setSize(f, f1);
            } else {
                NMS.setSize(this, f, f1, justCreated);
            }
        }
    }

    public static class ShulkerNPC extends CraftShulker implements NPCHolder {
        private final CitizensNPC npc;

        public ShulkerNPC(EntityShulkerNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}