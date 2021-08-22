/*
  Warnings:

  - A unique constraint covering the columns `[url]` on the table `Article` will be added. If there are existing duplicate values, this will fail.

*/
-- AlterTable
ALTER TABLE `Article` MODIFY `url` VARCHAR(500);

-- CreateIndex
CREATE UNIQUE INDEX `Article.url_unique` ON `Article`(`url`);
