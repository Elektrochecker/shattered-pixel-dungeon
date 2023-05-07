/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import java.util.ArrayList;

public class Ghostbulb extends Plant {

	{
		image = 17;
		seedClass = Seed.class;
	}

	@Override
	public void activate(final Char ch) {
		spawnGhosts(ch);
	}

	public static class Seed extends Plant.Seed {
		{
			image = ItemSpriteSheet.SEED_GHOSTBULB;

			plantClass = Ghostbulb.class;
		}
	}

	public static void spawnGhosts(Char ch) {
		
		if (ch instanceof Hero) {

			((Hero) ch).curAction = null;

			if (((Hero) ch).subClass == HeroSubClass.WARDEN && Dungeon.interfloorTeleportAllowed()) {
				ScrollOfMirrorImage.spawnImages((Hero) ch, 2);
				Buff.affect(ch, Invisibility.class, Invisibility.DURATION/2);
			} else {
				ScrollOfMirrorImage.spawnImages((Hero) ch, 1);
			}

		} else if (ch instanceof Mob) {
			ArrayList<Integer> spawnPoints = new ArrayList<>();

			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				int p = ch.pos + PathFinder.NEIGHBOURS8[i];
				if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
					spawnPoints.add(p);
				}
			}

			if (spawnPoints.size() > 0) {

				Mob m = null;
				if (!(ch instanceof Mob)
						|| ch.properties().contains(Char.Property.BOSS)
						|| ch.properties().contains(Char.Property.MINIBOSS)
						|| ch instanceof Mimic || ch instanceof Statue || ch instanceof NPC) {
					m = Dungeon.level.createMob();
				} else {
					Actor.fixTime();
					m = (Mob) Reflection.newInstance(ch.getClass());
					if (m != null) {
						Bundle store = new Bundle();
						ch.storeInBundle(store);
						m.restoreFromBundle(store);
						m.pos = 0;
						m.HP = m.HT;
						if (m.buff(PinCushion.class) != null) {
							m.remove(m.buff(PinCushion.class));
						}

						// If a thief has stolen an item, that item is not duplicated.
						if (m instanceof Thief) {
							((Thief) m).item = null;
						}
					}
				}

				if (m != null) {

					if (Char.hasProp(m, Char.Property.LARGE)) {
						for (int i : spawnPoints.toArray(new Integer[0])) {
							if (!Dungeon.level.openSpace[i]) {
								// remove the value, not at the index
								spawnPoints.remove((Integer) i);
							}
						}
					}

					if (!spawnPoints.isEmpty()) {
						m.pos = Random.element(spawnPoints);
						GameScene.add(m);
						ScrollOfTeleportation.appear(m, m.pos);
					}
				}

			}
		}

		if (Dungeon.level.heroFOV[ch.pos])

		{
			CellEmitter.get(ch.pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
		}
	}
}
