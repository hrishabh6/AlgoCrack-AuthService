-- V1__init_auth_schema.sql
-- AuthService schema — owns user table
-- Generated from: mysqldump --no-data leetcode (2026-03-05)

CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) NOT NULL,
  `img_url` varchar(255) DEFAULT NULL,
  `achievement_points` int DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `ranking` varchar(255) DEFAULT NULL,
  `about` varchar(255) DEFAULT NULL,
  `github_profile` varchar(255) DEFAULT NULL,
  `headline` varchar(255) DEFAULT NULL,
  `linkedin_profile` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `school` varchar(255) DEFAULT NULL,
  `skills` varchar(255) DEFAULT NULL,
  `twitter_profile` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
