-- 从“用户端 + 当天预约”版本升级会员资料，仅执行一次。
USE `billiard_club`;

ALTER TABLE `member`
  ADD COLUMN `gender` TINYINT DEFAULT NULL COMMENT '性别:0保密 1男 2女' AFTER `phone`,
  ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT '出生日期' AFTER `gender`;
