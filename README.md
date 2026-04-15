# 🌿 Monsai — API Backend

<p align="center">
  Backend robusto para monitoramento inteligente com IoT, autenticação segura e processamento em tempo real.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.2-02502c?style=for-the-badge&logo=springboot"/>
  <img src="https://img.shields.io/badge/Java-21-227e35?style=for-the-badge&logo=openjdk"/>
  <img src="https://img.shields.io/badge/MySQL-8.x-096732?style=for-the-badge&logo=mysql"/>
  <img src="https://img.shields.io/badge/MQTT-RealTime-649c7e?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Auth-JWT-5cb52d?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/API-OpenAPI%20%2F%20Swagger-b0d693?style=for-the-badge"/>
</p>

---

## 📌 Sobre o Projeto

O **Monsai Backend** é uma API desenvolvida com foco em **monitoramento inteligente e integração com dispositivos IoT**, permitindo o recebimento de dados em tempo real via MQTT, persistência em banco relacional e exposição de endpoints seguros via JWT.

💡 Ideal para sistemas como:

* Monitoramento ambiental
* Sensores inteligentes
* Sistemas acadêmicos com controle em tempo real
* Plataformas de telemetria

---

## ⚙️ Pré-requisitos

Antes de rodar o projeto:

* ☕ **Java 21 (JDK)**
* 📦 **Maven 3.9+**
* 🐬 **MySQL 8.x**
* 📡 **Broker MQTT (ex: Mosquitto)**

---

## 📁 Clone e Estrutura

### 🔽 Clone o repositório

```bash
git clone https://github.com/seu-org/aaci-monsai-backend.git
cd aaci-monsai-backend
```

### 🗂 Estrutura do projeto

```bash
src/
├── main/
│   ├── java/com/senai/monsai/
│   │   ├── application/
│   │   │   ├── dto/
│   │   │   └── service/
│   │   └── infrastructure/
│   │       └── config/
│   └── resources/
│       └── application.properties
pom.xml
```

---

## 🔐 Configuração do Ambiente

⚠️ O arquivo `.env` não é versionado.

### 📄 Crie um `.env` na raiz:

```env
# JWT
JWT_SECRET=monsaiSistemaSeguroJwtChaveMuitoForte123456
JWT_EXPIRATION=86400000

# MySQL
DB_URL=jdbc:mysql://localhost:3306/monsai_db?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=sua_senha_mysql

# MQTT
MQTT_BROKER_URL=tcp://localhost:1883
MQTT_CLIENT_ID=monsai-backend-api
MQTT_USERNAME=
MQTT_PASSWORD=
MQTT_TOPIC=monsai/telemetria
MQTT_KEEP_ALIVE=60
MQTT_QOS=1
```

---

## 🗄️ Banco de Dados

✔ Criado automaticamente
✔ Gerenciado pelo Hibernate (`ddl-auto=update`)

### (Opcional) Criar manualmente:

```sql
CREATE DATABASE monsai_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 🚀 Como Executar

### ▶️ Rodar com Maven

```bash
./mvnw spring-boot:run
```

### ▶️ Build + execução

```bash
./mvnw clean package -DskipTests
java -jar target/monsai-0.0.1-SNAPSHOT.jar
```

### ✅ Verificar API

```bash
curl http://localhost:8080/actuator/health
```

---

## 📖 Documentação da API

### 🔗 Acesse:

* Swagger UI: http://localhost:8080/swagger-ui/index.html
* OpenAPI: http://localhost:8080/v3/api-docs

### 🔐 Autenticação

A API utiliza **JWT**.

No Swagger:

```
Bearer seu_token
```

---

## 📡 Integração MQTT

O sistema consome dados em tempo real via MQTT:

* Broker: `tcp://localhost:1883`
* Topic: `monsai/telemetria`
* QoS: `1`

📥 Ideal para ingestão contínua de dados de sensores IoT.

---

## 📦 Principais Tecnologias

| Tecnologia      | Função              |
| --------------- | ------------------- |
| Spring Boot     | Framework principal |
| Spring Security | Autenticação        |
| JJWT            | Tokens JWT          |
| Spring Data JPA | Persistência        |
| MySQL           | Banco relacional    |
| MQTT (Paho)     | Comunicação IoT     |
| Swagger/OpenAPI | Documentação        |

---

## 🧠 Arquitetura

O projeto segue uma estrutura organizada em camadas:

* **Application** → regras de negócio
* **DTOs** → comunicação entre camadas
* **Infrastructure** → configs e integrações externas

---

## 💡 Diferenciais

✨ Integração com IoT via MQTT
🔐 Segurança com JWT
📊 Estrutura escalável
⚡ Processamento em tempo real
📄 Documentação automática com Swagger

---

## 📌 Status do Projeto

🚧 Em desenvolvimento / evolução contínua

---

## 👩‍💻 Autoria

Projeto desenvolvido no contexto acadêmico pelo SENAI.

---

## 📎 Licença

Este projeto é para fins educacionais.
