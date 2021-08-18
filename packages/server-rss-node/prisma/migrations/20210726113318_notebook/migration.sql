/*
  Warnings:

  - You are about to drop the `UserArticle` table. If the table is not empty, all the data it contains will be lost.
  - You are about to drop the `UserFeed` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE `UserArticle` DROP FOREIGN KEY `UserArticle_ibfk_1`;

-- DropForeignKey
ALTER TABLE `UserFeed` DROP FOREIGN KEY `UserFeed_ibfk_1`;

-- AlterTable
ALTER TABLE `Article` MODIFY `url` TEXT;

-- AlterTable
ALTER TABLE `ArticleRef` ADD COLUMN `has_seen` BOOLEAN NOT NULL DEFAULT false;

-- AlterTable
ALTER TABLE `Bucket` ADD COLUMN `in_focus` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `used_for` VARCHAR(191);

-- DropTable
DROP TABLE `UserArticle`;

-- DropTable
DROP TABLE `UserFeed`;

-- CreateTable
CREATE TABLE `Notebook` (
    `id` VARCHAR(191) NOT NULL,
    `name` VARCHAR(191) NOT NULL,
    `description` VARCHAR(191) NOT NULL DEFAULT '',
    `readonly` BOOLEAN NOT NULL DEFAULT false,
    `listed` BOOLEAN NOT NULL DEFAULT false,
    `streamId` VARCHAR(191) NOT NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `ownerId` VARCHAR(191) NOT NULL,

    UNIQUE INDEX `Notebook.name_ownerId_unique`(`name`, `ownerId`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `Notebook` ADD FOREIGN KEY (`streamId`) REFERENCES `Stream`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Notebook` ADD FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
