# Arquitetura de IA

## 1. Componentes

A IA está concentrada principalmente em `data/ai/`, com preferências em `data/preferences/AiPreferencesRepository.kt` e persistência de cache/uso no Room.

Componentes principais:

- `AiOrchestrator.kt` — coordena requisições, preferências, seleção de cliente, cache, uso e recuperação.
- `AiClientFactory.kt` — cria o cliente conforme o provedor.
- `AiClient.kt` — contrato comum dos clientes.
- `AiProvider.kt` — enumeração/configuração de provedores.
- `AiSystemPromptEngine.kt` — montagem/gestão do prompt de sistema.
- `AiCommandInterpreter.kt` e `AiCommandExecutor.kt` — interpretação e execução de comandos de aplicativo.
- `AiPlaylistGenerator.kt` — geração de playlists.
- `AiMusicResolver.kt` — resolução de referências musicais.
- `AiMetadataGenerator.kt` — geração/apoio a metadados.
- `UserProfileDigestGenerator.kt` — resumo de perfil de uso.
- `GeminiModelService.kt` — serviço relacionado ao Gemini.

## 2. Provedores

`AiClientFactory` contém mapeamento explícito de provedores para clientes. Neste snapshot, os provedores incluem Gemini, DeepSeek, Groq, Mistral, NVIDIA NIM, Kimi/Moonshot, GLM/Zhipu, OpenAI e OpenRouter.

Alguns provedores usam clientes dedicados; outros usam `GenericOpenAiClient` com base URL e modelo padrão específicos. A lista de provedores suportados deve ser mantida em sincronia com `AiProvider`, fábrica, preferências e interface de configuração.

## 3. Fluxo de uma solicitação

Fluxo simplificado observado:

```text
UI / estado de IA
      |
      v
AiOrchestrator
      |
      +--> AiPreferencesRepository (provedor, chave, modelo, prompt)
      |
      +--> AiClientFactory
      |       |
      |       v
      |    AiClient específico
      |
      +--> AiCacheDao / AiUsageDao
      |
      v
Resposta para o chamador
```

`AiOrchestrator` obtém prompt, chave e modelo a partir das preferências. A fábrica cria o cliente e o conteúdo é enviado ao provedor remoto. O orquestrador contém timeout por tentativa, recuperação de modelo em determinadas falhas, cooldown de provedor e cache com validade.

Valores encontrados na implementação do orquestrador neste snapshot:
- timeout por tentativa: 60 segundos;
- cooldown de provedor: 5 minutos;
- TTL do cache: 30 minutos.

Esses valores são detalhes de implementação e podem mudar; consulte as constantes em `AiOrchestrator.kt`.

## 4. Comandos do aplicativo

A camada de comandos é separada da geração de texto:

- `AiCommandInterpreter` interpreta a resposta/estrutura de comando.
- `AiCommandResponse` representa a resposta estruturada.
- `AiCommandExecutor` executa a ação no aplicativo.

Essa separação é importante: texto gerado não deve ser executado diretamente como código ou ação arbitrária. Novas ações devem ter representação tipada, validação de argumentos e tratamento de falha. Mantenha a interpretação e a execução em classes distintas.

## 5. Cache e métricas

O banco principal registra `AiCacheEntity` e `AiUsageEntity`, acessadas por `AiCacheDao` e `AiUsageDao`. O cache reduz requisições repetidas; os registros de uso dão suporte ao acompanhamento de utilização.

Ao alterar a estrutura de cache ou métricas, revise:
- entidade e DAO;
- versão/migração de `AurisDatabase`;
- política de expiração;
- backup, se os dados precisarem ser exportados;
- testes.

## 6. Preferências e credenciais

`AiPreferencesRepository` centraliza preferências por provedor, incluindo chave de API, modelo e prompt. Credenciais não devem ser exibidas em logs, mensagens de erro ou documentação. Antes de mudar o armazenamento, examine a implementação atual e a política de backup/restauração para evitar exportar segredos acidentalmente.

## 7. Testes recomendados

- chave ausente ou em branco;
- provedor indisponível e erro HTTP;
- timeout e cancelamento;
- modelo inexistente e recuperação;
- resposta malformada;
- cache válido/expirado;
- comando não reconhecido;
- argumentos inválidos;
- falha durante execução de comando;
- limites e concorrência de chamadas.
