<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>Monsai — README API Backend</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=Montserrat:wght@500;600&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet"/>
<style>
  :root {
    --peppermint: #edf7e8;
    --beryl-green: #cce5c1;
    --feijoa: #b0d693;
    --patina: #649c7e;
    --apple-1: #429636;
    --apple-2: #5cb52d;
    --forest-green: #227e35;
    --salem: #096732;
    --sherwood-green: #02502c;
    --bg: #0d1f14;
    --surface: #122b1a;
    --surface2: #1a3824;
    --border: #264d35;
    --text: #e8f5e2;
    --text-muted: #8bb89a;
    --code-bg: #091510;
  }

  * { margin: 0; padding: 0; box-sizing: border-box; }

  body {
    background: var(--bg);
    color: var(--text);
    font-family: 'Inter', sans-serif;
    font-weight: 400;
    line-height: 1.7;
    min-height: 100vh;
  }

  /* BG texture */
  body::before {
    content: '';
    position: fixed; inset: 0;
    background:
      radial-gradient(ellipse 80% 50% at 10% 0%, rgba(66,150,54,0.12) 0%, transparent 60%),
      radial-gradient(ellipse 60% 40% at 90% 100%, rgba(9,103,50,0.15) 0%, transparent 60%);
    pointer-events: none;
    z-index: 0;
  }

  .wrapper {
    position: relative; z-index: 1;
    max-width: 900px;
    margin: 0 auto;
    padding: 60px 32px 80px;
  }

  /* HEADER */
  .header {
    display: flex; align-items: center; gap: 18px;
    margin-bottom: 12px;
  }

  .logo-badge {
    width: 52px; height: 52px;
    background: linear-gradient(135deg, var(--apple-2), var(--forest-green));
    border-radius: 14px;
    display: flex; align-items: center; justify-content: center;
    font-size: 26px;
    box-shadow: 0 0 24px rgba(92,181,45,0.3);
    flex-shrink: 0;
  }

  h1 {
    font-family: 'Inter', sans-serif;
    font-weight: 700;
    font-size: 2.1rem;
    color: var(--feijoa);
    letter-spacing: -0.02em;
  }

  .tag-row {
    display: flex; flex-wrap: wrap; gap: 8px;
    margin-bottom: 36px; margin-top: 14px;
  }

  .tag {
    background: var(--surface2);
    border: 1px solid var(--border);
    border-radius: 999px;
    padding: 3px 13px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 500;
    font-size: 0.72rem;
    color: var(--patina);
    letter-spacing: 0.06em;
    text-transform: uppercase;
  }

  .tag.highlight {
    background: rgba(92,181,45,0.12);
    border-color: var(--apple-2);
    color: var(--apple-2);
  }

  .divider {
    border: none;
    border-top: 1px solid var(--border);
    margin: 36px 0;
  }

  /* SECTION */
  .section { margin-bottom: 44px; }

  h2 {
    font-family: 'Inter', sans-serif;
    font-weight: 700;
    font-size: 1.2rem;
    color: var(--feijoa);
    margin-bottom: 16px;
    display: flex; align-items: center; gap: 10px;
  }

  h2 .icon {
    width: 30px; height: 30px;
    background: rgba(92,181,45,0.13);
    border: 1px solid rgba(92,181,45,0.25);
    border-radius: 8px;
    display: flex; align-items: center; justify-content: center;
    font-size: 15px;
    flex-shrink: 0;
  }

  h3 {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 0.88rem;
    color: var(--patina);
    text-transform: uppercase;
    letter-spacing: 0.08em;
    margin: 24px 0 10px;
  }

  p {
    font-family: 'Inter', sans-serif;
    font-weight: 400;
    font-size: 0.95rem;
    color: #c2dcc8;
    margin-bottom: 10px;
  }

  a {
    color: var(--apple-2);
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    text-decoration: none;
    border-bottom: 1px dashed rgba(92,181,45,0.4);
    transition: color 0.2s, border-color 0.2s;
  }
  a:hover { color: var(--feijoa); border-color: var(--feijoa); }

  /* CODE BLOCKS */
  pre {
    background: var(--code-bg);
    border: 1px solid var(--border);
    border-left: 3px solid var(--apple-1);
    border-radius: 10px;
    padding: 18px 20px;
    overflow-x: auto;
    margin: 12px 0 18px;
    position: relative;
  }

  pre code {
    font-family: 'JetBrains Mono', monospace;
    font-size: 0.82rem;
    color: #a8d8b0;
    line-height: 1.6;
  }

  .pre-label {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 0.68rem;
    color: var(--patina);
    text-transform: uppercase;
    letter-spacing: 0.1em;
    margin-bottom: 6px;
  }

  /* CARDS */
  .card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 14px;
    margin: 16px 0;
  }

  .card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 18px 20px;
    transition: border-color 0.2s, box-shadow 0.2s;
  }

  .card:hover {
    border-color: var(--patina);
    box-shadow: 0 0 16px rgba(100,156,126,0.1);
  }

  .card-title {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 0.82rem;
    color: var(--feijoa);
    margin-bottom: 6px;
    display: flex; align-items: center; gap: 7px;
  }

  .card p {
    font-size: 0.85rem;
    color: var(--text-muted);
    margin: 0;
  }

  /* ENV TABLE */
  table {
    width: 100%;
    border-collapse: collapse;
    margin: 14px 0;
    font-size: 0.85rem;
  }

  thead th {
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 0.72rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: var(--patina);
    border-bottom: 1px solid var(--border);
    padding: 8px 12px;
    text-align: left;
  }

  tbody tr:nth-child(even) { background: rgba(255,255,255,0.02); }

  tbody td {
    padding: 9px 12px;
    border-bottom: 1px solid rgba(38,77,53,0.5);
    vertical-align: top;
    color: #c2dcc8;
  }

  tbody td:first-child {
    font-family: 'JetBrains Mono', monospace;
    font-size: 0.78rem;
    color: var(--apple-2);
    white-space: nowrap;
  }

  tbody td:last-child {
    font-size: 0.8rem;
    color: var(--text-muted);
  }

  /* ALERT */
  .alert {
    background: rgba(9,103,50,0.15);
    border: 1px solid rgba(9,103,50,0.5);
    border-radius: 10px;
    padding: 14px 18px;
    display: flex; gap: 12px; align-items: flex-start;
    margin: 16px 0;
    font-size: 0.88rem;
    color: #b0dbc0;
  }

  .alert .icon { font-size: 16px; margin-top: 1px; flex-shrink: 0; }

  .swagger-link {
    display: inline-flex; align-items: center; gap: 8px;
    background: linear-gradient(135deg, var(--apple-1), var(--forest-green));
    color: white !important;
    border: none !important;
    padding: 10px 22px;
    border-radius: 8px;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 0.88rem;
    margin-top: 10px;
    transition: opacity 0.2s, transform 0.2s;
  }
  .swagger-link:hover { opacity: 0.9; transform: translateY(-1px); }

  /* STEP LIST */
  .steps { list-style: none; counter-reset: step; margin: 14px 0; }

  .steps li {
    counter-increment: step;
    display: flex; gap: 14px; align-items: flex-start;
    margin-bottom: 16px;
  }

  .steps li::before {
    content: counter(step);
    min-width: 28px; height: 28px;
    background: rgba(92,181,45,0.15);
    border: 1px solid rgba(92,181,45,0.3);
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    font-family: 'Montserrat', sans-serif;
    font-weight: 600;
    font-size: 0.78rem;
    color: var(--apple-2);
    flex-shrink: 0;
    margin-top: 2px;
  }

  .steps li p { margin: 0; }

  /* FOOTER */
  footer {
    margin-top: 60px;
    padding-top: 24px;
    border-top: 1px solid var(--border);
    text-align: center;
    font-family: 'Montserrat', sans-serif;
    font-size: 0.78rem;
    color: var(--text-muted);
    font-weight: 500;
  }

  footer span { color: var(--patina); }
