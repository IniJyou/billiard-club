-- =====================================================================
-- 台球厅会员管理系统 数据库初始化脚本
-- 数据库: billiard_club   字符集: utf8mb4
-- 用法: mysql -u root -p < init.sql   （或复制到 Navicat/DBeaver 执行）
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `billiard_club`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `billiard_club`;

-- 关闭外键检查，便于按任意顺序 DROP
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `consumption_record`;
DROP TABLE IF EXISTS `order_bill`;
DROP TABLE IF EXISTS `recharge_record`;
DROP TABLE IF EXISTS `table_session`;
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `billiard_table`;
DROP TABLE IF EXISTS `member_level`;
DROP TABLE IF EXISTS `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 1. 系统用户表（员工/管理员）
-- =====================================================================
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录名',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码(MD5)',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  `role`        TINYINT      NOT NULL DEFAULT 2 COMMENT '角色:1管理员 2前台',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1启用 0禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- =====================================================================
-- 2. 会员等级表
-- =====================================================================
CREATE TABLE `member_level` (
  `id`               INT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`             VARCHAR(20) NOT NULL COMMENT '等级名称',
  `discount`         DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率(1.00=无折扣,0.85=85折)',
  `points_threshold` INT         NOT NULL DEFAULT 0 COMMENT '升级所需积分',
  `status`           TINYINT     NOT NULL DEFAULT 1 COMMENT '状态:1启用 0停用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- =====================================================================
-- 3. 会员表
-- =====================================================================
CREATE TABLE `member` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `card_no`     VARCHAR(20)   NOT NULL COMMENT '会员卡号',
  `name`        VARCHAR(50)   NOT NULL COMMENT '姓名',
  `phone`       VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
  `level_id`    INT           NOT NULL DEFAULT 1 COMMENT '等级ID',
  `balance`     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `points`      INT           NOT NULL DEFAULT 0 COMMENT '积分',
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:1正常 0挂失/停用',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_no` (`card_no`),
  UNIQUE KEY `uk_phone` (`phone`),
  CONSTRAINT `fk_member_level` FOREIGN KEY (`level_id`) REFERENCES `member_level` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- =====================================================================
