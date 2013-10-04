package com.github.CubieX.Towerz;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

public class TSchedulerHandler
{
   private Towerz plugin = null;
   public BukkitTask attackTimer = null;
   ArrayList<Player> nearbyPlayers = new ArrayList<Player>();
   List<Entity> worldEntList = null;

   public TSchedulerHandler(Towerz plugin)
   {
      this.plugin = plugin;
   }

   public void startTowerAttackTimer()
   {
      attackTimer = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         public void run()
         {
            if(!plugin.getCannons().isEmpty())
            {
               World w = Bukkit.getServer().getWorld(Towerz.activeWorld);

               if(null != w)
               {
                  worldEntList = w.getEntities();

                  for(Location cannonLoc : plugin.getCannons().keySet())
                  {
                     nearbyPlayers = getNearbyPlayers(cannonLoc);

                     if(!nearbyPlayers.isEmpty())
                     {
                        for(Entity ent : nearbyPlayers)
                        {
                           if(ent instanceof Player)
                           {
                              // SpawnArrow: origin, vector (= target.subtract(origin)), speed, spread)
                              // aim for players head
                              Arrow arrow = w.spawnArrow(cannonLoc, new Location(w, ent.getLocation().getX(), ent.getLocation().getY() + Towerz.targetElevationOffset + 1, ent.getLocation().getZ()).toVector().subtract(cannonLoc.toVector()), Towerz.projectileSpeed, Towerz.projectileSpread);
                              arrow.setMetadata("exploding", new FixedMetadataValue(plugin, true)); // used to mark the arrow as exploding (or whatever needed)
                              // this can be evaluated in the EntityDamageByEntityEvent to trigger certain actions depending on the projectiles properties
                              break; // only shoot one player per attack cycle
                           }
                        }
                     }
                  }
               }
            }
            else
            {
               if(Towerz.debug){Towerz.log.info(Towerz.logPrefix + "Canceled attackTimer.");}
               stopAttackTimer(); // no cannons registered, so stop timer task
            }
         }
      }, 1 * 20L, Towerz.ATTACK_RATE * 20L); // 1 second delay, 1 second cycle
   }

   // ################################################################################

   public BukkitTask getAttackTimer()
   {
      return attackTimer;
   }

   public void stopAttackTimer()
   {
      attackTimer.cancel();
   }

   private ArrayList<Player> getNearbyPlayers(Location origin)
   {
      if(null != worldEntList)
      {
         nearbyPlayers.clear();
         
         for(Entity ent : worldEntList)
         {
            if(ent instanceof Player)
            {
               if(origin.distance(ent.getLocation()) <= Towerz.attackRange)
               {
                  nearbyPlayers.add((Player)ent);
               }
            }
         }
      }     

      return nearbyPlayers;
   }
}
