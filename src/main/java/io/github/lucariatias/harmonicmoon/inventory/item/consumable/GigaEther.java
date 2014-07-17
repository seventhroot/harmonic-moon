package io.github.lucariatias.harmonicmoon.inventory.item.consumable;

import io.github.lucariatias.harmonicmoon.fight.Combatant;

public class GigaEther extends Consumable {

    public GigaEther() {
        super("Giga Ether", "A tonic that regenerates mana.\nReplenishes 10000 mana.");
    }

    @Override
    public void use(Combatant user, Combatant target) {
        target.setMana(target.getMana() + 10000);
    }

}
