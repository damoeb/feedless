/*
  Warnings:

  - You are about to drop the column `content_resolution` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `filter_expression` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `replay_policy` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `retention_policy` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `used_for` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `throttleId` on the `Subscription` table. All the data in the column will be lost.
  - You are about to drop the `ReleaseThrottle` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE `Subscription` DROP FOREIGN KEY `Subscription_ibfk_4`;

-- AlterTable
ALTER TABLE `ArticlePostProcessor` ADD COLUMN `context` JSON;

-- AlterTable
ALTER TABLE `Bucket` DROP COLUMN `content_resolution`,
    DROP COLUMN `filter_expression`,
    DROP COLUMN `replay_policy`,
    DROP COLUMN `retention_policy`,
    DROP COLUMN `used_for`,
    ADD COLUMN `output_reduce_throttle_count` INTEGER,
    ADD COLUMN `output_reduce_throttle_sort_direction` VARCHAR(191),
    ADD COLUMN `output_reduce_throttle_sort_field` VARCHAR(191),
    ADD COLUMN `segment_allocation_by_content` VARCHAR(191),
    ADD COLUMN `segment_allocation_by_time` JSON,
    ADD COLUMN `tags` JSON,
    ADD COLUMN `trigger_refresh_on` VARCHAR(191);

-- AlterTable
ALTER TABLE `Subscription` DROP COLUMN `throttleId`;

-- DropTable
DROP TABLE `ReleaseThrottle`;
