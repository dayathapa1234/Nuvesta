INSERT INTO symbol_info (symbol, name, exchange, asset_type, ipo_date, delisting_date, status)
VALUES ('^GSPC', 'S&P 500 Index', 'INDEX', 'INDEX', '1957-03-04', NULL, 'ACTIVE')
ON CONFLICT (symbol) DO NOTHING;

