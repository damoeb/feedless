/*
  Warnings:

  - You are about to drop the column `output_reduce_throttle_count` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `output_reduce_throttle_sort_direction` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `output_reduce_throttle_sort_field` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `segment_allocation_by_content` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `segment_allocation_by_time` on the `Bucket` table. All the data in the column will be lost.
  - A unique constraint covering the columns `[feed_url,ownerId]` on the table `Feed` will be added. If there are existing duplicate values, this will fail.
  - Made the column `trigger_refresh_on` on table `Bucket` required. This step will fail if there are existing NULL values in that column.
  - Made the column `ownerId` on table `Feed` required. This step will fail if there are existing NULL values in that column.

*/
-- DropForeignKey
ALTER TABLE `Feed` DROP FOREIGN KEY `Feed_ibfk_2`;

-- DropIndex
DROP INDEX `Feed.feed_url_unique` ON `Feed`;

-- AlterTable
ALTER TABLE `Bucket` DROP COLUMN `output_reduce_throttle_count`,
    DROP COLUMN `output_reduce_throttle_sort_direction`,
    DROP COLUMN `output_reduce_throttle_sort_field`,
    DROP COLUMN `segment_allocation_by_content`,
    DROP COLUMN `segment_allocation_by_time`,
    ADD COLUMN `segment_size` INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN `segment_sort_asc` BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN `filter` VARCHAR(191),
    ADD COLUMN `segment_sort_field` VARCHAR(191),
    ADD COLUMN `trigger_scheduled` VARCHAR(191),
    ADD COLUMN `trigger_scheduled_last_at` DATETIME(3),
    ADD COLUMN `trigger_scheduled_next_at` DATETIME(3),
    MODIFY `trigger_refresh_on` VARCHAR(191) NOT NULL DEFAULT 'change';

-- AlterTable
ALTER TABLE `Feed` MODIFY `ownerId` VARCHAR(191) NOT NULL DEFAULT 'system';

-- CreateIndex
CREATE UNIQUE INDEX `Feed.feed_url_ownerId_unique` ON `Feed`(`feed_url`, `ownerId`);

-- AddForeignKey
ALTER TABLE `Feed` ADD FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
