package br.com.estudosmilitares;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/** Aplicativo de preparação para concursos militares, implementado inteiramente em Java. */
public class MainActivity extends Activity {
    private static final int INK = Color.rgb(32, 46, 35), GREEN = Color.rgb(66, 100, 71);
    private static final int MUTED = Color.rgb(125, 137, 126), BG = Color.rgb(247, 248, 245);
    private static final String[] SUBJECTS = {"Matemática", "Português", "História do Brasil", "Geografia do Brasil", "Inglês"};
    private static final String[][] QUESTIONS = {
            {"Matemática", "Uma mochila custa R$ 240,00. Com 15% de desconto, qual é o novo preço?", "R$ 196,00", "R$ 204,00", "R$ 210,00", "R$ 216,00", "1", "15% de R$ 240,00 é R$ 36,00. Portanto, R$ 240,00 − R$ 36,00 = R$ 204,00."},
            {"Português", "Na frase ‘Os soldados marcharam silenciosamente’, a palavra ‘silenciosamente’ é:", "Adjetivo", "Substantivo", "Advérbio de modo", "Preposição", "2", "A palavra modifica o verbo marcharam e indica como a ação aconteceu: é um advérbio de modo."},
            {"História do Brasil", "Em que data foi proclamada a República no Brasil?", "7 de setembro de 1822", "15 de novembro de 1889", "13 de maio de 1888", "21 de abril de 1792", "1", "A Proclamação da República ocorreu em 15 de novembro de 1889."},
            {"Geografia do Brasil", "Qual é o maior bioma brasileiro em extensão territorial?", "Cerrado", "Mata Atlântica", "Caatinga", "Amazônia", "3", "A Amazônia é o maior bioma do Brasil em extensão territorial."},
            {"Inglês", "Choose the correct translation: ‘She studies every morning.’", "Ela estudou ontem de manhã.", "Ela estuda todas as manhãs.", "Ela vai estudar amanhã.", "Ela está estudando à noite.", "1", "‘She studies’ está no presente simples e ‘every morning’ significa ‘todas as manhãs’."}
    };
    private SharedPreferences prefs;
    private LinearLayout root, content, nav;
    private int questionIndex = 0, selectedAnswer = -1;
    private boolean answered = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(Color.WHITE);
        prefs = getSharedPreferences("estudos_militares", MODE_PRIVATE);
        showPage("Início");
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private void pad(View v, int left, int top, int right, int bottom) { v.setPadding(dp(left), dp(top), dp(right), dp(bottom)); }
    private void gap(LinearLayout parent, int height) { View v = new View(this); parent.addView(v, new LinearLayout.LayoutParams(1, dp(height))); }
    private void card(LinearLayout parent, View child) {
        LinearLayout box = column(); box.setBackgroundColor(Color.WHITE); pad(box, 17, 16, 17, 16);
        box.setElevation(dp(1)); box.addView(child); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(12); parent.addView(box, p);
    }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(13);
        b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));
        b.setOnClickListener(v -> action.run()); return b;
    }
    private String today() { return new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(new Date()); }
    private JSONArray sessions() { try { return new JSONArray(prefs.getString("sessions", "[]")); } catch (Exception e) { return new JSONArray(); } }
    private JSONArray answers() { try { return new JSONArray(prefs.getString("answers", "[]")); } catch (Exception e) { return new JSONArray(); } }
    private void saveSessions(JSONArray a) { prefs.edit().putString("sessions", a.toString()).apply(); }
    private void showPage(String page) {
        root = column(); root.setBackgroundColor(BG);
        LinearLayout header = row(); header.setBackgroundColor(Color.WHITE); pad(header, 18, 12, 18, 12);
        TextView logo = text("✳  Estudos Militares", 18, INK, true); header.addView(logo, new LinearLayout.LayoutParams(0, -2, 1));
        TextView date = text(today(), 10, MUTED, false); date.setGravity(Gravity.RIGHT); header.addView(date);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(this); content = column(); pad(content, 17, 19, 17, 16); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        nav = row(); nav.setBackgroundColor(Color.WHITE); pad(nav, 4, 5, 4, 5);
        String[] pages = {"Início", "Cronograma", "Questões", "Progresso"};
        for (String item : pages) {
            TextView tab = text(item, 10, item.equals(page) ? GREEN : MUTED, item.equals(page)); tab.setGravity(Gravity.CENTER);
            nav.addView(tab, new LinearLayout.LayoutParams(0, dp(47), 1)); tab.setOnClickListener(v -> { questionIndex=0; answered=false; showPage(item); });
        }
        root.addView(nav); setContentView(root);
        if (page.equals("Início")) home(); else if (page.equals("Cronograma")) schedule(); else if (page.equals("Questões")) practice(); else progress();
    }
    private void title(String kicker, String heading, String subtitle) {
        TextView k = text("✦  " + kicker.toUpperCase(new Locale("pt", "BR")), 10, GREEN, true); content.addView(k); gap(content, 7);
        content.addView(text(heading, 26, INK, true)); gap(content, 5); content.addView(text(subtitle, 13, MUTED, false)); gap(content, 19);
    }
    private void home() {
        title("Sua jornada começa aqui", "Bom dia, futuro sargento.", "Cada sessão de hoje te deixa mais perto da aprovação.");
        LinearLayout mission = column(); mission.setBackgroundColor(Color.rgb(41,63,48)); pad(mission, 18, 18, 18, 18);
        mission.addView(text("MISSÃO DO DIA", 10, Color.rgb(207,220,184), true)); gap(mission, 8);
        mission.addView(text("Pequenos passos.\nGrandes conquistas.", 23, Color.WHITE, true)); gap(mission, 7);
        mission.addView(text("Mantenha o foco no seu objetivo. Sua aprovação é construída um dia de cada vez.", 12, Color.rgb(218,226,214), false));
        content.addView(mission); gap(content, 14);
        JSONArray a = answers(); int correct = 0; for(int i=0;i<a.length();i++) if(a.optJSONObject(i)!=null && a.optJSONObject(i).optBoolean("correct")) correct++;
        int minutes = completedMinutes(); int accuracy = a.length()==0 ? 0 : Math.round(correct*100f/a.length());
        statsCard("RESUMO DA PREPARAÇÃO", "Horas estudadas: " + minutes/60 + "h", "Questões respondidas: " + a.length() + "    ·    Aproveitamento: " + (a.length()==0 ? "—" : accuracy+"%"));
        JSONArray list = sessions(); boolean found = false;
        for(int i=0;i<list.length();i++){JSONObject s=list.optJSONObject(i);if(s!=null){found=true;break;}}
        statsCard("CONCURSO ALVO", prefs.getString("target", "ESA · Escola de Sargentos"), "Escolha seu objetivo e mantenha a constância.");
        content.addView(button("＋  Planejar estudos", () -> addStudy())); gap(content, 15);
        content.addView(text("PLANO DE HOJE", 12, INK, true)); gap(content, 8);
        if(!found) content.addView(text("Seu cronograma está vazio. Adicione uma sessão para começar.", 12, MUTED, false));
        else for(int i=0;i<list.length();i++){JSONObject s=list.optJSONObject(i);if(s!=null)content.addView(text("•  "+s.optString("time")+"  ·  "+s.optString("subject")+" — "+s.optString("topic")+(s.optBoolean("done")?"  ✓":""),12,INK,false));}
        gap(content, 14); content.addView(button("Alterar concurso alvo", this::chooseTarget));
    }
    private void statsCard(String label, String value, String detail) {
        LinearLayout c = column(); c.setBackgroundColor(Color.WHITE); pad(c, 15, 13, 15, 13);
        c.addView(text(label, 10, MUTED, true)); gap(c, 7); c.addView(text(value, 18, INK, true)); gap(c, 4); c.addView(text(detail, 11, MUTED, false));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(10); content.addView(c,p);
    }
    private int completedMinutes() { JSONArray a=sessions(); int m=0; for(int i=0;i<a.length();i++){JSONObject s=a.optJSONObject(i);if(s!=null&&s.optBoolean("done"))m+=s.optInt("duration");} return m; }
    private void schedule() {
        title("Organização é estratégia", "Seu cronograma.", "Um plano consistente faz toda a diferença.");
        content.addView(button("＋  Adicionar estudo", this::addStudy)); gap(content, 14);
        JSONArray a=sessions();
        if(a.length()==0){statsCard("SEU PLANO DE HOJE", "Ainda sem sessões", "Adicione uma matéria, horário e duração para começar.");return;}
        for(int i=0;i<a.length();i++){
            final int index=i; JSONObject s=a.optJSONObject(i); if(s==null)continue;
            LinearLayout c=column(); c.setBackgroundColor(Color.WHITE); pad(c,15,13,15,13);
            c.addView(text(s.optString("time")+"  ·  "+s.optString("subject"),14,INK,true)); gap(c,5);
            c.addView(text(s.optString("topic")+"  ·  "+s.optInt("duration")+" min",12,MUTED,false)); gap(c,10);
            LinearLayout actions=row();
            actions.addView(button(s.optBoolean("done")?"Concluído ✓":"Marcar como concluído",()->toggleSession(index)),new LinearLayout.LayoutParams(0,-2,1));
            Button delete=button("Excluir",()->deleteSession(index)); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-2,-2);dp.leftMargin=dp(7);actions.addView(delete,dp);c.addView(actions);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);content.addView(c,p);
        }
    }
    private void addStudy() {
        LinearLayout form=column(); final EditText subject=new EditText(this); subject.setHint("Matéria (ex.: Matemática)");
        final EditText topic=new EditText(this); topic.setHint("O que você vai estudar?");
        final EditText time=new EditText(this); time.setHint("Horário (ex.: 14:00)"); time.setInputType(InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_TIME);
        final EditText duration=new EditText(this); duration.setHint("Duração em minutos (ex.: 60)"); duration.setInputType(InputType.TYPE_CLASS_NUMBER);
        form.addView(subject);form.addView(topic);form.addView(time);form.addView(duration);
        new AlertDialog.Builder(this).setTitle("Nova sessão de estudo").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{
            String sub=subject.getText().toString().trim(), top=topic.getText().toString().trim(); if(sub.isEmpty()||top.isEmpty()){toast("Preencha a matéria e o conteúdo.");return;}
            JSONObject s=new JSONObject();try{s.put("subject",sub);s.put("topic",top);s.put("time",time.getText().toString().trim().isEmpty()?"14:00":time.getText().toString().trim());s.put("duration",Integer.parseInt(duration.getText().toString().trim().isEmpty()?"60":duration.getText().toString().trim()));s.put("done",false);JSONArray all=sessions();all.put(s);saveSessions(all);}catch(Exception ignored){}
            toast("Sessão adicionada ao cronograma!");showPage("Cronograma");
        }).show();
    }
    private void toggleSession(int index) {JSONArray a=sessions();JSONObject s=a.optJSONObject(index);if(s!=null){s.put("done",!s.optBoolean("done"));saveSessions(a);showPage("Cronograma");}}
    private void deleteSession(int index) {JSONArray old=sessions(), next=new JSONArray();for(int i=0;i<old.length();i++)if(i!=index)next.put(old.opt(i));saveSessions(next);showPage("Cronograma");toast("Sessão removida.");}
    private void chooseTarget() {
        String[] choices={"ESA · Escola de Sargentos","EsPCEx · Escola Preparatória","EEAR · Escola de Especialistas"};
        new AlertDialog.Builder(this).setTitle("Concurso alvo").setItems(choices,(d,which)->{prefs.edit().putString("target",choices[which]).apply();showPage("Início");}).show();
    }
    private void practice() {
        title("Pratique e evolua", "Hora de praticar.", "Uma questão por vez. Você está no caminho certo.");
        if(questionIndex>=QUESTIONS.length){questionIndex=0;}
        String[] q=QUESTIONS[questionIndex]; statsCard(q[0].toUpperCase(new Locale("pt","BR")), "QUESTÃO "+(questionIndex+1)+" DE "+QUESTIONS.length, q[1]);
        for(int i=0;i<4;i++){final int choice=i;Button b=button((char)('A'+i)+"   "+q[i+2],()->selectAnswer(choice));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(7);content.addView(b,p);}
        gap(content,8);
        if(answered){JSONArray a=answers();JSONObject last=a.optJSONObject(a.length()-1);boolean ok=last!=null&&last.optBoolean("correct");statsCard(ok?"MUITO BEM! RESPOSTA CORRETA":"QUASE! CONTINUE PRATICANDO",q[7],"A prática de hoje é o resultado de amanhã.");content.addView(button(questionIndex==QUESTIONS.length-1?"Concluir prática":"Próxima questão",()->{questionIndex=(questionIndex+1)%QUESTIONS.length;answered=false;showPage("Questões");}));}
    }
    private void selectAnswer(int choice) {
        if(answered)return; String[] q=QUESTIONS[questionIndex]; boolean correct=choice==Integer.parseInt(q[6]);
        JSONArray a=answers();JSONObject result=new JSONObject();try{result.put("subject",q[0]);result.put("correct",correct);a.put(result);}catch(Exception ignored){}
        prefs.edit().putString("answers",a.toString()).apply();selectedAnswer=choice;answered=true;showPage("Questões");
    }
    private void progress() {
        title("Cada dia mais perto", "Seu progresso.", "Veja o quanto você já conquistou na sua preparação.");
        JSONArray a=answers();int correct=0;for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null&&x.optBoolean("correct"))correct++;}
        statsCard("QUESTÕES RESPONDIDAS",String.valueOf(a.length()),"Responda questões para acompanhar sua evolução.");
        statsCard("TAXA DE ACERTO",a.length()==0?"—":Math.round(correct*100f/a.length())+"%",correct+" acertos de "+a.length()+" questões.");
        statsCard("HORAS DE ESTUDO",completedMinutes()/60+"h", "Tempo dedicado ao seu objetivo.");
        content.addView(text("DESEMPENHO POR MATÉRIA",12,INK,true));gap(content,8);
        for(String subject:SUBJECTS){int total=0,right=0;for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null&&subject.equals(x.optString("subject"))){total++;if(x.optBoolean("correct"))right++;}}statsCard(subject,total==0?"Comece a praticar":Math.round(right*100f/total)+"% de acerto",total==0?"Nenhuma questão respondida ainda":right+" de "+total+" acertos");}
    }
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
}
