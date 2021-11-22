/*
  Warnings:

  - You are about to drop the column `filter` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `lastUpdatedAt` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `segment_size` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `segment_sort_asc` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `segment_sort_field` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `trigger_refresh_on` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `trigger_scheduled` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `trigger_scheduled_last_at` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `trigger_scheduled_next_at` on the `Bucket` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `Bucket` DROP COLUMN `filter`,
    DROP COLUMN `lastUpdatedAt`,
    DROP COLUMN `segment_size`,
    DROP COLUMN `segment_sort_asc`,
    DROP COLUMN `segment_sort_field`,
    DROP COLUMN `trigger_refresh_on`,
    DROP COLUMN `trigger_scheduled`,
    DROP COLUMN `trigger_scheduled_last_at`,
    DROP COLUMN `trigger_scheduled_next_at`;

-- CreateTable
CREATE TABLE `ArticleExporter` (
    `id` VARCHAR(191) NOT NULL,
    `segment` BOOLEAN NOT NULL DEFAULT false,
    `segment_sort_field` VARCHAR(191),
    `segment_sort_asc` BOOLEAN NOT NULL DEFAULT true,
    `segment_size` INTEGER,
    `segment_digest` BOOLEAN,
    `lastUpdatedAt` DATETIME(3),
    `trigger_refresh_on` VARCHAR(191) NOT NULL DEFAULT 'change',
    `trigger_scheduled_last_at` DATETIME(3),
    `trigger_scheduled_next_at` DATETIME(3),
    `trigger_scheduled` VARCHAR(191),
    `bucketId` VARCHAR(191) NOT NULL,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `ArticleExporterTarget` (
    `id` VARCHAR(191) NOT NULL,
    `type` VARCHAR(191) NOT NULL,
    `context` JSON,
    `exporterId` VARCHAR(191) NOT NULL,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `ArticleExporter` ADD FOREIGN KEY (`bucketId`) REFERENCES `Bucket`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `ArticleExporterTarget` ADD FOREIGN KEY (`exporterId`) REFERENCES `ArticleExporter`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
