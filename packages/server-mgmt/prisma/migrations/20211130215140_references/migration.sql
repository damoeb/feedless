/*
  Warnings:

  - You are about to drop the column `articleRefId` on the `ArticleRef` table. All the data in the column will be lost.
  - You are about to drop the column `fulltext_data` on the `Feed` table. All the data in the column will be lost.
  - You are about to drop the `Comment` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE "ArticleRef" DROP CONSTRAINT "ArticleRef_articleRefId_fkey";

-- DropForeignKey
ALTER TABLE "Comment" DROP CONSTRAINT "Comment_commentId_fkey";

-- DropForeignKey
ALTER TABLE "Comment" DROP CONSTRAINT "Comment_ownerId_fkey";

-- DropForeignKey
ALTER TABLE "Comment" DROP CONSTRAINT "Comment_rootId_fkey";

-- AlterTable
ALTER TABLE "ArticleRef" DROP COLUMN "articleRefId",
ADD COLUMN     "type" TEXT NOT NULL DEFAULT E'feed';

-- AlterTable
ALTER TABLE "Feed" DROP COLUMN "fulltext_data";

-- DropTable
DROP TABLE "Comment";

-- CreateTable
CREATE TABLE "ReferencedArticleRef" (
    "sourceId" TEXT NOT NULL,
    "targetId" TEXT NOT NULL,
    "reference_type" TEXT NOT NULL,

    PRIMARY KEY ("sourceId","targetId")
);

-- AddForeignKey
ALTER TABLE "ReferencedArticleRef" ADD FOREIGN KEY ("sourceId") REFERENCES "ArticleRef"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ReferencedArticleRef" ADD FOREIGN KEY ("targetId") REFERENCES "ArticleRef"("id") ON DELETE CASCADE ON UPDATE CASCADE;
