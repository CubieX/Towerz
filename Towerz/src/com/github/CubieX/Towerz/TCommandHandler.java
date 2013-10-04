package com.github.CubieX.Towerz;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TCommandHandler implements CommandExecutor
{
   private Towerz plugin = null;
   private TConfigHandler cHandler = null;

   public TCommandHandler(Towerz plugin, TConfigHandler cHandler) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {  
      Player player = null;

      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      if (cmd.getName().equalsIgnoreCase("tower"))
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }
         else if(args.length==1)
         {
            if (args[0].equalsIgnoreCase("version"))
            {               
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " version " + plugin.getDescription().getVersion());
               return true;
            }

            if (args[0].equalsIgnoreCase("reload"))
            {
               if(sender.isOp() || sender.hasPermission("towerz.admin"))
               {                        
                  cHandler.reloadConfig(sender);                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
               }
               
               return true;
            }            
         }
         else
         {
            sender.sendMessage(ChatColor.YELLOW + "Falsche Parameteranzahl.");
         }         
      }
      return false;
   }
   // ##########################################################################


}
