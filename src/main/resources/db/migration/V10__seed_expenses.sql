INSERT INTO
    expenses (
        category_id,
        description,
        supplier_name,
        expense_date,
        total_amount,
        vat_amount,
        net_amount,
        payment_method
    )
VALUES
    (
        1,
        'Gasóleo',
        'Bomba de combustível',
        '2026-09-05',
        61.50,
        11.50,
        50.00,
        'CARD'
    ),
    (
        2,
        'Almoço com cliente',
        'Restaurante Central',
        '2026-09-12',
        25.80,
        2.97,
        22.83,
        'MB_WAY'
    ),
    (
        3,
        'Papel e tinteiros',
        'Papelaria Moderna',
        '2026-09-20',
        36.90,
        6.90,
        30.00,
        'CASH'
    ),
    (
        4,
        'Subscrição de alojamento web',
        'Fornecedor de hosting',
        '2026-10-01',
        24.60,
        4.60,
        20.00,
        'TRANSFER'
    );