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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfCosmicTravel extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_COSMOS;

		collisionProperties = Ballistica.MAGIC_BOLT;
	}

	public int min(int lvl) {
		return 1 + lvl;
	}

	public int max(int lvl) {
		return 2 + 5 * lvl;
	}

	@Override
	public void onZap(Ballistica beam) {

		Char ch = Actor.findChar(beam.collisionPos);
		if (ch != null) {
			wandProc(ch, chargesPerCast());
			affectTarget(ch);
		}
	}

	private void affectTarget(Char ch) {
		int dmg = damageRoll();
		int enemyPos = ch.pos;
		int userPos = curUser.pos;

		if (!ch.properties().contains(Char.Property.IMMOVABLE)) {
			ScrollOfTeleportation.appear(curUser, enemyPos);
			ScrollOfTeleportation.appear(ch, userPos);
			Dungeon.observe();
			GameScene.updateFog();

		} else {
			dmg *= 3 / 2;
		}
		ch.damage(dmg, this);
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {
		curUser.sprite.parent.add(
				new Beam.LightRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(beam.collisionPos)));
		callback.call();
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		int enemyPos = defender.pos;
		int userPos = curUser.pos;
		float procChance = 1f / 2.5f * procChanceMultiplier(attacker);

		if (Random.Float() < procChance) {
			if (!defender.properties().contains(Char.Property.IMMOVABLE)) {
				ScrollOfTeleportation.appear(curUser, enemyPos);
				ScrollOfTeleportation.appear(defender, userPos);
				Dungeon.observe();
				GameScene.updateFog();
			} else {
				defender.damage(damage / 5 * (int) procChanceMultiplier(attacker), attacker);
			}
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(Random.Int(0x0000FF)); // random blue color
		particle.am = 0.5f;
		particle.setLifespan(1f);
		particle.speed.polar(Random.Float(PointF.PI2), 2f);
		particle.setSize(1f, 2f);
		particle.radiateXY(0.5f);
	}

}
