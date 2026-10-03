ALTER TABLE `user`
  ADD COLUMN `role` varchar(20) NOT NULL DEFAULT 'USER' AFTER `user_id`;

CREATE INDEX `idx_user_role` ON `user` (`role`);
