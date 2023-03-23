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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

//debuff effects
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;

//battlemage exclusive effects
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AdrenalineSurge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
//cleansing
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;

import java.util.ArrayList;

public class WandOfChaos extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_CHAOS;

		collisionProperties = Ballistica.WONT_STOP;
	}

	public int min(int lvl) {
		return 2 + lvl;
	}

	public int max(int lvl) {
		return 4 + 3 * lvl;
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return dst;
	}

	@Override
	public void onZap(Ballistica beam) {

		boolean terrainAffected = false;

		int level = buffedLvl();

		int maxDistance = Math.min(distance(), beam.dist);

		ArrayList<Char> chars = new ArrayList<>();

		Blob web = Dungeon.level.blobs.get(Web.class);

		int terrainPassed = 2, terrainBonus = 0;
		for (int c : beam.subPath(1, maxDistance)) {

			Char ch;
			if ((ch = Actor.findChar(c)) != null) {

				// we don't want to count passed terrain after the last enemy hit. That would be
				// a lot of bonus levels.
				// terrainPassed starts at 2, equivalent of rounding up when /3 for integer
				// arithmetic.
				terrainBonus += terrainPassed / 3;
				terrainPassed = terrainPassed % 3;

				if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).PASSIVE
						&& !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
					// avoid harming undiscovered passive chars
				} else {
					chars.add(ch);
				}
			}

			CellEmitter.center(c).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
		}

		if (terrainAffected) {
			Dungeon.observe();
		}

		int lvl = level + (chars.size() - 1) + terrainBonus;
		for (Char ch : chars) {
			wandProc(ch, chargesPerCast());
			ch.damage(damageRoll(lvl), this);
			ch.sprite.centerEmitter().burst(PurpleParticle.BURST, Random.IntRange(1, 2));
			ch.sprite.flash();

			// apply frost
			if (ch.isAlive()) {
				switch (Random.Int(9)) {
					default:
					case 0: // frost, stronger in water
						if (Dungeon.level.water[ch.pos])
							Buff.affect(ch, Chill.class, 2 + buffedLvl());
						else
							Buff.affect(ch, Chill.class, 1 + buffedLvl() / 2);
						break;
					case 1: // blind
						Buff.affect(ch, Blindness.class, 1 + buffedLvl());
						break;
					case 2:
						Buff.affect(ch, Cripple.class, 1 + buffedLvl());
						break;
					case 3:
						Buff.affect(ch, Amok.class, 1 + buffedLvl() / 2);
						break;
					case 4:
						Buff.affect(ch, Vertigo.class, 1 + buffedLvl());
						break;
					case 5:
						Buff.affect(ch, Bleeding.class).set(buffedLvl(), getClass());
						break;
					case 6:
						Buff.affect(ch, Burning.class).reignite(ch);
						break;
					case 7:
						Buff.affect(ch, Hex.class, 3 + buffedLvl());
						break;
					case 8:
						Buff.affect(ch, Ooze.class).set(Ooze.DURATION * buffedLvl());
						break;
				}
			}
		}
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		float procChance = 1f/2.5f*procChanceMultiplier(attacker);

		if (defender.HP <= damage && Random.Float() < procChance) {
			int buffOffset = ((Hero) attacker).pointsInTalent(Talent.EMPOWERED_STRIKE); // max. 3
			switch (Random.Int(7)) {
				default:
				case 0:
					if (Dungeon.isChallenged(Challenges.DARKNESS)) {
						Buff.prolong(attacker, Light.class, 2f + buffedLvl());
					} else {
						Buff.prolong(attacker, Light.class, 10f + buffedLvl());
					}
					break;
				case 1:
					Buff.affect(attacker, FireImbue.class).set(2 + FireImbue.DURATION * 0.05f * buffedLvl());
					break;
				case 2:
					Buff.affect(attacker, AdrenalineSurge.class).reset(1,
							40 + AdrenalineSurge.DURATION * 0.04f * buffedLvl());
					break;
				case 3:
					Buff.affect(attacker, FrostImbue.class, 2 + FrostImbue.DURATION * 0.05f * buffedLvl());
					break;
				case 4:
					cleanse(attacker, 2);
					break;
				case 5:
					Buff.prolong(attacker, Bless.class, 2 + Bless.DURATION * 0.1f * buffedLvl());
					break;
				case 6:
					Buff.affect(attacker, Healing.class).setHeal(8 + buffedLvl() / 2, 0, 1);
					break;
			}
		}
	}

	public static void cleanse(Char ch, float duration){
		for (Buff b : ch.buffs()){
			if (b.type == Buff.buffType.NEGATIVE
					&& !(b instanceof AllyBuff)
					&& !(b instanceof LostInventory)){
				b.detach();
			}
		}
		Buff.affect(ch, PotionOfCleansing.Cleanse.class, duration);
	}

	private int distance() {
		return buffedLvl() * 2 + 6;
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {

		int cell = beam.path.get(Math.min(beam.dist, distance()));
		curUser.sprite.parent
				.add(new Beam.DeathRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(cell)));
		callback.call();
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0x220000);
		particle.am = 0.6f;
		particle.setLifespan(1f);
		particle.acc.set(10, -10);
		particle.setSize(0.5f, 3f);
		particle.shuffleXY(1f);
	}

}
