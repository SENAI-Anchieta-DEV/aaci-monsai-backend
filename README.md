# 🌿 Monsai — Monitoramento Integrado de Sáude do Idoso (Backend API)

<p align="center">
  Plataforma backend para monitoramento inteligente com integração IoT, processamento em tempo real e API segura.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.2-02502c?style=for-the-badge&logo=springboot"/>
  <img src="https://img.shields.io/badge/Java-21-227e35?style=for-the-badge&logo=openjdk"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-096732?style=for-the-badge&logo=postgresql"/>
  <img src="https://img.shields.io/badge/MQTT-RealTime-649c7e?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Auth-JWT-5cb52d?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/API-OpenAPI%20%2F%20Swagger-b0d693?style=for-the-badge"/>
</p>

---

## 📌 Sobre o Projeto

O **Monsai Backend** é uma API desenvolvida para suportar sistemas de **monitoramento inteligente com dispositivos IoT**, permitindo ingestão de dados em tempo real via MQTT, processamento backend e persistência em banco relacional.

💡 Casos de uso:

* Monitoramento ambiental 🌱
* Sensores inteligentes 📡
* Telemetria em tempo real ⚡
* Sistemas acadêmicos com eventos em tempo real 🎓

---

## 🧠 Arquitetura do Sistema

```text
[ Dispositivos IoT ]
        │
        ▼
   ( MQTT Broker )
        │
        ▼
[ Monsai Backend API ]
        │
 ┌──────┼──────────┐
 ▼      ▼          ▼
Auth   Services   MQTT Listener
 │        │            │
 ▼        ▼            ▼
        PostgreSQL Database
```

### 📂 Camadas

* **Application**

  * DTOs
  * Services (regras de negócio)

* **Infrastructure**

  * Configurações (MQTT, Security, Swagger)
  * Integrações externas

* **Domain (implícito)**

  * Entidades e lógica central

---

## ⚙️ Pré-requisitos

* ☕ Java 21
* 📦 Maven 3.9+
* 🐘 PostgreSQL 16+
* 📡 Broker MQTT (Mosquitto recomendado)

---

## 📁 Setup do Projeto

### 🔽 Clone

```bash
git clone https://github.com/seu-org/aaci-monsai-backend.git
cd aaci-monsai-backend
```

---

## 🔐 Configuração do Ambiente

⚠️ Crie um arquivo `.env` na raiz:

```env
# JWT
JWT_SECRET=monsaiSistemaSeguroJwtChaveMuitoForte123456
JWT_EXPIRATION=86400000

# PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/monsai_db
DB_USERNAME=postgres
DB_PASSWORD=sua_senha

# MQTT
MQTT_BROKER_URL=tcp://localhost:1883
MQTT_CLIENT_ID=monsai-backend-api
MQTT_TOPIC=monsai/telemetria
MQTT_QOS=1
```

---

## 🗄️ Banco de Dados (PostgreSQL)

### ✔ Criar banco manualmente:

```sql
CREATE DATABASE monsai_db;
```

### ✔ Configuração padrão:

* Porta: `5432`
* Usuário: `postgres`

### ✔ ORM:

* Hibernate (`ddl-auto=update`)

---

## 🚀 Execução

### ▶️ Desenvolvimento

```bash
./mvnw spring-boot:run
```

### ▶️ Produção (JAR)

```bash
./mvnw clean package -DskipTests
java -jar target/monsai-0.0.1-SNAPSHOT.jar
```

---

## ✅ Health Check

```bash
curl http://localhost:8080/actuator/health
```

Resposta esperada:

```json
{"status":"UP"}
```

---

## 📖 API Docs

* Swagger UI: http://localhost:8080/swagger-ui/index.html
* OpenAPI: http://localhost:8080/v3/api-docs

### 🔐 Autenticação JWT

```text
Bearer seu_token
```

---

## 📡 Integração MQTT

| Configuração | Valor                |
| ------------ | -------------------- |
| Broker       | tcp://localhost:1883 |
| Topic        | monsai/telemetria    |
| QoS          | 1                    |

📥 Recebe dados em tempo real dos dispositivos IoT.

---

## 📦 Stack Tecnológica

| Tecnologia      | Papel             |
| --------------- | ----------------- |
| Spring Boot     | Core da aplicação |
| Spring Security | Autenticação      |
| JJWT            | Tokens JWT        |
| Spring Data JPA | Persistência      |
| PostgreSQL      | Banco de dados    |
| MQTT (Paho)     | Comunicação IoT   |
| Swagger         | Documentação      |

---

## 💡 Diferenciais Técnicos

* 🔐 Autenticação stateless com JWT
* 📡 Integração com IoT via MQTT
* ⚡ Processamento em tempo real
* 🧱 Arquitetura modular e escalável
* 📄 Documentação automática

---

## 🔗 Integração com Outros Módulos

Este backend faz parte de um ecossistema:

* 📱 Mobile App
* 💻 Frontend Web
* 🤖 IOT
* ☁️ Backend API (este projeto)
---

## 📌 Status

🚧 Em desenvolvimento contínuo

---

## 👩‍💻 Autoria

<p align="center">
  <strong>Direitos totalmente reservados a:</strong>
</p>

<p align="center">
  Allan Leal da Luz<br>
  André Mendes Godek<br>
  Christian Soares Maia<br>
  Izabella Carolina Hermano Alves
</p>

---

## 📄 Licença

Uso educacional.
