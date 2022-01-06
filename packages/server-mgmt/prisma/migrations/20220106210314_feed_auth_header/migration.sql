-- AlterTable
ALTER TABLE "Article" ADD COLUMN     "main_image_url" VARCHAR(500);

-- AlterTable
ALTER TABLE "Feed" ADD COLUMN     "feed_url_auth_header" TEXT;
