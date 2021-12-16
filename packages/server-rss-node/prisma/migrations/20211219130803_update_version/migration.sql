-- DropForeignKey
ALTER TABLE "ArticleExporter" DROP CONSTRAINT "ArticleExporter_bucketId_fkey";

-- DropForeignKey
ALTER TABLE "ArticleExporterTarget" DROP CONSTRAINT "ArticleExporterTarget_exporterId_fkey";

-- DropForeignKey
ALTER TABLE "ArticleRef" DROP CONSTRAINT "ArticleRef_articleId_fkey";

-- DropForeignKey
ALTER TABLE "ArticleRef" DROP CONSTRAINT "ArticleRef_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "Bucket" DROP CONSTRAINT "Bucket_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "Bucket" DROP CONSTRAINT "Bucket_streamId_fkey";

-- DropForeignKey
ALTER TABLE "EventHook" DROP CONSTRAINT "EventHook_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "Feed" DROP CONSTRAINT "Feed_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "Feed" DROP CONSTRAINT "Feed_streamId_fkey";

-- DropForeignKey
ALTER TABLE "FeedEvent" DROP CONSTRAINT "FeedEvent_feedId_fkey";

-- DropForeignKey
ALTER TABLE "Notebook" DROP CONSTRAINT "Notebook_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "Notebook" DROP CONSTRAINT "Notebook_streamId_fkey";

-- DropForeignKey
ALTER TABLE "Plugin" DROP CONSTRAINT "Plugin_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "ReferencedArticleRef" DROP CONSTRAINT "ReferencedArticleRef_sourceId_fkey";

-- DropForeignKey
ALTER TABLE "ReferencedArticleRef" DROP CONSTRAINT "ReferencedArticleRef_targetId_fkey";

-- DropForeignKey
ALTER TABLE "Subscription" DROP CONSTRAINT "Subscription_bucketId_fkey";

-- DropForeignKey
ALTER TABLE "Subscription" DROP CONSTRAINT "Subscription_feedId_fkey";

-- DropForeignKey
ALTER TABLE "Subscription" DROP CONSTRAINT "Subscription_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "User" DROP CONSTRAINT "User_settingsId_fkey";

-- AlterTable
ALTER TABLE "Bucket" ADD COLUMN     "lastUpdatedAt" TIMESTAMP(3);

-- AddForeignKey
ALTER TABLE "User" ADD CONSTRAINT "User_settingsId_fkey" FOREIGN KEY ("settingsId") REFERENCES "ProfileSettings"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "EventHook" ADD CONSTRAINT "EventHook_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Notebook" ADD CONSTRAINT "Notebook_streamId_fkey" FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Notebook" ADD CONSTRAINT "Notebook_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Plugin" ADD CONSTRAINT "Plugin_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Feed" ADD CONSTRAINT "Feed_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Feed" ADD CONSTRAINT "Feed_streamId_fkey" FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "FeedEvent" ADD CONSTRAINT "FeedEvent_feedId_fkey" FOREIGN KEY ("feedId") REFERENCES "Feed"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ReferencedArticleRef" ADD CONSTRAINT "ReferencedArticleRef_sourceId_fkey" FOREIGN KEY ("sourceId") REFERENCES "ArticleRef"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ReferencedArticleRef" ADD CONSTRAINT "ReferencedArticleRef_targetId_fkey" FOREIGN KEY ("targetId") REFERENCES "ArticleRef"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleRef" ADD CONSTRAINT "ArticleRef_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleRef" ADD CONSTRAINT "ArticleRef_articleId_fkey" FOREIGN KEY ("articleId") REFERENCES "Article"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Bucket" ADD CONSTRAINT "Bucket_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Bucket" ADD CONSTRAINT "Bucket_streamId_fkey" FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleExporter" ADD CONSTRAINT "ArticleExporter_bucketId_fkey" FOREIGN KEY ("bucketId") REFERENCES "Bucket"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleExporterTarget" ADD CONSTRAINT "ArticleExporterTarget_exporterId_fkey" FOREIGN KEY ("exporterId") REFERENCES "ArticleExporter"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Subscription" ADD CONSTRAINT "Subscription_feedId_fkey" FOREIGN KEY ("feedId") REFERENCES "Feed"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Subscription" ADD CONSTRAINT "Subscription_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Subscription" ADD CONSTRAINT "Subscription_bucketId_fkey" FOREIGN KEY ("bucketId") REFERENCES "Bucket"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- RenameIndex
ALTER INDEX "Article.url_unique" RENAME TO "Article_url_key";

-- RenameIndex
ALTER INDEX "ArticlePostProcessor.type_unique" RENAME TO "ArticlePostProcessor_type_key";

-- RenameIndex
ALTER INDEX "EventHook.script_source_url_unique" RENAME TO "EventHook_script_source_url_key";

-- RenameIndex
ALTER INDEX "Feed.feed_url_ownerId_unique" RENAME TO "Feed_feed_url_ownerId_key";

-- RenameIndex
ALTER INDEX "NoFollowUrl.url_prefix_unique" RENAME TO "NoFollowUrl_url_prefix_key";

-- RenameIndex
ALTER INDEX "Notebook.name_ownerId_unique" RENAME TO "Notebook_name_ownerId_key";

-- RenameIndex
ALTER INDEX "User.email_unique" RENAME TO "User_email_key";

-- RenameIndex
ALTER INDEX "User_settingsId_unique" RENAME TO "User_settingsId_key";
