/*
  Warnings:

  - You are about to drop the column `throttle_expr` on the `Subscription` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `Article` ADD COLUMN `source_url` VARCHAR(191),
    MODIFY `score` DOUBLE DEFAULT 0;

-- AlterTable
ALTER TABLE `ArticleRef` ADD COLUMN `tags` JSON;

-- AlterTable
ALTER TABLE `Feed` ADD COLUMN `fulltext_data` TEXT;

-- AlterTable
ALTER TABLE `Subscription` DROP COLUMN `throttle_expr`,
    ADD COLUMN `throttleId` VARCHAR(191),
    ALTER COLUMN `updatedAt` DROP DEFAULT;

-- CreateTable
CREATE TABLE `ReleaseThrottle` (
    `id` VARCHAR(191) NOT NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `take` INTEGER NOT NULL DEFAULT 10,
    `window` VARCHAR(191) NOT NULL DEFAULT 'd',
    `scoreCriteria` VARCHAR(191),
    `nextReleaseAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `Subscription` ADD FOREIGN KEY (`throttleId`) REFERENCES `ReleaseThrottle`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
