package br.com.estudosmilitares;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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

/** Tela nativa para criar, editar, abrir e remover materiais de estudo. */
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
        TextView help = text("Cadastre apostilas, anotações e links de estudo.", 13, MUTED, false);
        page.addView(help);
        page.addView(button("＋  Adicionar material", this::showEditor));

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1);
        scrollParams.topMargin = dp(12);
        page.addView(scroll, scrollParams);
        page.addView(button("Voltar", this::finish));
        setContentView(page);

        JSONArray materials = store.getMaterials();
        if (materials.length() == 0) {
            list.addView(text("Ainda não há materiais. Toque em Adicionar material para começar.", 14, MUTED, false));
            return;
        }
        for (int i = 0; i < materials.length(); i++) {
            JSONObject item = materials.optJSONObject(i);
            if (item != null) addMaterialCard(item);
        }
    }

    private void addMaterialCard(JSONObject item) {
        String id = item.optString("id");
        String title = item.optString("title", "Material");
        String description = item.optString("description", "");
        String link = item.optString("link", "");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setElevation(dp(1));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(10);
        list.addView(card, cardParams);
        card.addView(text(title, 17, INK, true));
        if (!description.trim().isEmpty()) card.addView(text(description, 13, MUTED, false));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        card.addView(actions);
        if (!link.trim().isEmpty()) actions.addView(button("Abrir link", () -> openLink(link)), weighted());
        actions.addView(button("Editar", () -> showEditor(item), weighted()));
        actions.addView(button("Excluir", () -> confirmDelete(id), weighted());
    }

    private void showEditor() {
        showEditor(null);
    }

    private void showEditor(JSONObject existing) {
        boolean editing = existing != null;
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(4), dp(20), 0);
        EditText title = field("Título do material");
        EditText description = field("Descrição ou anotação");
        EditText link = field("Link (opcional, https://...)");
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
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newTitle = title.getText().toString().trim();
            String newDescription = description.getText().toString().trim();
            String newLink = link.getText().toString().trim();
            if (newTitle.isEmpty()) {
                title.setError("Informe um título");
                return;
            }
            if (!newLink.isEmpty() && !isWebLink(newLink)) {
                link.setError("Use um link começando com http:// ou https://");
                return;
            }
            boolean saved;
            if (editing) {
                saved = store.updateMaterial(existing.optString("id"), newTitle, newDescription, newLink);
            } else {
                saved = store.addMaterial(newTitle, newDescription, newLink) != null;
            }
            if (!saved) {
                toast("Não foi possível salvar o material.");
                return;
            }
            dialog.dismiss();
            render();
        }));
        dialog.show();
    }

    private void confirmDelete(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir material?")
                .setMessage("Esta ação não pode ser desfeita.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d, w) -> {
                    store.deleteMaterial(id);
                    render();
                }).show();
    }

    private void openLink(String value) {
        if (!isWebLink(value)) {
            toast("Este material não tem um link web válido.");
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(value)));
        } catch (Exception e) {
            toast("Não foi possível abrir o link.");
        }
    }

    private boolean isWebLink(String value) {
        String lower = value.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://");
    }

    private EditText field(String hint) {
        EditText edit = new EditText(this);
        edit.setHint(hint);
        return edit;
    }

    private LinearLayout.LayoutParams weighted() {
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

    private Button button(String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
