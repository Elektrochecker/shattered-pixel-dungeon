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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class Transfiguration extends Spell {

	{
		image = ItemSpriteSheet.TRANSFIGURATION;
	}

	@Override
	protected void onCast(Hero hero) {
		hero.sprite.operate(hero.pos);
		updateQuickslot();
		Invisibility.dispel();

		detach( curUser.belongings.backpack );
		hero.spendAndNext( 1f );
		
		Sample.INSTANCE.play(Assets.Sounds.PUFF, 1f, 0.75f);

		Ballistica aim;
		//The direction of the aim only matters if it goes outside the map
		//So we try to aim in the cardinal direction that has the most space
		int x = hero.pos % Dungeon.level.width();
		int y = hero.pos / Dungeon.level.width();

		if (Math.max(x, Dungeon.level.width()-x) >= Math.max(y, Dungeon.level.height()-y)){
			if (x > Dungeon.level.width()/2){
				aim = new Ballistica(hero.pos, hero.pos - 1, Ballistica.WONT_STOP);
			} else {
				aim = new Ballistica(hero.pos, hero.pos + 1, Ballistica.WONT_STOP);
			}
		} else {
			if (y > Dungeon.level.height()/2){
				aim = new Ballistica(hero.pos, hero.pos - Dungeon.level.width(), Ballistica.WONT_STOP);
			} else {
				aim = new Ballistica(hero.pos, hero.pos + Dungeon.level.width(), Ballistica.WONT_STOP);
			}
		}

		ConeAOE aoe = new ConeAOE(aim, 3, 360, Ballistica.STOP_TARGET);

		for (int c : aoe.cells) {
			changeTerrain(c);
		}

		changeTerrain(hero.pos);
	}

	private void changeTerrain(int cell) {
		int[] map = Dungeon.level.map;
		switch (map[cell]) {
			default:
				break;

			//cycle basic floor types
			case Terrain.EMPTY:
				Level.set( cell, Terrain.EMPTY_DECO, Dungeon.level);
				break;
			case Terrain.EMPTY_DECO:
				Level.set( cell, Terrain.EMPTY_SP, Dungeon.level);
				break;
			case Terrain.EMPTY_SP:
				Level.set( cell, Terrain.EMPTY, Dungeon.level);
				break;

			//cycle special floor types
			case Terrain.WATER:
				Level.set( cell, Terrain.GRASS, Dungeon.level);
				break;
			case Terrain.GRASS:
				Level.set( cell, Terrain.HIGH_GRASS, Dungeon.level);
				break;
			case Terrain.HIGH_GRASS:
				Level.set( cell, Terrain.EMBERS, Dungeon.level);
				break;
			case Terrain.FURROWED_GRASS:
				Level.set( cell, Terrain.EMBERS, Dungeon.level);
				break;
			case Terrain.EMBERS:
					Level.set( cell, Terrain.WATER, Dungeon.level);
					break;

			//destroy obstacles
			case Terrain.BOOKSHELF:
				Level.set( cell, Terrain.EMBERS, Dungeon.level);
				break;
			case Terrain.BARRICADE:
				Level.set( cell, Terrain.EMBERS, Dungeon.level);
				break;

			//change floor under statues
			case Terrain.STATUE:
				Level.set( cell, Terrain.STATUE_SP, Dungeon.level);
				break;
			case Terrain.STATUE_SP:
				Level.set( cell, Terrain.STATUE, Dungeon.level);
				break;
		}
		
		GameScene.updateMap(cell);
	}

	@Override
	public int value() {
		// prices of ingredients, divided by output quantity, rounds down
		return (int) ((60 + 40) * (quantity / 8f));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs = new Class[] { PotionOfCorrosiveGas.class, ArcaneCatalyst.class };
			inQuantity = new int[] { 1, 1 };

			cost = 4;

			output = Transfiguration.class;
			outQuantity = 8;
		}

	}
}
