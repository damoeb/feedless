/*
  Warnings:

  - You are about to drop the `FeedEvent` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE "FeedEvent" DROP CONSTRAINT "FeedEvent_feedId_fkey";

-- DropIndex
DROP INDEX "Feed_feed_url_ownerId_key";

-- AlterTable
ALTER TABLE "Feed" ADD COLUMN     "lastStatusChangeAt" TIMESTAMP(3),
ADD COLUMN     "op_secret" VARCHAR(40) NOT NULL DEFAULT E'a-dammn-good-secret';

-- DropTable
DROP TABLE "FeedEvent";
