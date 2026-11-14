# Estudos Militares — Android em Java

Aplicativo Android nativo para organizar a preparação para ESA, EsPCEx e EEAR, feito em Java para Android.

## Funcionalidades

- Cronograma de estudos com sessão, horário, duração e conclusão.
- Materiais de estudo: cadastro, edição, exclusão e abertura de links web.
- Calendário mensal com sessões planejadas/concluídas e inclusão de sessões por dia.
- Perfil com nome e foto editáveis, cores predefinidas e sequência de dias estudados 🔥.
- Persistência local no aparelho com `SharedPreferences`.

## Abrir e executar

Abra o projeto no Android Studio, aguarde a sincronização do Gradle e execute no emulador ou dispositivo Android. O projeto usa JDK 17 e Android SDK 35.

## Notas

Os dados são salvos no aparelho e não sincronizam entre dispositivos. A foto do perfil é escolhida pelo seletor nativo de documentos. A sequência conta dias que tenham ao menos uma sessão concluída. As questões são exemplos de prática, não questões oficiais de concursos.

Para etapas posteriores: conectar o tema a todos os componentes da interface, adicionar fluxo completo de matérias e migrar o armazenamento para Room ou backend remoto, caso haja necessidade de sincronização.
