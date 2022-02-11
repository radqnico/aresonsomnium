package it.areson.aresonsomnium.listeners.external;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import it.areson.aresonsomnium.AresonSomnium;

public class WorldGuardListener extends FlagValueChangeHandler<StateFlag.State> {

    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<WorldGuardListener> {
        @Override
        public WorldGuardListener create(Session session) {
            return new WorldGuardListener(session);
        }
    }

    private StateFlag.State state;

    public WorldGuardListener(Session session) {
        super(session, AresonSomnium.wgPermissionFlyState);
    }

    @Override
    protected void onInitialValue(LocalPlayer localPlayer, ApplicableRegionSet applicableRegionSet, StateFlag.State state) {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA onInitialValue");
    }

    @Override
    protected boolean onSetValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, StateFlag.State state, StateFlag.State t1, MoveType moveType) {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA onSetValue");
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer localPlayer, Location location, Location location1, ApplicableRegionSet applicableRegionSet, StateFlag.State state, MoveType moveType) {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA onAbsentValue");
        return true;
    }


}
