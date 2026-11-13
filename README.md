# Estudos Militares — Android em Java

Aplicativo Android nativo para organizar a preparação para ESA, EsPCEx e EEAR. O projeto é escrito em Java e pode ser aberto no Android Studio.

## Funcionalidades atuais

- Painel inicial com resumo de questões, aproveitamento e horas de estudo.
- Cronograma de estudos com sessões, conclusão e exclusão.
- Questões de prática com feedback e explicação.
- Progresso geral e desempenho por matéria.
- Seleção de concurso-alvo: ESA, EsPCEx ou EEAR.
- Persistência local no aparelho com `SharedPreferences`.

## Camada de dados local

`app/src/main/java/br/com/estudosmilitares/AppStore.java` fornece operações para perfil, matérias, materiais de estudo e cronograma:

- Nome e URI da foto do perfil.
- Criar, editar e remover matérias.
- Criar, editar e remover materiais (título, descrição e link).
- Sessões de estudo com matéria, conteúdo, data, horário, duração e status.
- Consultar sessões por data e preencher dados do calendário mensal.
- Calcular sequência de dias com sessão concluída.

Exemplo de uso:

```java
AppStore store = new AppStore(this);
store.setProfileName("Renan");
store.addSubject("Matemática");
store.addMaterial("Apostila", "Revisão de porcentagem", "https://exemplo.com");
store.addStudySession("Matemática", "Porcentagem", "2026-11-14", "14:00", 60);
int sequencia = store.getStudyStreak();
JSONArray diasDoMes = store.getCalendarMonth(2026, 10); // mês 0-based: 10 = novembro
```

Datas devem usar `yyyy-MM-dd`, horários `HH:mm` e a duração deve ser de 1 a 1440 minutos. Para contar um dia na sequência, conclua a sessão com `setStudySessionDone(id, true)`.

## Limitações e próximos passos

Esta é uma camada de dados local para um MVP/offline de um único aparelho; não é um servidor e não sincroniza dados entre dispositivos. A interface precisa chamar `AppStore` para oferecer as telas de perfil, materiais e calendário. Para uso com contas, sincronização e recuperação de dados, o próximo passo é integrar um backend remoto com autenticação, API e banco de dados. Para grandes volumes e consultas complexas, também é recomendável migrar as listas JSON para Room/SQLite.

A foto é guardada como URI. Ao escolher uma imagem, use o seletor de fotos do Android e persista a permissão de leitura da URI para que o acesso continue após reiniciar o app.

## Abrir e executar

1. Abra esta pasta no Android Studio (Giraffe ou mais recente).
2. Aguarde a sincronização do Gradle.
3. Execute no emulador ou em um aparelho Android.

Requisitos: JDK 17 e Android SDK 35. As questões incluídas são exemplos de prática, não questões oficiais de concursos.
