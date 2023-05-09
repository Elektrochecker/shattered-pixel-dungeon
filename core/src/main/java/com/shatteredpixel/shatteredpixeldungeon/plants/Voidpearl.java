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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfChaos;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Voidpearl extends Plant {

	{
		image = 16;
		seedClass = Seed.class;
	}

	@Override
	public void activate(final Char ch) {

		if (ch instanceof Hero) {

			((Hero) ch).curAction = null;

			if (((Hero) ch).subClass == HeroSubClass.WARDEN) {
				WandOfChaos.cleanse(ch, 1);
			} else {

				ch.die(Voidpearl.class);
				if (!ch.isAlive()) {
					GLog.n(Messages.get(this, "ondeath"));
					Dungeon.fail(Voidpearl.class);
				}
			}

		} else if (ch instanceof Mob && !ch.properties().contains(Char.Property.BOSS)
				&& !ch.properties().contains(Char.Property.MINIBOSS)) {

			if (ch.isAlive())
				ch.die(Voidpearl.class);
		}

		//destroy all items on this tile
		Heap heap = Dungeon.level.heaps.get(pos);
		if (heap != null) {
			heap.destroy();
		}

		if (Dungeon.level.heroFOV[pos]) {
			CellEmitter.get(pos).burst(ShadowParticle.UP, 5);
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
		}
	}

	public static class Seed extends Plant.Seed {
		{
			image = ItemSpriteSheet.SEED_VOIDPEARL;

			plantClass = Voidpearl.class;
		}

		@Override
		public int value() {
			return 50 * quantity;
		}

		@Override
		public int energyVal() {
			return 4 * quantity;
		}
	}
}
