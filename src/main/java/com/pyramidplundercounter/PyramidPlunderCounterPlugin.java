package com.pyramidplundercounter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Pyramid Plunder Counter"
)
public class PyramidPlunderCounterPlugin extends Plugin
{
	// Drop Chances
	// Room 1 = 1/3500 0.000286
	// Room 2 = 1/2250 0.000444
	// Room 3 = 1/1250 0.0008
	// Room 4 = 1/750 0.001333
	// Room 5-8 = 1/650 0.001538
	private HashMap<Integer, Double> sceptreChance = new HashMap<>();
	private static final int PYRAMID_PLUNDER_REGION = 7749;
	static final String GRAND_GOLD_CHEST_TARGET = "<col=ffff>Grand Gold Chest";
	static final String SARCOPHAGUS_TARGET = "<col=ffff>Sarcophagus";
	static final String SPEAR_TRAP = "<col=ffff>Speartrap";
	int chestLooted = 0, sarcoLooted = 0, totalChestLooted = 0, totalSarcoLooted = 0;
	int sarcoTimer = -1;
	double totalChance = 1;
	double dryChance = 0;

	boolean usingChestOrSarco = false;
	boolean usingSpearTrap = false;
	boolean hasZombieSpawned = false;
	boolean swarmSpawned = false;

	List<NPC> spawnedNPC = new ArrayList<NPC>();

	@Inject
	private Client client;

	@Inject
	private PyramidPlunderCounterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PyramidPlunderCounterOverlay overlay;

