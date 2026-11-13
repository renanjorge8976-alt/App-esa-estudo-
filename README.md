# Estudos Militares — Android em Java

Aplicativo Android nativo para organizar a preparação para ESA, EsPCEx e EEAR. O projeto é escrito em Java e pode ser aberto no Android Studio.

## Funcionalidades

- Painel inicial com resumo de questões, aproveitamento e horas de estudo.
- Cronograma de estudos com sessões, conclusão e exclusão.
- Questões de prática e acompanhamento de progresso.
- Perfil com nome editável e foto selecionada no aparelho.
- Personalização da cor principal (verde militar, azul, roxo ou laranja).
- Indicador de sequência de dias estudados com chama 🔥.
- Persistência local no aparelho com `SharedPreferences`.

## Organização do código

- `MainActivity.java`: telas atuais de início, cronograma, questões e progresso.
- `ProfileActivity.java`: entrada do app para editar perfil, escolher cor e consultar sequência; o botão abre a área de estudos existente.
- `AppStore.java`: persistência local de perfil, matérias, materiais e sessões, além de calendário e sequência.
- `ThemeSettings.java`: persistência da cor principal escolhida.

A sequência considera dias com ao menos uma sessão concluída. A interface de perfil mostra chama ao atingir um dia; em dois ou mais, mostra o total de dias seguidos. Marcar sessões concluídas continua sendo feito na tela de cronograma existente.

## Limitações

Os dados permanecem no aparelho e não sincronizam entre dispositivos. A tela de perfil e cores está conectada ao armazenamento local; as telas de gerenciamento de matérias e materiais e a interface visual de calendário podem ser acrescentadas nas próximas etapas.

## Executar

Abra o projeto no Android Studio, aguarde a sincronização do Gradle e execute em um emulador ou dispositivo. Requisitos: JDK 17 e Android SDK 35. As questões incluídas são exemplos de prática, não questões oficiais de concursos.
