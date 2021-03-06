package com.arturagapov.easymath;

import java.io.Serializable;

/**
 * Created by Artur Agapov on 03.11.2016.
 */
public class Data implements Serializable {

    private int sessionCount;
    private int score;
    private int unitsComplete;
    private boolean isRate;
    private boolean isReallyRate;
    private boolean moreAppFirst;
    private boolean isPremium;

    private Data(int sessionCount, int score, int unitsComplete, boolean isRate, boolean isReallyRate, boolean moreAppFirst, boolean isPremium) {
        this.sessionCount = sessionCount;
        this.score = score;
        this.unitsComplete = unitsComplete;
        this.isRate = isRate;
        this.isReallyRate = isReallyRate;
        this.moreAppFirst = moreAppFirst;
        this.isPremium = isPremium;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public static Data userData = new Data(0, 0, 0, false, false, false, false);

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getUnitsComplete() {
        return unitsComplete;
    }

    public void setUnitsComplete(int unitsComplete) {
        this.unitsComplete = unitsComplete;
    }

    public boolean isRate() {
        return isRate;
    }

    public void setRate(boolean rate) {
        isRate = rate;
    }

    public boolean isReallyRate() {
        return isReallyRate;
    }

    public void setReallyRate(boolean reallyRate) {
        isReallyRate = reallyRate;
    }

    public boolean isMoreAppFirst() {
        return moreAppFirst;
    }

    public void setMoreAppFirst(boolean moreAppFirst) {
        this.moreAppFirst = moreAppFirst;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }
}
