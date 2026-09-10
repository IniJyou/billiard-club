-- 从阶段三原版升级到“用户端 + 当天预约”版本，仅执行一次。
USE `billiard_club`;

ALTER TABLE `member`
  ADD COLUMN `user_id` BIGINT DEFAULT NULL COMMENT '绑定的用户账号ID' AFTER `id`,
  ADD UNIQUE KEY `uk_member_user` (`user_id`),
  ADD CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`);

CREATE TABLE `table_reservation` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `reservation_no` VARCHAR(30)  NOT NULL,
  `user_id`        BIGINT       NOT NULL,
  `member_id`      BIGINT       NOT NULL,
  `table_id`       INT          NOT NULL,
  `start_time`     DATETIME     NOT NULL,
  `end_time`       DATETIME     NOT NULL,
  `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0待处理 1已开台 2已完成 3已取消',
  `session_id`     BIGINT       DEFAULT NULL,
  `remark`         VARCHAR(200) DEFAULT NULL,
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cancel_time`    DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reservation_no` (`reservation_no`),
  UNIQUE KEY `uk_reservation_session` (`session_id`),
  KEY `idx_reservation_table_time` (`table_id`, `start_time`, `end_time`),
  KEY `idx_reservation_user` (`user_id`),
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_reservation_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `fk_reservation_table` FOREIGN KEY (`table_id`) REFERENCES `billiard_table` (`id`),
  CONSTRAINT `fk_reservation_session` FOREIGN KEY (`session_id`) REFERENCES `table_session` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='球桌预约表';

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `status`)
SELECT '13900000000', 'e10adc3949ba59abbe56e057f20f883e', '演示用户', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = '13900000000');
