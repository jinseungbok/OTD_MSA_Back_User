
UPDATE inquiry
SET status = '01'
WHERE status = 'PENDING';

UPDATE inquiry
SET status = '02'
WHERE status = 'RESOLVED';

UPDATE inquiry
SET status = '01'
WHERE status IS NULL;