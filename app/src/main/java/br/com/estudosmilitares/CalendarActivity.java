package br.com.estudosmilitares;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Calendário mensal integrado ao cronograma de estudo.
 */
public class CalendarActivity extends Activity {
    private static final int INK = Color.rgb(32, 46, 35);
    private static final int MUTED = Color.rgb(115, 128, 117);
    private static final int BG = Color.rgb(247, 248, 245);

    private final Calendar month = Calendar.getInstance();
    private AppStore store;
    private int accent;
    private LinearLayout daysGrid;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        month.set(Calendar.DAY_OF_MONTH, 1);
        store = new AppStore(this);
        accent = new ThemeSettings(this).getAccentColor();
        render();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private void render() {
        getWindow().setStatusBarColor(accent);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(12));
        root.setBackgroundColor(BG);

        TextView header = text("Calendário de estudos", 24, INK, true);
        root.addView(header);
        root.addView(text("Veja o que foi planejado e concluído por dia.", 13, MUTED, false));

        LinearLayout monthBar = new LinearLayout(this);
        monthBar.setOrientation(LinearLayout.HORIZONTAL);
        monthBar.setGravity(Gravity.CENTER_VERTICAL);
        Button previous = button("‹", accent, () -> {
            month.add(Calendar.MONTH, -1);
            render();
        });
        Button next = button("›", accent, () -> {
            month.add(Calendar.MONTH, 1);
            render();
        });
        TextView monthLabel = text(
                month.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("pt", "BR")) + " " + month.get(Calendar.YEAR),
                18,
                INK,
                true
        );
        monthLabel.setGravity(Gravity.CENTER);
        monthBar.addView(previous);
        monthBar.addView(monthLabel, new LinearLayout.LayoutParams(0, -2, 1));
        monthBar.addView(next);
        root.addView(monthBar);

        LinearLayout weekdays = new LinearLayout(this);
        weekdays.setOrientation(LinearLayout.HORIZONTAL);
        String[] dayNames = {"S", "T", "Q", "Q", "S", "S", "D"};
        for (String name : dayNames) {
            TextView cell = text(name, 12, MUTED, true);
            cell.setGravity(Gravity.CENTER);
            weekdays.addView(cell, new LinearLayout.LayoutParams(0, dp(34), 1));
        }
        root.addView(weekdays);

        ScrollView scroll = new ScrollView(this);
        daysGrid = new LinearLayout(this);
        daysGrid.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(daysGrid);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        renderMonthGrid();
        root.addView(button("Voltar", accent, this::finish));
        setContentView(root);
    }

    private void renderMonthGrid() {
        daysGrid.removeAllViews();
        Calendar firstDay = (Calendar) month.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);

        int startOffset = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH);

        LinearLayout currentWeek = new LinearLayout(this);
        currentWeek.setOrientation(LinearLayout.HORIZONTAL);
        int cells = 0;

        for (int i = 0; i < startOffset; i++) {
            currentWeek.addView(emptyCell(), new LinearLayout.LayoutParams(0, dp(68), 1));
            cells++;
        }

        JSONArray monthData = store.getCalendarMonth(month.get(Calendar.YEAR), month.get(Calendar.MONTH));

        for (int day = 1; day <= daysInMonth; day++) {
            int finalDay = day;
            int planned = 0;
            int completed = 0;
            if (monthData.length() >= day) {
                JSONObject info = monthData.optJSONObject(day - 1);
                if (info != null) {
                    planned = info.optInt("planned");
                    completed = info.optInt("completed");
                }
            }

            String label = String.valueOf(day);
            if (planned > 0) {
                label += "\n" + (completed >= planned ? "✓" : "•") + " " + planned;
            }

            Button cell = button(label, accent, () -> showDay(finalDay));
            cell.setMinHeight(dp(68));
            cell.setTextSize(12);
            cell.setPadding(0, 0, 0, 0);
            currentWeek.addView(cell, new LinearLayout.LayoutParams(0, dp(68), 1));
            cells++;
            if (cells % 7 == 0) {
                daysGrid.addView(currentWeek);
                currentWeek = new LinearLayout(this);
                currentWeek.setOrientation(LinearLayout.HORIZONTAL);
            }
        }

        while (cells % 7 != 0) {
            currentWeek.addView(emptyCell(), new LinearLayout.LayoutParams(0, dp(68), 1));
            cells++;
        }
        if (currentWeek.getChildCount() > 0) {
            daysGrid.addView(currentWeek);
        }

        Button streak = button("🔥  " + store.getStudyStreak() + " dias seguidos", accent, null);
        streak.setEnabled(false);
        streak.setTextColor(Color.WHITE);
        daysGrid.addView(streak, new LinearLayout.LayoutParams(-1, -2));
    }

    private View emptyCell() {
        TextView empty = new TextView(this);
        empty.setHeight(dp(68));
        return empty;
    }

    private void showDay(int dayOfMonth) {
        Calendar selected = (Calendar) month.clone();
        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.getTime());
        JSONArray sessions = store.getStudySessionsForDate(date);

        StringBuilder message = new StringBuilder();
        if (sessions.length() == 0) {
            message.append("Nenhuma sessão planejada para este dia.");
        } else {
            for (int i = 0; i < sessions.length(); i++) {
                JSONObject item = sessions.optJSONObject(i);
                if (item == null) continue;
                message.append(item.optString("time", "--:--"))
                        .append(" · ")
                        .append(item.optString("subject"))
                        .append("\n")
                        .append(item.optString("topic"));
                if (item.optBoolean("done")) {
                    message.append("\n✓ concluída");
                } else {
                    message.append("\n• planejada");
                }
                if (i < sessions.length() - 1) message.append("\n\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(selected.getTime()))
                .setMessage(message.toString())
                .setNeutralButton("＋ Adicionar sessão", (dialog, which) -> addSession(date))
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void addSession(String date) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), 0, dp(18), 0);

        EditText subject = field("Matéria");
        EditText topic = field("Conteúdo");
        EditText time = field("Horário (HH:mm)");
        EditText duration = field("Duração em minutos");
        duration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        form.addView(subject);
        form.addView(topic);
        form.addView(time);
        form.addView(duration);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Adicionar sessão")
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String sub = subject.getText().toString().trim();
                String top = topic.getText().toString().trim();
                String timeValue = time.getText().toString().trim();
                int minutes;
                try {
                    minutes = Integer.parseInt(duration.getText().toString().trim());
                } catch (NumberFormatException e) {
                    duration.setError("Informe a duração em minutos.");
                    return;
                }
                if (sub.isEmpty()) {
                    subject.setError("Informe a matéria.");
                    return;
                }
                if (top.isEmpty()) {
                    topic.setError("Informe o conteúdo.");
                    return;
                }
                if (store.addStudySession(sub, top, date, timeValue, minutes) == null) {
                    time.setError("Use horário no formato HH:mm e duração entre 1 e 1440 minutos.");
                    return;
                }
                dialog.dismiss();
                render();
                Toast.makeText(this, "Sessão adicionada ao calendário.", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        return input;
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
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        button.setOnClickListener(v -> {
            if (action != null) action.run();
        });
        return button;
    }
}
