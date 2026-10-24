# Tutorial: rodando o AikaEmu localmente (teste)

Testado em Ubuntu 24.04 com .NET 8 e MariaDB 10.11 (também funciona no Windows com MySQL 8 e .NET 8 SDK).

## 1. Requisitos

| Item | Linux (apt) | Windows |
|---|---|---|
| .NET SDK 8 | `sudo apt install dotnet-sdk-8.0` | https://dotnet.microsoft.com/download |
| MySQL 8 ou MariaDB 10.x | `sudo apt install mariadb-server` | https://dev.mysql.com/downloads/ |
| Cliente do jogo (AikaBR) | você precisa ter o seu; **não faz parte do repositório** | idem |

## 2. Banco de dados

```bash
sudo service mariadb start        # ou: sudo mysqld_safe &
# O Config.json usa usuario root SEM senha. No MariaDB o root usa unix_socket,
# entao (apenas em maquina de desenvolvimento) troque para senha vazia:
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('');"

mysql -uroot -e "CREATE DATABASE aikaemu_auth; CREATE DATABASE aikaemu_game;"
mysql -uroot aikaemu_auth < sql/aikaemu_auth.sql
mysql -uroot aikaemu_game < sql/aikaemu_game.sql
mysql -uroot -e "INSERT INTO aikaemu_auth.game_servers(id,name,ip,port) VALUES (1,'AikaEmu','127.0.0.1',8822);
                 INSERT INTO aikaemu_auth.accounts(user,pass) VALUES ('admin','21232f297a57a5a743894a0e4a801fc3');"
```

Conta de teste: usuário `admin`, senha `admin` (o banco guarda o MD5).
Se for usar senha no root, ajuste `Config.json` dos três servidores (campo `Database.Pass`).

## 3. Compilar

```bash
dotnet publish src/AikaEmu.AuthServer -c Release -o out/AuthServer
dotnet publish src/AikaEmu.GameServer -c Release -o out/GameServer
dotnet publish src/AikaEmu.WebServer  -c Release -o out/WebServer
```

## 4. Iniciar (3 terminais, nesta ordem)

```bash
cd out/AuthServer && dotnet AikaEmu.AuthServer.dll   # portas 8831 (clientes) e 8832 (interna)
cd out/GameServer && dotnet AikaEmu.GameServer.dll   # porta 8822; leva ~30 s carregando dados
cd out/WebServer  && dotnet AikaEmu.WebServer.dll    # https 8090
```

Cada servidor deve ficar rodando (ele espera ENTER para encerrar — não pressione). Se for rodar em
segundo plano, mantenha o stdin aberto: `tail -f /dev/null | dotnet AikaEmu.GameServer.dll`.

Sinal de sucesso no GameServer: `GameServer started with success` e, no AuthServer,
`GameServer 1 registered with success`.

### Teste rápido sem o cliente

```bash
dotnet dev-certs https --trust       # certificado local (Windows/macOS)
curl -k -X POST -d 'id=admin&pw=21232f297a57a5a743894a0e4a801fc3' https://127.0.0.1:8090/member/aika_get_token.asp
```
Deve retornar um hash MD5 (login OK). Retornando `0` ou número negativo = falha de login.

## 5. Cliente

1. Copie `client/SL.bin` para a pasta do cliente AikaBR (aponta para 127.0.0.1).
2. Edite o `Settings.txt` do cliente conforme `src/AikaEmu.WebServer/README.md`
   (URLs `https://localhost:8090/...`).
3. Abra o cliente e entre com `admin` / `admin`.

## 6. Problemas comuns

| Sintoma | Causa / solução |
|---|---|
| `NLog.config not found` | Fixado: eram caminhos com `\` (só Windows). Atualize o código. |
| `Unknown collation: utf8mb4_0900_ai_ci` | Fixado nos `.sql` (agora `utf8mb4_general_ci`). |
| `Access denied for user 'root'` | Veja o `ALTER USER` do passo 2. |
| `Expected end of data packet` | Driver antigo com MariaDB; fixado atualizando `MySql.Data` para 8.4.0. |
| `Endpoint Routing does not support UseMvc` | Fixado em `Startup.cs`. |
