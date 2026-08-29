-- 已导入旧版 init.sql 的数据库执行本迁移；全新安装只需执行 init.sql。
USE `billiard_club`;

-- 把普通手机号索引升级为唯一索引。执行前若存在重复手机号，需先人工合并数据。
SET @has_old_phone_index = (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'member' AND index_name = 'idx_phone'
);
SET @drop_old_phone_index_sql = IF(
  @has_old_phone_index > 0,
  'ALTER TABLE `member` DROP INDEX `idx_phone`',
  'SELECT 1'
);
PREPARE drop_old_phone_index_stmt FROM @drop_old_phone_index_sql;
EXECUTE drop_old_phone_index_stmt;
DEALLOCATE PREPARE drop_old_phone_index_stmt;

SET @has_unique_phone_index = (
  SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'member' AND index_name = 'uk_phone'
);
SET @add_unique_phone_index_sql = IF(
  @has_unique_phone_index = 0,
  'ALTER TABLE `member` ADD UNIQUE KEY `uk_phone` (`phone`)',
  'SELECT 1'
);
PREPARE add_unique_phone_index_stmt FROM @add_unique_phone_index_sql;
EXECUTE add_unique_phone_index_stmt;
DEALLOCATE PREPARE add_unique_phone_index_stmt;
