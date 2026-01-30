-- Adiciona campos de status e timestamp de última atualização na tabela pix_transfer
ALTER TABLE pix_transfer
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
ADD COLUMN last_status_update TIMESTAMP;

-- Atualiza os registros existentes com o timestamp atual
UPDATE pix_transfer
SET last_status_update = created_at
WHERE last_status_update IS NULL;
