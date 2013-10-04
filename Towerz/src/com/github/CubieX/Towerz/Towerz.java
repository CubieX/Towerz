/*
 * Towerz - A CraftBukkit plugin that allows to build defence towers
 * Copyright (C) 2013  CubieX
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.github.CubieX.Towerz;

import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Towerz extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   public static final String logPrefix = "[Towerz] "; // Prefix to go in front of all log entries

   private TCommandHandler comHandler = null;
   private TConfigHandler cHandler = null;
   private TEntityListener eListener = null;
   private TSchedulerHandler schedHandler = null;
   
   private HashMap<Location, String> cannons = new HashMap<Location, String>();
 
   public static final int MIN_ATTACK_RANGE = 5;
   public static final int MAX_ATTACK_RANGE = 25;
   public static final int ATTACK_RATE = 2; // attacks per second for defence towers
   
   // config values
   static boolean debug = false;
   static String activeWorld = "world";
   static int attackRange = 10;
   static float projectileSpeed = attackRange / 6; // how fast a projectile travels: recommneded: 0.6 for short distance 
   static float projectileSpread = 12.0f; // how much does a projectile spread from its optimal target location: recommended: 12
   static int targetElevationOffset = 1; // offset for targeting in z direction (projectile will aim for this amount of blocks above players head)

   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {
      cHandler = new TConfigHandler(this);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }

      readConfigValues();

      schedHandler = new TSchedulerHandler(this);
      eListener = new TEntityListener(this, schedHandler);     
      comHandler = new TCommandHandler(this, cHandler);      
      getCommand("tower").setExecutor(comHandler);
      
      log.info(logPrefix + "version " + getDescription().getVersion() + " is enabled!");      
   }

   private boolean checkConfigFileVersion()
   {      
      boolean configOK = false;     

      if(cHandler.getConfig().isSet("config_version"))
      {
         String configVersion = getConfig().getString("config_version");

         if(configVersion.equals(usedConfigVersion))
         {
            configOK = true;
         }
      }

      return (configOK);
   }  

   public void readConfigValues()
   {
      boolean exceed = false;
      boolean invalid = false;

      if(getConfig().isSet("debug")){debug = getConfig().getBoolean("debug");}else{invalid = true;}
      if(getConfig().isSet("activeWorld")){activeWorld = getConfig().getString("activeWorld");}else{invalid = true;}
      
      if(getConfig().isSet("attackRange"))
      {
         if(getConfig().getInt("attackRange") < MIN_ATTACK_RANGE)
         {
            attackRange = MIN_ATTACK_RANGE;
            exceed = true;
         }
         else if(getConfig().getInt("attackRange") > MAX_ATTACK_RANGE)
         {
            attackRange = MAX_ATTACK_RANGE;
            exceed = true;
         }
         else
         {
            attackRange = getConfig().getInt("attackRange");
            projectileSpeed = attackRange / 6;
         }
      }
      else
      {
         invalid = true;
      }
           
      if(exceed)
      {
         log.warning(logPrefix + "One or more config values are exceeding their allowed range. Please check your config file!");
      }

      if(invalid)
      {
         log.warning(logPrefix + "One or more config values are invalid. Please check your config file!");
      }
   }

   @Override
   public void onDisable()
   {     
      this.getServer().getScheduler().cancelTasks(this);
      cHandler = null;
      eListener = null;
      comHandler = null;
      schedHandler = null;
      log.info(logPrefix + "version " + getDescription().getVersion() + " is disabled!");
   }

   // ########################################################################################
   
   public HashMap<Location, String> getCannons()
   {
      return cannons; 
   }
   
   public BukkitTask getAttackTimer()
   {
      return schedHandler.getAttackTimer();
   }
}


