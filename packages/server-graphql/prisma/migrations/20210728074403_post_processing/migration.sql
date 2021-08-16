/*
  Warnings:

  - You are about to drop the column `bucketId` on the `ArticlePostProcessor` table. All the data in the column will be lost.
  - A unique constraint covering the columns `[type]` on the table `ArticlePostProcessor` will be added. If there are existing duplicate values, this will fail.

*/
-- DropForeignKey
ALTER TABLE `ArticlePostProcessor` DROP FOREIGN KEY `ArticlePostProcessor_ibfk_1`;

-- AlterTable
ALTER TABLE `Article` ADD COLUMN `applyPostProcessors` BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN `has_readability` BOOLEAN,
    ADD COLUMN `lastScoredAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    ADD COLUMN `readability` JSON,
    ADD COLUMN `released` BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN `scores` JSON;

-- AlterTable
ALTER TABLE `ArticlePostProcessor` DROP COLUMN `bucketId`;

-- AlterTable
ALTER TABLE `ArticleRef` ADD COLUMN `date_released` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);

-- AlterTable
ALTER TABLE `Bucket` ADD COLUMN `lastPostProcessedAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);

-- CreateTable
CREATE TABLE `NoFollowUrl` (
    `id` VARCHAR(191) NOT NULL,
    `url_prefix` VARCHAR(191) NOT NULL,

    UNIQUE INDEX `NoFollowUrl.url_prefix_unique`(`url_prefix`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `_ArticlePostProcessorToBucket` (
    `A` VARCHAR(191) NOT NULL,
    `B` VARCHAR(191) NOT NULL,

    UNIQUE INDEX `_ArticlePostProcessorToBucket_AB_unique`(`A`, `B`),
    INDEX `_ArticlePostProcessorToBucket_B_index`(`B`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateIndex
CREATE UNIQUE INDEX `ArticlePostProcessor.type_unique` ON `ArticlePostProcessor`(`type`);

-- AddForeignKey
ALTER TABLE `_ArticlePostProcessorToBucket` ADD FOREIGN KEY (`A`) REFERENCES `ArticlePostProcessor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `_ArticlePostProcessorToBucket` ADD FOREIGN KEY (`B`) REFERENCES `Bucket`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
