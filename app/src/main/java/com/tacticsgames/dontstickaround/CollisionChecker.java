package com.tacticsgames.dontstickaround;

import android.graphics.Rect;
import android.view.View;

public class CollisionChecker {

    public static boolean areViewsColliding(View view1, View view2, int offset) {
        Rect rect1 = new Rect(view1.getLeft() + offset, view1.getTop() + offset, view1.getRight() - offset, view1.getBottom() - offset);
        Rect rect2 = new Rect(view2.getLeft() + offset, view2.getTop() + offset, view2.getRight() - offset, view2.getBottom() - offset);
//        view1.getGlobalVisibleRect(rect1);
//        view2.getGlobalVisibleRect(rect2);
        if (rect1.intersect(rect2)) {
            System.out.println("Game Over!");
            System.out.println("rect1: " + rect1.toString());
            System.out.println("rect2: " + rect2.toString());
            return true;
        } else {
            return false;
        }
    }

}