</style>
</head>
<body>
<div class="wrapper">

  <div class="header">
    <div class="logo-badge">🌿</div>
    <div>
      <h1>Monsai — API Backend</h1>
    </div>
  </div>

  <div class="tag-row">
    <span class="tag highlight">Spring Boot 4.0</span>
    <span class="tag">Java 21</span>
    <span class="tag">MySQL</span>
    <span class="tag">MQTT</span>
    <span class="tag">JWT</span>
    <span class="tag">Swagger / OpenAPI</span>
    <span class="tag">Maven</span>
  </div>

  <hr class="divider"/>

  <!-- PRÉ-REQUISITOS -->
  <div class="section">
    <h2><span class="icon">⚙️</span> Pré-requisitos</h2>
    <p>Antes de rodar o projeto, certifique-se de ter instalado em sua máquina:</p>

    <div class="card-grid">
      <div class="card">
        <div class="card-title">☕ Java 21 (JDK)</div>
        <p>Versão mínima exigida pelo Spring Boot 4.x. Recomenda-se o <strong>Eclipse Temurin 21</strong> ou OpenJDK.</p>
      </div>
      <div class="card">
        <div class="card-title">📦 Apache Maven 3.9+</div>
        <p>O projeto usa o Maven Wrapper (<code>mvnw</code>), mas ter o Maven instalado globalmente facilita o desenvolvimento.</p>
      </div>
      <div class="card">
        <div class="card-title">🐬 MySQL 8.x</div>
        <p>Banco de dados relacional principal. O banco <code>monsai_db</code> será criado automaticamente na primeira execução.</p>
      </div>
      <div class="card">
        <div class="card-title">📡 Broker MQTT</div>
        <p>Ex.: Mosquitto rodando localmente em <code>tcp://localhost:1883</code> para recebimento de telemetria dos dispositivos.</p>
      </div>
    </div>
  </div>

  <!-- CLONE E ESTRUTURA -->
  <div class="section">
    <h2><span class="icon">📁</span> Clone e Estrutura</h2>

    <div class="pre-label">Clone o repositório</div>
    <pre><code>git clone https://github.com/seu-org/aaci-monsai-backend.git
