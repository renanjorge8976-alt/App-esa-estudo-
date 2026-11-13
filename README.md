# Estudos Militares — Android em Java

Aplicativo Android nativo para organizar a preparação para ESA, EsPCEx e EEAR. O projeto é escrito em Java e pode ser aberto no Android Studio.

## Funcionalidades atuais

- Painel inicial com resumo de questões, aproveitamento e horas de estudo.
- Cronograma de estudos com sessões, conclusão e exclusão.
- Questões de prática com feedback e explicação.
- Progresso geral e desempenho por matéria.
- Seleção de concurso-alvo: ESA, EsPCEx ou EEAR.
- Persistência local no aparelho com `SharedPreferences`.

## Camada de dados incluída

`app/src/main/java/br/com/estudosmilitares/AppStore.java` fornece métodos para guardar e editar:

- Nome e URI da foto do perfil.
- Matérias.
- Materiais de estudo (título, descrição e link).
- Sessões do cronograma, com matéria, conteúdo, data, horário e duração.
- Calendário mensal com contagem de sessões planejadas e concluídas por dia.
- Sequência de dias estudados (`getStudyStreak`).

Exemplo de inicialização na `MainActivity`:

```java
AppStore store = new AppStore(this);
store.setProfileName("Renan");
store.addSubject("Matemática");
store.addMaterial("Apostila", "Revisão de porcentagem", "https://exemplo.com");
store.addStudySession("Matemática", "Porcentagem", "2026-11-14", "14:00", 60);
int sequencia = store.getStudyStreak();
JSONArray diasDoMes = store.getCalendarMonth(2026, 10); // mês 0-based: 10 = novembro
```

Datas do cronograma devem ser informadas no formato `yyyy-MM-dd`; horários, no formato `HH:mm`. Para calcular a sequência, marque sessões concluídas com `setStudySessionDone(id, true)`. O cálculo considera as datas das sessões concluídas.

> A camada de dados salva as informações localmente neste aparelho; ela não sincroniza automaticamente com outros aparelhos ou com um servidor. A interface atual ainda precisa chamar esses métodos para apresentar formulários de perfil, materiais e calendário ao usuário.

## Abrir e executar

1. Abra esta pasta no Android Studio (Giraffe ou mais recente).
2. Aguarde a sincronização do Gradle.
3. Execute no emulador ou em um aparelho Android.

Requisitos: JDK 17 e Android SDK 35. As questões incluídas são exemplos de prática, não questões oficiais de concursos.
