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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PrismaticGuard;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.plants.Ghostbulb;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class PrismaticImageSpell extends Spell {

	{
		image = ItemSpriteSheet.PRISMATIC_IMAGE;
	}

	@Override
	protected void onCast(Hero hero) {
		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.READ );
		detach( curUser.belongings.backpack );

		boolean found = false;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (m instanceof PrismaticImage) {
				found = true;
				m.HP = m.HT;
				m.sprite.emitter().burst(Speck.factory(Speck.HEALING), 4);
			}
		}

		if (!found) {
			Buff.affect(curUser, PrismaticGuard.class).set(PrismaticGuard.maxHP(curUser));
		}

		updateQuickslot();
		Invisibility.dispel();
		hero.spendAndNext( 1f );
	}

	@Override
	public int value() {
		// prices of ingredients, divided by output quantity, rounds down
		return (int) ((20 + 40) * (quantity / 2f));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs = new Class[] { Ghostbulb.Seed.class, ArcaneCatalyst.class };
			inQuantity = new int[] { 1, 1 };

			cost = 12;

			output = PrismaticImageSpell.class;
			outQuantity = 2;
		}

	}
}