cd aaci-monsai-backend</code></pre>

    <p>A estrutura principal do projeto:</p>
    <pre><code>src/
├── main/
│   ├── java/com/senai/monsai/
│   │   ├── application/
│   │   │   ├── dto/          # DTOs de entrada e saída
│   │   │   └── service/      # Lógica de negócio
│   │   └── infrastructure/
│   │       └── config/       # MqttConfig, SwaggerConfig
│   └── resources/
│       └── application.properties  # ⚠️  Substituir pelo .env
pom.xml</code></pre>
  </div>

  <!-- CONFIGURAÇÃO .ENV -->
  <div class="section">
    <h2><span class="icon">🔐</span> Configuração do <code>.env</code></h2>

    <div class="alert">
      <span class="icon">⚠️</span>
      <span>O arquivo <code>.env</code> <strong>não é versionado</strong>. Crie-o na raiz do projeto com as variáveis abaixo antes de iniciar a aplicação.</span>
    </div>

    <div class="pre-label">Crie o arquivo <code>.env</code> na raiz</div>
    <pre><code># ─── JWT ─────────────────────────────────────────────
JWT_SECRET=monsaiSistemaSeguroJwtChaveMuitoForte123456
JWT_EXPIRATION=86400000

# ─── MySQL ───────────────────────────────────────────
DB_URL=jdbc:mysql://localhost:3306/monsai_db?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=sua_senha_mysql

# ─── MQTT ────────────────────────────────────────────
MQTT_BROKER_URL=tcp://localhost:1883
MQTT_CLIENT_ID=monsai-backend-api
MQTT_USERNAME=
MQTT_PASSWORD=
MQTT_TOPIC=monsai/telemetria
MQTT_KEEP_ALIVE=60
MQTT_QOS=1</code></pre>

    <h3>Referência completa das variáveis</h3>
    <table>
      <thead>
        <tr>
          <th>Variável</th>
          <th>Descrição</th>
          <th>Padrão</th>
        </tr>
      </thead>
      <tbody>
        <tr><td>JWT_SECRET</td><td>Chave secreta para assinatura dos tokens JWT</td><td>—</td></tr>
        <tr><td>JWT_EXPIRATION</td><td>Tempo de expiração do token em milissegundos</td><td>86400000 (24h)</td></tr>
        <tr><td>DB_URL</td><td>JDBC URL de conexão com o MySQL</td><td>localhost:3306/monsai_db</td></tr>
        <tr><td>DB_USERNAME</td><td>Usuário do banco de dados</td><td>root</td></tr>
        <tr><td>DB_PASSWORD</td><td>Senha do banco de dados</td><td>—</td></tr>
        <tr><td>MQTT_BROKER_URL</td><td>URL do broker MQTT</td><td>tcp://localhost:1883</td></tr>
        <tr><td>MQTT_CLIENT_ID</td><td>Identificador do cliente MQTT</td><td>monsai-backend-api</td></tr>
        <tr><td>MQTT_TOPIC</td><td>Tópico MQTT para telemetria</td><td>monsai/telemetria</td></tr>
        <tr><td>MQTT_QOS</td><td>Nível de QoS (0, 1 ou 2)</td><td>1</td></tr>
      </tbody>
    </table>
  </div>

  <!-- BANCO DE DADOS -->
  <div class="section">
    <h2><span class="icon">🗄️</span> Configuração do Banco de Dados</h2>

    <ol class="steps">
      <li><p>Certifique-se de que o MySQL está rodando na porta <strong>3306</strong>.</p></li>
      <li><p>Configure <code>DB_USERNAME</code> e <code>DB_PASSWORD</code> no <code>.env</code> com suas credenciais.</p></li>
      <li>
        <p>O banco <code>monsai_db</code> será criado automaticamente pelo parâmetro <code>createDatabaseIfNotExist=true</code>. Caso prefira criar manualmente:</p>
        <pre><code>-- No seu cliente MySQL (ex: DBeaver, MySQL Workbench ou CLI):
