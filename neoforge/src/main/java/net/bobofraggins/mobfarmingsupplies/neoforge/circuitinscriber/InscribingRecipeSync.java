package net.bobofraggins.mobfarmingsupplies.neoforge.circuitinscriber;

import net.bobofraggins.mobfarmingsupplies.circuitinscriber.ClientInscribingRecipes;
import net.bobofraggins.mobfarmingsupplies.register.Registration;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/** Sends the Circuit Inscribing recipes to clients, where JEI shows them. */
public final class InscribingRecipeSync {

    private InscribingRecipeSync() {}

    public static void registerServer() {
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) ->
                event.sendRecipes(Registration.CIRCUIT_INSCRIBING_TYPE.get()));
    }

    /** Stores what arrives before JEI, which starts on this same event, reads it. */
    public static void registerClient() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (RecipesReceivedEvent event) -> {
            if (event.getRecipeTypes().contains(Registration.CIRCUIT_INSCRIBING_TYPE.get())) {
                ClientInscribingRecipes.set(event.getRecipeMap().byType(Registration.CIRCUIT_INSCRIBING_TYPE.get()));
            }
        });
    }
}
