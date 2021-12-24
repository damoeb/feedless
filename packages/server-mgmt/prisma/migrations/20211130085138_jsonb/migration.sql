/*
  Warnings:

  - You are about to drop the column `applyPostProcessors` on the `Article` table. All the data in the column will be lost.
  - You are about to drop the column `enclosures` on the `Feed` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "Article" DROP COLUMN "applyPostProcessors";

-- AlterTable
ALTER TABLE "Feed" DROP COLUMN "enclosures";

-- AlterTable
ALTER TABLE "FeedEvent" ALTER COLUMN "message" SET DATA TYPE TEXT;
