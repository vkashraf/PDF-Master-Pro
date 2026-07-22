package com.example.engine;

import android.content.Context;
import android.content.SharedPreferences;

public class PremiumManager {

    private static final String PREF_NAME = "pdf_master_pro_prefs";
    private static final String KEY_IS_PRO = "is_pro_unlocked";
    private static final String KEY_OPERATIONS_COUNT = "daily_ops_count";

    private final SharedPreferences prefs;

    public PremiumManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isProUnlocked() {
        return prefs.getBoolean(KEY_IS_PRO, false);
    }

    public void setProUnlocked(boolean unlocked) {
        prefs.edit().putBoolean(KEY_IS_PRO, unlocked).apply();
    }

    public int getDailyOperationsCount() {
        return prefs.getInt(KEY_OPERATIONS_COUNT, 0);
    }

    public void incrementDailyOperations() {
        int current = getDailyOperationsCount();
        prefs.edit().putInt(KEY_OPERATIONS_COUNT, current + 1).apply();
    }

    public boolean canPerformOperation() {
        if (isProUnlocked()) return true;
        return getDailyOperationsCount() < 10;
    }
}
