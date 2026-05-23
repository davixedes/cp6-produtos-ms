CREATE TABLE produto (
    codigo     BINARY(16)     NOT NULL,
    nome       VARCHAR(255),
    descricao  TEXT,
    preco      DECIMAL(15,2),
    categoria  VARCHAR(255),
    PRIMARY KEY (codigo)
);
