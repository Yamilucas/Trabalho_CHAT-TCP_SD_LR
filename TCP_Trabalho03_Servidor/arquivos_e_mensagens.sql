CREATE DATABASE servidor_arquivos_chat;
USE servidor_arquivos_chat;

CREATE TABLE arquivos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    tamanho BIGINT NOT NULL,
    caminho VARCHAR(500) NOT NULL,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mensagens_chat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_nome VARCHAR(100) NOT NULL,
    mensagem TEXT NOT NULL,
    data_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);