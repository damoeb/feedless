-- AlterTable
ALTER TABLE "Feed" ADD COLUMN     "failed_attempt_count" INTEGER NOT NULL DEFAULT 0,
ALTER COLUMN "ownerId" DROP DEFAULT;

-- AlterTable
ALTER TABLE "Article" ALTER COLUMN "content_raw" DROP NOT NULL;

-- AlterTable
ALTER TABLE "Article" DROP COLUMN "source_url";

-- AlterTable
ALTER TABLE "ArticleExporterTarget" DROP COLUMN "forward_errors";

-- AlterTable
ALTER TABLE "User" ADD COLUMN     "date_format" TEXT,
ADD COLUMN     "time_format" TEXT;

-- AlterTable
ALTER TABLE "ArticleExporter" ALTER COLUMN "segment_digest" SET NOT NULL,
ALTER COLUMN "segment_digest" SET DEFAULT false;

-- AlterTable
ALTER TABLE "Article" DROP COLUMN "fulltext_data";

-- AlterTable
ALTER TABLE "Feed" DROP COLUMN "lang";
