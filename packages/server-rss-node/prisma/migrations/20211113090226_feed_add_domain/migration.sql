/*
  Warnings:

  - You are about to alter the column `content_raw_mime` on the `Article` table. The data in that column could be lost. The data in that column will be cast from `VarChar(500)` to `VarChar(50)`.
  - Added the required column `domain` to the `Feed` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE `Article` MODIFY `content_raw_mime` VARCHAR(50);

-- AlterTable
ALTER TABLE `Feed` ADD COLUMN `domain` VARCHAR(191) NOT NULL;

-- CreateTable
CREATE TABLE `Comment` (
    `id` VARCHAR(191) NOT NULL,
    `rootId` VARCHAR(191) NOT NULL,
    `ownerId` VARCHAR(191) NOT NULL,
    `content_raw_mime` VARCHAR(50) NOT NULL DEFAULT 'text/markdown',
    `content_raw` LONGTEXT NOT NULL,
    `content_html` LONGTEXT NOT NULL,
    `commentId` VARCHAR(191),

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `Comment` ADD FOREIGN KEY (`rootId`) REFERENCES `Article`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Comment` ADD FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Comment` ADD FOREIGN KEY (`commentId`) REFERENCES `Comment`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AlterIndex
ALTER TABLE `Article` RENAME INDEX `Article_url_key` TO `Article.url_unique`;

-- AlterIndex
ALTER TABLE `ArticlePostProcessor` RENAME INDEX `ArticlePostProcessor_type_key` TO `ArticlePostProcessor.type_unique`;

-- AlterIndex
ALTER TABLE `EventHook` RENAME INDEX `EventHook_script_source_url_key` TO `EventHook.script_source_url_unique`;

-- AlterIndex
ALTER TABLE `Feed` RENAME INDEX `Feed_feed_url_ownerId_key` TO `Feed.feed_url_ownerId_unique`;

-- AlterIndex
ALTER TABLE `NoFollowUrl` RENAME INDEX `NoFollowUrl_url_prefix_key` TO `NoFollowUrl.url_prefix_unique`;

-- AlterIndex
ALTER TABLE `Notebook` RENAME INDEX `Notebook_name_ownerId_key` TO `Notebook.name_ownerId_unique`;

-- AlterIndex
ALTER TABLE `User` RENAME INDEX `User_email_key` TO `User.email_unique`;
