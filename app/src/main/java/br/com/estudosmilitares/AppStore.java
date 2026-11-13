package br.com.estudosmilitares;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Camada de dados local do Estudos Militares.
 * Adequada para um MVP/offline de um único aparelho. Para login e sincronização
 * entre aparelhos, os dados devem ser migrados para um serviço remoto.
 * Datas usam yyyy-MM-dd e horários HH:mm.
 */
public final class AppStore {
    private static final String PREFS = "estudos_militares";
    private static final String SUBJECTS_KEY = "custom_subjects";
    private static final String MATERIALS_KEY = "study_materials";
    // Mantém compatibilidade com as sessões já salvas pela MainActivity.
    private static final String SESSIONS_KEY = "sessions";
    private static final int MAX_DURATION_MINUTES = 24 * 60;

    private final SharedPreferences prefs;

    public AppStore(Context context) {
        if (context == null) throw new IllegalArgumentException("Context não pode ser nulo");
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // Perfil
    public String getProfileName() {
        return prefs.getString("profile_name", "");
    }

    public void setProfileName(String name) {
        prefs.edit().putString("profile_name", name == null ? "" : name.trim()).apply();
    }

    /** Guarde a URI persistente devolvida pelo seletor de imagens do Android. */
    public String getProfilePhotoUri() {
        return prefs.getString("profile_photo_uri", null);
    }

    public void setProfilePhotoUri(String uri) {
        if (isBlank(uri)) prefs.edit().remove("profile_photo_uri").apply();
        else prefs.edit().putString("profile_photo_uri", uri.trim()).apply();
    }

    // Matérias
    public JSONArray getSubjects() {
        return readArray(SUBJECTS_KEY);
    }

    public JSONObject addSubject(String name) {
        if (isBlank(name)) return null;
        JSONObject item = new JSONObject();
        try {
            item.put("id", newId());
            item.put("name", name.trim());
            append(SUBJECTS_KEY, item);
            return item;
        } catch (JSONException e) {
            return null;
        }
    }

    public boolean updateSubject(String id, String name) {
        if (isBlank(name)) return false;
        JSONObject changes = new JSONObject();
        try {
            changes.put("name", name.trim());
            return update(SUBJECTS_KEY, id, changes);
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean deleteSubject(String id) {
        return delete(SUBJECTS_KEY, id);
    }

    // Materiais (apostilas, links, anotações etc.)
    public JSONArray getMaterials() {
        return readArray(MATERIALS_KEY);
    }

    public JSONObject addMaterial(String title, String description, String link) {
        if (isBlank(title)) return null;
        JSONObject item = new JSONObject();
        try {
            item.put("id", newId());
            item.put("title", title.trim());
            item.put("description", safe(description));
            item.put("link", safe(link));
            append(MATERIALS_KEY, item);
            return item;
        } catch (JSONException e) {
            return null;
        }
    }

    public boolean updateMaterial(String id, String title, String description, String link) {
        if (isBlank(title)) return false;
        JSONObject changes = new JSONObject();
        try {
            changes.put("title", title.trim());
            changes.put("description", safe(description));
            changes.put("link", safe(link));
            return update(MATERIALS_KEY, id, changes);
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean deleteMaterial(String id) {
        return delete(MATERIALS_KEY, id);
    }

    // Cronograma
    public JSONArray getStudySessions() {
        return readArray(SESSIONS_KEY);
    }

    /** Cria uma sessão para uma data específica (yyyy-MM-dd). */
    public JSONObject addStudySession(String subject, String topic, String date,
                                       String time, int durationMinutes) {
        if (isBlank(subject) || isBlank(topic) || !isValidDate(date)
                || !isValidTime(time) || !isValidDuration(durationMinutes)) return null;
        JSONObject item = new JSONObject();
        try {
            item.put("id", newId());
            item.put("subject", subject.trim());
            item.put("topic", topic.trim());
            item.put("date", date.trim());
            item.put("time", time.trim());
            // Mantém o nome "duration" usado pela MainActivity existente.
            item.put("duration", durationMinutes);
            item.put("done", false);
            append(SESSIONS_KEY, item);
            return item;
        } catch (JSONException e) {
            return null;
        }
    }

    public boolean updateStudySession(String id, String subject, String topic,
                                      String date, String time, int durationMinutes) {
        if (isBlank(subject) || isBlank(topic) || !isValidDate(date)
                || !isValidTime(time) || !isValidDuration(durationMinutes)) return false;
        JSONObject changes = new JSONObject();
        try {
            changes.put("subject", subject.trim());
            changes.put("topic", topic.trim());
            changes.put("date", date.trim());
            changes.put("time", time.trim());
            changes.put("duration", durationMinutes);
            return update(SESSIONS_KEY, id, changes);
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean setStudySessionDone(String id, boolean done) {
        JSONObject changes = new JSONObject();
        try {
            changes.put("done", done);
            return update(SESSIONS_KEY, id, changes);
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean deleteStudySession(String id) {
        return delete(SESSIONS_KEY, id);
    }

    /** Retorna sessões da data solicitada (yyyy-MM-dd). */
    public JSONArray getStudySessionsForDate(String date) {
        JSONArray result = new JSONArray();
        if (!isValidDate(date)) return result;
        JSONArray sessions = getStudySessions();
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.optJSONObject(i);
            if (session != null && date.equals(session.optString("date"))) result.put(session);
        }
        return result;
    }

    /**
     * Retorna todos os dias do mês e a quantidade de sessões planejadas/concluídas.
     * month segue java.util.Calendar: janeiro = 0, dezembro = 11.
     */
    public JSONArray getCalendarMonth(int year, int month) {
        JSONArray result = new JSONArray();
        if (month < Calendar.JANUARY || month > Calendar.DECEMBER
                || year < 1 || year > 9999) return result;

        Calendar cursor = Calendar.getInstance();
        cursor.clear();
        cursor.set(year, month, 1);
        int dayCount = cursor.getActualMaximum(Calendar.DAY_OF_MONTH);
        JSONArray sessions = getStudySessions();

        for (int day = 1; day <= dayCount; day++) {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            int planned = 0;
            int completed = 0;
            for (int i = 0; i < sessions.length(); i++) {
                JSONObject session = sessions.optJSONObject(i);
                if (session != null && date.equals(session.optString("date"))) {
                    planned++;
                    if (session.optBoolean("done")) completed++;
                }
            }
            JSONObject entry = new JSONObject();
            try {
                entry.put("date", date);
                entry.put("planned", planned);
                entry.put("completed", completed);
                result.put(entry);
            } catch (JSONException ignored) {
                // Continua montando os demais dias do calendário.
            }
        }
        return result;
    }

    /** Número de dias consecutivos com ao menos uma sessão concluída, incluindo hoje. */
    public int getStudyStreak() {
        Set<String> completedDays = new HashSet<>();
        JSONArray sessions = getStudySessions();
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.optJSONObject(i);
            if (session != null && session.optBoolean("done")) {
                String date = session.optString("date", "");
                if (isValidDate(date)) completedDays.add(date);
            }
        }

        Calendar day = Calendar.getInstance();
        if (!completedDays.contains(formatDate(day.getTime()))) {
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        int streak = 0;
        while (completedDays.contains(formatDate(day.getTime()))) {
            streak++;
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private JSONArray readArray(String key) {
        try {
            return new JSONArray(prefs.getString(key, "[]"));
        } catch (JSONException e) {
            // Não sobrescreve os dados corrompidos até que uma alteração válida seja salva.
            return new JSONArray();
        }
    }

    private void append(String key, JSONObject item) {
        JSONArray array = readArray(key);
        array.put(item);
        prefs.edit().putString(key, array.toString()).apply();
    }

    private boolean update(String key, String id, JSONObject changes) {
        if (isBlank(id)) return false;
        JSONArray array = readArray(key);
        for (int i = 0; i < array.length(); i++) {
            JSONObject current = array.optJSONObject(i);
            if (current != null && id.equals(current.optString("id"))) {
                try {
                    java.util.Iterator<String> keys = changes.keys();
                    while (keys.hasNext()) {
                        String field = keys.next();
                        if (!"id".equals(field)) current.put(field, changes.get(field));
                    }
                    array.put(i, current);
                    prefs.edit().putString(key, array.toString()).apply();
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean delete(String key, String id) {
        if (isBlank(id)) return false;
        JSONArray oldArray = readArray(key);
        JSONArray newArray = new JSONArray();
        boolean removed = false;
        for (int i = 0; i < oldArray.length(); i++) {
            JSONObject item = oldArray.optJSONObject(i);
            if (item != null && id.equals(item.optString("id"))) removed = true;
            else newArray.put(oldArray.opt(i));
        }
        if (removed) prefs.edit().putString(key, newArray.toString()).apply();
        return removed;
    }

    private String newId() {
        return UUID.randomUUID().toString();
    }

    private boolean isValidDate(String value) {
        if (isBlank(value)) return false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date date = format.parse(value, position);
        return date != null && position.getIndex() == value.length();
    }

    private boolean isValidTime(String value) {
        if (isBlank(value)) return false;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date parsed = format.parse(value.trim(), position);
        return parsed != null && position.getIndex() == value.trim().length();
    }

    private boolean isValidDuration(int minutes) {
        return minutes >= 1 && minutes <= MAX_DURATION_MINUTES;
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
