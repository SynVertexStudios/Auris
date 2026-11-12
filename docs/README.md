# Auris — documentação técnica

**Projeto:** Auris Music Player  
**Pacote Android:** `com.goldensystem.auris`  
**Versão declarada no snapshot analisado:** `1.0.1.5` (`versionCode` `838`)  
**Stack principal:** Kotlin, Android, Jetpack Compose, Material 3, Media3/ExoPlayer, Room, DataStore e Hilt.

Esta pasta documenta a implementação encontrada no código-fonte do repositório. Ela é voltada a desenvolvimento, manutenção e evolução do aplicativo — não é um catálogo de funcionalidades para usuários finais.

## Documentos

| Documento | Conteúdo |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Arquitetura geral, camadas, inicialização e injeção de dependências |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | Módulos Gradle, pacotes e responsabilidades |
| [DATA_AND_STORAGE.md](DATA_AND_STORAGE.md) | Room, entidades, DAOs, DataStore, MediaStore e fluxo de biblioteca |
| [PLAYBACK.md](PLAYBACK.md) | Serviço de reprodução, Media3, fila, notificações, Cast e Wear OS |
| [INTEGRATIONS.md](INTEGRATIONS.md) | Serviços externos, APIs, sincronização e integrações de mídia |
| [AI.md](AI.md) | Arquitetura da IA, provedores, execução de comandos e cache |
| [BUILD_AND_TESTS.md](BUILD_AND_TESTS.md) | Configuração Gradle, variantes, CI e testes |

## Escopo e precisão

A documentação foi escrita a partir do snapshot `Auris-main.zip` fornecido para análise. Os nomes de classes, pacotes, módulos e configurações refletem esse snapshot. A versão registrada em `gradle.properties` é `1.0.1.5`, código `838`; isso não garante que corresponda à versão atualmente publicada.

Quando um comportamento depende de detalhes internos extensos, o documento aponta as classes responsáveis em vez de presumir uma regra que não foi confirmada. Segredos, chaves de API e valores privados de configuração não são reproduzidos.

## Convenções

- Caminhos são relativos à raiz do repositório.
- `app/src/main/java/com/goldensystem/auris/` é abreviado como `app/src/main/java/.../auris/`.
- Os nomes `ViewModel`, `StateHolder`, `Repository`, `Dao` e `Service` são usados conforme as classes reais do projeto.
- A presença de uma integração no código não significa que ela esteja configurada ou habilitada em toda instalação.
