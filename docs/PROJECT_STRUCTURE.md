# Estrutura do projeto

## Raiz

| Caminho | Papel observado |
|---|---|
| `app/` | Aplicativo Android principal |
| `shared/` | Contratos e modelos comuns ao telefone e Wear OS |
| `wear/` | Aplicativo para Wear OS |
| `baselineprofile/` | Configuração de Baseline Profile |
| `assets/` | Recursos auxiliares do projeto |
| `remote-config/` | Arquivos de configuração remota; não incluído como módulo em `settings.gradle.kts` |
| `roku/` | Código/recursos Roku mantidos no repositório |
| `tools/` | Ferramentas auxiliares |
| `.github/workflows/` | Pipelines de build e release |
| `gradle/` | Wrapper e catálogo de versões/dependências |

## Módulo `app`

Raiz Kotlin: `app/src/main/java/com/goldensystem/auris/`.

| Pacote | Responsabilidade |
|---|---|
| `data/ai/` | Orquestração, prompts, comandos, geração de playlists e clientes de IA |
| `data/backup/` | Formato, validação, histórico e restauração de backups |
| `data/database/` | Room: entidades, DAOs, relações e migrações |
| `data/gdrive/` | Repositório e proxy de streaming do Google Drive |
| `data/jellyfin/` | Integração Jellyfin |
| `data/media/` | Metadados, edição de tags, mapeamento e recursos de mídia |
| `data/navidrome/` | Integração Navidrome |
| `data/network/` | Definições de API e modelos de resposta |
| `data/netease/` | Integração NetEase |
| `data/preferences/` | Preferências do usuário, tema, equalizador, IA e playlists |
| `data/repository/` | Repositórios centrais da biblioteca, letras, atualizações e transições |
| `data/service/` | `MusicService`, notificações, Cast, player, transferência e serviços Android |
| `data/telegram/` | Cliente, cache, repositório e proxy Telegram |
| `data/qqmusic/` | Integração QQ Music |
| `data/worker/` | Workers e agendamento de sincronização/IA |
| `di/` | Módulos Hilt |
| `presentation/components/` | Componentes reutilizáveis Compose |
| `presentation/navigation/` | Rotas e navegação |
| `presentation/screens/` | Telas |
| `presentation/viewmodel/` | ViewModels e StateHolders |
| `ui/glancewidget/` | Widgets Android com Glance |
| `ui/theme/` | Tema e recursos visuais |
| `utils/` | Funções utilitárias compartilhadas |

## Módulo `shared`

Pacote: `com.theveloper.pixelplay.shared`.

Os arquivos definem contratos serializáveis usados na comunicação entre telefone e relógio, incluindo:

- comandos e resultados de reprodução;
- estado do player e volume;
- navegação/busca da biblioteca;
- capacidades do dispositivo;
- favoritos;
- caminhos de dados e intents;
- metadados e progresso de transferência;
- paleta de tema.

Esses DTOs são a fronteira de comunicação. Ao mudar um contrato, verifique simultaneamente os consumidores em `app` e `wear`, além da compatibilidade de serialização.

## Módulo `wear`

Pacote principal: `com.theveloper.pixelplay`.

Contém Activity e navegação próprias, telas de player/biblioteca/downloads/fila/volume/timer, ViewModels, repositórios, camada de dados Wear e banco Room local. O app Wear também declara Media3 para reprodução local independente.

## Testes e qualidade

No módulo principal existem testes unitários e instrumentados sob `app/src/test/` e `app/src/androidTest/`. Há testes direcionados a DAOs/migrações, sincronização e benchmark de inicialização. A pasta `baselineprofile/` complementa a estratégia de performance.
