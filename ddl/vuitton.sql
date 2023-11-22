/*
 Navicat Premium Data Transfer

 Source Server         : vuitton
 Source Server Type    : SQLite
 Source Server Version : 3036000 (3.36.0)
 Source Schema         : main

 Target Server Type    : SQLite
 Target Server Version : 3036000 (3.36.0)
 File Encoding         : 65001

 Date: 22/11/2023 20:30:22
*/

PRAGMA foreign_keys = false;

-- ----------------------------
-- Table structure for video
-- ----------------------------
DROP TABLE IF EXISTS "video";
CREATE TABLE "video" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "name" TEXT NOT NULL,
  "description" TEXT,
  "cover_img" TEXT
);

-- ----------------------------
-- Table structure for video_episode
-- ----------------------------
DROP TABLE IF EXISTS "video_episode";
CREATE TABLE "video_episode" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "video" TEXT NOT NULL,
  "episode_index" INTEGER NOT NULL,
  "episode_name" TEXT NOT NULL,
  "episode_url" TEXT NOT NULL
);

PRAGMA foreign_keys = true;