-- 4. 球桌表
-- =====================================================================
CREATE TABLE `billiard_table` (
  `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `table_no`        VARCHAR(10)  NOT NULL COMMENT '桌号',
  `table_type`      VARCHAR(20)  NOT NULL COMMENT '桌型:斯诺克/九球/中式台球',
  `price_per_hour`  DECIMAL(10,2) NOT NULL COMMENT '每小时单价(元)',
  `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态:0空闲 1使用中 2维护',
  `remark`          VARCHAR(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_table_no` (`table_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='球桌表';

-- =====================================================================
-- 5. 开台订单表
-- =====================================================================
CREATE TABLE `table_session` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_no`  VARCHAR(30) NOT NULL COMMENT '开台单号',
  `table_id`    INT         NOT NULL COMMENT '球桌ID',
  `member_id`   BIGINT      DEFAULT NULL COMMENT '会员ID(散客为空)',
  `start_time`  DATETIME    NOT NULL COMMENT '开台时间',
  `end_time`    DATETIME    DEFAULT NULL COMMENT '结账时间',
  `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态:0进行中 1已结账 2已取消',
  `operator_id` BIGINT      NOT NULL COMMENT '操作员ID',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_no` (`session_no`),
  KEY `idx_table_id` (`table_id`),
  KEY `idx_member_id` (`member_id`),
  CONSTRAINT `fk_session_table`    FOREIGN KEY (`table_id`)    REFERENCES `billiard_table` (`id`),
  CONSTRAINT `fk_session_member`   FOREIGN KEY (`member_id`)   REFERENCES `member` (`id`),
  CONSTRAINT `fk_session_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开台订单表';

-- =====================================================================
-- 6. 结账单表
-- =====================================================================
CREATE TABLE `order_bill` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bill_no`         VARCHAR(30)   NOT NULL COMMENT '账单号',
  `session_id`      BIGINT        NOT NULL COMMENT '开台单ID',
  `member_id`       BIGINT        DEFAULT NULL COMMENT '会员ID(散客为空)',
  `duration_hours`  DECIMAL(6,2)  NOT NULL COMMENT '消费时长(小时)',
  `original_amount` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `discount_rate`   DECIMAL(3,2)  NOT NULL DEFAULT 1.00 COMMENT '折扣率',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `final_amount`    DECIMAL(10,2) NOT NULL COMMENT '实收金额',
  `pay_way`         TINYINT       NOT NULL COMMENT '支付方式:1现金 2会员余额 3挂账',
  `points_earned`   INT           NOT NULL DEFAULT 0 COMMENT '本次获得积分',
  `operator_id`     BIGINT        NOT NULL COMMENT '操作员ID',
  `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结账时间',
  `remark`          VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_no` (`bill_no`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_member_id` (`member_id`),
  CONSTRAINT `fk_bill_session`  FOREIGN KEY (`session_id`)  REFERENCES `table_session` (`id`),
  CONSTRAINT `fk_bill_member`   FOREIGN KEY (`member_id`)   REFERENCES `member` (`id`),
  CONSTRAINT `fk_bill_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结账单表';

-- =====================================================================
-- 7. 充值记录表
-- =====================================================================
CREATE TABLE `recharge_record` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_no`   VARCHAR(30)   NOT NULL COMMENT '充值单号',
  `member_id`   BIGINT        NOT NULL COMMENT '会员ID',
  `amount`      DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  `gift_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '赠送金额',
  `pay_way`     TINYINT       NOT NULL COMMENT '支付方式:1现金 2微信 3支付宝 4银行卡',
  `operator_id` BIGINT        NOT NULL COMMENT '操作员ID',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  `remark`      VARCHAR(200)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_member_id` (`member_id`),
  CONSTRAINT `fk_recharge_member`   FOREIGN KEY (`member_id`)   REFERENCES `member` (`id`),
  CONSTRAINT `fk_recharge_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- =====================================================================
-- 8. 消费流水表（台费/商品等消费明细）
-- =====================================================================
CREATE TABLE `consumption_record` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id`   BIGINT        DEFAULT NULL COMMENT '会员ID',
  `bill_id`     BIGINT        DEFAULT NULL COMMENT '结账单ID',
  `type`        TINYINT       NOT NULL COMMENT '消费类型:1台费 2商品',
  `item_name`   VARCHAR(50)   NOT NULL COMMENT '消费项目',
  `amount`      DECIMAL(10,2) NOT NULL COMMENT '金额',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_bill_id` (`bill_id`),
  CONSTRAINT `fk_consume_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`),
  CONSTRAINT `fk_consume_bill`   FOREIGN KEY (`bill_id`)   REFERENCES `order_bill` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费流水表';

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- 会员等级：普通/银卡/金卡/钻石
INSERT INTO `member_level` (`id`, `name`, `discount`, `points_threshold`) VALUES
(1, '普通会员', 1.00, 0),
(2, '银卡会员', 0.95, 500),
(3, '金卡会员', 0.90, 2000),
(4, '钻石会员', 0.85, 5000);

-- 系统用户：密码均为 MD5("123456") = e10adc3949ba59abbe56e057f20f883e
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin',   'e10adc3949ba59abbe56e057f20f883e', '老板(管理员)', 1, 1),
('cashier', 'e10adc3949ba59abbe56e057f20f883e', '前台小王',      2, 1);

-- 球桌：2 张斯诺克 + 3 张九球 + 3 张中式
INSERT INTO `billiard_table` (`table_no`, `table_type`, `price_per_hour`, `status`) VALUES
('A01', '斯诺克',   38.00, 0),
('A02', '斯诺克',   38.00, 0),
('B01', '九球',     28.00, 0),
('B02', '九球',     28.00, 0),
('B03', '九球',     28.00, 0),
('C01', '中式台球', 20.00, 0),
('C02', '中式台球', 20.00, 0),
('C03', '中式台球', 20.00, 0);

-- 示例会员
INSERT INTO `member` (`card_no`, `name`, `phone`, `level_id`, `balance`, `points`) VALUES
('M20260001', '张三', '13800000001', 1, 200.00,  0),
('M20260002', '李四', '13800000002', 2, 500.00,  800),
('M20260003', '王五', '13800000003', 3, 1000.00, 2500);
