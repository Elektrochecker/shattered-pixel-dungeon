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

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class WaterOfEnchantment extends WellWater {

	@Override
	protected boolean affectHero(Hero hero) {

		if (!hero.isAlive())
			return false;

		Sample.INSTANCE.play(Assets.Sounds.DRINK);

		for (Item item : hero.belongings) {
			if (item instanceof Weapon && item.isEquipped(Dungeon.hero)) {
				((Weapon) item).enchant();

			} else if (item instanceof Armor && item.isEquipped(Dungeon.hero)) {
				((Armor) item).inscribe();

			}
		}

		hero.sprite.emitter().start(Speck.factory(Speck.ENCHANT), 0.4f, 4);

		CellEmitter.get(hero.pos).start(ShaftParticle.FACTORY, 0.2f, 3);

		Dungeon.hero.interrupt();

		GLog.p(Messages.get(this, "procced"));

		return true;
	}

	@Override
	protected Item affectItem(final Item item, int pos) {
		//stronger enchant for single item
		if (item instanceof Weapon) {

			//60% uncommon, 40% rare
			if (Math.random() < 0.6) {
				((Weapon) item).enchant(Weapon.Enchantment.randomUncommon());
			} else {
				((Weapon) item).enchant(Weapon.Enchantment.randomRare());
			}

			CellEmitter.get(pos).start(Speck.factory(Speck.ENCHANT), 0.4f, 4);
			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			return item;
		} else if (item instanceof Armor) {

			//60% uncommon, 40% rare
			if (Math.random() < 0.6) {
				((Armor) item).inscribe(Armor.Glyph.randomUncommon());
			} else {
				((Armor) item).inscribe(Armor.Glyph.randomRare());
			}

			CellEmitter.get(pos).start(Speck.factory(Speck.ENCHANT), 0.4f, 4);
			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			return item;
		}
		return null;
	}

	@Override
	protected Landmark record() {
		return Landmark.WELL_OF_ENCHANTMENT;
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);
		emitter.start(Speck.factory(Speck.ENCHANT), 0.5f, 0);
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
