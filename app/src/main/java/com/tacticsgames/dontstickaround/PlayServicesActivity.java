package com.tacticsgames.dontstickaround;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

/**
 * Created by Vlad on 21-Feb-16.
 */
public abstract class PlayServicesActivity extends AppCompatActivity {

    private static final String TAG = "PlayServicesActivity";

    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient googleApiClient;

    private boolean resolvingConnectionFailure = false;
    private boolean autoStartSignInFlow = true;

    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPlayServices();
        configureAnalyticsTracker();
        sendScreenName();
    }

    private void configureAnalyticsTracker() {
        StickApplication application = (StickApplication) getApplication();
        tracker = application.getDefaultTracker();
    }

    private void sendScreenName() {
        Log.i(TAG, "Setting screen name: " + getScreenName());
        tracker.setScreenName(getScreenName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    protected void sendAnalyticsEvent(int categoryId, int actionId) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(getResources().getString(categoryId))
                .setAction(getResources().getString(actionId))
                .build());
    }

    protected abstract String getScreenName();

    private void setupPlayServices() {
        // Create the Google API Client with access to Plus and Games
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new StickConnectionCallbacks())
                .addOnConnectionFailedListener(new StickOnConnectionFailedListener())
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    protected GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    protected boolean isSignedIn() {
        return (googleApiClient != null && googleApiClient.isConnected());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): disconnecting");
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            resolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                googleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }

    protected void incrementAchievement(int achievementId, int value) {
        if (value > 0) {
            Games.Achievements.increment(googleApiClient, getString(achievementId), value);
        }
    }

    protected void unlockAchievement(int achievementId) {
        Games.Achievements.unlock(googleApiClient, getString(achievementId));
    }

    protected void submitScoreToLeaderBoard(int leaderBoardId, int score) {
        Games.Leaderboards.submitScore(googleApiClient, getString(leaderBoardId),
                score);
    }

    protected void submitEvent(int eventId, int value) {
        if (value > 0) {
            Games.Events.increment(googleApiClient, getString(eventId), value);
        }
    }

    private class StickConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            Toast.makeText(PlayServicesActivity.this, "onConnected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onConnected(): connected to Google APIs");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Toast.makeText(PlayServicesActivity.this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onConnectionSuspended(): attempting to reconnect");
            googleApiClient.connect();
        }
    }

    private class StickOnConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Toast.makeText(PlayServicesActivity.this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onConnectionFailed(): attempting to resolve");
            if (resolvingConnectionFailure) {
                Log.d(TAG, "onConnectionFailed(): already resolving, ignoring");
                return;
            }

            autoStartSignInFlow = false;
            resolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(PlayServicesActivity.this, googleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                resolvingConnectionFailure = false;
            }
        }
    }
}
