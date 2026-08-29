DROP TABLE IF EXISTS consumption_record;
DROP TABLE IF EXISTS order_bill;
DROP TABLE IF EXISTS recharge_record;
DROP TABLE IF EXISTS table_session;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS billiard_table;
DROP TABLE IF EXISTS member_level;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50),
  role TINYINT NOT NULL,
  status TINYINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE member_level (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  discount DECIMAL(3,2) NOT NULL,
  points_threshold INT NOT NULL,
  status TINYINT NOT NULL
);

CREATE TABLE member (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  card_no VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) UNIQUE,
  level_id INT NOT NULL,
  balance DECIMAL(10,2) NOT NULL DEFAULT 0,
  points INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_test_member_level FOREIGN KEY (level_id) REFERENCES member_level(id)
);

CREATE TABLE billiard_table (
  id INT AUTO_INCREMENT PRIMARY KEY,
  table_no VARCHAR(10) NOT NULL UNIQUE,
  table_type VARCHAR(20) NOT NULL,
  price_per_hour DECIMAL(10,2) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(200)
);

CREATE TABLE table_session (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_no VARCHAR(30) NOT NULL UNIQUE,
  table_id INT NOT NULL,
  member_id BIGINT,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP,
  status TINYINT NOT NULL,
  operator_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_test_session_table FOREIGN KEY (table_id) REFERENCES billiard_table(id),
  CONSTRAINT fk_test_session_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_test_session_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
);

CREATE TABLE recharge_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  record_no VARCHAR(30) NOT NULL UNIQUE,
  member_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  gift_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  pay_way TINYINT NOT NULL,
  operator_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(200),
  CONSTRAINT fk_test_recharge_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_test_recharge_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
);

CREATE TABLE order_bill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bill_no VARCHAR(30) NOT NULL UNIQUE,
  session_id BIGINT NOT NULL,
  member_id BIGINT,
  duration_hours DECIMAL(6,2) NOT NULL,
  original_amount DECIMAL(10,2) NOT NULL,
  discount_rate DECIMAL(3,2) NOT NULL,
  discount_amount DECIMAL(10,2) NOT NULL,
  final_amount DECIMAL(10,2) NOT NULL,
  pay_way TINYINT NOT NULL,
  points_earned INT NOT NULL,
  operator_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  remark VARCHAR(200),
  CONSTRAINT fk_test_bill_session FOREIGN KEY (session_id) REFERENCES table_session(id),
  CONSTRAINT fk_test_bill_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_test_bill_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id)
);

CREATE TABLE consumption_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  member_id BIGINT,
  bill_id BIGINT,
  type TINYINT NOT NULL,
  item_name VARCHAR(50) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_test_consume_member FOREIGN KEY (member_id) REFERENCES member(id),
  CONSTRAINT fk_test_consume_bill FOREIGN KEY (bill_id) REFERENCES order_bill(id)
);

INSERT INTO member_level (id, name, discount, points_threshold, status) VALUES
(1, '普通会员', 1.00, 0, 1),
(2, '银卡会员', 0.95, 500, 1),
(3, '金卡会员', 0.90, 2000, 1);

INSERT INTO sys_user (id, username, password, real_name, role, status) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '测试管理员', 1, 1),
(2, 'cashier', 'e10adc3949ba59abbe56e057f20f883e', '测试前台', 2, 1),
(3, 'disabled', 'e10adc3949ba59abbe56e057f20f883e', '停用账号', 2, 0);

INSERT INTO billiard_table (id, table_no, table_type, price_per_hour, status) VALUES
(1, 'C01', '中式台球', 20.00, 0),
(2, 'B01', '九球', 28.00, 2);
