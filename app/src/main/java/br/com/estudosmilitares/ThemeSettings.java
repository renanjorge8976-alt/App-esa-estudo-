package br.com.estudosmilitares;

import android.content.Context;
import android.content.SharedPreferences;

/** Preferências visuais persistidas do aplicativo. */
public final class ThemeSettings {
    private static final String PREFS = "estudos_militares";
    private static final String KEY_ACCENT = "theme_accent";
    private final SharedPreferences preferences;

    public ThemeSettings(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getAccentColor() {
        return preferences.getInt(KEY_ACCENT, 0xFF426447);
    }

    /** Salva a cor principal escolhida pelo usuário. */
    public void setAccentColor(int color) {
        preferences.edit().putInt(KEY_ACCENT, color).apply();
    }
}
