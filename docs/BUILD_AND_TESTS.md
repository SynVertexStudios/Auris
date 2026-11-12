# Build, configuração e testes

## 1. Ambiente e versões Android

Configuração do módulo `app` neste snapshot:

| Propriedade | Valor |
|---|---|
| `namespace` | `com.goldensystem.auris` |
| `applicationId` | `com.goldensystem.auris` |
| `compileSdk` | 35 |
| `targetSdk` | 35 |
| `minSdk` | 29 |
| Java source/target | 11 |
| Kotlin JVM target | 11 |
| Compose | habilitado |
| `buildConfig` | habilitado |

O módulo `wear` usa `compileSdk 35`, `minSdk 30` e `targetSdk 34`. O módulo `shared` usa `minSdk 29`.

O projeto usa Gradle Wrapper. Prefira `./gradlew` em vez de depender de uma instalação global do Gradle.

## 2. Versão do aplicativo

`gradle.properties` define, neste snapshot:

- `APP_VERSION_NAME=1.0.1.5`
- `APP_VERSION_CODE=838`

O `app/build.gradle.kts` lê essas propriedades para preencher `versionName` e `versionCode`. Atualize os dois valores de forma coordenada ao preparar uma versão.

## 3. Variantes de build

O módulo principal declara:

- `debug`: adiciona o sufixo de application ID `.beta` e sufixo beta ao nome da versão;
- `release`: build não debuggable, com minificação R8 e remoção de recursos não utilizados;
- `benchmark`: derivada de release para benchmark, assinada com configuração debug.

A configuração de splits ABI é controlada pela propriedade Gradle `Auris.enableAbiSplits`, cujo padrão no script é `true`.

## 4. Assinatura

O script define configurações `debug`, `release` e `fixedDebug`. A configuração release depende de um keystore e de variáveis de ambiente:

- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

No CI, a configuração debug também pode apontar para o keystore de release quando `CI=true`. Não coloque senhas ou arquivos de assinatura no controle de versão. Configure os segredos no ambiente de CI.

## 5. Comandos comuns

Na raiz do repositório:

```bash
# Listar tarefas disponíveis
./gradlew tasks

# Compilar a variante debug
./gradlew :app:assembleDebug

# Compilar release (requer assinatura configurada)
./gradlew :app:assembleRelease

# Executar testes unitários do app
./gradlew :app:testDebugUnitTest

# Executar testes instrumentados (requer dispositivo/emulador)
./gradlew :app:connectedDebugAndroidTest

# Compilar o aplicativo Wear OS
./gradlew :wear:assembleDebug
```

Os comandos acima são tarefas Gradle padrão compatíveis com a configuração declarada; a execução pode depender de SDK instalado, JDK, rede e segredos de assinatura.

## 6. Dependências centrais identificadas

O build declara bibliotecas para:

- AndroidX Compose, Material 3, Navigation e Lifecycle;
- AndroidX Media3 ExoPlayer, UI, Session e Transformer;
- Room e Paging 3;
- Hilt/Dagger e KSP;
- DataStore e WorkManager;
- Retrofit, OkHttp, Gson e Kotlin Serialization;
- Coil;
- Google Play Services Cast e Wearable;
- Glance App Widgets;
- bibliotecas de metadados/tag de áudio e processamento de mídia.

As versões são centralizadas no catálogo Gradle em `gradle/libs.versions.toml`. Consulte o catálogo antes de atualizar uma dependência para evitar declarar versões conflitantes diretamente no build.

## 7. CI

`.github/workflows/` contém workflows para build debug, build principal e release; há também um workflow Wear OS desabilitado (`build-wearos.yml.disabled`). Examine os arquivos de workflow para confirmar gatilhos, artefatos e segredos exigidos antes de alterar o pipeline.

## 8. Testes presentes

O projeto contém testes unitários e instrumentados. Entre os testes instrumentados identificados:

- `AurisDatabaseMigrationTest`;
- `MusicDaoTest`;
- `SyncWorkerTest`;
- `StartupBenchmark`.

Ao alterar persistência, inclua cobertura de migração e DAO. Ao alterar sincronização, teste repetição, cancelamento e conectividade. Ao alterar inicialização, execute benchmark em ambiente controlado.

## 9. Diagnóstico de build

Se a compilação falhar:
1. Confirme JDK e Android SDK instalados.
2. Confira se as propriedades de versão estão presentes.
3. Verifique se a tarefa escolhida exige assinatura.
4. Verifique variáveis de ambiente e secrets do CI.
5. Confirme compatibilidade entre Gradle, Android Gradle Plugin, Kotlin e catálogo de versões.
6. Reproduza primeiro com a tarefa específica do módulo afetado.
