/*
  Warnings:

  - You are about to drop the column `filter_expr` on the `Bucket` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `Article` MODIFY `title` VARCHAR(200) NOT NULL;

-- AlterTable
ALTER TABLE `Bucket` DROP COLUMN `filter_expr`,
    ADD COLUMN `filter_expression` TEXT;
