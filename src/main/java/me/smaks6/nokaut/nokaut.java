package me.smaks6.nokaut;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.npc.HandType;
import static me.smaks6.nokaut.Main.gracze;

public class nokaut implements Listener{

	@EventHandler
    public void death(EntityDamageEvent event){
        if (event.getEntity() instanceof Player){
            Player p = (Player) event.getEntity();
            int hp = (int) p.getHealth();
            int dm = (int) event.getDamage();
            String hashmap = gracze.get(p.getName());
        	if(hp <= dm) {
                if(hashmap.equals("stoi")) {
                	gracze.replace(p.getName(), "chwila");
                	event.setCancelled(true);
                    PosePluginPlayer posePluginPlayer = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(p);
                    IPluginPose pose = PoseBuilder.builder(EnumPose.LYING).option(EnumPoseOption.HANDTYPE, HandType.LEFT).build(p);
                    posePluginPlayer.changePose(pose);
    				if(p.getHealth() <= 10.0) {
    					p.setHealth(10.0);
    				}
    				p.sendMessage(ChatColor.RED + Main.getInstance().getConfig().getString("helpnokautmessage"));
    				odliczanie(p);
                }else if(!hashmap.equals("stoi")) {
                	event.setCancelled(false);
                }else {
                	p.kickPlayer("[nokaut] An unexpected error has occurred in the program");
                }
				
        	}
        }
           
    }
	public void odliczanie(Player p){
		new BukkitRunnable() {
			
    		int czass = 0;
    		int czasm = Main.getInstance().getConfig().getInt("NokautTimeInMin");
    		String razem;
    		
			@Override
	        public void run() {
				
	    		String hashmap = gracze.get(p.getName());
				if(hashmap.equals("stoi")) {
					this.cancel();
				}
				
				if(hashmap.equals("chwila") && czass == 55) {
					gracze.replace(p.getName(), "lezy");
				}

				
    			if((czass <= 0) && (czasm >= 1)) {
    				--czasm;
    				czass = 60;
    			}
    			
    			if((czasm <= 0) && (czass <= 0)) {
    				gracze.replace(p.getName(), "stoi");
    				p.setHealth(0);
    				this.cancel();
    			}
    			
    			if(czass <= 9) {
        			razem = czasm + ":0" + czass;
    			}else {
        			razem = czasm + ":" + czass;
    			}

    			
    			p.sendTitle(ChatColor.RED + Main.getInstance().getConfig().getString("NokautTitle"),ChatColor.RED + razem, 1 , 20 , 1 );
    			
    			--czass;
	        }
	    }.runTaskTimer(Main.getInstance(), 0L, 20L);
	}
	
}
