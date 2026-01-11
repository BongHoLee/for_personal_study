-- =====================================================
-- Secondary DB Schema (new_db)
-- 물리적으로 분리된 두 번째 MySQL 데이터베이스
-- =====================================================

-- new_customers 테이블 (type='NEW'인 고객 데이터 저장)
CREATE TABLE IF NOT EXISTS new_customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_new_customers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