CREATE DATABASE monsai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;</code></pre>
      </li>
      <li><p>As tabelas são gerenciadas pelo <strong>Hibernate</strong> com estratégia <code>ddl-auto=update</code>: criadas automaticamente na primeira execução e atualizadas sem apagar dados.</p></li>
    </ol>
  </div>

  <!-- COMO RODAR -->
  <div class="section">
    <h2><span class="icon">🚀</span> Como Executar</h2>

    <h3>Usando o Maven Wrapper (recomendado)</h3>
    <pre><code># Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run</code></pre>

    <h3>Gerando e rodando o JAR</h3>
    <pre><code># Compilar e empacotar
./mvnw clean package -DskipTests

# Executar o JAR gerado
java -jar target/monsai-0.0.1-SNAPSHOT.jar</code></pre>

    <h3>Verificar se a API está no ar</h3>
    <pre><code>curl http://localhost:8080/actuator/health
# Esperado: {"status":"UP"}</code></pre>

    <div class="alert">
      <span class="icon">💡</span>
      <span>A porta padrão é <strong>8080</strong>. Para alterar, adicione <code>SERVER_PORT=outra_porta</code> ao <code>.env</code>.</span>
    </div>
  </div>

  <!-- SWAGGER -->
  <div class="section">
    <h2><span class="icon">📖</span> Swagger / OpenAPI</h2>

    <p>Com a aplicação rodando, acesse a documentação interativa da API:</p>

    <table>
      <thead>
        <tr><th>Interface</th><th>URL</th></tr>
      </thead>
      <tbody>
        <tr>
          <td>Swagger UI</td>
          <td><a href="http://localhost:8080/swagger-ui/index.html" target="_blank">http://localhost:8080/swagger-ui/index.html</a></td>
        </tr>
        <tr>
          <td>OpenAPI JSON</td>
          <td><a href="http://localhost:8080/v3/api-docs" target="_blank">http://localhost:8080/v3/api-docs</a></td>
        </tr>
      </tbody>
    </table>

    <a class="swagger-link" href="http://localhost:8080/swagger-ui/index.html" target="_blank">
      🔗 Abrir Swagger UI
    </a>

    <div class="alert" style="margin-top:20px;">
      <span class="icon">🔒</span>
      <span>A API utiliza <strong>JWT</strong>. Para testar endpoints protegidos no Swagger, clique em <em>"Authorize"</em> e insira o token no formato <code>Bearer {seu_token}</code> obtido no endpoint <code>POST /auth/login</code>.</span>
    </div>
  </div>

  <!-- DEPENDÊNCIAS PRINCIPAIS -->
  <div class="section">
    <h2><span class="icon">📦</span> Principais Dependências</h2>

    <div class="card-grid">
      <div class="card">
        <div class="card-title">🌱 Spring Boot 4.0.2</div>
        <p>Framework principal — Web, JPA, Security, DevTools, Integration.</p>
      </div>
      <div class="card">
        <div class="card-title">📘 SpringDoc OpenAPI 2.8</div>
        <p>Geração automática da documentação Swagger/OpenAPI 3.</p>
      </div>
      <div class="card">
        <div class="card-title">🔑 JJWT 0.11.5</div>
        <p>Criação e validação de tokens JWT para autenticação stateless.</p>
      </div>
      <div class="card">
        <div class="card-title">📡 Eclipse Paho MQTT 1.2.5</div>
        <p>Cliente MQTT para recebimento de dados de telemetria em tempo real.</p>
      </div>
      <div class="card">
        <div class="card-title">🐬 MySQL Connector</div>
        <p>Driver JDBC oficial para conexão com o banco MySQL 8.x.</p>
      </div>
      <div class="card">
        <div class="card-title">🏗️ Lombok</div>
        <p>Redução de boilerplate com anotações como <code>@Getter</code>, <code>@Builder</code>, <code>@AllArgsConstructor</code>.</p>
      </div>
    </div>
  </div>

  <footer>
    <p>Monsai Backend · Spring Boot 4 · Java 21 · <span>SENAI</span></p>
  </footer>

</div>
</body>
</html>
