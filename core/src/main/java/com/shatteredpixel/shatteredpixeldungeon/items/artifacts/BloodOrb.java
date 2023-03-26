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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class BloodOrb extends Artifact {

	public static final String AC_CAST = "CAST";

	{
		image = ItemSpriteSheet.ARTIFACT_ORB;

		levelCap = 10;
		exp = 0;

		charge = 0;

		defaultAction = AC_CAST;
		usesTargeting = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero) && charge > 0 && !cursed && hero.buff(MagicImmune.class) == null) {
			actions.add(AC_CAST);
		}
		return actions;
	}

	public int targetingPos(Hero user, int dst) {
		return dst;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (hero.buff(MagicImmune.class) != null)
			return;

		if (action.equals(AC_CAST)) {

			curUser = hero;

			if (!isEquipped(hero)) {
				GLog.i(Messages.get(Artifact.class, "need_to_equip"));
				usesTargeting = false;

			// } else if (charge < 1) {
			// 	GLog.i(Messages.get(this, "no_charge"));
			// 	usesTargeting = false;

			} else if (cursed) {
				GLog.w(Messages.get(this, "cursed"));
				usesTargeting = false;

			} else {
				usesTargeting = true;
				GameScene.selectCell(caster);
			}

		}
	}

	private CellSelector.Listener caster = new CellSelector.Listener() {

		@Override
		public void onSelect(Integer target) {
			if (target != null && (Dungeon.level.visited[target] || Dungeon.level.mapped[target])) {

				int range = 3 + level();
				if (Dungeon.level.distance(curUser.pos, target) > range) {
					GLog.w(Messages.get(EtherealChains.class, "cant_reach"));
					return;
				}

				bleedArea(target, curUser, 2);

			}

		}

		@Override
		public String prompt() {
			return Messages.get(BloodOrb.class, "prompt");
		}
	};

	private void bleedArea(Integer target, final Hero hero, int aoeSize) {
		Ballistica aim;
		// the direction of the aim only matters if it goes outside the map
		// So we just ensure it won't do that.
		if (target % Dungeon.level.width() > 10) {
			aim = new Ballistica(target, target - 1, Ballistica.WONT_STOP);
		} else {
			aim = new Ballistica(target, target + 1, Ballistica.WONT_STOP);
		}

		int projectileProps = Ballistica.STOP_TARGET;
		ConeAOE aoe = new ConeAOE(aim, aoeSize, 360, projectileProps);

		int charsHit = 0;

		for (Ballistica ray : aoe.outerRays) {
			((MagicMissile) hero.sprite.parent.recycle(MagicMissile.class)).reset(
					MagicMissile.BLOOD_CONE,
					target,
					ray.path.get(ray.dist),
					new Callback() {
						@Override
						public void call() {
							for (int cell : aoe.cells) {
								Char mob = Actor.findChar(cell);
								if (mob != null && !mob.properties().contains(Char.Property.INORGANIC)) {
									Buff.affect(mob, Bleeding.class).set(2, BloodOrb.class);
									// charsHit++;
								}
							}
						}
					});
				}
		//apply bleed to targeted point also
		Char t = Actor.findChar(target);
		Buff.affect(t, Bleeding.class).set(2, BloodOrb.class);
		charsHit++;
		// pay cost / RingOfEnergy.artifactChargeMultiplier(target);
		upgrade();

		throwSound();
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		hero.busy();
		hero.spend(1f);
		hero.next();
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new orbRecharge();
	}

	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null)
			return;
		int chargeTarget = 5 + (level() * 2);
		if (charge < chargeTarget * 2) {
			partialCharge += 0.5f * amount;
			if (partialCharge >= 1) {
				partialCharge--;
				charge++;
				updateQuickslot();
			}
		}
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped(Dungeon.hero)) {
			desc += "\n\n";
			if (cursed)
				desc += Messages.get(this, "desc_cursed");
			else
				desc += Messages.get(this, "desc_equipped");
		}
		return desc;
	}

	public class orbRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			if (cursed && Random.Int(150) == 0) {
				Buff.affect(target, Bleeding.class).set(2, BloodOrb.class);
			}

			updateQuickslot();
			spend(TICK);
			return true;
		}

		public void gainExp(float levelPortion) {
			if (cursed || target.buff(MagicImmune.class) != null || levelPortion == 0)
				return;

			exp += Math.round(levelPortion * 100);

			if (exp > 100 + level() * 100 && level() < levelCap) {
				exp -= 100 + level() * 100;
				GLog.p(Messages.get(this, "levelup"));
				upgrade();
			}

		}
	}
}