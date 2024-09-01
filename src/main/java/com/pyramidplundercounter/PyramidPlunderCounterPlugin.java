package com.pyramidplundercounter;

import com.google.gson.Gson;
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
	// Drop Chances - https://secure.runescape.com/m=news/poll-80-toa-changes--dmm-tweaks?oldschool=1
	// Room 1 = 1/4200
	// Room 2 = 1/2800
	// Room 3 = 1/1600
	// Room 4 = 1/950
	// Room 5 = 1/800
	// Room 6 = 1/750
	// Room 7 = 1/650
	// Room 8 = 1/650
	private HashMap<Integer, Double> sceptreChance = new HashMap<>();
	private HashMap<Integer, Integer> petBaseChance = new HashMap<>();
	private static final int PYRAMID_PLUNDER_REGION = 7749;
	static final String GRAND_GOLD_CHEST_TARGET = "<col=ffff>Grand Gold Chest";
	static final String SARCOPHAGUS_TARGET = "<col=ffff>Sarcophagus";
	static final String SPEAR_TRAP = "<col=ffff>Speartrap";
	int chestLooted = 0, sarcoLooted = 0;
	double totalChance = 1;
	double dryChance = 0;
	double totalPetChance = 1;
	double petDryChance = 0;

	boolean usingChestOrSarco = false;
	boolean usingSpearTrap = false;
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
		sceptreChance.put(1, 1.0/4200);
		sceptreChance.put(2, 1.0/2800);
		sceptreChance.put(3, 1.0/1600);
		sceptreChance.put(4, 1.0/950);
		sceptreChance.put(5, 1.0/800);
		sceptreChance.put(6, 1.0/750);
		sceptreChance.put(7, 1.0/650);
		sceptreChance.put(8, 1.0/650);

		petBaseChance.put(1, 41355);
		petBaseChance.put(2, 29540);
		petBaseChance.put(3, 25847);
		petBaseChance.put(4, 20678);
		petBaseChance.put(5, 20678);
		petBaseChance.put(6, 20678);
		petBaseChance.put(7, 10339);
		petBaseChance.put(8, 6893);
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
		if (!savedOutside) {
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
					chestLooted += 1;
					Double chance = sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM));
					totalChance *= (1-chance);
					dryChance = 1-totalChance;
					int baseChanceModifier = client.getRealSkillLevel(Skill.THIEVING) * 25;
					int realPetChance = petBaseChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM)) - baseChanceModifier;
					double petChance = 1.0D / realPetChance;
					totalPetChance *= (1-petChance);
					petDryChance = 1-totalPetChance;

					usingChestOrSarco = false;
					savedOutside = false;
				}
			}
			else if (usingChestOrSarco && statChanged.getSkill() == Skill.STRENGTH) {
				sarcoLooted += 1;
				Double chance = sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM));
				totalChance *= (1-chance);
				dryChance = 1-totalChance;
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
			// GRAND CHEST looting was unsuccessful if a scarab swarm spawns and targets you. You still get a chance at the sceptre
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
			if (swarmSpawned && spawnedNPC.contains(interactingChanged.getSource()) && interactingChanged.getTarget().equals(client.getLocalPlayer())) {
				swarmSpawned = false;
				chestLooted += 1;
				Double chance = sceptreChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM));
				totalChance *= (1-chance);
				dryChance = 1-totalChance;
				int baseChanceModifier = client.getRealSkillLevel(Skill.THIEVING) * 25;
				int realPetChance = petBaseChance.get(client.getVarbitValue(Varbits.PYRAMID_PLUNDER_ROOM)) - baseChanceModifier;
				double petChance = 1.0D / realPetChance;
				totalPetChance *= (1-petChance);
				petDryChance = 1-totalPetChance;
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
			chestLooted, sarcoLooted, totalChance, totalPetChance
		);
		try (Writer writer = new FileWriter(file)) {
            GSON.toJson(data, PyramidPlunderCounterData.class, writer);
		} catch (IOException e) {
			log.error("Error while exporting Pyramid Plunder Counter data", e);
		}
	}

	private void importData() {
		if (!config.saveData()) return;

        DATA_FOLDER.mkdirs();
        File data = new File(DATA_FOLDER, client.getLocalPlayer().getName() + ".json");

		if (!data.exists()) {
			try (Writer writer = new FileWriter(data)) {
				GSON.toJson(new PyramidPlunderCounterData(), PyramidPlunderCounterData.class, writer);
			} catch (IOException e) {
				log.warn("Error while initializing Pyramid Plunder Counter data file", e);
			}
			return;
		}

		try (Reader reader = new FileReader(data)) {
			PyramidPlunderCounterData importedData = GSON.fromJson(reader, PyramidPlunderCounterData.class);
			chestLooted = importedData.getChestsLooted();
			sarcoLooted = importedData.getSarcoLooted();
			totalChance = importedData.getChanceOfBeingDry();
			totalPetChance = importedData.getPetChanceOfBeingDry();
			dryChance = 1 - totalChance;
			petDryChance = 1 - totalPetChance;
        } catch (IOException e) {
			log.warn("Error while importing Pyramid Plunder Counter data", e);
		}
	}
}
