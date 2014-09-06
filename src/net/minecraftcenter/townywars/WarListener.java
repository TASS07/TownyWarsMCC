package net.minecraftcenter.townywars;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
//import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
//import com.palmergames.bukkit.towny.exceptions.EconomyException;
//import com.palmergames.bukkit.towny.exceptions.EmptyNationException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WarListener
  implements Listener
{
  
private TownyWars mplugin=null;
	
  WarListener(TownyWars aThis) { mplugin=aThis;}
  
  @EventHandler
  public void onNationDelete(DeleteNationEvent event){
	  
	  Nation nation = null;
	  War war = null;
	  
	  for(War w : WarManager.getWars())
		  for(Nation n : w.getNationsInWar())
			  if(n.getName().equals(event.getNationName())){
				  nation = n;
				  war = w;
				  break;
			  }
	  
	  if(war == null){
		  for(Rebellion r : Rebellion.getAllRebellions())
		    	if(r.getMotherNation().getName().equals(event.getNationName()))
		    		Rebellion.getAllRebellions().remove(r);
		  return;
	  }
	  
	  WarManager.getWars().remove(war);
	  
	  if(war.getRebellion() != null){
		  Rebellion.getAllRebellions().remove(war.getRebellion());
		  if(war.getRebellion().getRebelnation() != nation)
			  TownyUniverse.getDataSource().deleteNation(war.getRebellion().getRebelnation());
		  else if(war.getRebellion().getMotherNation() != nation)
			  war.getRebellion().peace();
	  }
	  
	  TownyUniverse.getDataSource().saveNations();
	  try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    Player player = event.getPlayer();
    try
    {
      Resident re = TownyUniverse.getDataSource().getResident(player.getName());
      Nation nation = re.getTown().getNation();
      //Player plr = Bukkit.getPlayer(re.getName());
      
      // add the player to the master list if they don't exist in it yet
      if (mplugin.getTownyWarsResident(re.getName())==null){
    	  mplugin.addTownyWarsResident(re.getName());
    	  System.out.println("resident added!");
      }
      
      War ww = WarManager.getWarForNation(nation);
      if (ww != null)
      {
        player.sendMessage(ChatColor.RED + "Warning: Your nation is at war with " + ww.getEnemy(nation));
        if ((WarManager.hasBeenOffered(ww, nation)) && ((nation.hasAssistant(re)) || (re.isKing()))) {
          player.sendMessage(ChatColor.GREEN + "The other nation has offered peace!");
        }
      }
    }
    catch (Exception ex) {}
  }
  
  @EventHandler
  public void onResidentLeave(TownRemoveResidentEvent event)
  {
    Nation n;
    try
    {
      n = event.getTown().getNation();
    }
    catch (NotRegisteredException ex)
    {
      return;
    }
    War war = WarManager.getWarForNation(n);
    if (war == null) {
      return;
    }
    try {
		if(WarManager.getWarForNation(event.getTown().getNation()).getTownPoints(event.getTown()) > TownyWars.pPlayer)
			war.chargeTownPoints(n, event.getTown(), TownyWars.pPlayer);
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @EventHandler
  public void onResidentAdd(TownAddResidentEvent event)
  {
    Nation n;
    try
    {
      n = event.getTown().getNation();
    }
    catch (NotRegisteredException ex)
    {
      return;
    }
    War war = WarManager.getWarForNation(n);
    if (war == null) {
      return;
    }
    war.chargeTownPoints(n, event.getTown(), -TownyWars.pPlayer);
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @EventHandler
  public void onNationAdd(NationAddTownEvent event)
  {
    War war = WarManager.getWarForNation(event.getNation());
    if (war == null) {
      return;
    }
      war.addNationPoint(event.getNation(), event.getTown());
      war.addNewTown(event.getTown());
    try {
		WarManager.save();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  @EventHandler
  public void onNationRemove(NationRemoveTownEvent event)
  {
	  War war = WarManager.getWarForNation(event.getNation());
      if (war == null) {
        return;
      }
      
      war.removeTown(event.getTown(), event.getNation());
      
      try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	 //MAKE FUCKING WORK when a town is disbanded because of lack of funds
    /*if (event.getTown() != WarManager.townremove)
    {
      War war = WarManager.getWarForNation(event.getNation());
      if (war == null) {
        return;
      }
      townadd = event.getTown();
      try
      {
    	  if(event.getNation().getNumTowns() != 0)
    		  event.getNation().addTown(event.getTown());
      }
      catch (AlreadyRegisteredException ex)
      {
        Logger.getLogger(WarListener.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else{
    	 for(Rebellion r : Rebellion.getAllRebellions())
    	    	if(r.isRebelLeader(event.getTown())){
    	    		Rebellion.getAllRebellions().remove(r);
    	    		break;
    	    	}
    	    	else if(r.isRebelTown(event.getTown())){
    	    		r.removeRebell(event.getTown());
    	    		break;
    	    	}
    }
    
    TownyUniverse.getDataSource().saveNations();
    WarManager.townremove = null;*/
  }
  
@SuppressWarnings("deprecation")
@EventHandler
  public void onPlayerDamage(EntityDamageByEntityEvent event){
	  // get the current system time
	  long hitTime=System.currentTimeMillis();
	  
	  // check if the entity damaged was a player
	  if (event.getEntity() instanceof Player){
		  String attacker=null;
		  
		  // check if the damaging entity was a player
		  if (event.getDamager() instanceof Player) {
			  attacker=((Player)event.getDamager()).getName();
		  }
		  
		  // check if the damaging entity was an arrow shot by a player
		  else if (event.getDamager() instanceof Projectile){
			  if (((Projectile)event.getDamager()).getShooter() instanceof Player){
				  attacker=((Player)((Projectile)event.getDamager()).getShooter()).getName();
			  }
		  }
		  
		  // if neither was true, then no need to update the player's stats
		  if (attacker==null) { return;}
		  
		  String playerName=((Player)event.getEntity()).getName();
		  if (mplugin.getTownyWarsResident(playerName)!=null) {
			  // update the player's stats
			  mplugin.getTownyWarsResident(playerName).setLastHitTime(hitTime);
			  mplugin.getTownyWarsResident(playerName).setLastAttacker(attacker);
		  }
		  
	  }
  }
  
  
  // here we want to differentiate between player deaths due solely to environmental damage
  // and due to environmental damage in combination with player hits

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event)
  {
	  // record the timestamp immediately
	  long deathTime = System.currentTimeMillis();
	  
	  // get the name of the dead resident
	  String playerName = event.getEntity().getName();
	  
	  // get the name of the cause of death
	  DamageCause damageCause=event.getEntity().getLastDamageCause().getCause();
	  
	  String playerKiller=null;
	  
	  // here, the kill was not done by a player, so we need to look up who to credit, if anyone
	  if (event.getEntity().getKiller()==null) {
		  
		  // let's look up who hit them last, and how long ago
		  long lastHitTime=0;
		  String lastAttacker=null;
		  TownyWarsResident cre= mplugin.getTownyWarsResident(playerName);
	
			  if (cre!=null){
				  lastHitTime=cre.getLastHitTime();
				  lastAttacker=cre.getLastAttacker();
				  // reset dead player's stats
				  cre.setLastAttacker(null);
				  cre.setLastHitTime(0);
			  }
	
			  // if the player has been hit by another player within the past 30 seconds, credit the killer
			  if (lastAttacker!=null && deathTime-lastHitTime<30000){
				  playerKiller=lastAttacker;
			  
				  // give the killer credit in chat :-)
				  event.setDeathMessage(event.getDeathMessage()+" to escape "+playerKiller);
			  }
	  }
	  // kill was done by another player
	  else {
		  playerKiller=event.getEntity().getKiller().getName();
	  }
	  
	  // we need to record the kill in all its glory to a log file for moderation use
	  // takes in the time of death in milliseconds, the player that was killed, the killer, the final cause of death, and the death message
	  int status = mplugin.writeKillRecord(deathTime,event.getEntity().getName(),playerKiller,damageCause.name(),event.getDeathMessage());
	  
	  if (status==0){
	  		System.out.println("death recorded!");
	  	}
	  	else {
	  			System.out.println("[ERROR] death recording failed! you should check on this!");
	  		}
	  
	  // if the player actually wasn't killed by another player, we can stop
	  if (playerKiller==null) { return; }

	  // if we've made it this far, it means that the death should affect Towny
	  // now we know who to credit, so let's adjust Towny to match
			  
	  try {
	      Town tdamagerr = TownyUniverse.getDataSource().getResident(playerKiller).getTown();
	      Nation damagerr = tdamagerr.getNation();

	      Town tdamagedd = TownyUniverse.getDataSource().getResident(playerName).getTown();
	      Nation damagedd = tdamagedd.getNation();
	      
	      War war = WarManager.getWarForNation(damagerr);
	      if ((war.hasNation(damagedd)) && (!damagerr.getName().equals(damagedd.getName())))
	      {
	        tdamagedd.pay(TownyWars.pKill, "Death cost");
	        tdamagerr.collect(TownyWars.pKill);
	      }
	      if ((war.hasNation(damagedd)) && (!damagerr.getName().equals(damagedd.getName()))) {
	        try
	        {
	          war.chargeTownPoints(damagedd, tdamagedd, 1);
	          int lP = war.getTownPoints(tdamagedd);
	          if (lP <= 10 && lP != -1 && WarManager.getWars().contains(war)) {
	            event.getEntity().sendMessage(ChatColor.RED + "Be careful! Your town only has a " + lP + " points left!");
	          }
	        }
	        catch (Exception ex)
	        {
	        	event.getEntity().sendMessage(ChatColor.RED + "An error occured, check the console!");
	        	ex.printStackTrace();
	        }
	      }
	    }
	    catch (Exception ex) {}
	    
	    try {
			WarManager.save();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
}
