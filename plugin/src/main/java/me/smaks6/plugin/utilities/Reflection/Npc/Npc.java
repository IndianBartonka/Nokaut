package me.smaks6.plugin.utilities.Reflection.Npc;

import com.mojang.authlib.GameProfile;
import me.smaks6.plugin.Main;
import me.smaks6.plugin.utilities.ReflectionUtilities.Reflection;
import me.smaks6.plugin.utilities.Runnables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class Npc {
    private final static Class<?> craftPlayerClass = Reflection.getBukkitClass("entity.CraftPlayer");
    private static Class<?> entityPlayerClass = null;
    private static Class<?> worldServerClass = null;
    private static Class<?> playerInteractManagerClass = null;
    private static Class<?> entityPoseClass = null;
    private static Class<?> craftServerClass = Reflection.getBukkitClass("CraftServer");
    private static Class<?> craftWorldClass = Reflection.getBukkitClass("CraftWorld");
    private static Class<?> minecraftServerClass = null;

    private final static Method getHandleMethod = Reflection.getMethod(craftPlayerClass, "getHandle");
//    private final static Method getProfileMethod = Reflection.getMethod(entityPlayerClass, "getProfile");
    private final static Method getServer = Reflection.getMethod(craftServerClass, "getServer");
    private final static Method getHandle = Reflection.getMethod(craftWorldClass, "getHandle");

    private Object entityPlayer;

    private Player knockedPlayer;
    private Player toShowPlayer;
    private SendNPCPacket sendNPCPacket;

    public Npc(Player knockedPlayer, Player toShowPlayer) {

        if(Reflection.isNewPackeges()){
            entityPlayerClass = Reflection.getNMSClass("level.EntityPlayer");
            worldServerClass = Reflection.getNMSClass("level.WorldServer");
            playerInteractManagerClass =  Reflection.getNMSClass("level.PlayerInteractManager");
            entityPoseClass = Reflection.getNMSClassNotServer("world.entity.EntityPose");
        }else {
            entityPlayerClass = Reflection.getNMSClass("EntityPlayer");
            worldServerClass = Reflection.getNMSClass("WorldServer");
            playerInteractManagerClass = Reflection.getNMSClass("PlayerInteractManager");
            entityPoseClass = Reflection.getNMSClass("EntityPose");
        }

        this.knockedPlayer = knockedPlayer;
        this.toShowPlayer = toShowPlayer;


        try {
            Object nmsServer = getNMSServer();

            Object nmsWorld = getNMSWorld();

            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), ChatColor.RED + Main.getInstance().getConfig().getString("NokautTitle"));

//            Object entityKnockedPlayer = getHandleMethod.invoke(knockedPlayer);
//            GameProfile playerGameProfile = (GameProfile) getProfileMethod.invoke(entityKnockedPlayer);
//            gameProfile.getProperties().putAll(playerGameProfile.getProperties());
//            gameProfile.getProperties().putAll(((CraftPlayer) knockedPlayer).getHandle().getProfile().getProperties());

            entityPlayer = getEntityPlayer(nmsServer, nmsWorld, gameProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendNPCPacket = new SendNPCPacket(entityPlayer, toShowPlayer);
        teleportEntity(-0.1);
        sendNPCPacket.sendPackets();

        Runnables.npcTimer(knockedPlayer, this);
    }


    public void teleportEntity(Double teleportHight) {
        try {
            Location loc = knockedPlayer.getLocation();
            Method setLocation = entityPlayerClass.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
            setLocation.invoke(entityPlayer, loc.getX(), loc.getY() + teleportHight, loc.getZ(), 0, 0);
            sendNPCPacket.sendTeleportPacket();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void delateEntity(){
        try {
            sendNPCPacket.sendDelatePacket();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private Object getNMSServer() throws InvocationTargetException, IllegalAccessException {


        Object nmsServer = getServer.invoke(Bukkit.getServer());

        return nmsServer;
    }

    private Object getNMSWorld() throws InvocationTargetException, IllegalAccessException {

        Object nmsWorld = getHandle.invoke(toShowPlayer.getWorld());

        return nmsWorld;
    }

    private Object getEntityPlayer(Object nmsServer, Object nmsWorld, GameProfile gameProfile) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Object entityPlayer;
        Object playerInteractManager = null;
        if(!Reflection.isNewPackeges()) {
            Constructor<?> minecraftServerConstructor = playerInteractManagerClass.getConstructor(worldServerClass);
            playerInteractManager = minecraftServerConstructor.newInstance(nmsWorld);
        }


        if(Reflection.isNewPackeges()){
            Constructor<?> constructor = entityPlayerClass.getConstructor
                    (minecraftServerClass, worldServerClass, GameProfile.class);

            entityPlayer = constructor.newInstance(nmsServer, nmsWorld, gameProfile);

        }else {
            Constructor<?> constructor = entityPlayerClass.getConstructor
                    (minecraftServerClass, worldServerClass, GameProfile.class, playerInteractManagerClass);
            entityPlayer = constructor.newInstance(nmsServer, nmsWorld, gameProfile, playerInteractManager);
        }



        entityPlayer.getClass().getMethod("setPose", entityPoseClass).invoke(entityPlayer, entityPoseClass.getEnumConstants()[3]);

        return entityPlayer;
    }
}
