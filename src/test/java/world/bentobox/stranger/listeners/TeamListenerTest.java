package world.bentobox.stranger.listeners;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;

import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.island.IslandCache;
import world.bentobox.bentobox.managers.island.IslandGrid;
import world.bentobox.stranger.CommonTestSetup;
import world.bentobox.stranger.Settings;
import world.bentobox.stranger.StrangerRealms;

/**
 * Regression tests for TeamListener.
 *
 * <p>Background: a prior version of {@link TeamListener#onTeamKick(TeamKickEvent)} and
 * {@link TeamListener#onTeamLeave(TeamLeaveEvent)} had inverted boolean logic that caused
 * {@code resize()} to fire on islands that were <em>not</em> in StrangerRealms' world.
 * That in turn called {@link Island#setRange(int)} with StrangerRealms' configured distance
 * (typically 64 with defaults), corrupting the range field of other game modes' islands and
 * causing {@code Island distance mismatch} crashes on the next server restart.
 */
public class TeamListenerTest extends CommonTestSetup {

    @Mock
    private World otherWorld; // a world NOT governed by StrangerRealms
    @Mock
    private Island otherIsland; // an island in that other world (e.g. AOneBlock)
    @Mock
    private Settings strangerSettings;
    @Mock
    private IslandCache islandCache;
    @Mock
    private IslandGrid islandGrid;

    // Do NOT use @Mock for StrangerRealms — its static initializer touches
    // BentoBox.getInstance() (NamespacedKey for the warped compass recipe),
    // which fails before super.setUp() has installed the plugin singleton.
    // Construct it manually after super.setUp().
    private StrangerRealms addon;
    private TeamListener listener;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        addon = mock(StrangerRealms.class);

        // Addon points at the standard mocked BentoBox plugin/managers.
        when(addon.getPlugin()).thenReturn(plugin);
        when(addon.getIslands()).thenReturn(im);
        when(addon.getSettings()).thenReturn(strangerSettings);
        // Defaults from StrangerRealms config: distance 32, memberBonus 32.
        when(strangerSettings.getIslandDistance()).thenReturn(32);
        when(strangerSettings.getMemberBonus()).thenReturn(32);

        // CRITICAL: the kicked/leaving island is in some other game mode's world.
        when(addon.inWorld(otherWorld)).thenReturn(false);
        when(otherIsland.getWorld()).thenReturn(otherWorld);
        // Island has two members so resize() would compute 32 + 32*(2-1) = 64.
        when(otherIsland.getMemberSet()).thenReturn(ImmutableSet.of(UUID.randomUUID(), UUID.randomUUID()));
        when(otherIsland.getProtectionRange()).thenReturn(50);

        // Island grid wiring (in case the guard is removed in the future and resize is reached).
        when(im.getIslandCache()).thenReturn(islandCache);
        when(islandCache.getIslandGrid(any())).thenReturn(islandGrid);

        listener = new TeamListener(addon);
    }

    @Test
    public void onTeamKick_islandInOtherGamemode_doesNotMutateRange() {
        TeamKickEvent event = mock(TeamKickEvent.class);
        when(event.getIsland()).thenReturn(otherIsland);

        listener.onTeamKick(event);

        // The whole point of the fix: never touch the range / protection range of an
        // island that isn't in StrangerRealms' world.
        verify(otherIsland, never()).setRange(anyInt());
        verify(otherIsland, never()).setProtectionRange(anyInt());
    }

    @Test
    public void onTeamLeave_islandInOtherGamemode_doesNotMutateRange() {
        TeamLeaveEvent event = mock(TeamLeaveEvent.class);
        when(event.getIsland()).thenReturn(otherIsland);

        listener.onTeamLeave(event);

        verify(otherIsland, never()).setRange(anyInt());
        verify(otherIsland, never()).setProtectionRange(anyInt());
    }

    @Test
    public void onTeamKick_memberBonusZero_doesNotMutateRange() {
        // Even within StrangerRealms' own world, a zero memberBonus means resizing
        // is disabled and the early-return should fire.
        when(strangerSettings.getMemberBonus()).thenReturn(0);
        when(addon.inWorld(otherWorld)).thenReturn(true); // pretend the island IS in stranger world

        TeamKickEvent event = mock(TeamKickEvent.class);
        when(event.getIsland()).thenReturn(otherIsland);

        listener.onTeamKick(event);

        verify(otherIsland, never()).setRange(anyInt());
        verify(otherIsland, never()).setProtectionRange(anyInt());
    }
}
