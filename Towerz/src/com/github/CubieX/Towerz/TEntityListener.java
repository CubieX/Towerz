package com.github.CubieX.Towerz;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.material.Sign;
import org.bukkit.metadata.MetadataValue;

public class TEntityListener implements Listener
{
   private Towerz plugin = null;
   private TSchedulerHandler schedHandler = null;

   public TEntityListener(Towerz plugin, TSchedulerHandler schedHandler)
   {        
      this.plugin = plugin;
      this.schedHandler = schedHandler;

      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onSignCreation(SignChangeEvent e)
   {
      if(e.getBlock().getType() == Material.WALL_SIGN) // only wall signs are valid
      {
         Sign s = (Sign) e.getBlock().getState().getData();

         if(e.getBlock().getRelative(s.getAttachedFace()).getType() == Material.DIAMOND_BLOCK) // get block the sign is attached to
         {
            if(!plugin.getCannons().containsKey(e.getBlock().getLocation()))
            {
               plugin.getCannons().put(e.getBlock().getLocation(), e.getPlayer().getName());

               if(null == plugin.getAttackTimer()) // initial creation of attackTimer task
               {
                  schedHandler.startTowerAttackTimer(); // only ONE attackTimer may exist at a time!
               }
               else
               {
                  // attackTimer task is already created, but currently not scheduled
                  if((!Bukkit.getServer().getScheduler().isCurrentlyRunning(plugin.getAttackTimer().getTaskId()))
                        && (!Bukkit.getServer().getScheduler().isQueued(plugin.getAttackTimer().getTaskId())))
                  {
                     schedHandler.startTowerAttackTimer(); // only ONE attackTimer may exist at a time!
                  }
               }

               e.getPlayer().sendMessage("§aAbwehrturm erstellt!");
            }
         }
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onBlockBreak(BlockBreakEvent e)
   {
      if((plugin.getCannons().containsKey(e.getBlock().getLocation()))) // cannon sign was broken
      {
         plugin.getCannons().remove(e.getBlock().getLocation());
         e.getPlayer().sendMessage("§aAbwehrturm geloescht!");
      }
      else
      {
         if(e.getBlock().getType() == Material.DIAMOND_BLOCK)
         {
            if(e.getBlock().getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN) // check if broken block has a cannon sign attached
            {
               deleteCannon(e.getPlayer(), e.getBlock().getRelative(BlockFace.NORTH).getLocation());
            }

            if(e.getBlock().getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN)
            {
               deleteCannon(e.getPlayer(), e.getBlock().getRelative(BlockFace.EAST).getLocation());
            }

            if(e.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN)
            {
               deleteCannon(e.getPlayer(), e.getBlock().getRelative(BlockFace.SOUTH).getLocation());
            }

            if(e.getBlock().getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN)
            {
               deleteCannon(e.getPlayer(), e.getBlock().getRelative(BlockFace.WEST).getLocation());
            }
         }
      }
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
   {
      if(e.getEntity() instanceof Player)
      {
         Player victim = (Player)e.getEntity();
         // used to customize the suffered damage from a tower
         if(e.getDamager() instanceof Arrow)
         {
            Arrow arrow = (Arrow)e.getDamager();

            if(null == arrow.getShooter()) // null when arrow was shot by plugin
            {
               if(arrow.hasMetadata("exploding")) // check for custom meta data set when the projectile was spawned or launched
               {
                  List<MetadataValue> mdValuesExploding = arrow.getMetadata("exploding"); // gets a list of all values with this key name that plugins set on this entity
                  // more than one plugin may define the same value, because meta data store is global!
                  // it's possible to exchange data between plugins using MetaData
                  // so make sure to get the value of this plugin!

                  for(MetadataValue mdVal : mdValuesExploding)
                  {
                     if(mdVal.getOwningPlugin() == plugin)
                     {
                        if(true == mdVal.asBoolean())
                        {
                           // add damage because arrow is an exploding one
                           e.setDamage(e.getDamage() * 2);

                           if(Towerz.debug){victim.sendMessage(ChatColor.GOLD + "Hit by Exploding Arrow! Suffered: " + ChatColor.WHITE + (e.getDamage() * 100 / 100) + ChatColor.GOLD + " damage.");}
                        }
                        
                        break;
                     }
                  }
               }
            }
         }
      }      
   }

   //================================================================================================
   /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onProjectileHit(ProjectileHitEvent e)
   {      
      if (e.getEntity() instanceof Arrow)
      {
         LivingEntity livEnt = e.getEntity().getShooter(); // will be "null" if projectile was fired by plugin

         if(null == livEnt)
         {
            if(e.getEntity().hasMetadata("exploding")) // check for custom meta data set when the projectile was spawned or launched
            {
               List<MetadataValue> mdValuesExploding = e.getEntity().getMetadata("exploding"); // gets a list of all values with this key name that plugins set on this entity
               // more than one plugin may define the same value, because meta data store is global!
               // it's possible to exchange data between plugins using MetaData
               // so make sure to get the value of this plugin!

               for(MetadataValue mdVal : mdValuesExploding)
               {
                  if(mdVal.getOwningPlugin() == plugin)
                  {
                     if(true == mdVal.asBoolean())
                     {
                        // add explosion effect, because arrow is an explosing one
                        // power of 0x4F = 79 is equivalent to TNT -> does only affect block damage? -> seems like 79 is HUGE explosion. Much more than TNT...
                        e.getEntity().getWorld().createExplosion(e.getEntity().getLocation().getX(), e.getEntity().getLocation().getY(), e.getEntity().getLocation().getZ(), 4.0f, false, false);
                     }
                     break;
                  }
               }
            }              
         }
      }
   }*/

   // ######################################################################################################

   void deleteCannon(Player p, Location loc)
   {
      // sign will also be broken in the process, so delete the location of this cannon from HashMap
      if(plugin.getCannons().containsKey(loc))
      {
         plugin.getCannons().remove(loc);
         p.sendMessage("§aAbwehrturm geloescht!");
      }
   }
}
