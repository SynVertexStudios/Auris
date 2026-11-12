# Integrações e serviços externos

## 1. Visão geral

O código organiza integrações por provedor. Em geral, cada integração tem repositório e, quando necessário, proxy de streaming e serviços de rede. A presença de classes no repositório não garante que uma conta esteja conectada ou que o serviço esteja disponível.

## 2. Integrações identificadas

| Integração | Código principal |
|---|---|
| Telegram | `data/telegram/TelegramClientManager.kt`, `TelegramRepository.kt`, `TelegramCacheManager.kt`, `TelegramStreamProxy.kt` |
| Google Drive | `data/gdrive/GDriveApiService.kt`, `GDriveRepository.kt`, `GDriveStreamProxy.kt` |
| Navidrome | `data/navidrome/NavidromeRepository.kt`, `NavidromeStreamProxy.kt`, `data/network/navidrome/` |
| Jellyfin | `data/jellyfin/JellyfinRepository.kt`, `JellyfinStreamProxy.kt`, `data/network/jellyfin/` |
| NetEase | `data/netease/NeteaseRepository.kt`, `NeteaseStreamProxy.kt`, `data/network/netease/` |
| QQ Music | `data/qqmusic/QqMusicRepository.kt`, `QqMusicStreamProxy.kt`, `data/network/qqmusic/` |
| Letras LRCLIB | `data/network/lyrics/LrcLibApiService.kt`, `data/repository/LyricsRepositoryImpl.kt` |
| Deezer | `data/network/deezer/DeezerApiService.kt` e modelos associados |
| Google Cast | `data/service/cast/CastOptionsProvider.kt`, `data/service/player/CastPlayer.kt` |
| Firebase | configuração de DI e plugin Google Services; confira `di/FirebaseMudule.kt` e configuração de build |
| GitHub/releases | `data/github/` e `UpdateRepository.kt` |

## 3. Camada de rede

As interfaces de API ficam principalmente em `data/network/`. O projeto usa Retrofit/OkHttp em sua infraestrutura de rede, configurada no módulo Hilt. Modelos de resposta e parsers ficam próximos dos serviços de cada provedor.

Ao adicionar endpoints:
1. Defina DTOs de resposta separados dos modelos de domínio.
2. Mantenha autenticação e cabeçalhos no cliente/repositório apropriado.
3. Trate respostas inválidas, falhas de rede, timeouts e limites do provedor.
4. Não registre tokens, senhas ou respostas que contenham credenciais.
5. Converta respostas externas para modelos internos antes de expô-las à UI.

## 4. Streaming remoto

As integrações de Telegram, Drive, Navidrome, Jellyfin, NetEase e QQ Music têm proxies de streaming. Isso indica que a reprodução pode exigir uma etapa de resolução/encaminhamento da origem em vez de usar diretamente uma URI local simples.

Ao mexer nesses fluxos, acompanhe a cadeia completa: identificação do item, obtenção de credenciais, resolução da URL, requisição/stream, MediaItem e ciclo de vida da sessão. Não grave credenciais em logs ou em entidades de música.

## 5. Autenticação

O manifesto declara Activities de autenticação específicas para Telegram, NetEase, Google Drive, QQ Music, Navidrome e Jellyfin. As telas e ViewModels correspondentes ficam em `presentation/<provedor>/`.

As credenciais e estado de conexão devem continuar sob responsabilidade da camada de dados/armazenamento apropriada. Não use estado visual de uma tela como fonte definitiva de autenticação.

## 6. Letras

`LrcLibApiService` fornece acesso à API LRCLIB. `LyricsRepository`/`LyricsRepositoryImpl` coordenam consulta e persistência, e `LyricsDao`/`LyricsEntity` armazenam dados locais. Existem também utilitários de parsing/importação, incluindo `TtmlLyricsParser` e validação de importação.

## 7. Sincronização em segundo plano

`data/worker/` contém `SyncWorker`, `SyncManager`, `AiWorker` e `AiWorkerManager`. A configuração de WorkManager usa `HiltWorkerFactory` em `AurisApplication`.

Antes de alterar tarefas:
- verifique critérios de rede e restrições;
- considere execução duplicada e idempotência;
- trate cancelamento e retries;
- não dependa de uma Activity ativa;
- mantenha o estado de sincronização observável.

## 8. Privacidade e segurança

Integrações podem processar identificadores de conta, tokens, nomes de arquivos e metadados pessoais. O código deve minimizar dados enviados, proteger credenciais, validar respostas e evitar logs sensíveis. A documentação não replica valores de configuração nem chaves encontradas no pacote enviado.
