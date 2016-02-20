package com.tacticsgames.dontstickaround;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final int JUMP_LENGTH = 50;
    private static final int OBSTACLE_COUNT = 10;

    private ImageView circleImage;
    private View gameOverLayout;
    private TextView gameOverScore;
    private RelativeLayout gameLayout;
    private boolean gameOver;

    private int bottomInPixels;
    private int passedObstacles;

    private Map<Integer, View> obstacles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        gameLayout = RelativeLayout.class.cast(findViewById(R.id.game_content));
        circleImage = ImageView.class.cast(findViewById(R.id.circleImage));
        gameOverLayout = findViewById(R.id.gameOverLayout);
        gameOverScore = TextView.class.cast(findViewById(R.id.gameOverScore));

        initialiseObstacles();
        startObstacles();
    }

    private void startObstacles(){
        gameOverLayout.setVisibility(View.GONE);
        gameOver = false;
        passedObstacles = 0;
        startObstacleAnimations();
    }

    private void initialiseObstacles() {
        obstacles = new HashMap<>();
        for (int i=0; i<OBSTACLE_COUNT; i++) {
            View obstacleView = getObstacleView();
            obstacles.put(i, obstacleView);
            gameLayout.addView(obstacleView);
        }
    }

    private void startObstacleAnimations() {
        for (int i=0; i<OBSTACLE_COUNT; i++) {
            startObstacleAnimation(i);
        }
    }

    private void startObstacleAnimation(final int id) {
        Random random = new Random();
        View obstacle = obstacles.get(id);
        ViewGroup.MarginLayoutParams params = ViewGroup.MarginLayoutParams.class.cast(obstacle.getLayoutParams());
        params.rightMargin = 0;
        params.bottomMargin = random.nextInt(900);
        obstacle.setLayoutParams(params);
        Animation a = new TranslateXAnimation(obstacle, getScreenWidth());
        a.setDuration((int)(getDpFromPixels(getScreenWidth()) * 10 / (1 + (random.nextFloat()*3))));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (! gameOver) {
                    startObstacleAnimation(id);
                    passedObstacles++;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        obstacle.startAnimation(a);
    }

    private View getObstacleView() {
        ImageView obstacle = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        obstacle.setLayoutParams(layoutParams);

        obstacle.setImageResource(R.drawable.circle);
        return obstacle;
    }

    private void translateImageUpWithAnimation() {
        Animation a = new TranslateYAnimation(circleImage, getPixelsFromDp(JUMP_LENGTH));
        a.setDuration(JUMP_LENGTH * 5);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                translateImageDownWithAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        circleImage.startAnimation(a);
    }

    private void translateImageDownWithAnimation() {
        Animation a = new TranslateYAnimation(circleImage, -bottomInPixels);
        a.setDuration(getDpFromPixels(bottomInPixels) * 10);
        circleImage.startAnimation(a);
    }

    public void onLayoutClick(View view) {
        translateImageUpWithAnimation();
    }

    public void onRetryClicked(View view) {
        startObstacles();
    }

    private void translateImageWithLayout() {
        bottomInPixels += getPixelsFromDp(20);
        ViewGroup.MarginLayoutParams layoutParams = ViewGroup.MarginLayoutParams.class.cast(circleImage.getLayoutParams());
        layoutParams.setMargins(layoutParams.leftMargin, 0, 0, bottomInPixels);
        circleImage.setLayoutParams(layoutParams);
    }

    private int getPixelsFromDp(int dp) {
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
    }

    private int getDpFromPixels(int pixels) {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = pixels / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) dp;
    }

    private int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private void showGameOver() {
        if (!gameOver) {
            gameOver = true;
            gameOverScore.setText(Integer.toString(passedObstacles));
            gameOverLayout.setVisibility(View.VISIBLE);
        }
    }

    private class TranslateYAnimation extends Animation {

        private int initialMargin;
        private int bottomMarginDelta;
        private View targetView;

        public TranslateYAnimation(View targetView, int bottomMarginDelta) {
            super();
            this.bottomMarginDelta = bottomMarginDelta;
            this.targetView = targetView;
            this.initialMargin = ViewGroup.MarginLayoutParams.class.cast(targetView.getLayoutParams()).bottomMargin;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ViewGroup.MarginLayoutParams params = ViewGroup.MarginLayoutParams.class.cast(targetView.getLayoutParams());
            bottomInPixels = initialMargin + (int) (bottomMarginDelta * interpolatedTime);
            params.bottomMargin = bottomInPixels;
            targetView.setLayoutParams(params);
        }
    }

    private class TranslateXAnimation extends Animation {

        private int rightMargin;
        private View targetView;

        public TranslateXAnimation(View targetView, int rightMargin) {
            super();
            this.rightMargin = rightMargin;
            this.targetView = targetView;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ViewGroup.MarginLayoutParams params = ViewGroup.MarginLayoutParams.class.cast(targetView.getLayoutParams());
            params.rightMargin = (int) (rightMargin * interpolatedTime);
            targetView.setLayoutParams(params);
            if (CollisionChecker.areViewsColliding(circleImage, targetView, getPixelsFromDp(5))) {
                showGameOver();
            }
        }
    }
}
