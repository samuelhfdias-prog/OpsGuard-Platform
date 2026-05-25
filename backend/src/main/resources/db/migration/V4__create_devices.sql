-- V4: Criação da tabela de dispositivos
-- Sintaxe compatível com H2 (dev) e MySQL (produção via Docker)
CREATE TABLE devices
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    serial_number   VARCHAR(50)  NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    organization_id BIGINT       NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_devices PRIMARY KEY (id),
    CONSTRAINT uq_devices_serial_number UNIQUE (serial_number),
    CONSTRAINT fk_devices_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);
