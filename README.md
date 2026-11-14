# Estudos Militares — Android em Java

Aplicativo Android nativo para organizar a preparação para ESA, EsPCEx e EEAR, feito em Java para Android.

## Funcionalidades

- Cronograma de estudos com sessão, horário, duração e conclusão.
- Materiais de estudo: cadastro, edição, exclusão e abertura de links web.
- Calendário mensal com sessões planejadas/concluídas e inclusão de sessões por dia.
- Perfil com nome e foto editáveis, cores predefinidas e sequência de dias estudados 🔥.
- Persistência local no aparelho com `SharedPreferences`.

## Testar o aplicativo no celular

Não é necessário instalar o Android Studio no celular para baixar um APK que tenha sido compilado pelo GitHub Actions.

1. Abra a página [Actions do repositório](https://github.com/renanjorge8976-alt/App-esa-estudo-/actions).
2. Escolha o workflow de compilação Android e abra uma execução concluída com sucesso (ícone verde).
3. Na página da execução, encontre a seção **Artifacts** e baixe o APK de teste. Se o download vier como ZIP, extraia o arquivo APK.
4. No Android, toque no APK para instalar. Se o sistema pedir, permita temporariamente a instalação de aplicativos dessa fonte.

Os avisos sobre versões do Node.js, `setup-java` ou Ubuntu são alertas de manutenção. Eles não significam necessariamente que a compilação falhou: confira o status final do job. Se a execução estiver vermelha, consulte o log do job para localizar o erro. Se não aparecer a seção **Artifacts**, o workflow não enviou um APK como artefato ou a compilação não terminou com sucesso.

> APKs de depuração são destinados a testes. Não instale APKs de fontes que você não confia. Para distribuir publicamente, é necessário gerar e assinar uma versão de lançamento.

## Abrir e executar pelo Android Studio

Abra o projeto no Android Studio, aguarde a sincronização do Gradle e execute no emulador ou dispositivo Android. O projeto usa JDK 17 e Android SDK 35.

## Notas

Os dados são salvos no aparelho e não sincronizam entre dispositivos. A foto do perfil é escolhida pelo seletor nativo de documentos. A sequência conta dias que tenham ao menos uma sessão concluída. As questões são exemplos de prática, não questões oficiais de concursos.

Para etapas posteriores: conectar o tema a todos os componentes da interface, adicionar fluxo completo de matérias e migrar o armazenamento para Room ou backend remoto, caso haja necessidade de sincronização.
