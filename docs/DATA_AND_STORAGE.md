# Dados e armazenamento

## 1. Fontes de dados

A biblioteca combina mais de uma fonte:

1. **MediaStore** — consulta à coleção de mídia indexada pelo Android para descobrir arquivos de áudio locais.
2. **Room** — armazenamento estruturado de metadados normalizados, favoritos, playlists, letras, histórico, dados de provedores e cache.
3. **DataStore** — preferências do usuário e configurações.
4. **Serviços remotos** — bibliotecas e streams de provedores externos, cada um com repositório próprio.

Não trate MediaStore, Room e DataStore como uma única base. Cada um tem finalidade e ciclo de atualização diferentes.

## 2. Room principal

Definição: `data/database/AurisDatabase.kt`.

- Banco: `AurisDatabase`.
- Versão de schema declarada neste snapshot: **41**.
- `exportSchema = true`.
- Criação e fornecimento: `di/AppModule.kt`.
- Migrações: objetos `Migration` no companion object de `AurisDatabase.kt`.

O banco registra DAOs para música, favoritos, playlists locais, letras, histórico de pesquisa, transições, Telegram, NetEase, Google Drive, QQ Music, Navidrome, Jellyfin, cache/uso de IA e outras estruturas.

### Principais grupos de entidades

| Grupo | Exemplos |
|---|---|
| Biblioteca local | `SongEntity`, `AlbumEntity`, `ArtistEntity`, `SongArtistCrossRef` |
| Busca | `SongSearchFtsEntity`, `SearchHistoryEntity` |
| Playlists | `PlaylistEntity`, `PlaylistSongEntity` |
| Preferências/curadoria de música | `FavoritesEntity`, `SongEngagementEntity`, `TransitionRuleEntity` |
| Letras e arte | `LyricsEntity`, `AlbumArtThemeEntity` |
| Fontes remotas | `TelegramSongEntity`, `NeteaseSongEntity`, `GDriveSongEntity`, `QqMusicSongEntity`, `NavidromeSongEntity`, `JellyfinSongEntity` e respectivas entidades de playlist/canal/pasta |
| IA | `AiCacheEntity`, `AiUsageEntity` |

A lista exata de entidades está no array `entities` da anotação `@Database`; consulte-o ao criar uma nova tabela.

### DAOs

Cada DAO concentra operações de leitura e escrita de seu domínio. Exemplos: `MusicDao`, `FavoritesDao`, `LocalPlaylistDao`, `LyricsDao`, `SearchHistoryDao`, `TelegramDao`, `GDriveDao`, `NavidromeDao`, `JellyfinDao`, `AiCacheDao` e `AiUsageDao`.

Regra de manutenção: mudanças em entidades devem ser acompanhadas de atualização de schema/migração, testes e exportação de schema conforme a configuração do projeto. Não remova migrações antigas apenas porque a versão atual é mais recente: instalações existentes podem precisar delas.

## 3. Preferências

O pacote `data/preferences/` contém repositórios e modelos para preferências. Entre eles:

- `UserPreferencesRepository`;
- `ThemePreferencesRepository`;
- `PlaylistPreferencesRepository`;
- `EqualizerPreferencesRepository`;
- `AiPreferencesRepository`.

A implementação usa DataStore Preferences fornecido por `AppModule.kt`. Use o repositório de preferências correspondente em vez de espalhar chaves de DataStore por telas.

## 4. Biblioteca local

`MediaStoreSongRepository.kt` implementa `SongRepository` e usa MediaStore, Room, favoritos e preferências. `MediaStoreObserver` observa alterações na coleção de mídia. O repositório aplica regras de diretório e filtros, normalização de metadados e paginação.

`MusicRepositoryImpl.kt` implementa operações mais amplas de biblioteca e coordena DAOs e outras fontes. `MusicRepository` é a abstração injetada em consumidores.

Fluxo conceitual de leitura:

```text
MediaStore / Room
      |
      v
MediaStoreSongRepository / MusicRepositoryImpl
      |
      v
Song / Album / Artist / Playlist
      |
      v
LibraryStateHolder / ViewModel
      |
      v
Compose + Paging
```

`LibraryViewModel` expõe fluxos paginados de músicas, álbuns, artistas e favoritos através de `LibraryStateHolder`.

## 5. Paginação e observação

O projeto usa Paging 3 (`Pager`, `PagingData`, `cachedIn`) para coleções grandes. A camada de apresentação deve consumir os fluxos paginados existentes, evitando carregar a biblioteca inteira em memória sem necessidade.

## 6. Backups

O pacote `data/backup/` separa o processo em etapas:

- representação e manifesto (`model/`);
- leitura/escrita de formato (`format/`);
- validação (`validation/`);
- planejamento e execução de restauração (`restore/`);
- manipuladores por seção (`module/`);
- histórico (`history/`).

A validação ocorre antes da restauração, com validadores de manifesto, schema e conteúdo. Ao adicionar uma nova seção persistente que deva ser exportada, avalie também adicionar seu handler de backup e restauração.

## 7. Banco local do Wear OS

O módulo `wear` tem `WearMusicDatabase`, `LocalSongEntity` e `LocalSongDao`. Essa persistência é separada do banco principal do telefone. A transferência de músicas para o relógio não deve ser confundida com sincronização automática de todo o banco Room do telefone.
