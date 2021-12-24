-- CreateTable
CREATE TABLE "User" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "settingsId" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "EventHook" (
    "id" TEXT NOT NULL,
    "ownerId" TEXT NOT NULL,
    "event" TEXT NOT NULL,
    "type" TEXT NOT NULL,
    "script_or_url" TEXT NOT NULL,
    "script_source_url" VARCHAR(500),

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ProfileSettings" (
    "id" TEXT NOT NULL,
    "useFulltext" BOOLEAN NOT NULL DEFAULT false,
    "useBetterRead" BOOLEAN NOT NULL DEFAULT false,
    "showNativeTags" BOOLEAN NOT NULL DEFAULT true,
    "showContentTags" BOOLEAN NOT NULL DEFAULT true,
    "queryEngines" JSONB,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Notebook" (
    "id" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "description" TEXT NOT NULL DEFAULT E'',
    "readonly" BOOLEAN NOT NULL DEFAULT false,
    "listed" BOOLEAN NOT NULL DEFAULT false,
    "streamId" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ownerId" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Feed" (
    "id" TEXT NOT NULL,
    "feed_url" VARCHAR(500) NOT NULL,
    "home_page_url" TEXT,
    "fulltext_data" TEXT,
    "domain" TEXT NOT NULL,
    "title" TEXT,
    "lang" TEXT,
    "tags" JSONB,
    "enclosures" JSONB,
    "author" TEXT,
    "is_private" BOOLEAN NOT NULL DEFAULT false,
    "ownerId" TEXT NOT NULL DEFAULT E'system',
    "expired" BOOLEAN NOT NULL DEFAULT false,
    "broken" BOOLEAN NOT NULL DEFAULT false,
    "inactive" BOOLEAN NOT NULL DEFAULT false,
    "filter" TEXT,
    "description" TEXT,
    "status" TEXT NOT NULL DEFAULT E'unresolved',
    "harvestIntervalMinutes" INTEGER,
    "nextHarvestAt" TIMESTAMP(3),
    "retention_size" INTEGER,
    "harvest_site" BOOLEAN NOT NULL DEFAULT false,
    "allowHarvestFailure" BOOLEAN NOT NULL DEFAULT false,
    "harvest_prerender" BOOLEAN NOT NULL DEFAULT false,
    "streamId" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "lastUpdatedAt" TIMESTAMP(3),

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "FeedEvent" (
    "id" TEXT NOT NULL,
    "message" JSONB NOT NULL,
    "feedId" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "is_error" BOOLEAN NOT NULL DEFAULT false,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Stream" (
    "id" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ArticleRef" (
    "id" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "date_released" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "ownerId" TEXT NOT NULL DEFAULT E'system',
    "favored" BOOLEAN NOT NULL DEFAULT false,
    "has_seen" BOOLEAN NOT NULL DEFAULT false,
    "tags" JSONB,
    "data" JSONB,
    "articleId" TEXT NOT NULL,
    "articleRefId" TEXT,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Article" (
    "id" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "released" BOOLEAN NOT NULL DEFAULT true,
    "applyPostProcessors" BOOLEAN NOT NULL DEFAULT true,
    "date_published" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "date_modified" TIMESTAMP(3),
    "comment_feed_url" TEXT,
    "source_url" VARCHAR(500),
    "url" VARCHAR(500),
    "author" TEXT,
    "title" VARCHAR(200) NOT NULL,
    "tags" JSONB,
    "fulltext_data" VARCHAR(500),
    "content_raw_mime" VARCHAR(50),
    "content_raw" TEXT NOT NULL,
    "content_text" TEXT,
    "has_harvest" BOOLEAN,
    "has_readability" BOOLEAN,
    "has_video" BOOLEAN NOT NULL DEFAULT false,
    "has_audio" BOOLEAN NOT NULL DEFAULT false,
    "length_video" INTEGER,
    "length_audio" INTEGER,
    "word_count_text" INTEGER,
    "score" DOUBLE PRECISION DEFAULT 0,
    "lastScoredAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "enclosure" JSONB,
    "data_json_map" JSONB,
    "readability" JSONB,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Bucket" (
    "id" TEXT NOT NULL,
    "title" TEXT NOT NULL,
    "description" TEXT,
    "listed" BOOLEAN NOT NULL DEFAULT false,
    "tags" JSONB,
    "in_focus" BOOLEAN NOT NULL DEFAULT true,
    "ownerId" TEXT NOT NULL,
    "lastPostProcessedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "streamId" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ArticleExporter" (
    "id" TEXT NOT NULL,
    "segment" BOOLEAN NOT NULL DEFAULT false,
    "segment_sort_field" TEXT,
    "segment_sort_asc" BOOLEAN NOT NULL DEFAULT true,
    "segment_size" INTEGER,
    "segment_digest" BOOLEAN,
    "lastUpdatedAt" TIMESTAMP(3),
    "trigger_refresh_on" TEXT NOT NULL DEFAULT E'change',
    "trigger_scheduled_last_at" TIMESTAMP(3),
    "trigger_scheduled_next_at" TIMESTAMP(3),
    "trigger_scheduled" TEXT,
    "bucketId" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ArticleExporterTarget" (
    "id" TEXT NOT NULL,
    "type" TEXT NOT NULL,
    "context" TEXT,
    "forward_errors" BOOLEAN NOT NULL DEFAULT false,
    "exporterId" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "NoFollowUrl" (
    "id" TEXT NOT NULL,
    "url_prefix" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Comment" (
    "id" TEXT NOT NULL,
    "rootId" TEXT NOT NULL,
    "ownerId" TEXT NOT NULL,
    "content_raw_mime" VARCHAR(50) NOT NULL DEFAULT E'text/markdown',
    "content_raw" TEXT NOT NULL,
    "content_html" TEXT NOT NULL,
    "commentId" TEXT,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ArticlePostProcessor" (
    "id" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "type" TEXT NOT NULL,
    "context" TEXT,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Subscription" (
    "id" TEXT NOT NULL,
    "inactive" BOOLEAN NOT NULL DEFAULT false,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "lastUpdatedAt" TIMESTAMP(3),
    "title" TEXT NOT NULL,
    "tags" JSONB,
    "feedId" TEXT NOT NULL,
    "ownerId" TEXT NOT NULL,
    "bucketId" TEXT NOT NULL,

    PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "_ArticleRefToStream" (
    "A" TEXT NOT NULL,
    "B" TEXT NOT NULL
);

-- CreateTable
CREATE TABLE "_ArticlePostProcessorToBucket" (
    "A" TEXT NOT NULL,
    "B" TEXT NOT NULL
);

-- CreateIndex
CREATE UNIQUE INDEX "User.email_unique" ON "User"("email");

-- CreateIndex
CREATE UNIQUE INDEX "User_settingsId_unique" ON "User"("settingsId");

-- CreateIndex
CREATE UNIQUE INDEX "EventHook.script_source_url_unique" ON "EventHook"("script_source_url");

-- CreateIndex
CREATE UNIQUE INDEX "Notebook.name_ownerId_unique" ON "Notebook"("name", "ownerId");

-- CreateIndex
CREATE UNIQUE INDEX "Feed.feed_url_ownerId_unique" ON "Feed"("feed_url", "ownerId");

-- CreateIndex
CREATE UNIQUE INDEX "Article.url_unique" ON "Article"("url");

-- CreateIndex
CREATE UNIQUE INDEX "NoFollowUrl.url_prefix_unique" ON "NoFollowUrl"("url_prefix");

-- CreateIndex
CREATE UNIQUE INDEX "ArticlePostProcessor.type_unique" ON "ArticlePostProcessor"("type");

-- CreateIndex
CREATE UNIQUE INDEX "_ArticleRefToStream_AB_unique" ON "_ArticleRefToStream"("A", "B");

-- CreateIndex
CREATE INDEX "_ArticleRefToStream_B_index" ON "_ArticleRefToStream"("B");

-- CreateIndex
CREATE UNIQUE INDEX "_ArticlePostProcessorToBucket_AB_unique" ON "_ArticlePostProcessorToBucket"("A", "B");

-- CreateIndex
CREATE INDEX "_ArticlePostProcessorToBucket_B_index" ON "_ArticlePostProcessorToBucket"("B");

-- AddForeignKey
ALTER TABLE "User" ADD FOREIGN KEY ("settingsId") REFERENCES "ProfileSettings"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "EventHook" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Notebook" ADD FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Notebook" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Feed" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Feed" ADD FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "FeedEvent" ADD FOREIGN KEY ("feedId") REFERENCES "Feed"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleRef" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleRef" ADD FOREIGN KEY ("articleId") REFERENCES "Article"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleRef" ADD FOREIGN KEY ("articleRefId") REFERENCES "ArticleRef"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Bucket" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Bucket" ADD FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleExporter" ADD FOREIGN KEY ("bucketId") REFERENCES "Bucket"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ArticleExporterTarget" ADD FOREIGN KEY ("exporterId") REFERENCES "ArticleExporter"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Comment" ADD FOREIGN KEY ("rootId") REFERENCES "Article"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Comment" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Comment" ADD FOREIGN KEY ("commentId") REFERENCES "Comment"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Subscription" ADD FOREIGN KEY ("feedId") REFERENCES "Feed"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Subscription" ADD FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Subscription" ADD FOREIGN KEY ("bucketId") REFERENCES "Bucket"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "_ArticleRefToStream" ADD FOREIGN KEY ("A") REFERENCES "ArticleRef"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "_ArticleRefToStream" ADD FOREIGN KEY ("B") REFERENCES "Stream"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "_ArticlePostProcessorToBucket" ADD FOREIGN KEY ("A") REFERENCES "ArticlePostProcessor"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "_ArticlePostProcessorToBucket" ADD FOREIGN KEY ("B") REFERENCES "Bucket"("id") ON DELETE CASCADE ON UPDATE CASCADE;
