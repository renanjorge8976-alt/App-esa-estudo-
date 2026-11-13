package br.com.estudosmilitares;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;

/** Tela inicial de perfil e personalização, conectada ao armazenamento local. */
public class ProfileActivity extends Activity {
    private static final int PICK_PHOTO = 41;
    private static final int GREEN = Color.rgb(66, 100, 71);
    private static final int INK = Color.rgb(32, 46, 35);
    private static final int MUTED = Color.rgb(115, 128, 117);
    private static final int BG = Color.rgb(247, 248, 245);

    private final String[] themeNames = {"Verde militar", "Azul", "Roxo", "Laranja"};
    private final int[] themeColors = {
            Color.rgb(66, 100, 71), Color.rgb(41, 98, 150),
            Color.rgb(111, 76, 148), Color.rgb(191, 102, 43)
    };

    private AppStore store;
    private ThemeSettings themeSettings;
    private LinearLayout root;
    private ImageView avatar;
    private TextView nameView, streakView;
    private Button themeButton;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new AppStore(this);
        themeSettings = new ThemeSettings(this);
        buildScreen();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private void buildScreen() {
        int accent = themeSettings.getAccentColor();
        getWindow().setStatusBarColor(accent);
        getWindow().setNavigationBarColor(Color.WHITE);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(20), dp(28), dp(20), dp(20));

        TextView heading = text("Estudos Militares", 25, INK, true);
        root.addView(heading);
        TextView subtitle = text("Seu espaço de preparação", 14, MUTED, false);
        root.addView(subtitle);
        space(root, 22);

        LinearLayout profile = new LinearLayout(this);
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setOrientation(LinearLayout.HORIZONTAL);
        avatar = new ImageView(this);
        avatar.setBackgroundColor(accent);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        profile.addView(avatar, new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout identity = new LinearLayout(this);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(dp(14), 0, 0, 0);
        nameView = text(displayName(), 19, INK, true);
        identity.addView(nameView);
        Button photoButton = button("Trocar foto", accent, this::pickPhoto);
        identity.addView(photoButton);
        profile.addView(identity, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(profile);
        loadPhoto();

        Button editName = button("Editar meu nome", accent, this::editName);
        root.addView(editName, matchWrap());
        space(root, 16);

        streakView = text("", 18, INK, true);
        streakView.setGravity(Gravity.CENTER);
        streakView.setPadding(dp(12), dp(16), dp(12), dp(16));
        streakView.setBackgroundColor(Color.WHITE);
        root.addView(streakView, matchWrap());
        refreshStreak();
        space(root, 16);

        TextView themeLabel = text("COR DO APLICATIVO", 12, MUTED, true);
        root.addView(themeLabel);
        themeButton = button("Escolher cor", accent, this::chooseTheme);
        root.addView(themeButton, matchWrap());
        space(root, 12);

        Button openStudy = button("Abrir meu app de estudos", accent, () -> {
            startActivity(new Intent(this, MainActivity.class));
        });
        root.addView(openStudy, matchWrap());

        TextView note = text("A sequência conta dias com pelo menos uma sessão concluída. Seus dados ficam salvos neste aparelho.", 12, MUTED, false);
        note.setPadding(0, dp(18), 0, 0);
        root.addView(note);
        setContentView(root);
    }

    private String displayName() {
        String saved = store.getProfileName();
        return saved == null || saved.trim().isEmpty() ? "Estudante" : saved;
    }

    private void refreshStreak() {
        int days = store.getStudyStreak();
        if (days >= 2) streakView.setText("🔥  " + days + " dias seguidos de estudo!");
        else if (days == 1) streakView.setText("🔥  1 dia de estudo — continue amanhã!");
        else streakView.setText("Sua sequência começa com uma sessão concluída.");
    }

    private void editName() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Seu nome");
        input.setText(store.getProfileName());
        int padding = dp(20);
        input.setPadding(padding, padding, padding, padding);
        new AlertDialog.Builder(this)
                .setTitle("Editar nome")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        input.setError("Digite seu nome");
                        return;
                    }
                    store.setProfileName(name);
                    nameView.setText(name);
                }).show();
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_PHOTO || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri,
                    data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
        } catch (SecurityException ignored) {
            // Algumas fontes de imagem não oferecem permissão persistente; ainda tentamos carregar a foto.
        }
        store.setProfilePhotoUri(uri.toString());
        loadPhoto();
    }

    private void loadPhoto() {
        if (avatar == null) return;
        String savedUri = store.getProfilePhotoUri();
        if (savedUri == null || savedUri.trim().isEmpty()) {
            avatar.setImageDrawable(null);
            avatar.setBackgroundColor(themeSettings.getAccentColor());
            return;
        }
        try (InputStream stream = getContentResolver().openInputStream(Uri.parse(savedUri))) {
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            if (bitmap != null) avatar.setImageBitmap(bitmap);
        } catch (Exception ignored) {
            avatar.setImageDrawable(null);
            avatar.setBackgroundColor(themeSettings.getAccentColor());
        }
    }

    private void chooseTheme() {
        new AlertDialog.Builder(this)
                .setTitle("Escolha a cor principal")
                .setItems(themeNames, (dialog, which) -> {
                    themeSettings.setAccentColor(themeColors[which]);
                    buildScreen();
                })
                .show();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return view;
    }

    private Button button(String label, int color, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        button.setOnClickListener(view -> action.run());
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(8);
        return params;
    }

    private void space(LinearLayout parent, int height) {
        View gap = new View(this);
        parent.addView(gap, new LinearLayout.LayoutParams(1, dp(height)));
    }
}
