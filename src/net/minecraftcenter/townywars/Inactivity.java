package net.minecraftcenter.townywars;

import java.util.ArrayList;

import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;

public class DailyInactivityTask {

	public void run() {
		
		getLogger().info("Twar Inactivity Penalty started");

		collectData(getResidentsInWars())
		processData()
		
		getLogger().info("Twar Inactivity Penalty ended");
		
		
	}
	
	//Returns an ArrayList of all residents whose nation is currently at war
	//Does so by getting complete list of residents from towny
	//Then filters for those whose nation is currently at war
	public ArrayList<Resident> getResidentsInWars() {
		
		ResidentsInWars = new ArrayList<Resident>();
		
		for (Resident resident : TownyUniverse.getDataSource().getResidents()) {

			Nation residentNation = resident.getTown().getNation();

			if (getWarForNation(Nation residentNation) {

				ResidentInWars.add(resident);

			}
		

		}
		
		return (ResidentsInWars);
	}

	//Retrieves required statistics (e.g. playtime) from the stats db
	//Only does so for provided ArrayList of residents
	public void collectData(ArrayList<Resident> Residents) {
		//Connect to mysql

		for (Resident resident : Residents) {
			
			//do something
		
		}
		
		//disconnect from mysql
		
	}
	
	public void processData() {
		//Calculations, dp deduction
	}
}
