package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.plants.Ghostbulb;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GhostDart extends TippedDart {
	
	{
		image = ItemSpriteSheet.GHOST_DART;
	}
	
	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		Ghostbulb.spawnGhosts(attacker);
		Ghostbulb.spawnGhosts(defender);
		return super.proc( attacker, defender, damage );
	}
	
}
