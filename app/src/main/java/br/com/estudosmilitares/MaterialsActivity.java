package br.com.estudosmilitares;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** Tela para listar, adicionar, editar e remover materiais de estudo. */
public class MaterialsActivity extends Activity {
    private static final int INK = Color.rgb(32, 46, 35);
    private static final int MUTED = Color.rgb(115, 128, 117);
    private static final int BG = Color.rgb(247, 248, 245);

    private AppStore store;
    private int accent;
    private LinearLayout list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new AppStore(this);
        accent = new ThemeSettings(this).getAccentColor();
        render();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private void render() {
        getWindow().setStatusBarColor(accent);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG);
        page.setPadding(dp(18), dp(18), dp(18), dp(12));

        TextView heading = text("Meus materiais", 25, INK, true);
        page.addView(heading);
        page.addView(text("Cadastre apostilas, anotações e links de estudo.", 13, MUTED, false));
        page.addView(button("＋ Adicionar material", accent, this::showEditor));

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1);
        scrollParams.topMargin = dp(12);
        page.addView(scroll, scrollParams);

        page.addView(button("Voltar", accent, this::finish));
        setContentView(page);

        JSONArray materials = store.getMaterials();
        if (materials.length() == 0) {
            list.addView(text("Ainda não há materiais. Toque em Adicionar material para começar.", 14, MUTED, false));
            return;
        }

        for (int i = 0; i < materials.length(); i++) {
            JSONObject item = materials.optJSONObject(i);
            if (item != null) addItemCard(item);
        }
    }

    private void addItemCard(JSONObject item) {
        String id = item.optString("id");
        String title = item.optString("title", "Material");
        String description = item.optString("description", "");
        String link = item.optString("link", "");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(10);
        list.addView(card, params);

        card.addView(text(title, 17, INK, true));
        if (!description.trim().isEmpty()) {
            card.addView(text(description, 13, MUTED, false));
        }
        if (!link.trim().isEmpty()) {
            card.addView(text(link, 12, Color.rgb(32, 92, 143), false));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        if (!link.trim().isEmpty()) {
            actions.addView(button("Abrir", accent, () -> openUrl(link)), weightedParams());
        }
        actions.addView(button("Editar", accent, () -> showEditor(item)), weightedParams());
        actions.addView(button("Excluir", accent, () -> deleteMaterial(id)), weightedParams());
        card.addView(actions);
    }

    private void showEditor() {
        showEditor(null);
    }

    private void showEditor(JSONObject existing) {
        boolean editing = existing != null;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), 0, dp(20), 0);

        EditText title = field("Título do material");
        EditText description = field("Descrição ou anotação");
        EditText link = field("Link (https://...) ");

        if (editing) {
            title.setText(existing.optString("title"));
            description.setText(existing.optString("description"));
            link.setText(existing.optString("link"));
        }

        form.addView(title);
        form.addView(description);
        form.addView(link);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "Editar material" : "Novo material")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newTitle = title.getText().toString().trim();
                String newDescription = description.getText().toString().trim();
                String newLink = link.getText().toString().trim();

                if (newTitle.isEmpty()) {
                    title.setError("Informe um título.");
                    return;
                }
                if (!newLink.isEmpty() && !newLink.startsWith("http://") && !newLink.startsWith("https://")) {
                    link.setError("Use link com http:// ou https://");
                    return;
                }

                boolean ok;
                if (editing) {
                    ok = store.updateMaterial(existing.optString("id"), newTitle, newDescription, newLink);
                } else {
                    ok = store.addMaterial(newTitle, newDescription, newLink) != null;
                }

                if (!ok) {
                    Toast.makeText(this, "Não foi possível salvar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                render();
            });
        });
        dialog.show();
    }

    private void deleteMaterial(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir material?")
                .setMessage("Esta ação remove o material da sua lista.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    store.deleteMaterial(id);
                    render();
                })
                .show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o link.", Toast.LENGTH_SHORT).show();
        }
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        return input;
    }

    private LinearLayout.LayoutParams weightedParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.setMargins(dp(2), dp(6), dp(2), 0);
        return params;
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
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        button.setOnClickListener(v -> action.run());
        return button;
    }
}