	@Inject
	private Gson GSON;
	public static File DATA_FOLDER;
	static {
		DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, "pyramid-plunder-counter");
		DATA_FOLDER.mkdirs();
	}
	boolean savedOutside = false;
	boolean loadedSession = false;

	@Override
	protected void startUp() throws Exception
	{
		if (client.getGameState().equals(GameState.LOGGED_IN)
			&& client.getLocalPlayer().getName() != null) {
			importData();
			loadedSession = true;
		}

		overlayManager.add(overlay);
		sceptreChance.put(1, 1.0/3500);
		sceptreChance.put(2, 1.0/2250);
		sceptreChance.put(3, 1.0/1250);
		sceptreChance.put(4, 1.0/750);
		sceptreChance.put(5, 1.0/650);
		sceptreChance.put(6, 1.0/650);
		sceptreChance.put(7, 1.0/650);
		sceptreChance.put(8, 1.0/650);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		String username = client.getLocalPlayer().getName();
		if (username != null) exportData(new File(DATA_FOLDER, username + ".json"));
	}

	@Provides
	PyramidPlunderCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PyramidPlunderCounterConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (!loadedSession && client.getGameState().equals(GameState.LOGGED_IN)) {
			importData();
			loadedSession = true;
		}
		if (isInPyramidPlunder()) {
			if (sarcoTimer > 0) {
				sarcoTimer -= 1;
			} else if (sarcoTimer == 0 && !hasZombieSpawned) {
				sarcoLooted += 1;
				Double chance = sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM));
				totalChance *= (1-chance);
				dryChance = 1-totalChance;
				sarcoTimer = -1;
				spawnedNPC.clear();
				savedOutside = false;
			}
		} else if (!savedOutside) {
			String username = client.getLocalPlayer().getName();
			if (username != null) {
				exportData(new File(DATA_FOLDER, username + ".json"));
				savedOutside = true;
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (isInPyramidPlunder()) {
			if (statChanged.getSkill() == Skill.THIEVING) {
				if (usingSpearTrap) {
					usingSpearTrap = false;
				} else if (usingChestOrSarco) {
					//Increment Chest count as you dont gain exp on unnsuccessful thieve.
					totalChestLooted += 1;
					chestLooted += 1;
					Double chance = sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM));
					totalChance *= (1-chance);
					dryChance = 1-totalChance;
					usingChestOrSarco = false;
					savedOutside = false;
				}
			}
			else if (usingChestOrSarco && statChanged.getSkill() == Skill.STRENGTH) {
				//You gain exp on unsuccessful sarcophagus looting, hence only way to check if
				//you were un(successful) is to see if a mummy spawned and it's targeting you
				totalSarcoLooted += 1;
				sarcoTimer = 7;
				hasZombieSpawned = false;
				usingChestOrSarco = false;
				savedOutside = false;
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (isInPyramidPlunder()) {
			if (menuOptionClicked.getMenuTarget().equals(SPEAR_TRAP)) {
				usingSpearTrap = true;
			}
			else if (menuOptionClicked.getMenuTarget().equals(GRAND_GOLD_CHEST_TARGET) || menuOptionClicked.getMenuTarget().equals(SARCOPHAGUS_TARGET)) {
				usingChestOrSarco = true;
			} else if (!menuOptionClicked.getMenuAction().toString().equals("CC_OP")) {
				usingChestOrSarco = false;
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (isInPyramidPlunder()) {
			if (sarcoTimer > 0 && npcSpawned.getNpc().getName().equals("Mummy")) spawnedNPC.add(npcSpawned.getNpc());
			// GRAND CHEST looting was unsuccessful if a scarab swarm spawns and targets you.
			if (usingChestOrSarco && npcSpawned.getNpc().getName().equals("Scarab Swarm")) {
				spawnedNPC.add(npcSpawned.getNpc());
				swarmSpawned = true;
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged interactingChanged)
	{
		if (isInPyramidPlunder()) {
			if (sarcoTimer > 0 && spawnedNPC.contains(interactingChanged.getSource()) && interactingChanged.getTarget().equals(client.getLocalPlayer())) {
				hasZombieSpawned = true;
				spawnedNPC.clear();
			}
			if (swarmSpawned && spawnedNPC.contains(interactingChanged.getSource()) && interactingChanged.getTarget().equals(client.getLocalPlayer())) {
				swarmSpawned = false;
				totalChestLooted += 1;
				spawnedNPC.clear();
			}
		}
	}

	public boolean isInPyramidPlunder()
	{
		return client.getLocalPlayer() != null
				&& PYRAMID_PLUNDER_REGION == client.getLocalPlayer().getWorldLocation().getRegionID()
				&& client.getVarbitValue(Varbits.PYRAMID_PLUNDER_TIMER) > 0;
	}

	private void exportData(File file) {
		if (!config.saveData()) return;

		PyramidPlunderCounterData data = new PyramidPlunderCounterData(
			totalChestLooted, totalSarcoLooted, chestLooted, sarcoLooted, totalChance
		);
		try {
			Writer writer = new FileWriter(file);
            GSON.toJson(data, PyramidPlunderCounterData.class, writer);
            writer.flush();
            writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while exporting Pyramid Plunder Counter data: " + e.getMessage());
		}
	}

	private void importData() {
		if (!config.saveData()) return;

        DATA_FOLDER.mkdirs();
        File data = new File(DATA_FOLDER, client.getLocalPlayer().getName() + ".json");

		try {
            if (!data.exists()) {
                Writer writer = new FileWriter(data);
                GSON.toJson(new PyramidPlunderCounterData(), PyramidPlunderCounterData.class, writer);
                writer.flush();
                writer.close();
            } else {
                PyramidPlunderCounterData importedData = GSON.fromJson(new FileReader(data), PyramidPlunderCounterData.class);
                totalChestLooted = importedData.getTotalChests();
                totalSarcoLooted = importedData.getTotalsarcophagi();
                chestLooted = importedData.getSuccessfulChests();
                sarcoLooted = importedData.getSuccessfulsarcophagi();
                totalChance = importedData.getChanceOfBeingDry();
                dryChance = 1 - totalChance;
            }
        } catch (IOException e) {
			e.printStackTrace();
			log.warn("Error while importing Pyramid Plunder Counter data: " + e.getMessage());
		}
	}
}
