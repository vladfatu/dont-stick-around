package com.tacticsgames.dontstickaround;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

/**
 * Created by Vlad on 21-Feb-16.
 */
public class MenuActivity extends PlayServicesActivity {

    private static final int RC_UNUSED = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);
    }

    @Override
    protected String getScreenName() {
        return getLocalClassName();
    }

    public void onStartGameClicked(View view) {
        sendAnalyticsEvent(R.string.analytics_category_action, R.string.analytics_event_start_game);
        startActivity(new Intent(this, GameActivity.class));
    }

    public void onViewLeaderBoardClicked(View view) {
        if (isSignedIn()) {
            sendAnalyticsEvent(R.string.analytics_category_action, R.string.analytics_event_view_leaderboards);
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getGoogleApiClient()),
                    RC_UNUSED);
        } else {
            sendAnalyticsEvent(R.string.analytics_category_action, R.string.analytics_event_view_leaderboards_failed);
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.leaderboards_not_available)).show();
        }
    }

    public void onViewAchievementsClicked(View view) {
        if (isSignedIn()) {
            sendAnalyticsEvent(R.string.analytics_category_action, R.string.analytics_event_view_achievements);
            startActivityForResult(Games.Achievements.getAchievementsIntent(getGoogleApiClient()),
                    RC_UNUSED);
        } else {
            sendAnalyticsEvent(R.string.analytics_category_action, R.string.analytics_event_view_achievements_failed);
            BaseGameUtils.makeSimpleDialog(this, getString(R.string.achievements_not_available)).show();
        }
    }

}
