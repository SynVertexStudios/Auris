# Reprodução de mídia

## 1. Serviço de reprodução

Classe central: `data/service/MusicService.kt`.

O serviço estende `MediaLibraryService` do AndroidX Media3. O manifesto o registra como serviço de primeiro plano do tipo `mediaPlayback`, com ações para MediaBrowser, Media3 MediaLibraryService e botões de mídia.

Isso permite manter a sessão de mídia ativa sem depender da Activity estar em primeiro plano. `MusicService` também coordena a sessão, comandos, estado de reprodução, notificação e integração com controles externos.

## 2. Interface e conexão com o player

A interface de reprodução é coordenada por classes de apresentação como `PlayerViewModel`, `PlaybackStateHolder` e `QueueStateHolder`. O `SessionToken` para `MusicService` é fornecido por Hilt em `di/AppModule.kt`.

O `PlayerViewModel` usa `MediaController`/Media3 para se conectar à sessão. A Activity e as telas não devem criar um player paralelo para a reprodução principal; alterações no estado devem ser encaminhadas ao controlador/serviço apropriado.

## 3. Engine e transições

O pacote `data/service/player/` contém:

- `DualPlayerEngine.kt` — engine de dois players;
- `TransitionController.kt` — coordenação de transições;
- `CastPlayer.kt` — integração de player com Cast;
- `HiFiCapabilityChecker.kt` e processadores relacionados — verificação/caminhos de capacidade de áudio.

O pacote também inclui processadores para taxa de amostragem Hi-Res e downmix surround. O comportamento exato de cada efeito deve ser confirmado no código da classe antes de alterar parâmetros ou ordem de processamento.

## 4. Fila e itens de mídia

Os modelos `PlaybackQueueSnapshot`, `PlaybackQueueItemSnapshot`, `PlayerInfo`, `Song` e `VideoItem` representam partes do estado de reprodução e da biblioteca. `MediaItemBuilder` auxilia a construção de itens Media3. `QueueUtils` e os gerenciadores de fila de vídeo tratam operações específicas de fila.

Ao adicionar uma nova origem de mídia, verifique:
- como o item é representado em `MediaItem` e metadados;
- como a URI é resolvida;
- se o item precisa de proxy/stream;
- como a fila é serializada/restaurada;
- como a sessão reporta metadados e comandos.

## 5. Notificação e controles externos

O projeto contém `MusicNotificationProvider`, `LocalOnlyMediaNotificationProvider`, `AurisMediaButtonReceiver` e `SleepTimerReceiver`. O manifesto também declara shortcuts, suporte a ações de mídia, serviço para Android Auto e receivers de widgets.

A sessão Media3 é o ponto de integração para controles de mídia do sistema, dispositivos Bluetooth e superfícies compatíveis. Preserve os comandos e metadados da sessão ao alterar o serviço.

## 6. Chromecast

A configuração está em `data/service/cast/CastOptionsProvider.kt`; o serviço usa listeners e classes do Google Cast. `CastPlayer` e os StateHolders de Cast fazem a ponte entre a interface e a sessão remota.

Para recursos de Cast, examine também `CastRouteStateHolder`, `CastStateHolder` e `CastTransferStateHolder` em `presentation/viewmodel/`.

## 7. Wear OS

O telefone contém componentes em `data/service/wear/` para publicação de estado, comandos e transferência de arquivos. O módulo `shared` define os contratos serializáveis; o módulo `wear` implementa a interface e os repositórios no relógio.

Fluxo de alto nível:

```text
App do relógio
   |
   v
Contratos em :shared
   |
   v
Data Layer / serviços de transferência
   |
   v
App do telefone
   |
   +--> estado/comandos do player
   +--> transferência de arquivos
```

A transferência possui serviço foreground no telefone (`WatchTransferForegroundService`) e componentes de coordenação/cancelamento/estado. Para mudar os formatos de mensagens, atualize os dois lados e os DTOs compartilhados.

## 8. Servidor HTTP local de mídia

`MediaFileHttpServerService.kt` e `CastSessionSecurity.kt` implementam uma superfície local usada para disponibilizar arquivos de mídia durante sessões compatíveis. Como envolve acesso à mídia e rede local, alterações devem preservar as verificações de sessão e a restrição de exposição do serviço.

## 9. Cuidados de manutenção

- Não amarre a reprodução ao ciclo de vida de uma tela Compose.
- Não faça I/O pesado na thread principal.
- Teste pause/retomada, troca de faixa, interrupções, desconexão de saída e encerramento/recriação do processo.
- Ao mudar a fila, confirme a consistência entre UI, MediaController e MediaSession.
- Ao alterar o serviço, revise permissões e tipos de foreground service no manifesto.
