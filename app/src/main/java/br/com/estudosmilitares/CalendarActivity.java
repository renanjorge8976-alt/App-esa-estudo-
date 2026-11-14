package br.com.estudosmilitares;

import android.app.Activity;
import android.app.AlertDialog;
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

/** Calendário mensal de estudos ligado ao cronograma local. */
public class CalendarActivity extends Activity {
    private static final int INK = Color.rgb(32, 46, 35);
    private static final int MUTED = Color.rgb(115, 128, 117);
    private static final int BG = Color.rgb(247, 248, 245);

    private final Calendar month = Calendar.getInstance();
    private AppStore store;
    private int accent;
    private LinearLayout page;
    private LinearLayout daysGrid;
    private TextView monthTitle;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new AppStore(this);
        accent = new ThemeSettings(this).getAccentColor();
        month.set(Calendar.DAY_OF_MONTH, 1);
        render();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private void render() {
        getWindow().setStatusBarColor(accent);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG);
        page.setPadding(dp(16), dp(16), dp(16), dp(12));

        TextView heading = text("Calendário de estudos", 24, INK, true);
        page.addView(heading);
        page.addView(text("Toque em um dia para ver ou adicionar sessões.", 13, MUTED, false));

        LinearLayout monthRow = new LinearLayout(this);
        monthRow.setGravity(Gravity.CENTER_VERTICAL);
        Button previous = button("‹", () -> {
            month.add(Calendar.MONTH, -1);
            render();
        });
        Button next = button("›", () -> {
            month.add(Calendar.MONTH, 1);
            render();
        });
        monthRow.addView(previous);
        monthTitle = text(month.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("pt", "BR"))
                + " " + month.get(Calendar.YEAR), 18, INK, true);
        monthTitle.setGravity(Gravity.CENTER);
        monthRow.addView(monthTitle, new LinearLayout.LayoutParams(0, -2, 1));
        monthRow.addView(next);
        page.addView(monthRow);

        LinearLayout weekdays = new LinearLayout(this);
        String[] weekdayNames = {"S", "T", "Q", "Q", "S", "S", "D"};
        for (String name : weekdayNames) {
            TextView label = text(name, 13, MUTED, true);
            label.setGravity(Gravity.CENTER);
            weekdays.addView(label, new LinearLayout.LayoutParams(0, dp(36), 1));
        }
        page.addView(weekdays);

        daysGrid = new LinearLayout(this);
        daysGrid.setOrientation(LinearLayout.VERTICAL);
        page.addView(daysGrid);
        populateGrid();

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        setContentView(scroll);
    }

    private void populateGrid() {
        daysGrid.removeAllViews();
        JSONArray data = store.getCalendarMonth(month.get(Calendar.YEAR), month.get(Calendar.MONTH));
        Calendar first = (Calendar) month.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        int offset = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7; // segunda-feira = coluna 0
        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);
        int cellIndex = 0;
        LinearLayout week = new LinearLayout(this);
        week.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < offset; i++) {
            week.addView(new TextView(this), new LinearLayout.LayoutParams(0, dp(68), 1));
            cellIndex++;
        }
        for (int day = 1; day <= daysInMonth; day++) {
            final int selectedDay = day;
            JSONObject info = data.optJSONObject(day - 1);
            int planned = info == null ? 0 : info.optInt("planned");
            int done = info == null ? 0 : info.optInt("completed");
            String label = String.valueOf(day);
            if (planned > 0) label += "\n" + (done == planned ? "✓ " : "• ") + planned;
            Button cell = button(label, () -> showDay(selectedDay));
            cell.setTextSize(12);
            cell.setMinHeight(dp(58));
            cell.setPadding(dp(1), dp(2), dp(1), dp(2));
            week.addView(cell, new LinearLayout.LayoutParams(0, dp(68), 1));
            cellIndex++;
            if (cellIndex % 7 == 0) {
                daysGrid.addView(week);
                week = new LinearLayout(this);
                week.setOrientation(LinearLayout.HORIZONTAL);
            }
        }
        if (cellIndex % 7 != 0) {
            while (cellIndex % 7 != 0) {
                week.addView(new TextView(this), new LinearLayout.LayoutParams(0, dp(68), 1));
                cellIndex++;
            }
            daysGrid.addView(week);
        }

        Button streak = button("🔥  " + store.getStudyStreak() + " dias seguidos", () -> { });
        streak.setClickable(false);
        daysGrid.addView(streak);
        Button back = button("Voltar", this::finish);
        daysGrid.addView(back);
    }

    private void showDay(int dayOfMonth) {
        Calendar selected = (Calendar) month.clone();
        selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.getTime());
        JSONArray sessions = store.getStudySessionsForDate(date);
        StringBuilder message = new StringBuilder();
        if (sessions.length() == 0) {
            message.append("Nenhuma sessão planejada.");
        } else {
            for (int i = 0; i < sessions.length(); i++) {
                JSONObject session = sessions.optJSONObject(i);
                if (session == null) continue;
                message.append(session.optString("time", "--:--"))
                        .append(" · ").append(session.optString("subject"))
                        .append("\n").append(session.optString("topic"))
                        .append(session.optBoolean("done") ? "  ✓ concluída" : "  · planejada");
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
        form.setPadding(dp(20), 0, dp(20), 0);
        form.setOrientation(LinearLayout.VERTICAL);
        EditText subject = field("Matéria");
        EditText topic = field("Conteúdo para estudar");
        EditText time = field("Horário (HH:mm), ex.: 14:00");
        EditText duration = field("Duração em minutos, ex.: 60");
        duration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        form.addView(subject);
        form.addView(topic);
        form.addView(time);
        form.addView(duration);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Adicionar sessão · " + date)
                .setView(form)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String sub = subject.getText().toString().trim();
            String top = topic.getText().toString().trim();
            String clock = time.getText().toString().trim();
            int minutes;
            try {
                minutes = Integer.parseInt(duration.getText().toString().trim());
            } catch (NumberFormatException e) {
                duration.setError("Informe a duração em minutos");
                return;
            }
            if (sub.isEmpty()) { subject.setError("Informe a matéria"); return; }
            if (top.isEmpty()) { topic.setError("Informe o conteúdo"); return; }
            if (store.addStudySession(sub, top, date, clock, minutes) == null) {
                time.setError("Confira o horário HH:mm e a duração (1 a 1440 minutos)");
                return;
            }
            dialog.dismiss();
            render();
            toast("Sessão adicionada ao calendário.");
        }));
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
