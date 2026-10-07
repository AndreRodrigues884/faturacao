-- O dashboard soma as faturas por data de emissão; sem índice, o PostgreSQL lia a tabela inteira.
CREATE INDEX idx_invoices_issue_date ON invoices (issue_date);