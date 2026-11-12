# Arquitetura

## 1. Visão geral

O Auris é um aplicativo Android predominantemente Kotlin, com interface construída em Jetpack Compose e Material 3. O módulo principal concentra a maior parte da lógica de produto. A arquitetura separa, por convenção de pacotes, apresentação, acesso a dados, serviços Android, integrações e utilitários.

O projeto usa Hilt para injeção de dependências, Room para persistência relacional local, DataStore para preferências e AndroidX Media3 para a reprodução de mídia.

```text
Interface Compose
      |
      v
Screens / Navigation
      |
      v
ViewModels e StateHolders
      |
      v
Repositories / Managers
      |             |
      v             v
Room / DataStore   APIs / MediaStore / serviços
      |
      v
Modelos de domínio e dados

MusicService (MediaLibraryService)
      |
      +--> Media3 / ExoPlayer
      +--> notificações e controles externos
      +--> Cast / Wear / widgets
```

O diagrama é conceitual: alguns componentes conversam diretamente com serviços ou repositórios especializados, e nem todo fluxo atravessa todas as camadas.

## 2. Módulos Gradle

Os módulos incluídos por `settings.gradle.kts` são `:app`, `:shared`, `:wear` e `:baselineprofile`.

- **`:app`** — aplicação Android principal. Contém interface, biblioteca, reprodução, banco local, integrações e serviços.
- **`:shared`** — biblioteca Android com contratos e modelos serializáveis compartilhados entre telefone e Wear OS. O módulo define `minSdk 29`, `compileSdk 35` e não deve depender de APIs específicas do app.
- **`:wear`** — aplicação Wear OS. Tem interface própria, persistência local para músicas transferidas e comunicação com o telefone. O módulo declara `minSdk 30`.
- **`:baselineprofile`** — módulo de geração/uso de Baseline Profiles para otimização de inicialização e execução.

`remote-config`, `roku`, `assets` e `tools` também existem na raiz, mas não aparecem como módulos incluídos no `settings.gradle.kts` deste snapshot. Não devem ser tratados como módulos Gradle ativos sem uma alteração explícita da configuração.

## 3. Camada de apresentação

Local principal: `app/src/main/java/com/goldensystem/auris/presentation/`.

Responsabilidades:

- `screens/`: telas e composição da interface.
- `components/`: componentes Compose reutilizáveis.
- `navigation/`: definição e extensões de navegação.
- `viewmodel/`: ViewModels e StateHolders que mantêm estado da interface e coordenam operações.
- `model/`: modelos usados na apresentação.
- `telegram/`, `gdrive/`, `jellyfin/`, `navidrome/`, `netease/` e `qqmusic/`: fluxos de interface específicos de autenticação ou integração.

A interface principal é iniciada por `MainActivity.kt`. A navegação Compose está em `presentation/navigation/AppNavigation.kt`. A lógica de estado não está concentrada em um único ViewModel: há vários StateHolders especializados, por exemplo `LibraryStateHolder`, `PlaybackStateHolder`, `QueueStateHolder`, `ThemeStateHolder` e `LyricsStateHolder`.

## 4. Camada de dados

Local principal: `.../auris/data/`.

O pacote `data` contém:

- `database/`: entidades Room, DAOs, conversores e `AurisDatabase`.
- `repository/`: contratos e implementações que agregam fontes de dados.
- `preferences/`: repositórios e modelos de preferências.
- `network/`: serviços HTTP e modelos de APIs.
- `service/`: serviços Android e componentes de reprodução.
- `worker/`: tarefas em segundo plano com WorkManager.
- `ai/`: orquestração de IA e clientes de provedores.
- `backup/`: exportação, validação e restauração de dados.
- `telegram/`, `gdrive/`, `navidrome/`, `jellyfin/`, `netease/`, `qqmusic/`: repositórios e proxies de streaming por origem.

## 5. Inicialização e dependências

`AurisApplication.kt` é a classe `Application` e está anotada com `@HiltAndroidApp`. Ela configura componentes globais do processo, incluindo configuração de WorkManager com `HiltWorkerFactory` e integração com o carregamento de imagens.

`MainActivity.kt` é a Activity de entrada e monta a interface Compose. O manifesto registra também `ExternalPlayerActivity`, `MusicService`, serviços auxiliares, receivers, widgets e providers.

Os módulos de injeção estão em `di/`:

- `AppModule.kt`: fornece dependências centrais, como banco Room, DataStore, cliente HTTP, player e repositórios.
- `DatabaseModule.kt`: fornece a base local adicional `AppDatabase`.
- `BackupModule.kt`: dependências de backup.
- `FirebaseMudule.kt`: configuração relacionada ao Firebase (o nome do arquivo está grafado assim no repositório).
- `AiEntryPoint.kt` e `Qualifiers.kt`: pontos de entrada e qualificadores específicos.

A maior parte das dependências é declarada como singleton quando o ciclo de vida deve abranger o processo. Consulte os escopos nas próprias funções `@Provides`/`@Inject` antes de alterar o ciclo de vida.

## 6. Estado e concorrência

O projeto usa Kotlin Coroutines e Flow para operações assíncronas e observação de dados. Os ViewModels usam `viewModelScope`; há também um escopo de aplicação qualificado como `@AppScope` em `AppModule.kt`.

Ao modificar código assíncrono:
1. Preserve o dispatcher adequado para I/O e trabalho de CPU.
2. Evite bloquear a thread principal com consultas, leitura de arquivos ou rede.
3. Prefira os `Flow` já oferecidos por repositórios e DAOs quando a interface precisa reagir a alterações.
4. Respeite o ciclo de vida de serviços de reprodução: a música pode continuar tocando sem a Activity visível.

## 7. Fronteiras importantes

- A Activity não é o dono do ciclo de vida da reprodução; `MusicService` mantém a sessão de mídia.
- A biblioteca local usa MediaStore e Room em conjunto; não assuma que toda informação exibida vem diretamente de uma tabela.
- Preferências e dados relacionais têm persistências diferentes: DataStore e Room não são intercambiáveis.
- As integrações remotas têm repositórios/proxies próprios; evite inserir lógica de um provedor no repositório genérico da biblioteca sem necessidade.
