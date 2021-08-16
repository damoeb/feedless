/*
  Warnings:

  - A unique constraint covering the columns `[settingsId]` on the table `User` will be added. If there are existing duplicate values, this will fail.
  - Added the required column `settingsId` to the `User` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE `Bucket` MODIFY `in_focus` BOOLEAN NOT NULL DEFAULT true;

-- AlterTable
ALTER TABLE `Feed` ADD COLUMN `is_private` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `ownerId` VARCHAR(191);

-- AlterTable
ALTER TABLE `User` ADD COLUMN `settingsId` VARCHAR(191) NOT NULL;

-- CreateTable
CREATE TABLE `ProfileSettings` (
    `id` VARCHAR(191) NOT NULL,
    `useFulltext` BOOLEAN NOT NULL DEFAULT false,
    `useBetterRead` BOOLEAN NOT NULL DEFAULT false,
    `showNativeTags` BOOLEAN NOT NULL DEFAULT true,
    `showContentTags` BOOLEAN NOT NULL DEFAULT true,
    `queryEngines` JSON,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateIndex
CREATE UNIQUE INDEX `User_settingsId_unique` ON `User`(`settingsId`);

-- AddForeignKey
ALTER TABLE `User` ADD FOREIGN KEY (`settingsId`) REFERENCES `ProfileSettings`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Feed` ADD FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
