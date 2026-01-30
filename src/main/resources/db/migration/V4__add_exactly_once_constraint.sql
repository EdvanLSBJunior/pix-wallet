-- Adiciona índice único composto para garantir exactly-once no processamento de webhooks
-- Previne múltiplas atualizações de status para o mesmo endToEndId + timestamp

CREATE UNIQUE INDEX idx_pix_transfer_webhook_exactly_once
ON pix_transfer (end_to_end_id, status, last_status_update);

-- Adiciona comentário explicativo
COMMENT ON INDEX idx_pix_transfer_webhook_exactly_once IS
'Garantia exactly-once: previne processamento duplicado do mesmo evento webhook';
