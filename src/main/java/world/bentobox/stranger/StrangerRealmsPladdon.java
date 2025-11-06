package world.bentobox.stranger;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;


public class StrangerRealmsPladdon extends Pladdon {

    private StrangerRealms addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new StrangerRealms();
        }
        return addon;
    }

}
