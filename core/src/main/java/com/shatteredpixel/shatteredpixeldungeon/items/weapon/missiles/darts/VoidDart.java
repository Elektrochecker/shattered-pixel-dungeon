package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class VoidDart extends TippedDart {
	
	{
		image = ItemSpriteSheet.VOID_DART;
	}
	
	@Override
	public int proc( Char attacker, Char defender, int damage ) {
		defender.damage(20, this);
		return super.proc( attacker, defender, damage );
	}
	
}
