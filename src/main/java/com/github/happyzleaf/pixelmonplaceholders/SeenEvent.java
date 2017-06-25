package com.github.happyzleaf.pixelmonplaceholders;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.pokedex.EnumPokedexRegisterStatus;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Optional;

/***************************************
 * PixelmonPlaceholders
 * Created on 03/06/2017.
 * @author Vincenzo Montanari
 *
 * Copyright (c). All rights reserved.
 ***************************************/
public class SeenEvent {
	@SubscribeEvent
	public void onBattleStarted(BattleStartedEvent event) {
		EntityPlayerMP player;
		ArrayList<EntityPixelmon> pixelmon = Lists.newArrayList();
		if (event.participant1[0] instanceof PlayerParticipant) {
			player = ((PlayerParticipant) event.participant1[0]).player;
			for (BattleParticipant participant : event.participant2) {
				if (participant.getEntity() instanceof EntityPixelmon) {
					pixelmon.add((EntityPixelmon) participant.getEntity());
				}
			}
			Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorage(player);
			if (optStorage.isPresent()) {
				PlayerStorage storage = optStorage.get();
				for (EntityPixelmon pokemon : pixelmon) {
					int id = pokemon.baseStats.nationalPokedexNumber;
					if (!storage.pokedex.hasSeen(id)) {
						storage.pokedex.set(id, EnumPokedexRegisterStatus.seen);
					}
				}
			}
		}
		if (event.participant2[0] instanceof PlayerParticipant) {
			player = ((PlayerParticipant) event.participant2[0]).player;
			for (BattleParticipant participant : event.participant1) {
				if (participant.getEntity() instanceof EntityPixelmon) {
					pixelmon.add((EntityPixelmon) participant.getEntity());
				}
			}
			Optional<PlayerStorage> optStorage = PixelmonStorage.pokeBallManager.getPlayerStorage(player);
			if (optStorage.isPresent()) {
				PlayerStorage storage = optStorage.get();
				for (EntityPixelmon pokemon : pixelmon) {
					int id = pokemon.baseStats.nationalPokedexNumber;
					if (!storage.pokedex.hasSeen(id)) {
						storage.pokedex.set(id, EnumPokedexRegisterStatus.seen);
					}
				}
			}
		}
	}
}
