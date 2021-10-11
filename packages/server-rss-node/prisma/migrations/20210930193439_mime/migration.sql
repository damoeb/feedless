/*
  Warnings:

  - You are about to drop the column `content_text` on the `Article` table. All the data in the column will be lost.
  - You are about to drop the column `scores` on the `Article` table. All the data in the column will be lost.
  - Added the required column `content_raw` to the `Article` table without a default value. This is not possible if the table is not empty.

*/
-- DropForeignKey
ALTER TABLE `ArticleRef` DROP FOREIGN KEY `ArticleRef_ibfk_2`;

-- DropForeignKey
ALTER TABLE `ArticleRef` DROP FOREIGN KEY `ArticleRef_ibfk_3`;

-- DropForeignKey
ALTER TABLE `ArticleRef` DROP FOREIGN KEY `ArticleRef_ibfk_1`;

-- DropForeignKey
ALTER TABLE `Bucket` DROP FOREIGN KEY `Bucket_ibfk_1`;

-- DropForeignKey
ALTER TABLE `Bucket` DROP FOREIGN KEY `Bucket_ibfk_2`;

-- DropForeignKey
ALTER TABLE `EventHook` DROP FOREIGN KEY `EventHook_ibfk_1`;

-- DropForeignKey
ALTER TABLE `Feed` DROP FOREIGN KEY `Feed_ibfk_2`;

-- DropForeignKey
ALTER TABLE `Feed` DROP FOREIGN KEY `Feed_ibfk_1`;

-- DropForeignKey
ALTER TABLE `FeedEvent` DROP FOREIGN KEY `FeedEvent_ibfk_1`;

-- DropForeignKey
ALTER TABLE `Notebook` DROP FOREIGN KEY `Notebook_ibfk_2`;

-- DropForeignKey
ALTER TABLE `Notebook` DROP FOREIGN KEY `Notebook_ibfk_1`;

-- DropForeignKey
ALTER TABLE `Subscription` DROP FOREIGN KEY `Subscription_ibfk_3`;

-- DropForeignKey
ALTER TABLE `Subscription` DROP FOREIGN KEY `Subscription_ibfk_1`;

-- DropForeignKey
ALTER TABLE `Subscription` DROP FOREIGN KEY `Subscription_ibfk_2`;

-- DropForeignKey
ALTER TABLE `User` DROP FOREIGN KEY `User_ibfk_1`;

-- AlterTable
ALTER TABLE `Article` DROP COLUMN `content_text`,
    DROP COLUMN `scores`,
    ADD COLUMN `content_raw` LONGTEXT NOT NULL,
    ADD COLUMN `content_raw_mime` VARCHAR(500),
    ADD COLUMN `data_json_map` JSON,
    ADD COLUMN `fulltext_data` LONGTEXT;

-- AddForeignKey
ALTER TABLE `User` ADD CONSTRAINT `User_settingsId_fkey` FOREIGN KEY (`settingsId`) REFERENCES `ProfileSettings`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `EventHook` ADD CONSTRAINT `EventHook_ownerId_fkey` FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Notebook` ADD CONSTRAINT `Notebook_streamId_fkey` FOREIGN KEY (`streamId`) REFERENCES `Stream`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Notebook` ADD CONSTRAINT `Notebook_ownerId_fkey` FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Feed` ADD CONSTRAINT `Feed_ownerId_fkey` FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Feed` ADD CONSTRAINT `Feed_streamId_fkey` FOREIGN KEY (`streamId`) REFERENCES `Stream`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `FeedEvent` ADD CONSTRAINT `FeedEvent_feedId_fkey` FOREIGN KEY (`feedId`) REFERENCES `Feed`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `ArticleRef` ADD CONSTRAINT `ArticleRef_ownerId_fkey` FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `ArticleRef` ADD CONSTRAINT `ArticleRef_articleId_fkey` FOREIGN KEY (`articleId`) REFERENCES `Article`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `ArticleRef` ADD CONSTRAINT `ArticleRef_articleRefId_fkey` FOREIGN KEY (`articleRefId`) REFERENCES `ArticleRef`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Bucket` ADD CONSTRAINT `Bucket_ownerId_fkey` FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Bucket` ADD CONSTRAINT `Bucket_streamId_fkey` FOREIGN KEY (`streamId`) REFERENCES `Stream`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Subscription` ADD CONSTRAINT `Subscription_feedId_fkey` FOREIGN KEY (`feedId`) REFERENCES `Feed`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Subscription` ADD CONSTRAINT `Subscription_ownerId_fkey` FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Subscription` ADD CONSTRAINT `Subscription_bucketId_fkey` FOREIGN KEY (`bucketId`) REFERENCES `Bucket`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- RenameIndex
ALTER TABLE `Article` RENAME INDEX `Article.url_unique` TO `Article_url_key`;

-- RenameIndex
ALTER TABLE `ArticlePostProcessor` RENAME INDEX `ArticlePostProcessor.type_unique` TO `ArticlePostProcessor_type_key`;

-- RenameIndex
ALTER TABLE `EventHook` RENAME INDEX `EventHook.script_source_url_unique` TO `EventHook_script_source_url_key`;

-- RenameIndex
ALTER TABLE `Feed` RENAME INDEX `Feed.feed_url_ownerId_unique` TO `Feed_feed_url_ownerId_key`;

-- RenameIndex
ALTER TABLE `NoFollowUrl` RENAME INDEX `NoFollowUrl.url_prefix_unique` TO `NoFollowUrl_url_prefix_key`;

-- RenameIndex
ALTER TABLE `Notebook` RENAME INDEX `Notebook.name_ownerId_unique` TO `Notebook_name_ownerId_key`;

-- RenameIndex
ALTER TABLE `User` RENAME INDEX `User.email_unique` TO `User_email_key`;

-- AlterTable
ALTER TABLE `Article` ADD COLUMN `updatedAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);
