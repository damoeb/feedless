/*
  Warnings:

  - You are about to drop the column `content_html` on the `Article` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `Article` DROP COLUMN `content_html`,
    ADD COLUMN `content_text` LONGTEXT;
