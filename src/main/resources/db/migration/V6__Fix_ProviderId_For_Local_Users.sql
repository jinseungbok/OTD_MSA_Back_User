UPDATE user
SET provider_id = CONCAT('LOCAL_', uid)
WHERE provider_type = '01'
  AND (provider_id LIKE 'UNKNOWN_%' OR provider_id IS NULL);