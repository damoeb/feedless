-- DropForeignKey
ALTER TABLE "User" DROP CONSTRAINT "User_settingsId_fkey";

-- DropIndex
DROP INDEX "User_settingsId_key";

-- AlterTable
ALTER TABLE "User" DROP COLUMN "settingsId",
ADD COLUMN     "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- DropTable
DROP TABLE "ProfileSettings";

-- AlterTable
ALTER TABLE "Bucket" DROP COLUMN "lastPostProcessedAt";

-- DropForeignKey
ALTER TABLE "ReferencedArticleRef" DROP CONSTRAINT "ReferencedArticleRef_sourceId_fkey";

-- DropForeignKey
ALTER TABLE "ReferencedArticleRef" DROP CONSTRAINT "ReferencedArticleRef_targetId_fkey";

-- DropForeignKey
ALTER TABLE "_ArticleRefToStream" DROP CONSTRAINT "_ArticleRefToStream_A_fkey";

-- DropForeignKey
ALTER TABLE "_ArticleRefToStream" DROP CONSTRAINT "_ArticleRefToStream_B_fkey";

-- AlterTable
ALTER TABLE "Article" DROP COLUMN "readability",
ADD COLUMN     "source_used" TEXT DEFAULT E'feed';

-- AlterTable
ALTER TABLE "ArticleRef" ADD COLUMN     "streamId" TEXT NOT NULL;

-- AlterTable
ALTER TABLE "Bucket" ADD COLUMN     "type" INTEGER NOT NULL DEFAULT 0;

-- DropTable
DROP TABLE "ReferencedArticleRef";

-- DropTable
DROP TABLE "_ArticleRefToStream";

-- AddForeignKey
ALTER TABLE "ArticleRef" ADD CONSTRAINT "ArticleRef_streamId_fkey" FOREIGN KEY ("streamId") REFERENCES "Stream"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
